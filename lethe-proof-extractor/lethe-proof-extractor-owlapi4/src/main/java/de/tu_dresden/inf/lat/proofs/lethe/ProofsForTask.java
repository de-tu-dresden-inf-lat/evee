package de.tu_dresden.inf.lat.proofs.lethe;

import de.tu_dresden.inf.lat.proofs.data.exceptions.ProofException;
import de.tu_dresden.inf.lat.proofs.data.exceptions.ProofGenerationException;
import de.tu_dresden.inf.lat.proofs.interfaces.IProof;
import de.tu_dresden.inf.lat.proofs.interfaces.IProofEvaluator;
import de.tu_dresden.inf.lat.proofs.interfaces.IProofGenerator;
import de.tu_dresden.inf.lat.proofs.json.JsonProofParser;
import de.tu_dresden.inf.lat.proofs.proofGenerators.MinimalTreeProofGenerator;
import de.tu_dresden.inf.lat.proofs.tools.RecursiveProofEvaluator;
import de.tu_dresden.inf.lat.proofs.tools.measures.OWLAxiomSizeWeightedTreeSizeMeasure;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class ProofsForTask {

    public static void main(String[] args) throws OWLOntologyCreationException, ProofGenerationException, ProofException {
            String filename = args[0];

            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

            IProof<OWLAxiom> task = new JsonProofParser().fromFile(new File(filename));

            OWLOntology ontology = manager.createOntology();
            Set<OWLAxiom> axioms = new HashSet(task.getInferences().get(0).getPremises());
            manager.addAxioms(ontology, axioms);

            IProofEvaluator<OWLAxiom> evaluator = new RecursiveProofEvaluator<OWLAxiom>(new OWLAxiomSizeWeightedTreeSizeMeasure());

            IProofGenerator<OWLAxiom, OWLOntology> proofGen =
                    new MinimalTreeProofGenerator<>(new LetheProofGenerator());
            proofGen.setOntology(ontology);

            System.out.println("generating proof...");
            IProof<OWLAxiom> proof = proofGen.getProof(task.getFinalConclusion());
            System.out.println("value: "+evaluator.evaluate(proof));

            System.out.println("Proof:");
            System.out.println("======");
            System.out.println(proof);


        }

}
