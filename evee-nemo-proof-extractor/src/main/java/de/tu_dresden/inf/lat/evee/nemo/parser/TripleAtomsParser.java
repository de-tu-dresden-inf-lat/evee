package de.tu_dresden.inf.lat.evee.nemo.parser;

import com.google.common.collect.Sets;
import de.tu_dresden.inf.lat.evee.nemo.parser.exceptions.ConceptTranslationError;
import de.tu_dresden.inf.lat.evee.nemo.parser.tools.OWLHelper;
import de.tu_dresden.inf.lat.evee.nemo.parser.tools.ParsingHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.semanticweb.owlapi.model.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Christian Alrabbaa
 *
 */
public class TripleAtomsParser {
	private static final Logger logger = LogManager.getLogger(TripleAtomsParser.class);

	private static final String subclassOfStr = "http://www.w3.org/2000/01/rdf-schema#subClassOf";

	//

	private static final String existentialRestrictionStr = "http://www.w3.org/2002/07/owl#someValuesFrom";

	private static final String conjuctionStr = "http://www.w3.org/2002/07/owl#intersectionOf";

	//

	private static final String typeStr = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";

	private static final String conceptStr = "http://www.w3.org/2002/07/owl#Class";

	private static final String propertyStr = "http://www.w3.org/2002/07/owl#onProperty";

	//

	private static final String firstStr = "http://www.w3.org/1999/02/22-rdf-syntax-ns#first";

	private static final String restStr = "http://www.w3.org/1999/02/22-rdf-syntax-ns#rest";

	private static final String nilStr = "http://www.w3.org/1999/02/22-rdf-syntax-ns#nil";

	//

	public static final String triple = "TRIPLE";

	private static final OWLHelper owlHelper = OWLHelper.getInstance();
	private static final ParsingHelper parsingHelper = ParsingHelper.getInstance();

	private static final OWLSubClassOfAxiom defaultAxiom =
			owlHelper.getOWLSubClassOfAxiom(owlHelper.getOWLBot(), owlHelper.getOWLTop());

	private final Set<List<String>> tripleAtoms;
	public TripleAtomsParser(Set<List<String>> tripleAtomsArgs){
		this.tripleAtoms = Sets.newHashSet(tripleAtomsArgs);
	}

	public OWLAxiom parse(List<String> args) throws ConceptTranslationError {
		logger.debug("Parsing Triple");

		if (args.get(1).equals(subclassOfStr))
			return parseSubclassOf(args);

		logger.debug("Skipping non axiom Triple! args -> " + args);

		return defaultAxiom;
	}

	private OWLAxiom parseSubclassOf(List<String> args) throws ConceptTranslationError {
		String lhsStr = args.get(0);
		String rhsStr = args.get(2);

		OWLClassExpression lhs = getConceptFromId(lhsStr);
		OWLClassExpression rhs = getConceptFromId(rhsStr);

		OWLAxiom result = owlHelper.getOWLSubClassOfAxiom(lhs, rhs);

		logger.debug("parse result -> " + result);

		return result;
	}

	private OWLObjectSomeValuesFrom parseExistentialRestriction(Set<List<String>> relevantFacts)
			throws ConceptTranslationError {
		logger.debug("Parsing an existential");

		String currentPropertyStr =
				relevantFacts.stream().filter(x -> x.get(1).equals(propertyStr)).findFirst().get().get(2);

		OWLObjectProperty property = owlHelper.getPropertyName(format(currentPropertyStr));

		String fillerConceptStr = relevantFacts.stream().filter(x -> x.get(1).equals(existentialRestrictionStr))
				.findFirst().get().get(2);

		OWLClassExpression filler = getConceptFromId(fillerConceptStr);

		return owlHelper.getOWLExistentialRestriction(property, filler);

	}

	private OWLObjectIntersectionOf parseConjunction(String id, Set<List<String>> relevantFacts) {

		logger.debug("Parsing a conjunction");

		relevantFacts.addAll(getAllRelevantTripleFacts(id));

		if (logger.isDebugEnabled()) {
			System.out.println("All relevant facts");
			relevantFacts.forEach(System.out::println);
		}

		Set<OWLClassExpression> conjuncts = new HashSet<>();
		relevantFacts.stream().filter(x -> x.get(1).equals(firstStr)).forEach(x -> {
			try {
				conjuncts.add(getConceptFromId(x.get(2)));
			} catch (ConceptTranslationError e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});

		OWLObjectIntersectionOf result = owlHelper.getOWLConjunction(conjuncts);

		logger.debug("parse result -> " + result);

		return result;
	}

	private Set<List<String>> getAllRelevantTripleFacts(String id) {
		Set<String> explored = new HashSet<>();
		Set<List<String>> result = new HashSet<>();

		getAllRelevantTripleFacts(id, explored, result);

		return result;
	}

	private void getAllRelevantTripleFacts(String id, Set<String> explored, Set<List<String>> result) {
		Set<List<String>> relevantFacts;
		explored.add(id);

		relevantFacts = this.tripleAtoms.stream().filter(x -> !result.contains(x)).filter(x -> x.contains(id))
				.collect(Collectors.toSet());

		for (List<String> atomArgs : relevantFacts) {
			result.add(atomArgs);

			for (String arg : atomArgs) {
				if (parsingHelper.isID(arg)) {
					if (!explored.contains(arg)) {
						getAllRelevantTripleFacts(arg, explored, result);
					}
				}
			}
		}
	}

	private OWLClassExpression getConceptFromId(String id) throws ConceptTranslationError {
		if (!parsingHelper.isID(id))
			return owlHelper.getOWLConceptName(format(id));

		Set<List<String>> relevantFacts = this.tripleAtoms.stream().filter(x -> x.get(0).equals(id))
				.collect(Collectors.toSet());

		if (relevantFacts.stream().anyMatch(x -> x.get(1).equals(existentialRestrictionStr)))
			return parseExistentialRestriction(relevantFacts);

		if (relevantFacts.stream().anyMatch(x -> x.get(1).equals(conjuctionStr)))
			return parseConjunction(id, relevantFacts);

		throw new ConceptTranslationError("Failed to parse " + triple + " facts with id = " + id);
	}

	private void isConcept(Set<List<String>> relevantFacts) {
		// Currently only check when debugging
		if (logger.isDebugEnabled()) {
			String currentType = relevantFacts.stream().filter(x -> x.get(1).equals(typeStr)).findFirst()
					.get().get(2);
			assert currentType.equals(conceptStr) : "Concept check failed!";
		}
	}

	private String format(String arg) {
		if (arg.startsWith("<") && arg.endsWith(">"))
			return arg.substring(1, arg.length() - 1);
		return arg;
	}

}
