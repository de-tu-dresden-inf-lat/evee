package de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui;

import org.junit.Test;

/**
 * Protégé disposes of the menu actions of every window it closes, also of the windows it creates and closes again
 * when opening an ontology, in which the action was never used.
 */
public class EveeDynamicProofSignatureSelectionWindowTest {

    @Test
    public void disposeWithoutOpening() {
        new EveeDynamicProofSignatureSelectionWindow().dispose();
    }
}
