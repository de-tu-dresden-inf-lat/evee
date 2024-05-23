package de.tu_dresden.inf.lat.evee.proofs;

import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProof;
import de.tu_dresden.inf.lat.evee.proofs.json.JsonProofParser;
import de.tu_dresden.inf.lat.evee.proofs.tools.evaluators.AggregateProofEvaluator;
import de.tu_dresden.inf.lat.evee.proofs.tools.evaluators.inferences.JustificationComplexityEvaluator;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLAxiom;

import java.io.File;

import static org.junit.Assert.assertEquals;

/**
 * @author Stefan Borgwardt
 *
 */
public class EvaluatorTest {

    @Test
    public void justificationComplexityTest() {

        IProof<OWLAxiom> proof = JsonProofParser.getInstance().fromFile(new File(
                Thread.currentThread().getContextClassLoader().getResource("task00076.json").getPath()));

        double complexity = new AggregateProofEvaluator<>(new JustificationComplexityEvaluator()).evaluate(proof);

        System.out.println(complexity);

        assertEquals(24230.0d, complexity, 0.5d);
    }
}
