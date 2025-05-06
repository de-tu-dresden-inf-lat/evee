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

	private static final String PREDNAME_EXISTS = "<http://www.w3.org/2002/07/owl#someValuesFrom>";
	private static final String PREDNAME_CONJ = "<http://www.w3.org/2002/07/owl#intersectionOf>";

	private static final String PREDNAME_PROP = "<http://www.w3.org/2002/07/owl#onProperty>";

	private static final String PREDNAME_FIRST = "<http://www.w3.org/1999/02/22-rdf-syntax-ns#first>";
	private static final String PREDNAME_REST = "<http://www.w3.org/1999/02/22-rdf-syntax-ns#rest>";
	private static final String PREDNAME_NIL = "<http://www.w3.org/1999/02/22-rdf-syntax-ns#nil>";

	public static final String REPOF = "repOf";
	public static final String REPOF_PROP = "repOfProp";

	private final OWLHelper owlHelper = OWLHelper.getInstance();
	private final ParsingHelper parsingHelper = ParsingHelper.getInstance();

	//all relevant triples of a proof to parse placeholders
	private Set<List<String>> parsingBase;
	// 1:1 mapping of equivalent placholders. ie all repOf() and repOfProp() facts of proof
	private Map<String,String> equivalentPlaceholders;


	// caches already parsed placeholders.
	// Key is the placeholder id and value the corresponding OWL concept 
	private Map<String, OWLClassExpression> conceptCache = new HashMap<>();
	private Map<String, List<OWLObjectPropertyExpression>> roleChainCache = new HashMap<>();

	public PlaceholderParser(){}

	/*
	 * computes facts of input that are relevant for parsing placeholders 
	 */
	public void initParsingBase(List<IInference<String>> inferences){

        Set<List<String>> parsingBase = new HashSet<>();
        Map<String,String> equiv = new HashMap<>();

        for(IInference<String> inf : inferences){
            String conc = inf.getConclusion();

            if(!parsingHelper.containsPlaceholders(conc))
                continue;

            if(parsingHelper.isRdfTriple(conc)){
                parsingBase.add(parsingHelper.getPredicateArguments(conc));
            }
            else if(parsingHelper.getPredicateName(conc).equals(REPOF) || parsingHelper.getPredicateName(conc).equals(REPOF_PROP)){
				List<String> args = parsingHelper.getPredicateArguments(conc);         
				equiv.put(args.get(1), args.get(0)); //second arg of repOf predicate is Placeholder introduced by nemo
            }
        }
        
        this.parsingBase = parsingBase;
		this.equivalentPlaceholders = equiv;
    }
	
	public OWLClassExpression parseConceptOrPlaceholder(String conceptName) throws ConceptTranslationError {
		if (parsingHelper.isPlaceholder(conceptName))
			return getConceptFromPlaceholder(conceptName);

		return owlHelper.getOWLConceptName(parsingHelper.format(conceptName));
	}

	public OWLClassExpression getConceptFromPlaceholder(String placeholder) throws ConceptTranslationError {
		if (conceptCache.get(placeholder) != null){
			return conceptCache.get(placeholder);
		}

		Set<List<String>> relevantFacts = getPlaceholderFacts(placeholder);

		OWLClassExpression parsedConcept;
		if (relevantFacts.isEmpty()) //no match in triples -> 1:1 mapping of placholders
			parsedConcept = parseRepresentativeOf(placeholder);
		else if (relevantFacts.stream().anyMatch(x -> x.get(1).equals(PREDNAME_EXISTS))) //existential restriction
			parsedConcept = parseExistentialRestriction(relevantFacts);
		else if (relevantFacts.stream().anyMatch(x -> x.get(1).equals(PREDNAME_CONJ) || x.get(1).equals(PREDNAME_REST))) // conjunction
			parsedConcept = parseConjunction(placeholder, relevantFacts);
		else // error
			throw new ConceptTranslationError("Failed to parse placeholder " + placeholder);
		
		conceptCache.put(placeholder, parsedConcept);
		return parsedConcept;
	}

	public List<OWLObjectPropertyExpression> parseRoleOrPlaceholder(String roleName) throws ConceptTranslationError{
		if (parsingHelper.isPlaceholder(roleName))
			return getRoleChainFromPlaceholder(roleName);
		
		OWLObjectPropertyExpression role = owlHelper.getPropertyName(parsingHelper.format(roleName));
		return Collections.singletonList(role);
	}

	public List<OWLObjectPropertyExpression> getRoleChainFromPlaceholder(String placeholder) throws ConceptTranslationError {
		if(roleChainCache.get(placeholder) != null)
			return roleChainCache.get(placeholder);

		Set<List<String>> relevantFacts = getListFacts(placeholder, PREDNAME_REST);
		if (relevantFacts.isEmpty())
			return parseRepresentativeOfProp(placeholder);

		return parseRoleChain(placeholder, relevantFacts);
	}

	private OWLClassExpression parseRepresentativeOf(String placeholder) throws ConceptTranslationError{
		String rep = equivalentPlaceholders.get(placeholder);
		if (rep == null)
			throw new ConceptTranslationError("Failed to parse placeholder " + placeholder);
		
		return getConceptFromPlaceholder(rep);
	}

	private List<OWLObjectPropertyExpression> parseRepresentativeOfProp(String placeholder)throws ConceptTranslationError{
		String rep = equivalentPlaceholders.get(placeholder);
		if (rep == null)
			throw new ConceptTranslationError("Failed to parse placeholder " + placeholder);
		
		return getRoleChainFromPlaceholder(rep);
	}

	private OWLObjectSomeValuesFrom parseExistentialRestriction(Set<List<String>> relevantFacts)
			throws ConceptTranslationError {
		logger.debug("Parsing an existential");

		String currentPropertyStr =
				relevantFacts.stream().filter(x -> x.get(1).equals(PREDNAME_PROP)).findFirst().get().get(2);

		OWLObjectProperty property = owlHelper.getPropertyName(parsingHelper.format(currentPropertyStr));

		String fillerConceptStr = relevantFacts.stream().filter(x -> x.get(1).equals(PREDNAME_EXISTS))
				.findFirst().get().get(2);

		OWLClassExpression filler = parseConceptOrPlaceholder(fillerConceptStr);

		return owlHelper.getOWLExistentialRestriction(property, filler);
	}

	private OWLObjectIntersectionOf parseConjunction(String placeholder, Set<List<String>> relevantFacts) {

		logger.debug("Parsing a conjunction");
		relevantFacts.addAll(getRelevantConjFacts(placeholder));

		Set<OWLClassExpression> conjuncts = new HashSet<>();
		relevantFacts.stream().filter(x -> x.get(1).equals(PREDNAME_FIRST)).forEach(x -> {
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

	private List<OWLObjectPropertyExpression> parseRoleChain(String placeholder, Set<List<String>> relevantFacts){
		List<OWLObjectPropertyExpression> chain = new ArrayList<>();

		String next = placeholder;
		while(!next.equals(PREDNAME_NIL)){
			String current = next;
			String prop = relevantFacts.stream()
								.filter(x -> x.get(0).equals(current) && x.get(1).equals(PREDNAME_FIRST))
									.findFirst().get().get(2);

			chain.add(owlHelper.getPropertyName(parsingHelper.format(prop)));

			next = relevantFacts.stream()
								.filter(x -> x.get(0).equals(current) && x.get(1).equals(PREDNAME_REST))
									.findFirst().get().get(2);
		}

		return chain;
	}

	private Set<List<String>> getRelevantConjFacts(String placeholder) {
		 return getListFacts(placeholder, PREDNAME_REST, PREDNAME_CONJ);
	}

	/**
	 * @returns all facts in parsing base that are connected to @param placeholder by a Predicate in @param linkPredNames
	 **/
	private Set<List<String>> getListFacts(String placeholder, String... linkPredNames){
		Set<List<String>> facts = new HashSet<>();

		getPlaceholderFacts(placeholder).forEach(atomArgs -> {
			facts.add(atomArgs);

			String next = atomArgs.get(2);
			if (parsingHelper.isPlaceholder(next) 
					&& Arrays.stream(linkPredNames).anyMatch(atomArgs.get(1)::equals))
				facts.addAll(getListFacts(next, linkPredNames));
		});

		return facts;
	}

	private Set<List<String>> getPlaceholderFacts(String placeholder){
		return this.parsingBase.stream()
		   			.filter(x -> x.get(0).equals(placeholder))
			   			.collect(Collectors.toSet());
	}

	//for manually setting parsingBase & equivalentPlaceholders i.e. for testing
	public void setParsingBase(Set<List<String>> parsingBase){
		this.parsingBase = Sets.newHashSet(parsingBase);
	}
	
	public void setEquivalentPlaceholders(Map<String, String> equiv){
		equivalentPlaceholders = new HashMap<String, String>(equiv);
	}

}
