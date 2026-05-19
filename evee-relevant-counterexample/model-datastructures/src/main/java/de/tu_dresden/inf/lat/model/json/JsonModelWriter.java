package de.tu_dresden.inf.lat.model.json;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

/**
 * Write model objects to JSON files.
 *
 * @author Christian Alrabbaa
 *
 */
public class JsonModelWriter<MODEL_ELEMENT_TYPE> {

	public final static String FILE_ENDING = ".json";

	public JsonModelWriter() {
	}

	public void writeToFile(Collection<MODEL_ELEMENT_TYPE> modelElements, String fileName) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());

		writer.writeValue(new File(fileName + FILE_ENDING), modelElements);
	}
}
