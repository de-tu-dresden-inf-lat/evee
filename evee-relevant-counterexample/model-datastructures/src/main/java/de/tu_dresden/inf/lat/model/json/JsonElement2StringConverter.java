package de.tu_dresden.inf.lat.model.json;

import com.fasterxml.jackson.databind.util.StdConverter;

import de.tu_dresden.inf.lat.model.data.Element;

/**
 * @author Christian Alrabbaa
 *
 */
public class JsonElement2StringConverter extends StdConverter<Element, String> {

	@Override
	public String convert(Element value) {
		return value.getName();
	}

}
