package de.tu_dresden.inf.lat.evee.protege.nemoBasedProofService;

import de.tu_dresden.inf.lat.evee.nemo.ECalculus;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeProofService;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui.EveeDynamicProofLoadingUI;

public class EveeNemoBasedEnvelopeProofService extends AbstractEveeProofService {

    private static final String identifier = EveeNemoBasedProofPreferencesManager.ENVELOPE;


    protected EveeNemoBasedEnvelopeProofService() {
        super(new EveeNemoBasedDynamicProofAdapter(
            ECalculus.ENVELOPE, 
            new EveeNemoBasedProofPreferencesManager(), 
            new EveeDynamicProofLoadingUI(identifier))
        );
    }
    
}
