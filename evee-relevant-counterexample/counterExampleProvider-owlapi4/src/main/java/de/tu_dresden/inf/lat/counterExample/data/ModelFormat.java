package de.tu_dresden.inf.lat.counterExample.data;

/**
 * @author Christian Alrabbaa
 *
 */
public enum ModelFormat {
	Sets, Individuals;

	/**
	 * Return a ModelFormat that corresponds to the input String
	 * 
	 * @param str
	 * @return
	 */
	public static ModelFormat getTypeValue(String str) {
		if (str.equalsIgnoreCase("sets"))
			return ModelFormat.Sets;

		if (str.equalsIgnoreCase("individuals"))
			return ModelFormat.Individuals;

		throw new IllegalArgumentException("No ModelFormat value for \"" + str + "\"");
	}
}
