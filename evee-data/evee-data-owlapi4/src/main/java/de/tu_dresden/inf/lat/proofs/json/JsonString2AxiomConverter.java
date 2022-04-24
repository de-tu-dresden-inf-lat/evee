package de.tu_dresden.inf.lat.proofs.json;

import org.semanticweb.owlapi.model.OWLAxiom;

import com.fasterxml.jackson.databind.util.StdConverter;

import de.tu_dresden.inf.lat.prettyPrinting.parsing.OWLParser;

public class JsonString2AxiomConverter extends StdConverter<String, OWLAxiom> {

	private final OWLParser parser = new OWLParser();

	@Override
	public OWLAxiom convert(String s) {
		return parser.parse(s);
	}
}
