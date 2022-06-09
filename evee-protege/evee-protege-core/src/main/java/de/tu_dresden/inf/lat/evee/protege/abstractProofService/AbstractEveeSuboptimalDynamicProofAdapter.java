package de.tu_dresden.inf.lat.evee.protege.abstractProofService;

import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProofGenerator;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.preferences.AbstractEveeSuboptimalProofPreferencesManager;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui.EveeDynamicSuboptimalProofLoadingUI;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

public abstract class AbstractEveeSuboptimalDynamicProofAdapter extends AbstractEveeDynamicProofAdapter {

    private EveeDynamicSuboptimalProofLoadingUI suboptimalLoadingUI;

    public AbstractEveeSuboptimalDynamicProofAdapter(IProofGenerator<OWLAxiom, OWLOntology> iProofGen, AbstractEveeSuboptimalProofPreferencesManager proofPreferencesManager, EveeDynamicSuboptimalProofLoadingUI uiWindow) {
        super(iProofGen, proofPreferencesManager, uiWindow);
        this.suboptimalLoadingUI = uiWindow;
        this.suboptimalLoadingUI.setPreferencesManager(proofPreferencesManager);
    }

}
