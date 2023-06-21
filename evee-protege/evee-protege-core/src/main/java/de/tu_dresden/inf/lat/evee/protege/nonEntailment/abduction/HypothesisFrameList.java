package de.tu_dresden.inf.lat.evee.protege.nonEntailment.abduction;

import org.protege.editor.core.ui.list.MListButton;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.frame.OWLFrame;
import org.protege.editor.owl.ui.framelist.OWLFrameList;
import org.semanticweb.owlapi.model.OWLAxiom;

import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class HypothesisFrameList extends OWLFrameList<Set<OWLAxiom>> {

    public HypothesisFrameList(OWLEditorKit editorKit, OWLFrame<Set<OWLAxiom>> frame) {
        super(editorKit, frame);
    }

    @Override
    protected List<MListButton> getButtons(Object value){
        return Collections.emptyList();
    }

}
