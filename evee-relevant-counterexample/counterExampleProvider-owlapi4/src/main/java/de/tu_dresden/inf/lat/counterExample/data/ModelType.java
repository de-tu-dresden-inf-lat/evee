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
		if (str.toLowerCase().equals("alpha"))
			return ModelType.Alpha;

		if (str.toLowerCase().equals("beta"))
			return ModelType.Beta;

		if (str.toLowerCase().equals("diff"))
			return ModelType.Diff;

		if (str.toLowerCase().equals("flatdiff"))
			return ModelType.FlatDiff;

		if (str.toLowerCase().equals("fullcanonical"))
			return ModelType.FullCanonical;

		throw new IllegalArgumentException("No ModelType value for \"" + str + "\"");
	}

}
