package de.tu_dresden.inf.lat.evee.protege.concreteDomainReasoner.proofService;

import de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui.AbstractEveeProofPreferencesUI;

/** The preferences page of the proof services for concrete domains. */
public class CDProofPreferencesUI extends AbstractEveeProofPreferencesUI {

    public CDProofPreferencesUI() {
        super();
        this.setAbstractProofPreferencesManager(new CDProofPreferencesManager());
    }
}
