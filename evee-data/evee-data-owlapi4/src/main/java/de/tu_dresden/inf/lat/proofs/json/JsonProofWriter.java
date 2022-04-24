package de.tu_dresden.inf.lat.proofs.json;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import de.tu_dresden.inf.lat.proofs.data.exceptions.FormattingException;
import de.tu_dresden.inf.lat.proofs.interfaces.IProof;
import de.tu_dresden.inf.lat.proofs.interfaces.IProofWriter;

/**
 * Write proof objects into JSon files.
 *
 * @author Christian Alrabbaa
 *
 */
public class JsonProofWriter<SENTENCE> implements IProofWriter<SENTENCE> {

	public final static String FILE_ENDING = ".json";

	public JsonProofWriter() {
	}

//	private static class LazyHolder<SENTENCE> {
//		static JsonProofWriter instance = new JsonProofWriter<>();
//	}

	public static <SENTENCE> JsonProofWriter<SENTENCE> getInstance() {
//		return LazyHolder.instance;
		return new JsonProofWriter<>();
	}

	@Override
	public void writeToFile(Collection<IProof<SENTENCE>> proofs, String filePrefix) throws IOException {

		ObjectMapper mapper = new ObjectMapper();
		ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());

		writer.writeValue(new File(filePrefix + FILE_ENDING), proofs);

	}

	@Override
	public void writeToFile(IProof<SENTENCE> proof, String filePrefix) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());

		writer.writeValue(new File(filePrefix + FILE_ENDING), proof);
	}

	@Override
	public String toString(IProof<SENTENCE> proof) throws FormattingException {
		ObjectMapper mapper = new ObjectMapper();
		ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());

		ByteArrayOutputStream output = new ByteArrayOutputStream();

		try {
			writer.writeValue(output, proof);
		} catch (IOException e) {
			throw new FormattingException(e);
		}

		return output.toString();
	}
}
