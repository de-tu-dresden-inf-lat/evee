package de.tu_dresden.inf.lat.evee.protege.nemoBasedProofService;

import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeProofService;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui.EveeDynamicProofLoadingUI;

import de.tu_dresden.inf.lat.evee.nemo.ECalculus;


public class EveeNemoBasedElkProofService extends AbstractEveeProofService{

    private static final String identifier = EveeNemoBasedProofPreferencesManager.ELK;

    public EveeNemoBasedElkProofService() {
        super(new EveeNemoBasedDynamicProofAdapter(
            ECalculus.ELK, 
            new EveeNemoBasedProofPreferencesManager(), 
            new EveeDynamicProofLoadingUI(identifier))
        );
    }

}
