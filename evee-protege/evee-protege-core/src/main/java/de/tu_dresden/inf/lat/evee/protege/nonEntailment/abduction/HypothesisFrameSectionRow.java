package de.tu_dresden.inf.lat.evee.protege.nonEntailment.abduction;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.protege.editor.owl.ui.frame.AbstractOWLFrameSectionRow;
import org.protege.editor.owl.ui.frame.OWLFrameSection;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class HypothesisFrameSectionRow extends AbstractOWLFrameSectionRow<Set<OWLAxiom>, OWLAxiom, OWLAxiom> {

    protected HypothesisFrameSectionRow(OWLEditorKit owlEditorKit, OWLFrameSection<Set<OWLAxiom>, OWLAxiom, OWLAxiom> section, OWLOntology ontology, Set<OWLAxiom> rootObject, OWLAxiom axiom) {
        super(owlEditorKit, section, ontology, rootObject, axiom);
    }

    @Override
    protected OWLObjectEditor<OWLAxiom> getObjectEditor() {
        return null;
    }

    @Override
    protected OWLAxiom createAxiom(OWLAxiom owlAxiom) {
        return null;
    }

    @Override
    public List<? extends OWLObject> getManipulatableObjects() {
        return Collections.singletonList(this.getAxiom());
    }

    @Override
    public boolean isEditable(){
        return false;
    }

    @Override
    public boolean isDeleteable(){
        return false;
    }
}
