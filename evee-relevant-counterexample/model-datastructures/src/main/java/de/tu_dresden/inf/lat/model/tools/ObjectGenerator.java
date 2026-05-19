package de.tu_dresden.inf.lat.model.tools;

import de.tu_dresden.inf.lat.model.data.Element;
import org.semanticweb.owlapi.model.OWLClass;

/**
 * @author Christian Alrabbaa
 *
 */
public class ObjectGenerator {

	private static char elementPrefix = 'a';
	private static char conceptNamePrefix = 'A';
	private static int elementIndex, conceptNameIndex;

	private ObjectGenerator() {
		elementIndex = 0;
		conceptNameIndex = 0;
	}

	private static class LazyHolder {
		static ObjectGenerator instance = new ObjectGenerator();
	}

	public static ObjectGenerator getInstance() {
		return LazyHolder.instance;
	}

	public Element getNextElement() {
		if (elementIndex == 2000000) {
			elementIndex = 0;
			elementPrefix = getPrefix(elementPrefix);
		}
		return new Element(String.valueOf(elementPrefix) + elementIndex++);
	}

	public OWLClass getNextConceptName() {
		if (conceptNameIndex == 2000000) {
			conceptNameIndex = 0;
			conceptNamePrefix = getPrefix(conceptNamePrefix);
		}

		return ToOWLTools.getInstance().getOWLConceptName(String.valueOf(conceptNamePrefix) + conceptNameIndex++);
	}

	private static char getPrefix(char currentPrefix) {
		int next = (int) currentPrefix;

		if (next == 90)
			next = 65;

		else if (next == 122)
			next = 97;

		else
			next++;

		return (char) next;
	}
}
