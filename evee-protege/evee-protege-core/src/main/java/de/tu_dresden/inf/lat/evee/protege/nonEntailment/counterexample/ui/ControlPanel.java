package de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample.ui;

import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.counterexample.ICounterexampleGenerationEventListener;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.counterexample.IGraphModelControlPanel;
import org.apache.log4j.Logger;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ControlPanel extends  JPanel implements IGraphModelControlPanel {
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
    private final DefaultListModel<OWLClass> classListModel = new DefaultListModel();
    private final DefaultListModel<OWLAxiom> disjAxiomsListModel = new DefaultListModel();
    private final Logger logger = Logger.getLogger(ControlPanel.class);
    private final OWLDataFactory df = OWLManager.getOWLDataFactory();
    private int currentlabelsNum = 2;
    private JList classList;
    private JList disjAxiomsList;
    private OWLEditorKit owlEditorKit;
    private ControlPanelEventListener controlPanelEventListener;
    private ICounterexampleGenerationEventListener counterexampleGenerationEventListener;

    public ControlPanel(OWLEditorKit owlEditorKit) {
        this.controlPanelEventListener = new ControlPanelEventListener();
        this.owlEditorKit = owlEditorKit;
        this.createClassList();
        this.createDisjAxiomsList();
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setAlignmentX(0.5F);
        this.setBorder(new EmptyBorder(new Insets(15, 15, 15, 15)));
        this.setMaximumSize(new Dimension(200,  2000));
        this.add(Box.createRigidArea(new Dimension(0, SMALL_SPACE)));
        this.add(getLabelNumSliderPanel());
        this.add(Box.createRigidArea(new Dimension(0, SMALL_SPACE)));
        this.add(getRefreshButton());
        this.add(Box.createRigidArea(new Dimension(0, BIG_SPACE)));
        this.add(getClassListPanel());
        this.add(Box.createRigidArea(new Dimension(0, BIG_SPACE)));
        this.add(getAxiomListPanel());
    }

    @Override
    public void addCounterexampleGenerationEventListener(ICounterexampleGenerationEventListener listener) {
        this.counterexampleGenerationEventListener = listener;
    }

    @Override
    public void removeCurrentCounterexampleGenerationEventListener() {
        this.counterexampleGenerationEventListener = null;
    }

    @Override
    public int getCurrentLabelsNum() {
        return currentlabelsNum;
    }

    @Override
    public Set<OWLAxiom> getAdditionalAxioms() {
        return Arrays.stream(disjAxiomsListModel.toArray())
                .map(ax -> (OWLAxiom) ax)
                .collect(Collectors.toSet());
    }

    @Override
    public void selectNode(String nodeId) {

    }

    @Override
    public void refreshSelectedClasses(Collection<OWLClass> selection) {
        classListModel.removeAllElements();
        for (OWLClass cl : selection) {
            classListModel.addElement(cl);
        }
    }

    @Override
    public Component toComponent() {
        return this;
    }

    private JPanel getAxiomListPanel() {
        JPanel axiomListPanel = new JPanel();
        axiomListPanel.setLayout(new BoxLayout(axiomListPanel, BoxLayout.Y_AXIS));
        JPanel axiomListBorder = new JPanel();
        axiomListBorder.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEmptyBorder(5, 5, 5, 5),
                DISJ_LIST));
        JScrollPane scrollable = new JScrollPane(this.disjAxiomsList);
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
        removeAxiomsButton.addActionListener(controlPanelEventListener);
        removeAxiomsButton.setAlignmentX(0.5F);
        return removeAxiomsButton;
    }

    private JButton getRefreshButton() {
        JButton refreshButton = new JButton(REFRESH);
        refreshButton.addActionListener(controlPanelEventListener);
        refreshButton.setAlignmentX(0.5F);
        return refreshButton;
    }

    private JButton getRecomputeButton() {
        JButton recomputeButton = new JButton(RECOMPUTE);
        recomputeButton.addActionListener(controlPanelEventListener);
        recomputeButton.setAlignmentX(0.5F);
        return recomputeButton;
    }

    private JButton getAddToAxiomListButton() {
        JButton addToAxiomList = new JButton(ADD_DISJ);
        addToAxiomList.addActionListener(controlPanelEventListener);
        addToAxiomList.setAlignmentX(0.5F);
        return addToAxiomList;
    }

    private JButton getAddToOntologyButton() {
        JButton addToOntology = new JButton(ADD_TO_ONT);
        addToOntology.addActionListener(controlPanelEventListener);
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
        labelNumSlider.addChangeListener(controlPanelEventListener);
        labelNumSliderPanel.add(labelNumSlider);
        return labelNumSliderPanel;
    }
    private void createClassList() {
        this.classList = new JList(this.classListModel);
        this.classList.setSelectionMode(2);
        this.classList.setCellRenderer(new OWLCellRenderer(this.owlEditorKit));
    }

    private void createDisjAxiomsList() {
        this.disjAxiomsList = new JList(this.disjAxiomsListModel);
        this.disjAxiomsList.setSelectionMode(2);
        this.disjAxiomsList.setCellRenderer(new OWLCellRenderer(this.owlEditorKit));
    }


    private class ControlPanelEventListener
            implements ActionListener, ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            JSlider source = (JSlider)e.getSource();
            if (!source.getValueIsAdjusting()) {
                logger.info("number of labels is adjusted. New number is "+source.getValue());
                currentlabelsNum = (source.getValue());
            }
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            String actionCommand = e.getActionCommand();
            logger.info("action command "+actionCommand);
            if(actionCommand == ADD_DISJ) {
                addDisjointnesses();
            } else if (actionCommand == ADD_TO_ONT) {
                addAxiomsToOntology();
            } else if (actionCommand == REMOVE_DISJ) {
                removeDisjointnesses();
            } else if (actionCommand == REFRESH) {
                refreshModel();
            } else if (actionCommand == RECOMPUTE) {
                recomputeModel();
            }
        }

    }
    private void recomputeModel() {
        counterexampleGenerationEventListener.onModelRecomputed(this);
    }
    private void refreshModel() {
        logger.debug("refresh method is called");
        counterexampleGenerationEventListener.onModelRefreshed(this);
    }

    private void addDisjointnesses() {
        if (classList.getSelectedValues().length < 2) {
            JOptionPane.showMessageDialog(new JPanel(), "Please select at least 2 classes", "Error", 0);
        } else {
            disjAxiomsListModel.addElement(
                    df.getOWLDisjointClassesAxiom(Arrays.stream(classList.getSelectedValues())
                            .map(ax -> (OWLClassExpression) ax)
                            .collect(Collectors.toSet())));
        }
    }

    private void addAxiomsToOntology() {
        counterexampleGenerationEventListener.onDisjointnessesAddedToOntology(this);
        disjAxiomsListModel.removeAllElements();

    }
    private void removeDisjointnesses() {
        Arrays.stream(disjAxiomsList.getSelectedValues()).map((ax) -> (OWLAxiom) ax).forEach((ax) -> {
            disjAxiomsListModel.removeElement(ax);
        });
    }

}
