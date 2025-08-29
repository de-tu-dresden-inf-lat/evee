package de.tu_dresden.inf.lat.evee.protege.abstractProofService;

import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProof;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.preferences.AbstractEveeSuboptimalProofPreferencesManager;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui.EveeDynamicSuboptimalProofLoadingUI;
import org.semanticweb.owlapi.model.OWLAxiom;

public abstract class AbstractEveeSuboptimalDynamicProofAdapter extends AbstractEveeDynamicProofAdapter {

    private final EveeDynamicSuboptimalProofLoadingUI suboptimalLoadingUI;

    public AbstractEveeSuboptimalDynamicProofAdapter(AbstractEveeSuboptimalProofPreferencesManager proofPreferencesManager, EveeDynamicSuboptimalProofLoadingUI uiWindow) {
        super(proofPreferencesManager, uiWindow);
        this.suboptimalLoadingUI = uiWindow;
        this.suboptimalLoadingUI.setPreferencesManager(proofPreferencesManager);
    }

    @Override
    protected void proofGenerationCancelled(IProof<OWLAxiom> newProof) {
        super.proofGenerationCancelled(newProof);
        this.suboptimalLoadingUI.showSubOptimalProofMessage();
    }

}
