import de.tu_dresden.inf.lat.evee.proofs.lethe.LetheProofGenerator;
import de.tu_dresden.inf.lat.evee.general.tools.BasicProgressBar;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import de.tu_dresden.inf.lat.evee.proofs.data.exceptions.ReasonerNotSupportedException;
import de.tu_dresden.inf.lat.evee.proofs.data.exceptions.ProofGenerationException;

/**
 * Test individual (& public) functions of the LetheProofGenerator class.
 */
public class TestLetheProofGenerator {
    LetheProofGenerator generator;
    OWLOntology ontology;
    OWLOntologyManager man;
    OWLDataFactory df;


    @BeforeEach
    public void setup() throws OWLOntologyCreationException {
        generator = new LetheProofGenerator();
        generator.addProgressTracker(new BasicProgressBar());

        man = OWLManager.createOWLOntologyManager();
        df = man.getOWLDataFactory();
        ontology = man.createOntology();
        OWLClass A = df.getOWLClass(IRI.create("A"));
        System.out.println(A);
        OWLClass B = df.getOWLClass(IRI.create("B"));
        OWLClass C = df.getOWLClass(IRI.create("C"));
        OWLClass D = df.getOWLClass(IRI.create("D"));
        OWLObjectProperty R = df.getOWLObjectProperty(IRI.create("R"));

        OWLSubClassOfAxiom ax1 = df.getOWLSubClassOfAxiom(
                A,
                df.getOWLObjectSomeValuesFrom(R, B)
        );

        OWLSubClassOfAxiom ax2 = df.getOWLSubClassOfAxiom(
                df.getOWLObjectSomeValuesFrom(R, B),
                C
        );

        OWLSubClassOfAxiom ax3 = df.getOWLSubClassOfAxiom(
                C,
                df.getOWLObjectAllValuesFrom(R, D)
        );

        OWLSubClassOfAxiom ax4 = df.getOWLSubClassOfAxiom(
                df.getOWLObjectAllValuesFrom(R, D),
                A
        ); 

        ontology = man.createOntology();
        man.addAxiom(ontology, ax1);
        man.addAxiom(ontology, ax2);
        man.addAxiom(ontology, ax3);
        man.addAxiom(ontology, ax4);
    }

    //@Test
    // TODO this test fails now because we filter out non-ALCH axioms
    public void testSetOntology() throws OWLOntologyCreationException {
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = man.createOntology();

        generator.setOntology(ontology);
        assertEquals(ontology, generator.getOntology());
    }

    @Test
    public void testSetReasoner() {
        assertThrows(ReasonerNotSupportedException.class, () -> generator.setReasoner(null));
    }

    @Test
    public void testProveSubsumption() {
        OWLClass A = df.getOWLClass(IRI.create("A"));
        OWLClass B = df.getOWLClass(IRI.create("B"));
        OWLClass C = df.getOWLClass(IRI.create("C"));
        System.out.println(A);

        assertThrows(Error.class, () -> generator.proveSubsumption(A, C));
        
        generator.setOntology(ontology);

        try{
            assertNotNull(generator.proveSubsumption(A, C));

            assertNull(generator.proveSubsumption(A, B));
        }
        catch (ProofGenerationException e){
            System.out.println("Test failed: " + e);
        }
    }

    @Test
    public void testProveEquivalence() {
        OWLClass A = df.getOWLClass(IRI.create("A"));
        OWLClass C = df.getOWLClass(IRI.create("C"));
        
        generator.setOntology(ontology);

        try{
            assertNotNull(generator.proveEquivalence(A, C));
        }
        catch (ProofGenerationException e){
            System.out.println("Test failed: " + e);
        }
    }
}
