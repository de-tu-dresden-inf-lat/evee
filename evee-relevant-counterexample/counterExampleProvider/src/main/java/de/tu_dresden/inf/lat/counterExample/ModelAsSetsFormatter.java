package de.tu_dresden.inf.lat.counterExample;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObjectProperty;

import de.tu_dresden.inf.lat.model.data.Element;
import de.tu_dresden.inf.lat.model.data.Relation;
import de.tu_dresden.inf.lat.model.tools.GeneralTools;
import de.tu_dresden.inf.lat.model.tools.ToOWLTools;

/**
 * @author Christian Alrabbaa
 *
 */
public class ModelAsSetsFormatter {

	private static final Logger logger = Logger.getLogger(ModelAsSetsFormatter.class);
	private static final ToOWLTools owlTools = ToOWLTools.getInstance();

	private static Set<String> addedRelations;

	/**
	 * Print out the provided model as a set of sets
	 * 
	 * @param model
	 * @throws FileNotFoundException
	 */
	public static void writeAsSets(Set<Element> model, File outpuFile) throws FileNotFoundException {

		addedRelations = new HashSet<>();

		StringBuffer domain = new StringBuffer();
		Map<OWLClass, StringBuffer> concepts = new HashMap<>();
		Map<OWLObjectProperty, StringBuffer> roles = new HashMap<>();

		model.forEach(element -> {
			append(domain, element.getName());
			fillConcepts(concepts, element);
			fillRoles(roles, element);
		});

		FileOutputStream out = new FileOutputStream(outpuFile);

		writeModel(domain, concepts, roles, out);
	}

	/**
	 * Print out the provided model as a set of sets
	 * 
	 * @param model
	 */
	public static void printAsSets(Set<Element> model) {

		addedRelations = new HashSet<>();

		StringBuffer domain = new StringBuffer();
		Map<OWLClass, StringBuffer> concepts = new HashMap<>();
		Map<OWLObjectProperty, StringBuffer> roles = new HashMap<>();

		model.forEach(element -> {
			append(domain, element.getName());
			fillConcepts(concepts, element);
			fillRoles(roles, element);
		});

		printModel(domain, concepts, roles);

	}

	/**
	 * write the formated strings to the output text file
	 * 
	 * @param domain
	 * @param concepts
	 * @param roles
	 */
	private static void writeModel(StringBuffer domain, Map<OWLClass, StringBuffer> concepts,
			Map<OWLObjectProperty, StringBuffer> roles, FileOutputStream out) {

		GeneralTools.writeTo("\u0394\u1D35 = \u22A4\u1D35 = { "
				+ domain.toString().substring(0, domain.toString().length() - 2) + " }\n", out);
		concepts.keySet().forEach(key -> {
			GeneralTools.writeTo(owlTools.getShortIRIString(key.toString()) + "\u1D35 = { "
					+ concepts.get(key).toString().substring(0, concepts.get(key).toString().length() - 2) + " }\n",
					out);
		});

		roles.keySet().forEach(key -> {
			GeneralTools.writeTo(
					owlTools.getShortIRIString(key.toString()) + "\u1D35 = { "
							+ roles.get(key).toString().substring(0, roles.get(key).toString().length() - 2) + " }\n",
					out);
		});

	}

	/**
	 * Print out the formated strings
	 * 
	 * @param domain
	 * @param concepts
	 * @param roles
	 */
	private static void printModel(StringBuffer domain, Map<OWLClass, StringBuffer> concepts,
			Map<OWLObjectProperty, StringBuffer> roles) {

		print("\u0394\u1D35 = \u22A4\u1D35 = { " + domain.toString().substring(0, domain.toString().length() - 2)
				+ " }");
		concepts.keySet().forEach(key -> {
			print(owlTools.getShortIRIString(key.toString()) + "\u1D35 = { "
					+ concepts.get(key).toString().substring(0, concepts.get(key).toString().length() - 2) + " }");
		});

		roles.keySet().forEach(key -> {
			print(owlTools.getShortIRIString(key.toString()) + "\u1D35 = { "
					+ roles.get(key).toString().substring(0, roles.get(key).toString().length() - 2) + " }");
		});

	}

	private static void print(String string) {
		logger.info(string);
	}

	/**
	 * Map each role to a set of its pairs of elements
	 * 
	 * @param concepts
	 * @param element
	 */
	private static void fillRoles(Map<OWLObjectProperty, StringBuffer> roles, Element element) {

		element.getRelations().forEach(relation -> {
			if (relation.isForward()) {
				OWLObjectProperty role = relation.getRoleName();
				if (roles.keySet().contains(role)) {
					StringBuffer pair = getPair(element, relation);
					if (!pair.toString().equals(""))
						append(roles.get(role), pair);
				} else {
					StringBuffer newString = new StringBuffer();
					StringBuffer pair = getPair(element, relation);
					if (!pair.toString().equals("")) {
						append(newString, pair);
						roles.put(role, newString);
					}
				}
			}
		});

	}

	/**
	 * Format relations into pairs in roles
	 * 
	 * @param element
	 * @param relation
	 * @return
	 */
	private static StringBuffer getPair(Element element, Relation relation) {

		StringBuffer res = new StringBuffer();
		String a = element.getName(), b = relation.getElement2().getName();

		if (!(addedRelations.contains(relation.getRoleName() + "( " + a + ", " + b + " )")
				|| addedRelations.contains(relation.getRoleName() + "( " + b + ", " + a + " )"))) {
			if (relation.isBackward() && relation.isForward()) {
				if (element.equals(relation.getElement2())) {
					res.append("( " + a + ", " + b + " )");
					addedRelations.add(relation.getRoleName() + "( " + a + ", " + b + " )");
				} else {
					res.append("( " + a + ", " + b + " ), ");
					res.append("( " + b + ", " + a + " )");
					addedRelations.add(relation.getRoleName() + "( " + a + ", " + b + " )");
					addedRelations.add(relation.getRoleName() + "( " + b + ", " + a + " )");
				}
				return res;
			}

			if (relation.isBackward()) {
				res.append("( " + b + ", " + a + " )");
				addedRelations.add(relation.getRoleName() + "( " + b + ", " + a + " )");
				return res;
			}

			res.append("( " + a + ", " + b + " )");
			addedRelations.add(relation.getRoleName() + "( " + a + ", " + b + " )");
		}
		return res;
	}

	/**
	 * Map each concept to a set of its elements
	 * 
	 * @param concepts
	 * @param element
	 */
	private static void fillConcepts(Map<OWLClass, StringBuffer> concepts, Element element) {

		element.getTypes().forEach(type -> {
			OWLClass cls = (OWLClass) type;
			if (!cls.isOWLThing())
				if (concepts.keySet().contains(cls))
					append(concepts.get(cls), element.getName());
				else {
					StringBuffer newString = new StringBuffer();
					append(newString, element.getName());
					concepts.put(cls, newString);
				}
		});
	}

	private static void append(StringBuffer strBuffer, Object toAppend) {
		strBuffer.append(toAppend);
		strBuffer.append(", ");
	}

}
