package de.tu_dresden.inf.lat.evee.concreteDomains.data.names;
/**
 * @author Christian Alrabbaa
 *
 */
public enum ConcreteDomainName {
	LinearConstraints, QGreater;
	public static ConcreteDomainName getConcreteDomainName(String str) {
		ConcreteDomainName result = parseArg(str);
		if (result != null)
			return result;

		throw new IllegalArgumentException("No getConcreteDomainName value for \"" + str + "\"");
	}

	public static boolean isName(String str) {
		return parseArg(str) != null;
	}

	private static ConcreteDomainName parseArg(String str) {
		if (str.equalsIgnoreCase("linearconstraints"))
			return ConcreteDomainName.LinearConstraints;

		if (str.equalsIgnoreCase("qgreater"))
			return ConcreteDomainName.QGreater;

		return null;
	}

}
