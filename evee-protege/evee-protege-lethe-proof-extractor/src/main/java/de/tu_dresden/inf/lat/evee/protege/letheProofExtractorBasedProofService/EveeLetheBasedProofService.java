package de.tu_dresden.inf.lat.evee.protege.letheProofExtractorBasedProofService;

import de.tu_dresden.inf.lat.evee.proofs.lethe.LetheProofGenerator;
import de.tu_dresden.inf.lat.evee.proofs.proofGenerators.MinimalTreeProofGenerator;
import de.tu_dresden.inf.lat.evee.proofs.proofGenerators.OWLSignatureBasedMinimalTreeProofGenerator;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeProofService;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui.EveeDynamicProofLoadingUI;

public class EveeLetheBasedProofService extends AbstractEveeProofService {

    private static final String identifier = EveeLetheBasedExtractorProofPreferencesManager.DETAILED;

    public EveeLetheBasedProofService(){
        super(new EveeLetheBasedDynamicProofAdapter(
                        new OWLSignatureBasedMinimalTreeProofGenerator(
                                new MinimalTreeProofGenerator<>(
                                        new LetheProofGenerator())),
                new EveeLetheBasedExtractorProofPreferencesManager(identifier),
                new EveeDynamicProofLoadingUI(identifier)));
    }

}
