package de.tu_dresden.inf.lat.evee.protege.modelgeneration;

import org.graphstream.graph.Graph;
import org.graphstream.ui.swing_viewer.SwingViewer;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.ViewerListener;
import org.graphstream.ui.view.ViewerPipe;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.renderer.OWLCellRenderer;
import org.semanticweb.owlapi.model.OWLClass;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class GraphModelTabComponent extends JPanel implements ViewerListener {
    private Viewer viewer;
    private Graph graph;
    protected boolean loop = true;
    private DefaultListModel<OWLClass> listModel;
    private Map<String, List<OWLClass>> classMap;
    private OWLEditorKit owlEditorKit;

    public GraphModelTabComponent(Viewer viewer,
                                  Graph graph,
                                  Map<String,
                                  List<OWLClass>> classMap,
                                  OWLEditorKit owlEditorKit) {
        this.classMap = classMap;
        this.owlEditorKit = owlEditorKit;
        this.listModel = new DefaultListModel<>();
        this.viewer = viewer;
        this.graph = graph;
        ListenerRunnable listenerRunnable = new ListenerRunnable(this);
        Thread listenerThread = new Thread(listenerRunnable,"Listener Thread");
        listenerThread.setDaemon(true);
        listenerThread.start();
        View view = viewer.addDefaultView(false);
        setLayout(new BoxLayout(this,BoxLayout.X_AXIS));
        add(new JScrollPane((Component) view));
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel,BoxLayout.Y_AXIS));
        rightPanel.setBorder(new EmptyBorder(new Insets(20,20,20,20)));
        rightPanel.setMaximumSize(new Dimension(200,450));
        JList classList = new JList<>(listModel);
        classList.setCellRenderer(new OWLCellRenderer(owlEditorKit));
        rightPanel.add(new JScrollPane(classList));
        add(rightPanel);
    }
    public void listen() throws InterruptedException {

        ViewerPipe fromViewer = viewer.newViewerPipe();
        fromViewer.addViewerListener(this);
        fromViewer.addSink(graph);
        while(loop) {
            fromViewer.blockingPump();
            System.out.println("we are still doing this shit");
        }
    }

    @Override
    public void viewClosed(String s) {

    }

    @Override
    public void buttonPushed(String s) {
        if(classMap.containsKey(s)) {
            List<OWLClass> classList = classMap.get(s);
            listModel.removeAllElements();
            for(OWLClass cl:classList) {
                listModel.addElement(cl);
            }
        }
    }

    @Override
    public void buttonReleased(String s) {

    }

    @Override
    public void mouseOver(String s) {

    }

    @Override
    public void mouseLeft(String s) {

    }
    private class ListenerRunnable implements Runnable {
        private GraphModelTabComponent clickListener;
        public ListenerRunnable(GraphModelTabComponent clickListener) {
            this.clickListener = clickListener;
        }

        @Override
        public void run() {
            try {
                clickListener.listen();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
