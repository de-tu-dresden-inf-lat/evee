package de.tu_dresden.inf.lat.model.json;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.tu_dresden.inf.lat.model.interfaces.IModel;

/**
 * @author Christian Alrabbaa
 *
 */
public class JsonModelParser {

	public JsonModelParser() {
	}

	public IModel elkModelFromFile(File file) {
		ObjectMapper mapper = new ObjectMapper();

		try {
			return mapper.readValue(file, IModel.class);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

}
