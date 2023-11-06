package de.tu_dresden.inf.lat.evee.protege.nonEntailment.abduction;

import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.ISignatureModificationEventGenerator;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.ISignatureModificationEventListener;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.SignatureModificationEvent;
import org.protege.editor.owl.ui.framelist.OWLFrameListPopupMenuAction;
import org.semanticweb.owlapi.model.OWLEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.ActionEvent;
import java.util.Set;

public class AddToForbiddenVocabularyPopupMenuAction<Axiom> extends OWLFrameListPopupMenuAction<Axiom>
        implements ISignatureModificationEventGenerator {

    private ISignatureModificationEventListener signatureModificationEventListener;
    private final Set<OWLEntity> resultSignature;

    private final Logger logger = LoggerFactory.getLogger(AddToForbiddenVocabularyPopupMenuAction.class);

    public AddToForbiddenVocabularyPopupMenuAction(Set<OWLEntity> resultSignature){
        this.resultSignature = resultSignature;
    }
    @Override
    protected String getName() {
        return "Add signature to forbidden vocabulary";
    }

    @Override
    protected void initialise() throws Exception {}

    @Override
    protected void dispose() throws Exception {}

    @Override
    protected void updateState() {}

    @Override
    public void actionPerformed(ActionEvent e) {
        this.signatureModificationEventListener.handleSignatureModificationEvent(
                new SignatureModificationEvent(this));
    }

    @Override
    public void registerSignatureModificationEventListener(ISignatureModificationEventListener listener) {
        this.signatureModificationEventListener = listener;
    }

    @Override
    public Set<OWLEntity> getAdditionalSignatureNames() {
        return this.resultSignature;
    }
}
