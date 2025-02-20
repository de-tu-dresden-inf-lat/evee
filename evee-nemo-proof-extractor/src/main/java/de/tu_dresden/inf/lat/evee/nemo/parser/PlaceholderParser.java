package de.tu_dresden.inf.lat.evee.nemo.parser;

import com.google.common.collect.Sets;
import de.tu_dresden.inf.lat.evee.nemo.parser.exceptions.ConceptTranslationError;
import de.tu_dresden.inf.lat.evee.nemo.parser.tools.OWLHelper;
import de.tu_dresden.inf.lat.evee.nemo.parser.tools.ParsingHelper;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInference;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.semanticweb.owlapi.model.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Christian Alrabbaa
 *
 */
public class PlaceholderParser {
	private static final Logger logger = LogManager.getLogger(PlaceholderParser.class);

	//
	private static final String subclassOfStr = "<http://www.w3.org/2000/01/rdf-schema#subClassOf>";

	private static final String existentialRestrictionStr = "<http://www.w3.org/2002/07/owl#someValuesFrom>";
	private static final String conjuctionStr = "<http://www.w3.org/2002/07/owl#intersectionOf>";

	private static final String typeStr = "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>";
	private static final String conceptStr = "<http://www.w3.org/2002/07/owl#Class>";
	private static final String propertyStr = "<http://www.w3.org/2002/07/owl#onProperty>";

	private static final String firstStr = "<http://www.w3.org/1999/02/22-rdf-syntax-ns#first>";
	private static final String restStr = "<http://www.w3.org/1999/02/22-rdf-syntax-ns#rest>";
	private static final String nilStr = "<http://www.w3.org/1999/02/22-rdf-syntax-ns#nil>";
	//

	public static final String triple = "TRIPLE";
	public static final String repOf = "repOf";

	private final OWLHelper owlHelper = OWLHelper.getInstance();
	private final ParsingHelper parsingHelper = ParsingHelper.getInstance();

	//all relevant triples of a proof to parse placeholders
	private Set<List<String>> parsingBase;
	// 1:1 mapping of equivalent placholders. ip all repOf() facts of proof
	private Map<String,String> equivalentPlaceholders;


	// chaches already parsed placeholders. 
	// Key is the placeholder id and value the corresponding OWL concept 
	private Map<String, OWLClassExpression> placeholderCache = new HashMap<>();

	public PlaceholderParser(List<IInference<String>> inferences){
		computeParsingBase(inferences);
	}

	public OWLClassExpression getConceptFromPlaceholder(String placeholder) throws ConceptTranslationError {
		if (placeholderCache.get(placeholder) != null){
			return placeholderCache.get(placeholder);
		}

		Set<List<String>> relevantFacts = this.parsingBase.stream().filter(x -> x.get(0).equals(placeholder))
				.collect(Collectors.toSet());

		OWLClassExpression parsedConcept;
		if (relevantFacts.isEmpty()) //no match in triples -> 1:1 mapping of placholders
			parsedConcept = parseRepresentativeOf(placeholder);
		else if (relevantFacts.stream().anyMatch(x -> x.get(1).equals(existentialRestrictionStr))) //existential restriction
			parsedConcept = parseExistentialRestriction(relevantFacts);
		else if (relevantFacts.stream().anyMatch(x -> x.get(1).equals(conjuctionStr) || x.get(1).equals(restStr))) // conjunction
			parsedConcept = parseConjunction(placeholder, relevantFacts);
		else // error
			throw new ConceptTranslationError("Failed to parse placeholder " + placeholder);
		
		placeholderCache.put(placeholder, parsedConcept);
		return parsedConcept;
	}
	
	public OWLClassExpression parseConceptOrPlaceholder(String conceptName) throws ConceptTranslationError {
		if (parsingHelper.isPlaceholder(conceptName))
			return getConceptFromPlaceholder(conceptName);

		return owlHelper.getOWLConceptName(format(conceptName));
	}
	
	private OWLAxiom parseSubclassOf(List<String> args) throws ConceptTranslationError {
		String lhsStr = args.get(0);
		String rhsStr = args.get(2);

		OWLClassExpression lhs = parseConceptOrPlaceholder(lhsStr);
		OWLClassExpression rhs = parseConceptOrPlaceholder(rhsStr);

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

		OWLClassExpression filler = parseConceptOrPlaceholder(fillerConceptStr);

		return owlHelper.getOWLExistentialRestriction(property, filler);
	}

	private OWLObjectIntersectionOf parseConjunction(String id, Set<List<String>> relevantFacts) {

		logger.debug("Parsing a conjunction");
		relevantFacts.addAll(getAllRelevantTripleFacts(id));

		Set<OWLClassExpression> conjuncts = new HashSet<>();
		relevantFacts.stream().filter(x -> x.get(1).equals(firstStr)).forEach(x -> {
			try {
				conjuncts.add(parseConceptOrPlaceholder(x.get(2)));
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
		Set<List<String>> result = new HashSet<>();
		getAllRelevantTripleFacts(id, result);

		return result;
	}

	private void getAllRelevantTripleFacts(String id, Set<List<String>> result) {		
		 Set<List<String>> relevantFacts = this.parsingBase.stream()
		 	.filter(x -> !result.contains(x))
				.filter(x -> x.get(0).equals(id))
					.collect(Collectors.toSet());

		for (List<String> atomArgs : relevantFacts) {
			result.add(atomArgs);

			String next = atomArgs.get(2);
			if (parsingHelper.isPlaceholder(next) && 
					(atomArgs.get(1).equals(restStr) || atomArgs.get(1).equals(conjuctionStr))) {
				getAllRelevantTripleFacts(next, result);
			}
		}
	}

	private OWLClassExpression parseRepresentativeOf(String placeholder) throws ConceptTranslationError{
		String rep = equivalentPlaceholders.get(placeholder);
		if (rep == null)
			throw new ConceptTranslationError("Failed to parse placeholder " + placeholder);
		
		return getConceptFromPlaceholder(rep);
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

	/*
	 * computes facts of input that are relevant for parsing placeholders 
	 */
	private void computeParsingBase(List<IInference<String>> inferences){

        Set<List<String>> parsingBase = new HashSet<>();
        Map<String,String> equiv = new HashMap<>();

        for(IInference<String> inf : inferences){
            String conc = inf.getConclusion();

            if(!parsingHelper.containsPlaceholders(conc))
                continue;

            if(parsingHelper.isRdfTriple(conc)){
                parsingBase.add(parsingHelper.getPredicateArguments(conc));
            }
            else if(parsingHelper.getPredicateName(conc).equals(repOf)){
				List<String> args = parsingHelper.getPredicateArguments(conc);         
				equiv.put(args.get(1), args.get(0)); //second arg of repOf predicate is unique
            }
        }
        
        this.parsingBase = parsingBase;
		this.equivalentPlaceholders = equiv;
    }

	//for manually setting parsingBase & equivalentPlaceholders i.e. for testing
	public void setParsingBase(Set<List<String>> parsingBase){
		this.parsingBase = Sets.newHashSet(parsingBase);
	}
	
	public void setEquivalentPlaceholders(Map<String, String> equiv){
		equivalentPlaceholders = new HashMap<String, String>(equiv);
	}

	public void printCache(){
		System.out.println("CACHE: \n" + placeholderCache);
	}

}
