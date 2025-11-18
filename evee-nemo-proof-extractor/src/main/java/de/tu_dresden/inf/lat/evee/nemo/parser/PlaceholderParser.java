package de.tu_dresden.inf.lat.evee.nemo.parser;

import de.tu_dresden.inf.lat.evee.nemo.parser.exceptions.ConceptTranslationError;
import de.tu_dresden.inf.lat.evee.nemo.parser.tools.OWLHelper;
import de.tu_dresden.inf.lat.evee.nemo.parser.tools.ParsingHelper;
import de.tu_dresden.inf.lat.evee.proofs.data.exceptions.ProofNotSupportedException;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInference;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.semanticweb.owlapi.model.*;

import com.google.common.collect.Sets;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Christian Alrabbaa
 *
 */
public class PlaceholderParser {
	private static final Logger logger = LogManager.getLogger(PlaceholderParser.class);

	private static final String PREDNAME_EXISTS = "<http://www.w3.org/2002/07/owl#someValuesFrom>";
	private static final String PREDNAME_FORALL = "<http://www.w3.org/2002/07/owl#allValuesFrom>";

	private static final String PREDNAME_CONJ = "<http://www.w3.org/2002/07/owl#intersectionOf>";
	private static final String PREDNAME_DISJ = "<http://www.w3.org/2002/07/owl#unionOf>";

	private static final String PREDNAME_NEG = "<http://www.w3.org/2002/07/owl#complementOf>";
	
	private static final String PREDNAME_NUMRES_EQ = "<http://www.w3.org/2002/07/owl#qualifiedCardinality>";
	private static final String PREDNAME_NUMRES_MAX = "<http://www.w3.org/2002/07/owl#maxQualifiedCardinality>";
	private static final String PREDNAME_NUMRES_MIN = "<http://www.w3.org/2002/07/owl#minQualifiedCardinality>";
	
	private static final String PREDNAME_NUMRES_EQ_UNQUAL = "<http://www.w3.org/2002/07/owl#cardinality>";
	private static final String PREDNAME_NUMRES_MAX_UNQUAL = "<http://www.w3.org/2002/07/owl#maxCardinality>";
	private static final String PREDNAME_NUMRES_MIN_UNQUAL = "<http://www.w3.org/2002/07/owl#minCardinality>";

	private final Set<String> numResNames = Sets.newHashSet(PREDNAME_NUMRES_EQ, PREDNAME_NUMRES_MAX, PREDNAME_NUMRES_MIN, PREDNAME_NUMRES_EQ_UNQUAL, PREDNAME_NUMRES_MAX_UNQUAL, PREDNAME_NUMRES_MIN_UNQUAL);

	private static final String PREDNAME_ONEOF = "<http://www.w3.org/2002/07/owl#oneOf>";
	private static final String PREDNAME_HASSELF = "<http://www.w3.org/2002/07/owl#hasSelf>";

	private static final String PREDNAME_PROP = "<http://www.w3.org/2002/07/owl#onProperty>";
	private static final String PREDNAME_CLS = "<http://www.w3.org/2002/07/owl#onClass>";
	private static final String PREDNAME_DATA = "<http://www.w3.org/2002/07/owl#onDataRange>";

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
	
	public OWLClassExpression parseConceptOrPlaceholder(String conceptName) throws ConceptTranslationError, ProofNotSupportedException {
		if (parsingHelper.isPlaceholder(conceptName))
			return getConceptFromPlaceholder(conceptName);

		return owlHelper.getOWLConceptName(parsingHelper.format(conceptName));
	}

	public OWLClassExpression getConceptFromPlaceholder(String placeholder) throws ConceptTranslationError, ProofNotSupportedException {
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
		else if (relevantFacts.stream().anyMatch(x -> x.get(1).equals(PREDNAME_NEG))) //negation
			parsedConcept = parseNegation(relevantFacts);
		else if (relevantFacts.stream().anyMatch(x -> x.get(1).equals(PREDNAME_FORALL))) //universal restriction
			parsedConcept = parseUniversalRestriction(relevantFacts);
		else if (relevantFacts.stream().anyMatch(x -> x.get(1).equals(PREDNAME_DISJ))) //disjunction
			parsedConcept = parseDisjunction(placeholder, relevantFacts);
		else if (relevantFacts.stream().anyMatch(x -> x.get(1).equals(PREDNAME_ONEOF))) //OneOf
			parsedConcept = parseOneOf(placeholder, relevantFacts);
		else if (relevantFacts.stream().anyMatch(x -> x.get(1).equals(PREDNAME_HASSELF))) //hasSelf restriction
			parsedConcept = parseHasSelf(relevantFacts);
		else if (relevantFacts.stream().anyMatch(x -> numResNames.contains(x.get(1)))) //number restriction
			parsedConcept = parseNumberRestriction(relevantFacts);
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
	
	private OWLClassExpression parseRepresentativeOf(String placeholder) throws ConceptTranslationError, ProofNotSupportedException{
		String rep = equivalentPlaceholders.get(placeholder);
		if (rep == null)
			throw new ConceptTranslationError("error parsing placeholder " + placeholder);

		if(rep.equals(placeholder))
			throw new ProofNotSupportedException("constructor of " + placeholder + "not supported");
		
		return getConceptFromPlaceholder(rep);
	}
	
	private List<OWLObjectPropertyExpression> parseRepresentativeOfProp(String placeholder)throws ConceptTranslationError{
		String rep = equivalentPlaceholders.get(placeholder);
		if (rep == null)
		throw new ConceptTranslationError("Failed to parse placeholder " + placeholder);
		
		return getRoleChainFromPlaceholder(rep);
	}
	
	private OWLObjectSomeValuesFrom parseExistentialRestriction(Set<List<String>> relevantFacts)
	throws ConceptTranslationError, ProofNotSupportedException {
		logger.debug("Parsing an existential");
		
		String currentPropertyStr =
		relevantFacts.stream().filter(x -> x.get(1).equals(PREDNAME_PROP)).findFirst().get().get(2);
		
		OWLObjectProperty property = owlHelper.getPropertyName(parsingHelper.format(currentPropertyStr));
		
		String fillerConceptStr = relevantFacts.stream().filter(x -> x.get(1).equals(PREDNAME_EXISTS))
		.findFirst().get().get(2);
		
		OWLClassExpression filler = parseConceptOrPlaceholder(fillerConceptStr);
		
		return owlHelper.getOWLExistentialRestriction(property, filler);
	}

	private OWLObjectAllValuesFrom parseUniversalRestriction(Set<List<String>> relevantFacts) throws ConceptTranslationError, ProofNotSupportedException{
		String currentPropertyStr =
		relevantFacts.stream().filter(x -> x.get(1).equals(PREDNAME_PROP)).findFirst().get().get(2);
		
		OWLObjectProperty property = owlHelper.getPropertyName(parsingHelper.format(currentPropertyStr));
		
		String fillerConceptStr = relevantFacts.stream().filter(x -> x.get(1).equals(PREDNAME_FORALL))
		.findFirst().get().get(2);
		
		OWLClassExpression filler = parseConceptOrPlaceholder(fillerConceptStr);
		
		return owlHelper.getOWLUniversalRestriction(property, filler);
	}

	private OWLObjectCardinalityRestriction parseNumberRestriction(Set<List<String>> relevantFacts) throws ConceptTranslationError, ProofNotSupportedException{
		String predicate = relevantFacts.stream().filter(x -> numResNames.contains(x.get(1))).findFirst().get().get(1);

		String propStr =
		relevantFacts.stream().filter(x -> x.get(1).equals(PREDNAME_PROP)).findFirst().get().get(2);
		OWLObjectProperty prop = owlHelper.getPropertyName(parsingHelper.format(propStr));
		
		String cardStr = relevantFacts.stream().filter(x -> x.get(1).equals(predicate)).findFirst().get().get(2);
		int card = Integer.parseInt(cardStr);

		switch (predicate) {
			case PREDNAME_NUMRES_EQ:
				return owlHelper.getOWLNumberRestrExact(prop, card, getNumResFiller(relevantFacts));
			case PREDNAME_NUMRES_EQ_UNQUAL:
				return owlHelper.getOWLNumberRestrExact(prop, card);
			case PREDNAME_NUMRES_MAX:
				return owlHelper.getOWLNumberRestrMax(prop, card, getNumResFiller(relevantFacts));
			case PREDNAME_NUMRES_MAX_UNQUAL:
				return owlHelper.getOWLNumberRestrMax(prop, card);
			case PREDNAME_NUMRES_MIN:
				return owlHelper.getOWLNumberRestrMin(prop, card, getNumResFiller(relevantFacts));
			case PREDNAME_NUMRES_MIN_UNQUAL:
				return owlHelper.getOWLNumberRestrMin(prop, card);
			default:
				throw new ConceptTranslationError("error parsing object number restriction");
		}

	}

	private OWLClassExpression getNumResFiller(Set<List<String>> relevantFacts) throws ConceptTranslationError, ProofNotSupportedException{
		Optional<List<String>> fillerStr = relevantFacts.stream()
			.filter(x -> x.get(1).equals(PREDNAME_CLS) || x.get(1).equals(PREDNAME_DATA))
				.findFirst();
					
		if (!fillerStr.isPresent())
			throw new ConceptTranslationError("no filler concept of qualified NumRes in parsing base");
	
		return parseConceptOrPlaceholder(fillerStr.get().get(2));
	}

	private OWLObjectIntersectionOf parseConjunction(String placeholder, Set<List<String>> relevantFacts) throws ConceptTranslationError, ProofNotSupportedException {
		logger.debug("Parsing a conjunction");
		relevantFacts.addAll(getRelevantConjFacts(placeholder));
		
		Set<OWLClassExpression> conjuncts = new HashSet<>();
		relevantFacts = relevantFacts.stream().filter(x -> x.get(1).equals(PREDNAME_FIRST)).collect(Collectors.toSet());
		for(List<String> fact: relevantFacts) {
			conjuncts.add(parseConceptOrPlaceholder(fact.get(2)));
		};
		
		OWLObjectIntersectionOf result = owlHelper.getOWLConjunction(conjuncts);
		
		logger.debug("parse result -> " + result);
		
		return result;
	}

	private OWLClassExpression parseDisjunction(String placeholder, Set<List<String>> relevantFacts) throws ConceptTranslationError, ProofNotSupportedException {
		relevantFacts.addAll(getRelevantDisjFacts(placeholder));
		
		Set<OWLClassExpression> disjuncts = new HashSet<>();
		relevantFacts = relevantFacts.stream().filter(x -> x.get(1).equals(PREDNAME_FIRST)).collect(Collectors.toSet());
		for(List<String> fact: relevantFacts) {
			disjuncts.add(parseConceptOrPlaceholder(fact.get(2)));
		};

		return owlHelper.getOWLDisjunction(disjuncts);
	}


	private OWLObjectComplementOf parseNegation(Set<List<String>> relevantFacts) throws ConceptTranslationError, ProofNotSupportedException {

		String clsStr = relevantFacts.stream().filter(x -> x.get(1).equals(PREDNAME_NEG)).findFirst().get().get(2);
		OWLClassExpression cls = parseConceptOrPlaceholder(clsStr);

		return owlHelper.getOWLComplementOf(cls);
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

	private OWLObjectOneOf parseOneOf(String placeholder, Set<List<String>> relevantFacts){
		relevantFacts.addAll(getRelevantOneOfFacts(placeholder));

		Set<OWLIndividual> individuals = new HashSet<>();

		relevantFacts = relevantFacts.stream().filter(x -> x.get(1).equals(PREDNAME_FIRST)).collect(Collectors.toSet());
		for(List<String> fact: relevantFacts) {
			OWLIndividual ind = owlHelper.getNamedIndividual(parsingHelper.format(fact.get(2)));
			individuals.add(ind);
		};
		
		return owlHelper.getOWLOneOf(individuals.toArray(new OWLIndividual[0]));
	}

	private OWLObjectHasSelf parseHasSelf(Set<List<String>> relevantFacts){
		String propStr = relevantFacts.stream()
			.filter(x -> x.get(1).equals(PREDNAME_PROP))
			.findFirst().get().get(2);
		
		OWLObjectProperty prop = owlHelper.getPropertyName(parsingHelper.format(propStr));

		return owlHelper.getOWLHasSelf(prop);
	}
	
	private Set<List<String>> getRelevantConjFacts(String placeholder) {
		return getListFacts(placeholder, PREDNAME_REST, PREDNAME_CONJ);
	}

	private Set<List<String>> getRelevantDisjFacts(String placeholder) {
		return getListFacts(placeholder, PREDNAME_REST, PREDNAME_DISJ);
	}

	private Set<List<String>> getRelevantOneOfFacts(String placeholder) {
		return getListFacts(placeholder, PREDNAME_REST, PREDNAME_ONEOF);
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
		this.parsingBase = new HashSet<>(parsingBase);
	}
	
	public void setEquivalentPlaceholders(Map<String, String> equiv){
		equivalentPlaceholders = new HashMap<String, String>(equiv);
	}
	
}
