package de.tu_dresden.inf.lat.model.json;

import com.fasterxml.jackson.databind.util.StdConverter;

import de.tu_dresden.inf.lat.model.data.Element;

/**
 * @author Christian Alrabbaa
 *
 */
public class JsonString2ElementConverter extends StdConverter<String, Element> {

	@Override
	public Element convert(String value) {
		return new Element(value);
	}

}
