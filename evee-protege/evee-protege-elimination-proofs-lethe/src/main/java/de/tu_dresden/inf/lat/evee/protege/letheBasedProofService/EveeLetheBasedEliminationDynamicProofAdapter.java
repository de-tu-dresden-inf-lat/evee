package de.tu_dresden.inf.lat.evee.protege.letheBasedProofService;

import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProofGenerator;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeEliminationDynamicProofAdapter;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.preferences.AbstractEveeEliminationProofPreferencesManager;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui.EveeDynamicSuboptimalProofLoadingUI;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

public class EveeLetheBasedEliminationDynamicProofAdapter extends AbstractEveeEliminationDynamicProofAdapter {

    private final EveeLetheBasedEliminationProofPreferencesManager letheEliminationPreferencesManager;

    public EveeLetheBasedEliminationDynamicProofAdapter(IProofGenerator<OWLAxiom, OWLOntology> iProofGen,
                                                        EveeLetheBasedEliminationProofPreferencesManager proofPreferencesManager,
                                                        EveeDynamicSuboptimalProofLoadingUI uiWindow) {
        super(iProofGen, proofPreferencesManager, uiWindow);
        this.letheEliminationPreferencesManager = proofPreferencesManager;
    }

    @Override
    public void start(OWLAxiom entailment, OWLEditorKit editorKit){
//        todo: set proofGenerationParameters
        super.start(entailment, editorKit);
    }

}
