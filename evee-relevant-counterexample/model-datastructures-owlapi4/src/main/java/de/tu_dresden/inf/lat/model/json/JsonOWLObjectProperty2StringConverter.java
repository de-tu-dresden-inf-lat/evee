package de.tu_dresden.inf.lat.model.json;

import org.semanticweb.owlapi.model.OWLObjectProperty;

import com.fasterxml.jackson.databind.util.StdConverter;

/**
 * @author Christian Alrabbaa
 *
 */
public class JsonOWLObjectProperty2StringConverter extends StdConverter<OWLObjectProperty, String> {

	@Override
	public String convert(OWLObjectProperty value) {
		return value.toString();
	}

}
