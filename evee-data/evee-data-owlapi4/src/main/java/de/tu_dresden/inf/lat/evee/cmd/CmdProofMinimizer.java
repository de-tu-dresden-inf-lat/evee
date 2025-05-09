package de.tu_dresden.inf.lat.evee.cmd;

import de.tu_dresden.inf.lat.evee.general.data.exceptions.FormattingException;
import de.tu_dresden.inf.lat.evee.proofs.data.exceptions.ProofGenerationFailedException;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProof;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProofParser;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProofWriter;
import de.tu_dresden.inf.lat.evee.proofs.json.JsonProofWriter;
import de.tu_dresden.inf.lat.evee.proofs.json.JsonStringProofParser;
import de.tu_dresden.inf.lat.evee.proofs.tools.MinimalProofExtractor;
import de.tu_dresden.inf.lat.evee.proofs.tools.measures.TreeSizeMeasure;

import java.io.File;
import java.io.IOException;

public class CmdProofMinimizer {
    public static void main(String[] args) throws ProofGenerationFailedException, FormattingException, IOException {
        if(args.length!=1){
            System.out.println("Usage:");
            System.out.println(CmdProofMinimizer.class.toString()+" PROOF-FILE");
            System.out.println("  minimizes the provided proof and stores it in \"minimized-proof.json\"");
            System.exit(0);
        }

        IProofParser<String> parser = new JsonStringProofParser();
        IProof<String> derivationStructure = parser.fromFile(new File(args[0]));

        MinimalProofExtractor<String> extractor = new MinimalProofExtractor<>(new TreeSizeMeasure<>());
        IProof<String> minimized = extractor.extract(derivationStructure);

        IProofWriter<String> writer = new JsonProofWriter<>();
        writer.writeToFile(minimized, "minimized");

    }
}
