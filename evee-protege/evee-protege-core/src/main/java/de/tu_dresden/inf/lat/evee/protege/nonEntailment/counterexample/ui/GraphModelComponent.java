package de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample.ui;

import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.counterexample.ICounterexampleGenerationEventListener;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.counterexample.IGraphModelControlPanel;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.counterexample.IGraphView;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.counterexample.IInteractiveGraphModelComponent;
import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.*;

import javax.swing.*;
import java.awt.*;
import java.util.*;


public class GraphModelComponent extends JPanel implements IInteractiveGraphModelComponent {
    private final JPanel modelViewPanel;
    private final IGraphModelControlPanel controlPanel;
    private final Logger logger = Logger.getLogger(GraphModelComponent.class);

    public GraphModelComponent(IGraphView graphView,
                               IGraphModelControlPanel controlPanel) {
        this.controlPanel = controlPanel;
        graphView.setControlPanel(this.controlPanel);
        this.setLayout(new BoxLayout(this, 0));
        this.modelViewPanel = new JPanel();
        this.modelViewPanel.setLayout(new BoxLayout(this.modelViewPanel, 0));
        this.modelViewPanel.setMinimumSize(new Dimension(500, 500));
        this.modelViewPanel.add(graphView.toComponent());
        this.add(this.modelViewPanel);
        this.add(this.controlPanel.toComponent());
        updateUI();
    }

    @Override
    public void update(IGraphView graphView) {
        this.modelViewPanel.removeAll();
        graphView.setControlPanel(controlPanel);
        this.modelViewPanel.add(graphView.toComponent());
        this.controlPanel.refreshSelectedClasses(new HashSet<>());
        updateUI();
    }

    @Override
    public void addCounterexampleGenerationEventListener(ICounterexampleGenerationEventListener listener) {
        controlPanel.addCounterexampleGenerationEventListener(listener);
    }

    @Override
    public void removeCurrentCounterexampleGenerationEventListener() {
        controlPanel.removeCurrentCounterexampleGenerationEventListener();
    }

    @Override
    public int getCurrentLabelsNum() {
        return controlPanel.getCurrentLabelsNum();
    }

    @Override
    public Set<OWLAxiom> getAdditionalAxioms() {
        return controlPanel.getAdditionalAxioms();
    }

    @Override
    public void selectNode(String nodeId) {
       controlPanel.selectNode(nodeId);
    }

    @Override
    public void refreshSelectedClasses(Collection<OWLClass> selection) {
        controlPanel.refreshSelectedClasses(selection);
    }

    @Override
    public Component toComponent() {
        return this;
    }
}

