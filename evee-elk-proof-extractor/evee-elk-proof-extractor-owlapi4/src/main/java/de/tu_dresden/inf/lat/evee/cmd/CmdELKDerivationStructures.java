package de.tu_dresden.inf.lat.evee.cmd;

import de.tu_dresden.inf.lat.evee.general.data.exceptions.FormattingException;
import de.tu_dresden.inf.lat.evee.proofGenerators.ELKProofGenerator;
import de.tu_dresden.inf.lat.evee.proofs.data.exceptions.ProofGenerationException;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProof;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProofGenerator;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProofWriter;
import de.tu_dresden.inf.lat.evee.proofs.json.JsonProofWriter;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.io.File;
import java.io.IOException;

/**
 * Create a derivation structure from command line
 */
public class CmdELKDerivationStructures {
    public static void main(String[] args) throws OWLOntologyCreationException, ProofGenerationException, FormattingException, IOException {

        if(args.length<3){
            System.out.println("Usage:");
            System.out.println(CmdELKDerivationStructures.class.toString()+" OWL_FILE LHS RHS");
            System.out.println("  where LHS and RHS are full IRIs of classes.");
            System.out.println("  creates a file \"deriviation-structure.json\" containing the deriviation structure for that input");
            System.exit(0);
        }

        System.out.print("Loading ontology...");
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        OWLOntology ont = man.loadOntologyFromOntologyDocument(new File(args[0]));
        System.out.println("done.");

        OWLDataFactory factory = man.getOWLDataFactory();
        OWLClass lhs = factory.getOWLClass(IRI.create(args[1]));
        OWLClass rhs = factory.getOWLClass(IRI.create(args[2]));
        OWLAxiom axiom = factory.getOWLSubClassOfAxiom(lhs,rhs);
        System.out.println("Axiom to prove: "+axiom);

        System.out.print("Generating proof...");
        IProofGenerator<OWLAxiom, OWLOntology> generator = new ELKProofGenerator(ont);
        IProof<OWLAxiom> proof = generator.getProof(axiom);
        System.out.println();

        IProofWriter<OWLAxiom> proofWriter = new JsonProofWriter<>();
        proofWriter.writeToFile(proof, "derivation-structure");
    }
}
