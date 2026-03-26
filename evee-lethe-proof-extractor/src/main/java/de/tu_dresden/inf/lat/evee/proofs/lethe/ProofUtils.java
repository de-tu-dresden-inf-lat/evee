package de.tu_dresden.inf.lat.evee.proofs.lethe;

import java.io.IOException;

import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInference;
import org.semanticweb.owlapi.model.OWLAxiom;

import de.tu_dresden.inf.lat.prettyPrinting.formatting.SimpleOWLFormatter;
import de.tu_dresden.inf.lat.evee.general.data.exceptions.FormattingException;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProof;
import de.tu_dresden.inf.lat.evee.proofs.json.JsonProofWriter;
import de.tu_dresden.inf.lat.evee.proofs.tools.MinimalHypergraphProofExtractor;

public class ProofUtils {
    public static void showProof(IProof<OWLAxiom> proof) throws FormattingException, IOException {
        showProof(proof, false);
    }

    public static void showProof(IProof<OWLAxiom> proof, boolean outputToFile) throws FormattingException, IOException {
        if (proof == null) {
            System.out.println("No proof...");
            return;
        }



        JsonProofWriter<OWLAxiom> proofWriter = JsonProofWriter.getInstance();
        proofWriter.writeToFile(proof, "unoptimised proof");

        System.out.println("\nShowing proof...\n============================================================");
        System.out.println(proof);
        System.out.println("============================================================");
        proof = MinimalHypergraphProofExtractor.makeUnique(proof);

        System.out.println("\nShowing minimized proof...\n============================================================");

        System.out.println("Final conclusion: "+ SimpleOWLFormatter.format(proof.getFinalConclusion()));
        //JsonProofWriter<OWLAxiom> proofWriter = JsonProofWriter.getInstance();
        //String jsonString = proofWriter.toString(proof);
        //System.out.println(jsonString);
        System.out.println(proof);

        System.out.println("============================================================");
    }

    public static void checkProof(IProof<OWLAxiom> proof) {
        assert proof.hasInferenceFor(proof.getFinalConclusion()) : "final conclusion not proven!";
        for(IInference<OWLAxiom> inference: proof.getInferences()){
            for(OWLAxiom premise: inference.getPremises()){
                assert proof.hasInferenceFor(premise) : "axiom without inference used: "+ SimpleOWLFormatter.format(premise);
            }
        }
    }

}
