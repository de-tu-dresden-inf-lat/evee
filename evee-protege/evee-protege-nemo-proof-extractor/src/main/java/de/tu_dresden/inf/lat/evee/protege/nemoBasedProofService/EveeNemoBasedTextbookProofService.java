package de.tu_dresden.inf.lat.evee.protege.nemoBasedProofService;

import de.tu_dresden.inf.lat.evee.nemo.ECalculus;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeProofService;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui.EveeDynamicProofLoadingUI;

public class EveeNemoBasedTextbookProofService extends AbstractEveeProofService{

    private static final String identifier = EveeNemoBasedProofPreferencesManager.TEXTBOOK;

    protected EveeNemoBasedTextbookProofService() {
        super(new EveeNemoBasedDynamicProofAdapter(
            ECalculus.ELK, 
            new EveeNemoBasedProofPreferencesManager(), 
            new EveeDynamicProofLoadingUI(identifier))
        );
    }
    
}
