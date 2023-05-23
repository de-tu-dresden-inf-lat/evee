package de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample;

import org.apache.log4j.Logger;
import org.graphstream.graph.Graph;
import org.graphstream.ui.geom.Point2;
import org.graphstream.ui.spriteManager.Sprite;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;
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
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class GraphModelComponent extends JPanel {
    private final String DISJ_LIST = "Disjointnesses:";
    private final String CLASS_LIST = "Classes of selected element:";
    private final String NUM_SLIDER = "Number of displayed classes:";
    private final String RECOMPUTE_BUTTON = "Recompute example";
    private final String REFRESH_BUTTON = "Refresh example";
    private final String ADD_DISJ_BUTTON = "Add disjointnesses";
    private final String REMOVE_DISJ_BUTTON = "Remove disjointnesses";
    private final String ADD_TO_ONT_BUTTON = "Add all to ontology";
    private final int LABELS_MIN = 0;
    private final int LABELS_MAX = 10;
    private final int LABELS_INIT = 2;
    private final int BIG_SPACE = 30;
    private final int SMALL_SPACE = 20;
    private final DefaultListModel<OWLClass> classListModel;
    private final DefaultListModel<OWLAxiom> axiomListModel;
    private final OWLEditorKit owlEditorKit;
    private final ModelManager modelManager;
    private final OWLDataFactory df;
    private final JPanel viewPanel;
    private Viewer viewer;
    private Map<String, List<OWLClass>> classMap;
    private JList classList;
    private JList axiomList;
    private Component viewComponent;
    private View view;
    private Set<Sprite> classLabels;
    private final Logger logger = Logger.getLogger(GraphModelComponent.class);

    public GraphModelComponent(ModelManager modelManager, OWLEditorKit owlEditorKit) {
        this.modelManager = modelManager;
        this.classMap = this.modelManager.getClassMap();
        this.owlEditorKit = owlEditorKit;
        this.classListModel = new DefaultListModel();
        this.axiomListModel = new DefaultListModel();
        this.viewer = this.modelManager.getViewer();
        this.classLabels = this.modelManager.getClassLabels();
        this.df = OWLManager.getOWLDataFactory();
        this.setLayout(new BoxLayout(this, 0));
        this.viewPanel = new JPanel();
        this.viewPanel.setLayout(new BoxLayout(this.viewPanel, 0));
        this.viewPanel.setMinimumSize(new Dimension(500, 500));
        this.view = this.viewer.addDefaultView(false);
        MouseManager mouseManager =new MouseManager(classLabels,classMap,classListModel,viewer);
        this.view.setMouseManager(mouseManager);
        this.viewComponent = (Component) view;
        this.viewComponent.addMouseWheelListener(mouseManager);
        this.viewPanel.add(this.viewComponent);
        this.add(this.viewPanel);
        this.add(this.getRightPanel());
    }

    private void resetModel() {
        this.classMap = this.modelManager.getClassMap();
        this.viewer = this.modelManager.getViewer();
        this.classLabels = this.modelManager.getClassLabels();
        this.viewPanel.remove(this.viewComponent);
        MouseManager mouseManager =new MouseManager(classLabels,classMap,classListModel,viewer);
        this.view = this.viewer.addDefaultView(false);
        this.view.setMouseManager(mouseManager);
        this.viewComponent = (Component) view;
        this.viewComponent.addMouseWheelListener(mouseManager);
        this.viewPanel.add(this.viewComponent);
        this.updateUI();
        logger.debug("model is updated");
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
        this.axiomList.setPreferredSize(new Dimension(180,300));
        JPanel axiomListPanel = new JPanel();
        axiomListPanel.setLayout(new BoxLayout(axiomListPanel, BoxLayout.Y_AXIS));
        JPanel axiomListBorder = new JPanel();
        axiomListBorder.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEmptyBorder(5, 5, 5, 5),
                DISJ_LIST));
        axiomListBorder.add(new JScrollPane(this.axiomList));
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
        classListBorder.add(new JScrollPane(this.classList));
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
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        buttonPanel.add(this.getAddToOntologyButton());
        return buttonPanel;
    }
    private JButton getRemoveAxiomsButton() {
        JButton removeAxiomsButton = new JButton(REMOVE_DISJ_BUTTON);
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
        JButton refreshButton = new JButton(REFRESH_BUTTON);
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logger.debug("refreshing model");
                modelManager.refreshModel();
                resetModel();
            }
        });
        refreshButton.setAlignmentX(0.5F);
        return refreshButton;
    }

    private JButton getRecomputeButton() {
        JButton recomputeButton = new JButton(RECOMPUTE_BUTTON);
        recomputeButton.addActionListener(new ActionListener() {
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
                    logger.debug("recomputing model");
                    modelManager.recomputeModel(additionalAxioms);
                    resetModel();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(new JPanel(), ex.getMessage(), "Error", 0);
                }
            }
        });
        recomputeButton.setAlignmentX(0.5F);
        return recomputeButton;
    }

    private JButton getAddToAxiomListButton() {
        JButton addToAxiomList = new JButton(ADD_DISJ_BUTTON);
        addToAxiomList.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (classList.getSelectedValues().length < 2) {
                    JOptionPane.showMessageDialog(new JPanel(), "Please select at least 2 classes", "Error", 0);
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
        JButton addToOntology = new JButton(ADD_TO_ONT_BUTTON);
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
                NUM_SLIDER));
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
}

