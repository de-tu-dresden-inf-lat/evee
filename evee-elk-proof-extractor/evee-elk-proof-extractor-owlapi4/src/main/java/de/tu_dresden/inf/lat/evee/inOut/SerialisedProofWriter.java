package de.tu_dresden.inf.lat.evee.inOut;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.semanticweb.owlapi.model.OWLAxiom;

import de.tu_dresden.inf.lat.prettyPrinting.formatting.SimpleOWLFormatter;
import de.tu_dresden.inf.lat.evee.proofs.data.Inference;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProof;
import de.tu_dresden.inf.lat.evee.tools.GeneralTools;

/**
 * Write proofs in serialised fashion
 * 
 * @author Christian Alrabbaa
 *
 */
public class SerialisedProofWriter {

	public SerialisedProofWriter() {
	}

	private static class LazyHolder {
		static SerialisedProofWriter instance = new SerialisedProofWriter();
	}

	public static SerialisedProofWriter getInstance() {
		return LazyHolder.instance;
	}

	/**
	 * Save the provided list of proofs in one text file
	 * 
	 * @param proofs
	 * @param fullPath
	 * @throws IOException
	 */
	public void writeSerializedProofs(OWLAxiom conclusion, Collection<IProof<OWLAxiom>> proofs, File file)
			throws IOException {

		List<IProof<OWLAxiom>> proofsList = new LinkedList<IProof<OWLAxiom>>(proofs);

		FileOutputStream outStream = new FileOutputStream(file);

		GeneralTools.writeTo("PROOF(s) OF:\t" + SimpleOWLFormatter.format(conclusion) + "\n\n", outStream);
		for (int i = 0; i < proofs.size(); i++) {
			GeneralTools.writeTo("PROOF NUMBER " + (i + 1) + ":\n\n", outStream);
			writeSerializedProofs(proofsList.get(i), outStream);
			GeneralTools.writeTo(
					"===========================================\n===========================================\n\n",
					outStream);
		}

		outStream.close();
	}

	/**
	 * Write the provided proof in human-readable-DL format
	 * 
	 * @param proof
	 * @param outStream
	 * @throws IOException
	 */
	private void writeSerializedProofs(IProof<OWLAxiom> proof, FileOutputStream outStream) throws IOException {

		for (int i = 0; i < proof.getInferences().size(); i++) {
			Inference<OWLAxiom> inf = (Inference<OWLAxiom>) proof.getInferences().get(i);
			GeneralTools.writeTo("\t" + SimpleOWLFormatter.format(inf.getConclusion()).replace("\n", ",\n\t"),
					outStream);
			if (!inf.getPremises().isEmpty())
				GeneralTools.writeTo("\n\twas inferred using\n", outStream);
			inf.getPremises().forEach(p -> {
				String axiomStr = SimpleOWLFormatter.format(p);
				if (axiomStr.contains("\n"))
					axiomStr = axiomStr.replace("\n", ",\n\t");
				else
					axiomStr += ", ";
				GeneralTools.writeTo("\t" + axiomStr, outStream);
			});

			String ruleLine = "\n\tby applying: ";
			if (inf.getPremises().isEmpty())
				ruleLine = "\n\t";

			GeneralTools.writeTo(ruleLine + inf.getRuleName(), outStream);

			String lineSep = "\n\t-----\n";
			if (i == proof.getInferences().size() - 1)
				lineSep = "\n\n";

			GeneralTools.writeTo(lineSep, outStream);
		}

	}
}
