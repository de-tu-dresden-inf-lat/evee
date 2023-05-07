package de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample;

import org.apache.log4j.Logger;
import org.graphstream.graph.Graph;
import org.graphstream.ui.geom.Point2;
import org.graphstream.ui.geom.Point3;
import org.graphstream.ui.spriteManager.Sprite;
import org.graphstream.ui.spriteManager.SpriteManager;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.ViewerListener;
import org.graphstream.ui.view.ViewerPipe;
import org.graphstream.ui.view.camera.Camera;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.renderer.OWLCellRenderer;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class GraphModelComponent extends JPanel {

    private final DefaultListModel<OWLClass> classListModel;
    private final DefaultListModel<OWLAxiom> axiomListModel;
    private final OWLEditorKit owlEditorKit;
    private final ModelManager modelManager;
    private final OWLDataFactory df;
    private final JPanel viewPanel;
    static final int LABELS_MIN = 0;
    static final int LABELS_MAX = 10;
    static final int LABELS_INIT = 2;
    private Viewer viewer;
    private Graph graph;
    private Map<String, List<OWLClass>> classMap;
    private JList classList;
    private JList axiomList;
    private ListenerRunnable listenerRunnable;
    private Component viewComponent;
    private View view;
    private Thread listenerThread;
    private String previousNodeID ="";
    private Sprite selectSprite;
    private Set<Sprite> classLabels;
    private Map<Sprite,Point2> labelCoordinates;
    private boolean changeAxiomList;
    private final Logger logger = Logger.getLogger(GraphModelComponent.class);

    public GraphModelComponent(ModelManager modelManager, OWLEditorKit owlEditorKit) {
        this.modelManager = modelManager;
        this.classMap = this.modelManager.getClassMap();
        this.owlEditorKit = owlEditorKit;
        this.classListModel = new DefaultListModel();
        this.axiomListModel = new DefaultListModel();
        this.viewer = this.modelManager.getViewer();
        this.graph = this.modelManager.getGraph();
        this.classLabels = this.modelManager.getClassLabels();
        this.labelCoordinates = new HashMap<>();
        this.labelCoordinates = classLabels.stream()
                .collect(Collectors.toMap(l ->l,l-> new Point2(l.getX(),l.getY())));
        this.createSelectSprite();
        this.df = OWLManager.getOWLDataFactory();
        this.listenerRunnable = new ListenerRunnable(this,this.viewer,this.graph);
        this.listenerThread = new Thread(this.listenerRunnable, "Listener Thread");
        this.listenerThread.setDaemon(true);
        this.listenerThread.start();
        this.setLayout(new BoxLayout(this, 0));
        this.viewPanel = new JPanel();
        this.viewPanel.setLayout(new BoxLayout(this.viewPanel, 0));
        this.viewPanel.setMinimumSize(new Dimension(500, 500));
        this.view = this.viewer.addDefaultView(false);
        Camera cam = view.getCamera();
        cam.setViewPercent(1);
        this.viewComponent = (Component) view;
        this.enableZoom();
        this.viewPanel.add(this.viewComponent);
        this.add(this.viewPanel);
        this.add(this.getRightPanel());
        this.changeAxiomList = false;
    }
    private void refreshModel() {
        Camera cam = view.getCamera();
        cam.resetView();
        labelCoordinates.entrySet().stream()
                .forEach(e -> e.getKey().setPosition(e.getValue().x,e.getValue().y,0));
    }

    private void resetModel() {
        this.previousNodeID ="";
        this.classMap = this.modelManager.getClassMap();
        this.viewer = this.modelManager.getViewer();
        this.graph = this.modelManager.getGraph();
        this.classLabels = this.modelManager.getClassLabels();
        this.labelCoordinates = new HashMap<>();
        this.labelCoordinates = classLabels.stream()
                .collect(Collectors.toMap(l ->l,l-> new Point2(l.getX(),l.getY())));
        this.createSelectSprite();
        this.viewPanel.remove(this.viewComponent);
        this.view = this.viewer.addDefaultView(false);
        this.viewComponent = (Component) view;
        this.enableZoom();
        this.viewPanel.add(this.viewComponent);
        this.updateUI();
        this.changeAxiomList = false;
        logger.debug("model is updated");
    }

    private void resetListenerThread() {
        listenerRunnable.requestStop();
        logger.debug("listener thread stop is requested");
        this.listenerRunnable = new ListenerRunnable(this,viewer,graph);
        this.listenerThread = new Thread(this.listenerRunnable, "Listener Thread");
        this.listenerThread.setDaemon(true);
        this.listenerThread.start();
    }

    private JPanel getRightPanel() {
        this.createClassList();
        this.createAxiomList();
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setAlignmentX(0.5F);
        rightPanel.setBorder(new EmptyBorder(new Insets(15, 15, 15, 15)));
        rightPanel.setMaximumSize(new Dimension(200,  2000));
        rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        rightPanel.add(getClassListPanel());
        rightPanel.add(Box.createRigidArea(new Dimension(0, 40)));
        rightPanel.add(getLabelNumSliderPanel());
        rightPanel.add(Box.createRigidArea(new Dimension(0, 40)));
        rightPanel.add(getAxiomListPanel());
        return rightPanel;
    }
    private JPanel getAxiomListPanel() {
        this.axiomList.setPreferredSize(new Dimension(180,300));
        JPanel axiomListPanel = new JPanel();
        axiomListPanel.setLayout(new BoxLayout(axiomListPanel, BoxLayout.Y_AXIS));
        JPanel axiomListBorder = new JPanel();
        axiomListBorder.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEmptyBorder(5, 5, 5, 5),
                "OWLDisjointClassesAxioms:"));
        axiomListBorder.add(new JScrollPane(this.axiomList));
        axiomListPanel.add(axiomListBorder);
        axiomListPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        axiomListPanel.add(getButtonPanel());
        axiomListPanel.add(Box.createRigidArea(new Dimension(0, 50)));
        return axiomListPanel;
    }
    private JPanel getClassListPanel() {
        JPanel classListPanel = new JPanel();
        classListPanel.setLayout(new BoxLayout(classListPanel, BoxLayout.Y_AXIS));
        JPanel classListBorder = new JPanel();
        classListBorder.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEmptyBorder(0, 0, 0, 0),
                "OWLClasses:"));
        classListBorder.add(new JScrollPane(this.classList));
        classListBorder.setAlignmentY(TOP_ALIGNMENT);
        classListPanel.add(classListBorder);
        classListPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        JButton addToAxiomListButton = this.getAddToAxiomListButton();
        addToAxiomListButton.setAlignmentY(TOP_ALIGNMENT);
        classListPanel.add(addToAxiomListButton);

        return classListPanel;
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
                changeAxiomList = true;
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
                    if(changeAxiomList == true) {
                        logger.debug("recomputing model");
                        modelManager.recomputeModel(additionalAxioms);
                        resetModel();
                        resetListenerThread();
                    } else {
                        logger.debug("refreshing model");
//                        refreshModel();
                        modelManager.refreshModel();
                       resetModel();
                        resetListenerThread();
                    }
                } catch (Exception ex) {
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
                    changeAxiomList = true;
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

    private JPanel getLabelNumSliderPanel() {
        JPanel labelNumSliderPanel = new JPanel();
        labelNumSliderPanel.setLayout(new BoxLayout(labelNumSliderPanel, BoxLayout.Y_AXIS));
        labelNumSliderPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEmptyBorder(0, 0, 0, 0),
                "Displayed OWLClasses:"));
        JSlider labelNumSlider = new JSlider(LABELS_MIN,LABELS_MAX,LABELS_INIT);
        labelNumSlider.setMajorTickSpacing(2);
        labelNumSlider.setMinorTickSpacing(1);
        labelNumSlider.setPaintTicks(true);
        labelNumSlider.setPaintLabels(true);
        labelNumSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider)e.getSource();
                if (!source.getValueIsAdjusting()) {
                    logger.debug("number of labels is adjusted. New number is "+source.getValue());
                    modelManager.setMaxLabelNumber(source.getValue());
                }
            }
        });
        labelNumSliderPanel.add(labelNumSlider);
        return labelNumSliderPanel;
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
    public void selectNewNode(String nodeID) {
        logger.debug("button is released");
        if (classMap.containsKey(nodeID)) {

            List<OWLClass> classList = classMap.get(nodeID);
            classListModel.removeAllElements();
            for (OWLClass cl : classList) {
                classListModel.addElement(cl);
            }
            if(previousNodeID.isEmpty()) {
                selectSprite.setAttribute("ui.style","stroke-color:#000000;");
            }
            selectSprite.attachToNode(nodeID);
            selectSprite.setPosition(0,0,0);
            previousNodeID = nodeID;
            logger.debug("node is selected");
        }
    }

    private void enableZoom() {
        viewComponent.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
//                JOptionPane.showMessageDialog(new JPanel(), "Please select at least 2 OWLCLasses");
                e.consume();
                int i = e.getWheelRotation();
                double factor = Math.pow(1.25, i);
                Camera cam = view.getCamera();
                double zoom = cam.getViewPercent() * factor;
                Point2 pxCenter  = cam.transformGuToPx(cam.getViewCenter().x, cam.getViewCenter().y, 0);
                Point3 guClicked = cam.transformPxToGu(e.getX(), e.getY());
                double newRatioPx2Gu = cam.getMetrics().ratioPx2Gu/factor;
                double x = guClicked.x + (pxCenter.x - e.getX())/newRatioPx2Gu;
                double y = guClicked.y - (pxCenter.y - e.getY())/newRatioPx2Gu;
                cam.setViewCenter(x, y, 0);
                cam.setViewPercent(zoom);
                classLabels.forEach(l -> adjustLabelPosition(l,zoom));
            }
        });
    }

    private void adjustLabelPosition(Sprite label, double zoom) {
        double X = labelCoordinates.get(label).x*zoom;
        double Y = labelCoordinates.get(label).y*zoom;
        label.setPosition(X,Y,0);
    }

    private class ListenerRunnable implements Runnable, ViewerListener {
        private final GraphModelComponent graphComponent;
        private long currentTime;
        private final Viewer viewer;
        private final Graph graph;
        private boolean isFirstClick;
        private  final AtomicBoolean running = new AtomicBoolean(false);

        public ListenerRunnable(GraphModelComponent graphComponent,
                                Viewer viewer,
                                Graph graph) {
            this.graphComponent = graphComponent;
            this.viewer = viewer;
            this.graph = graph;
            this.isFirstClick = true;
        }
        public void viewClosed(String s) {
            logger.debug("view is closed");
        }

        public void buttonPushed(String s) {
            if(isFirstClick) {
                viewer.disableAutoLayout();
                this.isFirstClick = false;
            }
            currentTime = System.currentTimeMillis();
        }

        public void buttonReleased(String s) {
            if(System.currentTimeMillis()-currentTime <300) {
                graphComponent.selectNewNode(s);
            }
        }

        public void mouseOver(String s) {
        }

        public void mouseLeft(String s) {
        }
        public void requestStop() {
            running.set(false);
        }

        @Override
        public void run() {
            running.set(true);
            ViewerPipe fromViewer = this.viewer.newViewerPipe();
            fromViewer.addViewerListener(this);
            fromViewer.addSink(this.graph);

            while (running.get()) {
                try {
                    fromViewer.blockingPump();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            logger.debug("listener thread is stopped");
        }
    }


}

