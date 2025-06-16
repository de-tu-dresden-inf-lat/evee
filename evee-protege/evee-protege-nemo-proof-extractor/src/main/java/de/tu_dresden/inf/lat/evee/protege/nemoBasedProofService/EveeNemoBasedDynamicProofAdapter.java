package de.tu_dresden.inf.lat.evee.protege.nemoBasedProofService;

import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeDynamicProofAdapter;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.preferences.AbstractEveeProofPreferencesManager;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui.EveeDynamicProofLoadingUI;
import de.tu_dresden.inf.lat.evee.nemo.*;

public class EveeNemoBasedDynamicProofAdapter extends AbstractEveeDynamicProofAdapter{

    public EveeNemoBasedDynamicProofAdapter(
            ECalculus calc, 
            AbstractEveeProofPreferencesManager proofPreferencesManager,
            EveeDynamicProofLoadingUI uiWindow) {

        super(proofPreferencesManager, uiWindow);
        
        NemoProofGenerator gen = new NemoProofGenerator();
        gen.setCalculus(calc);
        setInnerProofGenerator(gen);
        
        //TODO needed?
        //resetCachingProofGenerator();
    }
    
}
