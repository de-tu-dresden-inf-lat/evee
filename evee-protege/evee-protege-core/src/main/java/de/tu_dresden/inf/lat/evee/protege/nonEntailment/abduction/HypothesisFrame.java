package de.tu_dresden.inf.lat.evee.protege.nonEntailment.abduction;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.frame.AbstractOWLFrame;
import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.Set;

public class HypothesisFrame extends AbstractOWLFrame<Set<OWLAxiom>> {

    public HypothesisFrame(OWLEditorKit editorKit, String label) {
        super(editorKit.getOWLModelManager().getOWLOntologyManager());
        this.addSection(new HypothesisFrameSection(editorKit, label, this));
    }

}
