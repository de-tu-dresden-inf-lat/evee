package de.tu_dresden.inf.lat.evee.protege.nonEntailment.core;

import de.tu_dresden.inf.lat.evee.proofs.interfaces.OWLNonEntailmentExplainer;
import org.protege.editor.core.plugin.ProtegePluginInstance;
import org.protege.editor.owl.OWLEditorKit;

import java.awt.*;

public interface NonEntailmentExplanationService extends OWLNonEntailmentExplainer, ProtegePluginInstance {

    public void setup(OWLEditorKit editorKit);

    public String getName();

    public void computeExplanation();

    public Component getResultComponent();

}
