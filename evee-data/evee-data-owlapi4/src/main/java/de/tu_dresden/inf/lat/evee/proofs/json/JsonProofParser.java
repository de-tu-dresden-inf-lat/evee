package de.tu_dresden.inf.lat.evee.proofs.json;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.tu_dresden.inf.lat.evee.proofs.data.exceptions.ParsingException;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProof;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProofParser;
import org.semanticweb.owlapi.model.OWLAxiom;

public class JsonProofParser implements IProofParser {

	private static class LazyHolder {
		static JsonProofParser instance = new JsonProofParser();
	}

	public static JsonProofParser getInstance() {
		return LazyHolder.instance;
	}

	@Override
	public IProof<OWLAxiom> fromFile(File file) {
		ObjectMapper mapper = new ObjectMapper();

		JavaType axiomProofType = mapper.getTypeFactory().constructParametricType(IProof.class, OWLAxiom.class);

		try {
			return mapper.readValue(file, axiomProofType);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public IProof<OWLAxiom> parseProof(String string) throws ParsingException {

		ObjectMapper mapper = new ObjectMapper();

		JavaType axiomProofType = mapper.getTypeFactory().constructParametricType(IProof.class, OWLAxiom.class);

		try {
			return mapper.readValue(string, axiomProofType);
		} catch (IOException e) {
			throw new ParsingException(e);
		}
	}

}
