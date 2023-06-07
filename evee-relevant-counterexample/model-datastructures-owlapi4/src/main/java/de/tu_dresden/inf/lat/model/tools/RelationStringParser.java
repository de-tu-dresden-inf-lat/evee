package de.tu_dresden.inf.lat.model.tools;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.tu_dresden.inf.lat.model.data.Element;
import de.tu_dresden.inf.lat.model.data.Relation;
import de.tu_dresden.inf.lat.model.data.RelationDirection;
import org.semanticweb.owlapi.model.OWLObjectProperty;

import de.tu_dresden.inf.lat.exceptions.ParsingException;

/**
 * @author Christian Alrabbaa
 *
 */
public class RelationStringParser {

	private static final String roleNameRegexStr = "(?!-)[a-zA-Z0-9-]+(?<!-)";

	private static final String isBothRegexStr = "<-" + roleNameRegexStr + "->";
	private static final String isPredecessorRegexStr = "-(?<!<-)" + roleNameRegexStr + "->";
	private static final String isSuccessorRegexStr = "<-" + roleNameRegexStr + "(?!->)-";

	private static final Pattern isPredecessorRegex = Pattern.compile(isPredecessorRegexStr);
	private static final Pattern isSuccessorRegex = Pattern.compile(isSuccessorRegexStr);
	private static final Pattern isBothRegex = Pattern.compile(isBothRegexStr);

	private static final Pattern rolenameRegex = Pattern.compile(roleNameRegexStr);

	private static final List<Pattern> regexs = Arrays.asList(isBothRegex, isSuccessorRegex, isPredecessorRegex);
	private static final List<RelationDirection> directions = Arrays.asList(RelationDirection.Bidirectional,
			RelationDirection.Backward, RelationDirection.Forward);

	public static Relation parse(String str) throws ParsingException {
		return parseRelationString(str.trim());
	}

	private static Relation parseRelationString(String str) throws ParsingException {

		OWLObjectProperty role = null;
		Element element1, element2;
		String[] elementsStr;

		for (int i = 0; i < regexs.size(); i++) {
			Matcher matcher = regexs.get(i).matcher(str);

			if (matcher.find()) {
				elementsStr = matcher.replaceFirst(" ").trim().replaceAll(" +", " ").split(" ");
				assert elementsStr.length == 2 : "more than 2 elements found!" + Arrays.asList(elementsStr)
						+ "\nA relation must have exactly 2 elements";

				element1 = new Element(elementsStr[0]);
				element2 = new Element(elementsStr[1]);

				matcher = rolenameRegex.matcher(matcher.group());
				matcher.find();
				role = ToOWLTools.getInstance().getPropertyName(matcher.group());

				return new Relation(role, element1, element2, directions.get(i));
			}
		}

		throw new ParsingException("The input string is not a valid relation string");
	}
}