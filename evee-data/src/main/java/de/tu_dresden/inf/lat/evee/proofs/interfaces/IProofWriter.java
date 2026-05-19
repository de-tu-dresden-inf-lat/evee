package de.tu_dresden.inf.lat.evee.proofs.interfaces;

import java.io.IOException;
import java.util.Collection;

import de.tu_dresden.inf.lat.evee.general.data.exceptions.FormattingException;

/**
 * Interface for writing proofs into files and strings.
 */
public interface IProofWriter<SENTENCE> {

	/**
	 * writes the proof into a file with the given prefix. The ending of the file is
	 * chosen by the implementation, as each implementation might generate files
	 * from a different type (.json, .txt, .graphml, xml, etc.)
	 *
	 */
	void writeToFile(IProof<SENTENCE> proof, String prefix) throws IOException, FormattingException;

	void writeToFile(Collection<IProof<SENTENCE>> proofs, String prefix) throws IOException, FormattingException;

	String toString(IProof<SENTENCE> proof) throws FormattingException;
}
