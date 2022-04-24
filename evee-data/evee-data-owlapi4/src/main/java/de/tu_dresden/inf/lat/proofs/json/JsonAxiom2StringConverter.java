package de.tu_dresden.inf.lat.proofs.json;

import org.semanticweb.owlapi.model.OWLAxiom;

import com.fasterxml.jackson.databind.util.StdConverter;

import de.tu_dresden.inf.lat.prettyPrinting.formatting.Formatter;
import de.tu_dresden.inf.lat.prettyPrinting.formatting.ParsableOWLFormatter;

public class JsonAxiom2StringConverter extends StdConverter<OWLAxiom, String> {

	private final Formatter<OWLAxiom> formatter = new ParsableOWLFormatter();

	@Override
	public String convert(OWLAxiom owlAxiom) {
		String ret = formatter.format(owlAxiom);
		if (ret.isEmpty()) {
			throw new IllegalStateException("Could not format axiom: " + owlAxiom);
		}
		return ret;
	}

}
