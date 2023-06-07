package de.tu_dresden.lat.model.json;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import de.tu_dresden.inf.lat.model.data.Mapper;

/**
 * Write the map of representatives2concepts to a JSON file
 * 
 * @author Christian Alrabbaa
 *
 */
public class JsonMapperWriter {

	public final static String FILE_ENDING = ".json";

	public void writeToFile(Mapper modelMapper, String fileName) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());

		writer.writeValue(new File(fileName + FILE_ENDING), modelMapper);
	}

}
