package de.tu_dresden.inf.lat.evee.protege.modelgeneration;

import org.graphstream.graph.Graph;
import org.graphstream.ui.graphicGraph.GraphicGraph;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.ViewerListener;
import org.graphstream.ui.view.ViewerPipe;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.renderer.OWLCellRenderer;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class GraphModelComponent extends JPanel implements ViewerListener {

    protected boolean loop = true;
    private Viewer viewer;
    private Graph graph;
    private final DefaultListModel<OWLClass> classListModel;
    private final DefaultListModel<OWLAxiom> axiomListModel;
    private Map<String, List<OWLClass>> classMap;
    private final OWLEditorKit owlEditorKit;
    private JList classList;
    private JList axiomList;
    private final ModelManager modelManager;
    private final OWLDataFactory df;
    private ListenerRunnable listenerRunnable;
    private Component view;
    private Thread listenerThread;
    private final JPanel viewPanel;

    public GraphModelComponent(Viewer viewer, Graph graph, Map<String, List<OWLClass>> classMap, ModelManager modelManager, OWLEditorKit owlEditorKit) {
        this.modelManager = modelManager;
        this.classMap = classMap;
        this.owlEditorKit = owlEditorKit;
        this.classListModel = new DefaultListModel();
        this.axiomListModel = new DefaultListModel();
        this.viewer = viewer;
        this.graph = graph;
        this.df = OWLManager.getOWLDataFactory();
        this.listenerRunnable = new ListenerRunnable(this);
        this.listenerThread = new Thread(this.listenerRunnable, "Listener Thread");
        this.listenerThread.setDaemon(true);
        this.listenerThread.start();
        this.setLayout(new BoxLayout(this, 0));
        this.viewPanel = new JPanel();
        this.viewPanel.setLayout(new BoxLayout(this.viewPanel, 0));
        this.viewPanel.setMinimumSize(new Dimension(500, 500));
        this.view = (Component) this.viewer.addDefaultView(false);
        this.viewPanel.add(this.view);
        this.add(this.viewPanel);
        this.add(this.getRightPanel());
    }

    private void refresh() {
        this.classMap = this.modelManager.getClassMap();
        this.viewer = this.modelManager.getViewer();
        this.graph = this.modelManager.getGraph();
        this.viewPanel.remove(this.view);
        this.view = (Component) this.viewer.addDefaultView(false);
        this.viewPanel.add(this.view);
        this.listenerRunnable = new ListenerRunnable(this);
        this.listenerThread = new Thread(this.listenerRunnable, "Listener Thread");
        this.listenerThread.setDaemon(true);
        this.listenerThread.start();
    }

    JPanel getRightPanel() {
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, 1));
        rightPanel.setAlignmentX(0.5F);
        rightPanel.setBorder(new EmptyBorder(new Insets(20, 20, 20, 20)));
        rightPanel.setMaximumSize(new Dimension(200, 450));
        this.createClassList();
        rightPanel.add(new JScrollPane(this.classList));
        rightPanel.add(this.getAddToAxiomListButton());
        this.createAxiomList();
        rightPanel.add(new JScrollPane(this.axiomList));
        rightPanel.add(this.getButtonPanel());
        return rightPanel;
    }

    private JPanel getButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, 2));
        buttonPanel.setAlignmentX(0.5F);
        buttonPanel.add(this.getRefreshButton());
        buttonPanel.add(this.getRemoveAxiomsButton());
        return buttonPanel;
    }

    private JButton getRemoveAxiomsButton() {
        JButton removeAxiomsButton = new JButton("remove");
        removeAxiomsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Arrays.stream(axiomList.getSelectedValues()).map((ax) -> (OWLAxiom) ax).forEach((ax) -> {
                    axiomListModel.removeElement(ax);
                });
            }
        });
        removeAxiomsButton.setAlignmentX(0.5F);
        return removeAxiomsButton;
    }

    private JButton getRefreshButton() {
        JButton refreshButton = new JButton("refresh model");
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (axiomListModel.isEmpty()) {
                    JOptionPane.showMessageDialog(new JPanel(), "Please add at least one OWLDisjointClassesAxiom", "Error", 0);
                } else {
                    Set<OWLAxiom> additionalAxioms = Arrays.stream(axiomListModel.toArray()).map((ax) -> (OWLAxiom) ax)
                            .collect(Collectors.toSet());

                    modelManager.refreshModel(additionalAxioms);
                    refresh();
                }
            }
        });
        refreshButton.setAlignmentX(0.5F);
        return refreshButton;
    }

    private JButton getAddToAxiomListButton() {
        JButton addToAxiomList = new JButton("add OWLDisjointClassesAxiom");
        addToAxiomList.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (classList.getSelectedValues().length != 2) {
                    JOptionPane.showMessageDialog(new JPanel(), "Please secect 2 OWLCLasses", "Error", 0);
                } else {
                    axiomListModel.addElement(df.getOWLDisjointClassesAxiom((OWLClass) classList.getSelectedValues()[0], (OWLClass) classList.getSelectedValues()[1]));
                }
            }
        });
        addToAxiomList.setAlignmentX(0.5F);
        return addToAxiomList;
    }

    private void createClassList() {
        this.classList = new JList(this.classListModel);
        this.classList.setSelectionMode(2);
        this.classList.setCellRenderer(new OWLCellRenderer(this.owlEditorKit));
    }

    private void createAxiomList() {
        this.axiomList = new JList(this.axiomListModel);
        this.axiomList.setSelectionMode(2);
        this.axiomList.setCellRenderer(new OWLCellRenderer(this.owlEditorKit));
    }

    public void listen() throws InterruptedException {
        ViewerPipe fromViewer = this.viewer.newViewerPipe();
        fromViewer.addViewerListener(this);
        fromViewer.addSink(this.graph);

        while (this.loop) {
            fromViewer.blockingPump();

        }
    }

    public void viewClosed(String s) {
    }

    public void buttonPushed(String s) {
        if (classMap.containsKey(s)) {
            List<OWLClass> classList = classMap.get(s);
            classListModel.removeAllElements();
            for (OWLClass cl : classList) {
                classListModel.addElement(cl);
            }
        }
    }

    public void buttonReleased(String s) {
    }

    public void mouseOver(String s) {
    }

    public void mouseLeft(String s) {
    }

    private class ListenerRunnable implements Runnable {
        private final GraphModelComponent clickListener;

        public ListenerRunnable(GraphModelComponent clickListener) {
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
