package de.tu_dresden.inf.lat.evee.concreteDomains;

import de.tu_dresden.inf.lat.evee.concreteDomains.elkCD.ELKCDReasoner;
import de.tu_dresden.inf.lat.evee.concreteDomains.owlApi.ConstraintSizeWeightedTreeSizeMeasure;
import de.tu_dresden.inf.lat.evee.concreteDomains.owlApi.OWLCDProofGenerator;
import de.tu_dresden.inf.lat.evee.concreteDomains.owlApi.OWLCDReasonerFactory;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProof;
import de.tu_dresden.inf.lat.evee.proofs.proofGenerators.MinimalTreeProofGenerator;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.StringDocumentSource;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.InferenceType;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** PACER can be used from Java as the wiki page "Concrete domains" shows. */
public class JavaUsageTest {

    private static final String NS = "http://ex.org/o#";

    @Test
    public void reasonAndProve() throws Exception {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new StringDocumentSource(
                "Prefix(:=<" + NS + ">)\nPrefix(cd:=<http://tu-dresden.de/inf/lat/evee/cd#>)\n"
                        + "Ontology(<http://ex.org/o>\nAnnotation(cd:concreteDomain cd:QDiff)\n"
                        + "AnnotationAssertion(cd:constraint :Adult \":age > 17\")\n"
                        + "AnnotationAssertion(cd:constraint :Age70 \":age = 70\")\n"
                        + "SubClassOf(:Patient1 :Age70)\n)\n"));
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLAxiom axiom = factory.getOWLSubClassOfAxiom(factory.getOWLClass(IRI.create(NS + "Patient1")),
                factory.getOWLClass(IRI.create(NS + "Adult")));

        // the static type ELKCDReasoner, whose precomputeInferences has to be a Java varargs method
        @SuppressWarnings("unchecked")
        ELKCDReasoner<CDConstraint> reasoner =
                (ELKCDReasoner<CDConstraint>) new OWLCDReasonerFactory().createReasoner(ontology);
        try {
            reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
            assertTrue(reasoner.isEntailed(axiom));

            OWLCDProofGenerator generator = new OWLCDProofGenerator(reasoner);
            MinimalTreeProofGenerator<OWLAxiom, OWLOntology> minimal = new MinimalTreeProofGenerator<>(generator,
                    new ConstraintSizeWeightedTreeSizeMeasure(generator.constraints()));
            minimal.setOntology(ontology);
            IProof<OWLAxiom> derivations = generator.getProof(axiom);
            IProof<OWLAxiom> proof = minimal.getProof(axiom);
            assertFalse(proof.getInferences().isEmpty());
            assertTrue(derivations.getInferences().containsAll(proof.getInferences()));
            generator.dispose();
            assertTrue("the reasoner should still be running", reasoner.isEntailed(axiom));
        } finally {
            reasoner.dispose();
        }
    }
}
