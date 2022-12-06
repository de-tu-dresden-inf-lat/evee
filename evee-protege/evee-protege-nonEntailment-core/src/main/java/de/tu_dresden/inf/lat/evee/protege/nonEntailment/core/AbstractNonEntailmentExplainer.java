package de.tu_dresden.inf.lat.evee.protege.nonEntailment.core;

import java.awt.*;
import java.awt.event.ActionListener;

abstract public class AbstractNonEntailmentExplainer {

    abstract public void computeNonEntailmentExplanation();

    abstract public void addListener(ActionListener listener);

    abstract public Component getResultComponent();

}
