package de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample.ui;

import de.tu_dresden.inf.lat.evee.general.data.exceptions.ModelGenerationException;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.core.NonEntailmentExplanationLoadingUIManager;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.core.NonEntailmentExplanationProgressTracker;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample.ModelManager;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample.MouseManager;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.IExplanationLoadingUIListener;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.ExplanationLoadingUIEvent;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.ExplanationLoadingUIEventType;
import org.apache.log4j.Logger;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.renderer.OWLCellRenderer;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;



public class GraphModelComponent extends JPanel {
    protected static final String DEFAULT_UI_TITLE = "LOADING";
    private final String DISJ_LIST = "Disjointnesses:";
    private final String CLASS_LIST = "Classes of selected element:";
    private final String NUM_SLIDER = "Number of displayed classes:";
    private final String RECOMPUTE= "Recompute example";
    private final String REFRESH= "Refresh example";
    private final String ADD_DISJ= "Add disjointness";
    private final String REMOVE_DISJ= "Remove disjointnesses";
    private final String ADD_TO_ONT= "Add all to ontology";
    private final int LABELS_MIN = 0;
    private final int LABELS_MAX = 10;
    private final int LABELS_INIT = 2;
    private final int BIG_SPACE = 30;
    private final int SMALL_SPACE = 20;
    protected NonEntailmentExplanationLoadingUIManager loadingUI;
    private DefaultListModel<OWLClass> classListModel;
    private final DefaultListModel<OWLAxiom> axiomListModel;
    private final OWLEditorKit owlEditorKit;
    private final ModelManager modelManager;
    private final OWLDataFactory df = OWLManager.getOWLDataFactory();
    private final JPanel modelViewPanel;
    private Viewer viewer;
    private Map<String, List<OWLClass>> individualClassMap;
    private Map<String[],List<OWLObjectProperty>> objectPropertyMap;
    private JList classList;
    private JList axiomList;
    private Component viewComponent;
    private View view;
    private final Logger logger = Logger.getLogger(GraphModelComponent.class);
    private final GraphComponentListener listener;

    public GraphModelComponent(ModelManager modelManager, OWLEditorKit owlEditorKit) {
        this.listener = new GraphComponentListener();
        this.modelManager = modelManager;
        this.owlEditorKit = owlEditorKit;
        this.classListModel = new DefaultListModel();
        this.axiomListModel = new DefaultListModel();
        this.setLayout(new BoxLayout(this, 0));
        this.modelViewPanel = new JPanel();
        this.modelViewPanel.setLayout(new BoxLayout(this.modelViewPanel, 0));
        this.modelViewPanel.setMinimumSize(new Dimension(500, 500));
        this.configureModelViewPanel();
        this.add(this.modelViewPanel);
        this.add(this.getRightPanel());
        this.loadingUI = new NonEntailmentExplanationLoadingUIManager(DEFAULT_UI_TITLE);
        this.loadingUI.registerLoadingUIListener(listener);
        this.loadingUI.setup(owlEditorKit);
        this.loadingUI.initialise();
    }
    private void resetModelView() {
        this.modelViewPanel.remove(this.viewComponent);
        this.classListModel.removeAllElements();
        this.configureModelViewPanel();
        this.updateUI();
    }
    private void configureModelViewPanel() {
        this.viewer = this.modelManager.getGraphModelViewer();
        this.individualClassMap = this.modelManager.getIndividualClassMap();
        this.objectPropertyMap = this.modelManager.getObjectPropertyMap();
        MouseManager mouseManager =new MouseManager(individualClassMap,objectPropertyMap,classListModel,viewer);
        this.view = this.viewer.addDefaultView(false);
        this.view.setMouseManager(mouseManager);
        this.viewComponent = (Component) view;
        this.viewComponent.addMouseWheelListener(mouseManager);
        this.modelViewPanel.add(this.viewComponent);
    }

    private JPanel getRightPanel() {
        this.createClassList();
        this.createAxiomList();
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setAlignmentX(0.5F);
        rightPanel.setBorder(new EmptyBorder(new Insets(15, 15, 15, 15)));
        rightPanel.setMaximumSize(new Dimension(200,  2000));
        rightPanel.add(Box.createRigidArea(new Dimension(0, SMALL_SPACE)));
//        rightPanel.add(getStyleCombobox());
        rightPanel.add(getLabelNumSliderPanel());
        rightPanel.add(Box.createRigidArea(new Dimension(0, SMALL_SPACE)));
        rightPanel.add(getRefreshButton());
        rightPanel.add(Box.createRigidArea(new Dimension(0, BIG_SPACE)));
        rightPanel.add(getClassListPanel());
        rightPanel.add(Box.createRigidArea(new Dimension(0, BIG_SPACE)));
        rightPanel.add(getAxiomListPanel());
        return rightPanel;
    }
    private JPanel getAxiomListPanel() {
//        this.axiomList.setPreferredSize(new Dimension(180,300));
        JPanel axiomListPanel = new JPanel();
        axiomListPanel.setLayout(new BoxLayout(axiomListPanel, BoxLayout.Y_AXIS));
        JPanel axiomListBorder = new JPanel();
        axiomListBorder.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEmptyBorder(5, 5, 5, 5),
                DISJ_LIST));
        JScrollPane scrollable = new JScrollPane(this.axiomList);
        scrollable.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollable.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        axiomListBorder.add(scrollable);
        axiomListPanel.add(axiomListBorder);
        axiomListPanel.add(Box.createRigidArea(new Dimension(0, SMALL_SPACE)));
        axiomListPanel.add(getButtonPanel());
        axiomListPanel.add(Box.createRigidArea(new Dimension(0, BIG_SPACE)));
        return axiomListPanel;
    }
    private JPanel getClassListPanel() {
        JPanel classListPanel = new JPanel();
        classListPanel.setLayout(new BoxLayout(classListPanel, BoxLayout.Y_AXIS));
        JPanel classListBorder = new JPanel();
        classListBorder.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEmptyBorder(0, 0, 0, 0),
                CLASS_LIST));
        JScrollPane scrollable = new JScrollPane(this.classList);
        scrollable.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollable.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        classListBorder.add(scrollable);
        classListBorder.setAlignmentY(TOP_ALIGNMENT);
        classListPanel.add(classListBorder);
        classListPanel.add(Box.createRigidArea(new Dimension(0, SMALL_SPACE)));
        JButton addToAxiomListButton = this.getAddToAxiomListButton();
        addToAxiomListButton.setAlignmentY(TOP_ALIGNMENT);
        classListPanel.add(addToAxiomListButton);

        return classListPanel;
    }

    private JPanel getButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setAlignmentY(TOP_ALIGNMENT);
        buttonPanel.add(this.getRecomputeButton());
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        buttonPanel.add(this.getRemoveAxiomsButton());
        buttonPanel.add(Box.createRigidArea(new Dimension(0, SMALL_SPACE)));
        buttonPanel.add(this.getAddToOntologyButton());
        return buttonPanel;
    }
    private JButton getRemoveAxiomsButton() {
        JButton removeAxiomsButton = new JButton(REMOVE_DISJ);
        removeAxiomsButton.addActionListener(listener);
        removeAxiomsButton.setAlignmentX(0.5F);
        return removeAxiomsButton;
    }

    private JButton getRefreshButton() {
        JButton refreshButton = new JButton(REFRESH);
        refreshButton.addActionListener(listener);
        refreshButton.setAlignmentX(0.5F);
        return refreshButton;
    }

    private JButton getRecomputeButton() {
        JButton recomputeButton = new JButton(RECOMPUTE);
        recomputeButton.addActionListener(listener);
        recomputeButton.setAlignmentX(0.5F);
        return recomputeButton;
    }

    private JButton getAddToAxiomListButton() {
        JButton addToAxiomList = new JButton(ADD_DISJ);
        addToAxiomList.addActionListener(listener);
        addToAxiomList.setAlignmentX(0.5F);
        return addToAxiomList;
    }

    private JButton getAddToOntologyButton() {
        JButton addToOntology = new JButton(ADD_TO_ONT);
        addToOntology.addActionListener(listener);
        addToOntology.setAlignmentX(CENTER_ALIGNMENT);
        return addToOntology;
    }

    private JPanel getLabelNumSliderPanel() {
        JPanel labelNumSliderPanel = new JPanel();
        labelNumSliderPanel.setLayout(new BoxLayout(labelNumSliderPanel, BoxLayout.Y_AXIS));
        labelNumSliderPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEmptyBorder(0, 0, 0, 0),
                NUM_SLIDER));
        JSlider labelNumSlider = new JSlider(LABELS_MIN,LABELS_MAX,LABELS_INIT);
        labelNumSlider.setMajorTickSpacing(2);
        labelNumSlider.setMinorTickSpacing(1);
        labelNumSlider.setPaintTicks(true);
        labelNumSlider.setPaintLabels(true);
        labelNumSlider.addChangeListener(listener);
        labelNumSliderPanel.add(labelNumSlider);
        return labelNumSliderPanel;
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




    private class GraphComponentListener implements ActionListener,ChangeListener,IExplanationLoadingUIListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            JSlider source = (JSlider)e.getSource();
            if (!source.getValueIsAdjusting()) {
                logger.debug("number of labels is adjusted. New number is "+source.getValue());
                modelManager.setMaxLabelNumber(source.getValue());
            }
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            String actionCommand = e.getActionCommand();

            if(actionCommand == ADD_DISJ) {
                addDisjointnesses();
            } else if (actionCommand == ADD_TO_ONT) {
                addAxiomsToOntology();
            } else if (actionCommand == REMOVE_DISJ) {
                removeDisjointnesses();
            } else if (actionCommand == REFRESH) {
                refresh();
            } else if (actionCommand == RECOMPUTE) {
                recompute();
            }
        }
        @Override
        public void handleUIEvent(ExplanationLoadingUIEvent event) {
            if (event.getType().equals(
                    ExplanationLoadingUIEventType
                            .EXPLANATION_GENERATION_CANCELLED)){

                logger.debug("Cancelling non entailment explanation generation of service");

            }
        }

        private void addDisjointnesses() {
            if (classList.getSelectedValues().length < 2) {
                JOptionPane.showMessageDialog(new JPanel(), "Please select at least 2 classes", "Error", 0);
            } else {
                axiomListModel.addElement(
                        df.getOWLDisjointClassesAxiom(Arrays.stream(classList.getSelectedValues())
                                .map(ax -> (OWLClassExpression) ax)
                                .collect(Collectors.toSet())));
            }
        }

        private void addAxiomsToOntology() {
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
        private void removeDisjointnesses() {
            Arrays.stream(axiomList.getSelectedValues()).map((ax) -> (OWLAxiom) ax).forEach((ax) -> {
                axiomListModel.removeElement(ax);
            });
        }

        private void refresh() {
            logger.debug("refreshing model");
            modelManager.refreshGraphModelViewer();
            resetModelView();
        }
        private void recompute() {
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
            logger.debug("recomputing model");
            loadingUI.resetLoadingUI();
            loadingUI.activeLoadingUI();
            logger.debug("loading UI is activated");
            NonEntailmentExplanationProgressTracker progressTracker = new NonEntailmentExplanationProgressTracker();
            progressTracker.registerLoadingUIListener(loadingUI);
            ModelRefreshWorker worker = new ModelRefreshWorker(additionalAxioms,progressTracker);
            worker.execute();
        }
    }



    public class ModelRefreshWorker extends SwingWorker<Void, Void> {
        private final Set<OWLAxiom> additionalAxioms;
        private final NonEntailmentExplanationProgressTracker progressTracker;


        public ModelRefreshWorker(Set<OWLAxiom> additionalAxioms, NonEntailmentExplanationProgressTracker progressTracker) {
            this.additionalAxioms = additionalAxioms;

            this.progressTracker = progressTracker;
        }

        @Override
        protected Void doInBackground() {
            try {

                modelManager.refreshModelViewer(additionalAxioms,progressTracker);
                resetModelView();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(new JPanel(), ex.getMessage(), "Error", 0);
            }


            return null;
        }
        @Override
        protected void done() {
            try {
                get();

            } catch (Exception e) {

            }
            progressTracker.done();
        }
    }
}

