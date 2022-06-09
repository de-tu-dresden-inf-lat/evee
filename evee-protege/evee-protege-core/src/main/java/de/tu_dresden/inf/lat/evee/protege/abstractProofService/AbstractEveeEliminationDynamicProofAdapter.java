package de.tu_dresden.inf.lat.evee.protege.abstractProofService;

import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProof;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProofGenerator;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.preferences.AbstractEveeEliminationProofPreferencesManager;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui.EveeDynamicSuboptimalProofLoadingUI;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

public abstract class AbstractEveeEliminationDynamicProofAdapter extends AbstractEveeSuboptimalDynamicProofAdapter {

    private final EveeDynamicSuboptimalProofLoadingUI suboptimalUiWindow;
    private final AbstractEveeEliminationProofPreferencesManager eliminationPreferencesManager;

    public AbstractEveeEliminationDynamicProofAdapter(IProofGenerator<OWLAxiom, OWLOntology> iProofGen,
                                                      AbstractEveeEliminationProofPreferencesManager proofPreferencesManager,
                                                      EveeDynamicSuboptimalProofLoadingUI uiWindow) {
        super(iProofGen, proofPreferencesManager, uiWindow);
        this.suboptimalUiWindow = uiWindow;
        this.eliminationPreferencesManager = proofPreferencesManager;
    }

    @Override
    protected void proofGenerationCancelled(IProof<OWLAxiom> newProof) {
        super.proofGenerationCancelled(newProof);
        this.suboptimalUiWindow.showSubOptimalProofMessage();
    }

    @Override
    public void start(OWLAxiom entailment, OWLEditorKit editorKit){
//        todo: set eliminationProofGenerationParameters
        super.start(entailment, editorKit);
    }

}
