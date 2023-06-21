package de.tu_dresden.inf.lat.counterExample.data;

/**
 * @author Christian Alrabbaa
 *
 */
public enum ModelType {

	Alpha, Beta, Diff, FlatDiff, FullCanonical;

	/**
	 * Return a ModelType that corresponds to the input String
	 * 
	 * @param str
	 * @return
	 */
	public static ModelType getTypeValue(String str) {
		if (str.equalsIgnoreCase("alpha"))
			return ModelType.Alpha;

		if (str.equalsIgnoreCase("beta"))
			return ModelType.Beta;

		if (str.equalsIgnoreCase("diff"))
			return ModelType.Diff;

		if (str.equalsIgnoreCase("flatdiff"))
			return ModelType.FlatDiff;

		if (str.equalsIgnoreCase("fullcanonical"))
			return ModelType.FullCanonical;

		throw new IllegalArgumentException("No ModelType value for \"" + str + "\"");
	}

}
