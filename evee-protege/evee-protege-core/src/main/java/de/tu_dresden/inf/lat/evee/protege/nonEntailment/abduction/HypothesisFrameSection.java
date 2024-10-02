package de.tu_dresden.inf.lat.evee.protege.nonEntailment.abduction;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.protege.editor.owl.ui.frame.AbstractOWLFrameSection;
import org.protege.editor.owl.ui.frame.OWLFrame;
import org.protege.editor.owl.ui.frame.OWLFrameSectionRow;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class HypothesisFrameSection extends AbstractOWLFrameSection<Set<OWLAxiom>, OWLAxiom, OWLAxiom> {

    protected HypothesisFrameSection(OWLEditorKit editorKit, String label, OWLFrame<? extends Set<OWLAxiom>> frame) {
        super(editorKit, label, frame);
    }

    @Override
    protected OWLAxiom createAxiom(OWLAxiom owlAxiom) {
        return null;
    }

    @Override
    public OWLObjectEditor<OWLAxiom> getObjectEditor() {
        return null;
    }

    @Override
    protected void refill(OWLOntology owlOntology) {
        Set<OWLAxiom> hypothesis = this.getRootObject();
        for (OWLAxiom axiom : hypothesis){
            HypothesisFrameSectionRow row = new HypothesisFrameSectionRow(this.getOWLEditorKit(),
                    this, this.getOWLEditorKit().getOWLModelManager().getActiveOntology(),
                    this.getRootObject(), axiom);
            this.addRow(row);
        }
    }

    @Override
    protected void clear() {

    }

    @Override
    public Comparator<OWLFrameSectionRow<Set<OWLAxiom>, OWLAxiom, OWLAxiom>> getRowComparator() {
        return null;
    }

    @Override
    public boolean canAdd(){
        return false;
    }

    @Override
    public boolean canAcceptDrop(List<OWLObject> objects){
        return false;
    }

}
