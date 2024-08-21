package de.tu_dresden.inf.lat.evee.protege.letheProofExtractorBasedProofService;

import de.tu_dresden.inf.lat.evee.proofs.lethe.LetheProofGenerator;
import de.tu_dresden.inf.lat.evee.proofs.proofGenerators.MinimalTreeProofGenerator;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeProofService;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui.EveeDynamicProofLoadingUI;

public class EveeLetheBasedProofService extends AbstractEveeProofService {

    public EveeLetheBasedProofService(){
        super(new EveeLetheBasedDynamicProofAdapter(
                new MinimalTreeProofGenerator<>(
                        new LetheProofGenerator()),
                new EveeLetheBasedExtractorProofPreferencesManager(
                        EveeLetheBasedExtractorProofPreferencesManager.PROOF_SERVICE_ID),
                new EveeDynamicProofLoadingUI(
                        EveeLetheBasedExtractorProofPreferencesManager.PROOF_SERVICE_NAME)));
    }

}
