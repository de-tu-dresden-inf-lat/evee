package de.tu_dresden.inf.lat.model.json;

import org.semanticweb.owlapi.model.OWLObjectProperty;

import com.fasterxml.jackson.databind.util.StdConverter;

import de.tu_dresden.inf.lat.model.tools.ToOWLTools;

public class JsonString2OWLObjectPropertyConverter extends StdConverter<String, OWLObjectProperty> {

	@Override
	public OWLObjectProperty convert(String value) {
		return ToOWLTools.getInstance().getPropertyName(value);
	}

}
