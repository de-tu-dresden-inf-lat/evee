package de.tu_dresden.inf.lat.evee.concreteDomains.data.names;
/**
 * @author Christian Alrabbaa
 *
 */
public enum ReasonerName {
	Hermit, Lethe, Elk;
	public static ReasonerName getReasonerName(String str) {
		ReasonerName result = parseArg(str);
		if (result != null)
			return result;

		throw new IllegalArgumentException("No ReasonerName value for \"" + str + "\"");
	}

	public static boolean isName(String str) {
		return parseArg(str) != null;
	}

	private static ReasonerName parseArg(String str) {
		if (str.equalsIgnoreCase("elk"))
			return ReasonerName.Elk;

		if (str.equalsIgnoreCase("lethe"))
			return ReasonerName.Lethe;

		if (str.equalsIgnoreCase("hermit"))
			return ReasonerName.Hermit;

		return null;
	}

}
