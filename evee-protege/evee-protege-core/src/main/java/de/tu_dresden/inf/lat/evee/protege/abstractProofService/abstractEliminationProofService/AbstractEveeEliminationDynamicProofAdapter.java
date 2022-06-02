package de.tu_dresden.inf.lat.evee.protege.abstractProofService.abstractEliminationProofService;

import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProof;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProofGenerator;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeDynamicProofAdapter;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.abstractEliminationProofService.preferences.AbstractEveeEliminationProofPreferencesManager;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.abstractEliminationProofService.ui.EveeDynamicEliminationProofLoadingUI;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

public abstract class AbstractEveeEliminationDynamicProofAdapter extends AbstractEveeDynamicProofAdapter {

    public AbstractEveeEliminationDynamicProofAdapter(IProofGenerator<OWLAxiom, OWLOntology> iProofGen, String uiTitle, AbstractEveeEliminationProofPreferencesManager proofPreferencesManager) {
        super(iProofGen, uiTitle, proofPreferencesManager);
    }

    @Override
    protected void createUI(OWLEditorKit editorKit){
        this.uiWindow = new EveeDynamicEliminationProofLoadingUI(this, this.uiTitle, editorKit,
                (AbstractEveeEliminationProofPreferencesManager) this.proofPreferencesManager);
        this.uiWindow.updateMessage(this.LOADING);
    }

    @Override
    protected void proofGenerationCancelled(IProof<OWLAxiom> newProof) {
        super.proofGenerationCancelled(newProof);
        ((EveeDynamicEliminationProofLoadingUI) this.uiWindow).showSubOptimalProofMessage();
    }

    @Override
    protected void setProofGeneratorParameters() {
//        todo: implement setting of preferences once possible
    }
}
