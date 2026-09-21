package de.tu_dresden.inf.lat.evee.concreteDomains.data.names;

import de.tu_dresden.inf.lat.evee.concreteDomains.data.exceptions.ParsingException;

/**
 * @author Christian Alrabbaa
 *s
 */
public enum ConcreteDomainName {
	//LinearConstraints, QGreater;
	QDiff, QMult, QLinear;
	public static ConcreteDomainName getConcreteDomainName(String str)  throws ParsingException {

		ConcreteDomainName result = parseArg(str);
		if (result != null)
			return result;

		throw new ParsingException("cannot parse domain name: \"" + str + "\"");
	}

	public static boolean isName(String str) {
		return parseArg(str) != null;
	}

	private static ConcreteDomainName parseArg(String str) {
		if (str.equalsIgnoreCase("qDiff"))
			return ConcreteDomainName.QDiff;

		if (str.equalsIgnoreCase("qMult"))
			return ConcreteDomainName.QMult;

		if (str.equalsIgnoreCase("qLinear"))
			return ConcreteDomainName.QLinear;

		return null;
	}

}
