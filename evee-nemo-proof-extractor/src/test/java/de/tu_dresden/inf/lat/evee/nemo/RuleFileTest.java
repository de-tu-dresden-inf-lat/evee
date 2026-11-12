package de.tu_dresden.inf.lat.evee.nemo;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

/** The rule files of all calculi are found in the package of NemoReasoner, where the Protégé plugin embeds them. */
public class RuleFileTest {

    @Test
    public void ruleFilesOfAllCalculi() throws IOException {
        NemoReasoner reasoner = new NemoReasoner();
        for (ECalculus calculus : ECalculus.values()) {
            File ruleFile = reasoner.prepareRuleFile(reasoner.getRuleFileName(calculus));
            try {
                assertTrue(calculus + ": " + ruleFile, ruleFile.length() > 0);
            } finally {
                ruleFile.delete();
            }
        }
    }
}
