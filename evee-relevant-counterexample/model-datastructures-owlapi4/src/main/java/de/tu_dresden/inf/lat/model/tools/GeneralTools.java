package de.tu_dresden.inf.lat.model.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;

import org.apache.log4j.Logger;

/**
 * @author Christian Alrabbaa
 *
 */
public class GeneralTools {

	private static final Logger logger = Logger.getLogger(GeneralTools.class);

	/**
	 * @param str
	 * @param outStream
	 */
	public static void writeTo(String str, FileOutputStream outStream) {

		try {
			outStream.write(str.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param collection
	 * @param outStream
	 */
	public static void writeCollectionTo(Collection<String> collection, FileOutputStream outStream) {

		collection.forEach(str -> {
			writeTo(str, outStream);
		});
	}

	/**
	 * @param process
	 * @throws IOException
	 */
	public static void printCommandOutput(Process process) throws IOException {

		String line;
		BufferedReader brCleanUp = new BufferedReader(new InputStreamReader(process.getInputStream()));
		while ((line = brCleanUp.readLine()) != null) {
			logger.debug(line);
		}
		brCleanUp.close();

		brCleanUp = new BufferedReader(new InputStreamReader(process.getErrorStream()));
		while ((line = brCleanUp.readLine()) != null) {
			if (process.exitValue() != 0)
				logger.info("ERR***" + line);
		}
		brCleanUp.close();

	}

	/**
	 * @param filePath
	 * @return
	 * @throws IOException
	 */
	public static File createFile(String filePath) throws IOException {

		File newFile = new File(filePath);

		if (newFile.exists())
			newFile.delete();

		newFile.createNewFile();

		return newFile;
	}

	/**
	 * @param start
	 * @param finish
	 * @return
	 */
	public static String getDuration(Instant start, Instant finish) {

		return "Elapsed Time = " + Duration.between(start, finish).toString();
	}
}
