package de.tu_dresden.inf.lat.evee.concreteDomains.data.names;

import de.tu_dresden.inf.lat.evee.concreteDomains.data.exceptions.ParsingException;

/**
 * @author Christian Alrabbaa
 *s
 */
public enum ConcreteDomainName {
	//LinearConstraints, QGreater;
	QDiff, QMult, QLinear;

	/** the names of the concrete domains, for messages */
	public static final String NAMES = "QDiff, QMult or QLinear";

	/** the IRIs of the concrete domains, for messages */
	public static final String IRIS = "cd:QDiff, cd:QMult or cd:QLinear";

	public static ConcreteDomainName getConcreteDomainName(String str)  throws ParsingException {

		ConcreteDomainName result = parseArg(str);
		if (result != null)
			return result;

		throw new ParsingException("The concrete domain \"" + str + "\" is unknown; use " + NAMES + ", or one of the IRIs " + IRIS);
	}

	public static boolean isName(String str) {
		return parseArg(str) != null;
	}

	/** the IRI of the concrete domain, e.g. cd:QDiff, which can name it instead of the string */
	public String getIRI() {
		return CDAnnotationNames.NAMESPACE + name();
	}

	/** the concrete domain with this IRI, or null if there is none */
	public static ConcreteDomainName ofIRI(String iri) {
		for (ConcreteDomainName domain : values())
			if (domain.getIRI().equals(iri))
				return domain;
		return null;
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
