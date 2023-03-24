package de.tu_dresden.inf.lat.evee.protege.modelgeneration;

import com.google.common.collect.Sets;
import de.tu_dresden.inf.lat.evee.general.data.exceptions.ModelGenerationException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.ui.graphicGraph.GraphicGraph;
import org.graphstream.ui.spriteManager.Sprite;
import org.graphstream.ui.spriteManager.SpriteManager;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.ViewerListener;
import org.graphstream.ui.view.ViewerPipe;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.refactor.ontology.OntologyMerger;
import org.protege.editor.owl.ui.renderer.OWLCellRenderer;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class GraphModelComponent extends JPanel implements ViewerListener {

    private final DefaultListModel<OWLClass> classListModel;
    private final DefaultListModel<OWLAxiom> axiomListModel;
    private final OWLEditorKit owlEditorKit;
    private final ModelManager modelManager;
    private final OWLDataFactory df;
    private final JPanel viewPanel;
    protected boolean loop = true;
    private Viewer viewer;
    private Graph graph;
    private Map<String, List<OWLClass>> classMap;
    private JList classList;
    private JList axiomList;
    private ListenerRunnable listenerRunnable;
    private Component view;
    private Thread listenerThread;
    private String previousNodeID ="";
    private Sprite selectSprite;
    private long currentTime;

    public GraphModelComponent(ModelManager modelManager, OWLEditorKit owlEditorKit) {
        this.modelManager = modelManager;
        this.classMap = this.modelManager.getClassMap();
        this.owlEditorKit = owlEditorKit;
        this.classListModel = new DefaultListModel();
        this.axiomListModel = new DefaultListModel();
        this.viewer = this.modelManager.getViewer();
        this.graph = this.modelManager.getGraph();
        this.createSelectSprite();
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
        this.previousNodeID ="";
        this.classMap = this.modelManager.getClassMap();
        this.viewer = this.modelManager.getViewer();
        this.graph = this.modelManager.getGraph();
        this.createSelectSprite();
        this.viewPanel.remove(this.view);
        this.view = (Component) this.viewer.addDefaultView(false);
        this.viewPanel.add(this.view);
        this.listenerRunnable = new ListenerRunnable(this);
        this.listenerThread = new Thread(this.listenerRunnable, "Listener Thread");
        this.listenerThread.setDaemon(true);
        this.listenerThread.start();
        this.updateUI();
    }

    JPanel getRightPanel() {
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setAlignmentX(0.5F);
//        rightPanel.setAlignmentY(BOTTOM_ALIGNMENT);
        rightPanel.setBorder(new EmptyBorder(new Insets(15, 15, 15, 15)));
        rightPanel.setMaximumSize(new Dimension(200,  2000));
//        rightPanel.setPreferredSize(new Dimension(200,  600));
        rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        this.createClassList();
        JPanel classListPanel = new JPanel();
        classListPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEmptyBorder(0, 0, 0, 0),
                "OWLClasses:"));
        classListPanel.add(new JScrollPane(this.classList));

        rightPanel.add(classListPanel);
        rightPanel.add(this.getAddToAxiomListButton());
        rightPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        this.createAxiomList();
        JPanel axiomListPanel = new JPanel();
        axiomListPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEmptyBorder(0, 0, 0, 0),
                "OWLDisjointClassesAxioms:"));
        axiomListPanel.add(new JScrollPane(this.axiomList));

        rightPanel.add(axiomListPanel);
        rightPanel.add(this.getButtonPanel());

        return rightPanel;
    }

    private JPanel getButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setAlignmentY(TOP_ALIGNMENT);
        JPanel hPanel = new JPanel();
        hPanel.setLayout(new BoxLayout(hPanel, BoxLayout.X_AXIS));
        hPanel.add(this.getRefreshButton());
        hPanel.add(Box.createRigidArea(new Dimension(2, 0)));
        hPanel.add(this.getRemoveAxiomsButton());
        buttonPanel.add(hPanel);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        buttonPanel.add(this.getAddToOntologyButton());
        return buttonPanel;
    }

    private JButton getRemoveAxiomsButton() {
        JButton removeAxiomsButton = new JButton("Remove");
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
        JButton refreshButton = new JButton("Refresh model");
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                Set<OWLAxiom> additionalAxioms = new HashSet<>();
                Arrays.stream(axiomListModel.toArray()).map((ax) -> (OWLDisjointClassesAxiom) ax)
                        .forEach(ax -> ax.asPairwiseAxioms().forEach(pax -> {
                                            OWLClassExpression first = pax.getClassExpressionsAsList().get(0);
                                            OWLClassExpression second = pax.getClassExpressionsAsList().get(1);
                                            additionalAxioms.add(
                                                    df.getOWLSubClassOfAxiom(
                                                            df.getOWLObjectIntersectionOf(first, second),
                                                            df.getOWLNothing()));
                                        }
                                )
                        );

                try {
                    modelManager.refreshModel(additionalAxioms);

                    refresh();
                } catch (ModelGenerationException ex) {
                    JOptionPane.showMessageDialog(new JPanel(), ex.getMessage(), "Error", 0);
                }

            }
        });
        refreshButton.setAlignmentX(0.5F);
        return refreshButton;
    }

    private JButton getAddToAxiomListButton() {
        JButton addToAxiomList = new JButton("Add OWLDisjointClassesAxiom");
        addToAxiomList.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (classList.getSelectedValues().length < 2) {
                    JOptionPane.showMessageDialog(new JPanel(), "Please select at least 2 OWLCLasses", "Error", 0);
                } else {
                    axiomListModel.addElement(
                            df.getOWLDisjointClassesAxiom(Arrays.stream(classList.getSelectedValues())
                                    .map(ax -> (OWLClassExpression) ax)
                                    .collect(Collectors.toSet())));
                }
            }
        });
        addToAxiomList.setAlignmentX(0.5F);
        return addToAxiomList;
    }

    private JButton getAddToOntologyButton() {
        JButton addToOntology = new JButton("Add to active ontology");
        addToOntology.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OWLOntology activeOntology = owlEditorKit.getOWLModelManager().getActiveOntology();
                Set<OWLAxiom> additionalAxiomsWorkingCopy = new HashSet<>();
                Set<OWLAxiom> additionalAxiomsActiveOntology = Arrays.stream(axiomListModel.toArray())
                        .map((ax) -> (OWLDisjointClassesAxiom) ax)
                        .collect(Collectors.toSet());
                Arrays.stream(axiomListModel.toArray()).map((ax) -> (OWLDisjointClassesAxiom) ax)
                        .forEach(ax -> ax.asPairwiseAxioms().forEach(pax -> {
                                            OWLClassExpression first = pax.getClassExpressionsAsList().get(0);
                                            OWLClassExpression second = pax.getClassExpressionsAsList().get(1);
                                    additionalAxiomsWorkingCopy.add(
                                                    df.getOWLSubClassOfAxiom(
                                                            df.getOWLObjectIntersectionOf(first, second),
                                                            df.getOWLNothing()));
                                        }
                                )
                        );
                OWLOntologyManager  man = OWLManager.createOWLOntologyManager();
                man.addAxioms(activeOntology,additionalAxiomsActiveOntology);
                man.addAxioms(modelManager.getOnt(),additionalAxiomsWorkingCopy);
                axiomListModel.removeAllElements();
            }
        });
        addToOntology.setAlignmentX(CENTER_ALIGNMENT);
        return addToOntology;
    }

    private void createSelectSprite() {
        SpriteManager sman = new SpriteManager(graph);
        selectSprite = sman.addSprite("select");
        selectSprite.setAttribute("ui.style","fill-color:rgba(0,0,0,0);");
        selectSprite.setAttribute("ui.style","stroke-color:rgba(0,0,0,0);");
        selectSprite.setAttribute("ui.style","fill-mode:plain;");
        selectSprite.setAttribute("ui.style","stroke-mode:plain;");
        selectSprite.setAttribute("ui.style","size:30px;");
        selectSprite.setPosition(0,0,0);

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
        currentTime = System.currentTimeMillis();

    }

    public void buttonReleased(String s) {
        if(System.currentTimeMillis()-currentTime <300) {
            if (classMap.containsKey(s)) {

                List<OWLClass> classList = classMap.get(s);
                classListModel.removeAllElements();
                for (OWLClass cl : classList) {
                    classListModel.addElement(cl);
                }

                if(previousNodeID.isEmpty()) {
                    selectSprite.setAttribute("ui.style","stroke-color:#000000;");
                }
                selectSprite.attachToNode(s);
                selectSprite.setPosition(0,0,0);
                previousNodeID = s;
            }
        }
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
