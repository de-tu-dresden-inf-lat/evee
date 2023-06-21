package de.tu_dresden.inf.lat.counterExample;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.semanticweb.elk.owlapi.ElkReasoner;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import com.google.common.collect.Sets;

import de.tu_dresden.inf.lat.counterExample.tools.Segmenter;
import de.tu_dresden.inf.lat.model.data.ConjunctionMapper;
import de.tu_dresden.inf.lat.model.data.Element;
import de.tu_dresden.inf.lat.model.data.ElkModel;
import de.tu_dresden.inf.lat.model.data.Mapper;
import de.tu_dresden.inf.lat.model.data.RestrictionMapper;
import de.tu_dresden.inf.lat.model.tools.GeneralTools;
import de.tu_dresden.inf.lat.model.tools.ObjectGenerator;
import de.tu_dresden.inf.lat.model.tools.ToOWLTools;

/**
 * @author Christian Alrabbaa
 *
 */
public class ELKModelGenerator {

	private static final Logger logger = Logger.getLogger(ELKModelGenerator.class);
	private static final ToOWLTools owlTools = ToOWLTools.getInstance();
	private static final ObjectGenerator generator = ObjectGenerator.getInstance();
	private OWLOntology ontology, originalOntology;
	private final Set<OWLAxiom> originalAxioms;
	private final OWLAxiom conclusion;
	private final Mapper mapper;
//	private final Set<OWLAxiom> addedAxioms;
	private ElkModel fullCanonicalModel;

	/**
	 * Constructor
	 * 
	 * @param ontology
	 * @throws OWLOntologyCreationException
	 */
	public ELKModelGenerator(OWLOntology ontology, OWLAxiom conclusion) throws OWLOntologyCreationException {
		this.originalAxioms = new HashSet<>();
		this.originalOntology = ontology;
		this.ontology = ontology;
		this.conclusion = conclusion;
		this.mapper = new Mapper(owlTools.getLHS(conclusion), owlTools.getRHS(conclusion));

		finishInit();

	}

	private void finishInit() throws OWLOntologyCreationException {
		this.originalAxioms.addAll(this.ontology.getAxioms());

		if (!mapper.getAliasLHS().equals(mapper.getOriginalLHS())) {
			OWLEquivalentClassesAxiom newAxiom = owlTools.getOWLEquivalenceAxiom(mapper.getAliasLHS(),
					mapper.getOriginalLHS());
			addAxiom(newAxiom);
		}

		if (!mapper.getAliasRHS().equals(mapper.getOriginalRHS())) {
			OWLEquivalentClassesAxiom newAxiom = owlTools.getOWLEquivalenceAxiom(mapper.getAliasRHS(),
					mapper.getOriginalRHS());
			addAxiom(newAxiom);
		}

		this.ontology = Segmenter.getSegmentAsOntology(this.ontology,
				Sets.newHashSet(this.mapper.getAliasLHS(), this.mapper.getAliasRHS()), IRI.create("http://ModuleExtraction"));
	}

	public ELKModelGenerator(OWLOntology ontology) throws OWLOntologyCreationException {
		this(ontology, owlTools.getOWLSubClassOfAxiom(owlTools.getOWLBot(), owlTools.getOWLBot()));
	}

	/**
	 * Private Constructor, to pass the same mapper object to new ELKCounterModel
	 * Objects
	 * 
	 * @param ontology
	 * @param conclusion
	 * @param mapper
	 * @throws OWLOntologyCreationException
	 */
//	private ELKCounterModel(OWLOntology ontology, OWLSubClassOfAxiom conclusion, Mapper mapper,
//			Set<OWLAxiom> addedAxioms) throws OWLOntologyCreationException {
//		this.ontology = ontology;
//		this.conclusion = conclusion;
//		this.mapper = mapper;
//		this.addedAxioms = addedAxioms;
//	}

	/**
	 * Return the ontology
	 * 
	 * @return Ontology
	 */
	public OWLOntology getOntology() {
		return ontology;
	}

	/**
	 * Return the conclusion, for which a counter example is generated
	 * 
	 * @return
	 */
	public OWLAxiom getConclusion() {
		return conclusion;
	}

	/**
	 * Return the mapper object
	 * 
	 * @return
	 */
	public Mapper getMapper() {
		return mapper;
	}

	/**
	 * Generates the full canonical model of the module corresponding to the
	 * provided axiom
	 * 
	 * @return {@code Set<Element>}
	 * @throws OWLOntologyCreationException
	 */
	public ElkModel generateFullRelevantCanonicalModel() throws OWLOntologyCreationException {

		if (this.fullCanonicalModel != null)
			return this.fullCanonicalModel;

//		if (!mapper.getAliasLHS().equals(mapper.getOriginalLHS())) {
//			OWLEquivalentClassesAxiom newAxiom = owlTools.getOWLEquivalenceAxiom(mapper.getAliasLHS(),
//					mapper.getOriginalLHS());
//			addAxiom(newAxiom);
//		}
//
//		if (!mapper.getAliasRHS().equals(mapper.getOriginalRHS())) {
//			OWLEquivalentClassesAxiom newAxiom = owlTools.getOWLEquivalenceAxiom(mapper.getAliasRHS(),
//					mapper.getOriginalRHS());
//			addAxiom(newAxiom);
//		}

//		ELKCounterModel elkCounterModel = new ELKCounterModel(this.ontology, this.conclusion, this.mapper,
//				this.addedAxioms);

		Set<Element> canonicalModelElements = this.computeCanonicalModel();// getCanonicalModel();

		logger.info("Full canonical model has been generated! total number of model elements = "
				+ canonicalModelElements.size());

		this.fullCanonicalModel = new ElkModel(canonicalModelElements, mapper);
		return this.fullCanonicalModel;
	}

	public Set<Element> generateFullRawCanonicalModelElements() throws OWLOntologyCreationException {

		Set<Element> canonicalModelElements = this.computeCanonicalModel();

		logger.info("Full raw canonical model has been generated! total number of model elements = "
				+ canonicalModelElements.size());

		return canonicalModelElements;
	}

	/**
	 * Starting from the element that represents the LHS of the conclusion, store
	 * all reachable elements in the "relevant elements" set.
	 * 
	 * @param startingElement
	 * @param modModelElements
	 * @param relevantElements
	 */
	private void getChain(Element startingElement, Set<Element> modModelElements, Set<Element> relevantElements) {
		startingElement.getRelations().forEach(relation -> {
			relevantElements.add(relation.getElement2());
			if (!relevantElements.contains(startingElement))
				getChain(relation.getElement2(), modModelElements, relevantElements);
		});
		relevantElements.add(startingElement);
	}

	/**
	 * Compute the part of the canonical model that serves as a counter model for
	 * the conclusion
	 *
	 * @throws OWLOntologyCreationException
	 */
	private Set<Element> ComputePartOfCanonicalModel() throws OWLOntologyCreationException {
		// TODO fix me, best to get rid of me

//		OWLEquivalentClassesAxiom newAxiom = owlTools.getOWLEquivalenceAxiom(mapper.getAliasLHS(),
//				mapper.getOriginalLHS());
//
//		addAxiom(newAxiom);
//
//		OWLOntology modOntology = Segmenter.getSegmentAsOntology(this.ontology, this.mapper.getAliasLHS(),
//				IRI.create(""));
//
//		logger.debug("Extracting relevant axioms");
//		logger.debug("Total number of needed axioms = " + modOntology.axioms().count() + " out of "
//				+ ontology.axioms().count());
//
//		ELKCounterModel modModel = new ELKCounterModel(modOntology, conclusion, mapper, addedAxioms);
//
		return this.computeCanonicalModel();// getCanonicalModel();
	}

	/**
	 * Compute the canonical model of the provided ontology
	 * 
	 * @throws OWLOntologyCreationException
	 */
	private Set<Element> computeCanonicalModel() throws OWLOntologyCreationException {

		// Setup the needed structure
		logger.info("Normalising the TBox");
		Instant start = Instant.now(), total = start;

		Set<OWLClassExpression> subConcepts = getAllSubConcepts();
		createMappersAndAxioms(subConcepts);

		Instant finish = Instant.now();
		logger.info(GeneralTools.getDuration(start, finish));

		// classify
		logger.info("Classifying");
		start = Instant.now();

		OWLReasoner reasoner = classify();

		finish = Instant.now();
		logger.info(GeneralTools.getDuration(start, finish));

		// create an element for every concept name and map for the other direction
		logger.info("Asserting elements to Concepts");
		start = Instant.now();

		Set<Element> elements = createElements(reasoner);

		finish = Instant.now();
		logger.info(GeneralTools.getDuration(start, finish));

		// apply the class hierarchy on every element
		logger.info("Propagating classification effect");
		start = Instant.now();

		applyClassHierarchy(elements, reasoner);

		finish = Instant.now();
		logger.info(GeneralTools.getDuration(start, finish));

//		if (!isFinilized) {
//			// create relations and add them to the corresponding elements
//			logger.info("Instantiating relations");
//			start = Instant.now();
//
//			UpdateElements(elements);
//
//			// remove artificial concept names (for restrictions and LHS of the conclusion)
//			elements.forEach(element -> element.updateTypes(mapper));
//
//			finish = Instant.now();
//			logger.info(GeneralTools.getDuration(start, finish));
//		}
		logger.info("Total " + GeneralTools.getDuration(total, finish));

//		System.out.println(
//				"original ontology axioms count after normalisation = " + this.originalOntology.axioms().count());
		// remove axioms generated from normalisation
		removePreviouslyAddedAxioms();
//		System.out.println("restored original Ontology axioms count = " + this.originalOntology.axioms().count());

		return elements;
	}

//	private void UpdateElements(Set<Element> elements) {
//		RestrictionMapper restrictionsMap = this.mapper.getRestrictionMapper();
//
//		elements.forEach(element_1 -> {
//			element_1.getTypes().forEach(type -> {
//
//				// index 0 isSuccessor,
//				// index 1 isPredecessor, and
//				// index 2 the filler.
//				Object[] data = new Object[] { false, false, null };
//
//				if (restrictionsMap.getClass2Restriction().containsKey(type)) {
//					OWLObjectSomeValuesFrom qer = restrictionsMap.getClass2Restriction().get(type);
//					OWLObjectProperty prop = prepareRelation(qer, data);
//
//					Element element_2 = getElement((OWLClassExpression) data[2]);
//
//					if (element_2.equals(element_1))
//						data[0] = data[1] = true;
//
//					element_1.addRelation(new Relation(prop, element_2, (boolean) data[0], (boolean) data[1]));
//					element_2.addRelation(new Relation(prop, element_1, (boolean) data[1], (boolean) data[0]));
//
//				}
//			});
//		});
//
//	}

//	private Element getElement(OWLClassExpression filler) {
//
//		if (filler instanceof OWLClass)
//			return mapper.getClassRepresentatives().get(filler);
//
//		if (filler instanceof OWLObjectSomeValuesFrom)
//			return mapper.getClassRepresentatives()
//					.get(mapper.getRestrictionMapper().getRestriction2Class().get(filler));
//
//		return mapper.getClassRepresentatives().get(mapper.getConjunctionMapper().getConjunction2Class().get(filler));
//	}

//	private OWLObjectProperty prepareRelation(OWLClassExpression type, Object[] data) {
//
//		if (type instanceof OWLObjectSomeValuesFrom) {
//			OWLObjectSomeValuesFrom qer = (OWLObjectSomeValuesFrom) type;
//			OWLObjectPropertyExpression proExp = qer.getProperty();
//
//			data[2] = qer.getFiller();
//
//			if (proExp instanceof OWLObjectProperty) {
//				data[1] = true;
//				return ((OWLObjectProperty) proExp);
//			}
//			if (proExp instanceof OWLObjectInverseOf)
//				data[0] = true;
//			return ((OWLObjectInverseOf) proExp).getNamedProperty();
//		}
//
//		assert false : "It should not be something other than OWLObjectSomeValuesFrom, but it was "
//				+ type.getClass().getSimpleName() + " -> " + type;
//		return null;
//
//	}

	private void applyClassHierarchy(Set<Element> elements, OWLReasoner reasoner) {

		// * Set<Element> notNeeded = new HashSet<>();

//		mapper.getRestrictionMapper().getClass2Restriction().entrySet().forEach(System.out::println);

		elements.forEach(element -> {
			Set<OWLClassExpression> tmp = new HashSet<>();
			for (OWLClassExpression exp : element.getTypes()) {

//				System.out.println("super of " + exp + " are : ");
//				reasoner.getSuperClasses(exp).entities().forEach(System.out::println);
//				reasoner.getEquivalentClasses(exp).entities().forEach(System.out::println);
				// * Set<OWLClass> superClasses =
				// resoner.getSuperClasses(exp).entities().collect(Collectors.toSet());
				tmp.addAll(reasoner.getSuperClasses(exp, false).getFlattened());
				tmp.addAll(reasoner.getEquivalentClasses(exp).getEntities());

				// TODO check if this is correct
				// The assumption is the following: If a concept has only top as a super
				// concept, then a dedicated element for this class is not needed

//				* if (superClasses.size() == 1 && superClasses.contains(owlTools.getOWLTop())) {
//				* 	notNeeded.add(element);
//				*	continue;
//			*	}

			}
//			System.out.println("supper of " + element);
//			System.out.println(tmp);

			element.addTypes(tmp);
			// TODO add an option to choose optimised model, also remove unreachable element
			// that result of optimisation.
			// element.addTypes(filterEdges(tmp));
		});

		// * elements.removeAll(notNeeded);
	}

	/**
	 * filter artificial types that correspond to existential restrictions which are
	 * already satisfied by other types of the same element
	 * 
	 * @param types
	 * @return
	 */
	private Set<OWLClassExpression> filterEdges(Set<OWLClassExpression> types) {
		Set<OWLClassExpression> toRemove = new HashSet<>(), result = new HashSet<>();
		OWLAxiom testAxiom;
		TautologyChecker tChecker;
		for (OWLClassExpression exp1 : types) {
			for (OWLClassExpression exp2 : types) {
				testAxiom = owlTools.getOWLSubClassOfAxiom(getRestrictionFromAlias(exp1),
						getRestrictionFromAlias(exp2));
				if (!exp1.equals(exp2) && !exp2.equals(owlTools.getOWLTop())) {
					tChecker = new TautologyChecker(testAxiom);
					if (tChecker.isTautology())
						toRemove.add(exp2);
				}
			}
		}
		for (OWLClassExpression exp : types) {
			if (!toRemove.contains(exp))
				result.add(exp);
		}
		return result;
	}

	private OWLClassExpression getRestrictionFromAlias(OWLClassExpression alias) {
		if (this.mapper.getRestrictionMapper().getClass2Restriction().containsKey(alias))
			return this.mapper.getRestrictionMapper().getClass2Restriction().get(alias);
		return alias;
	}

	/**
	 * Return a set of elements, where for each satisfiable concept name (in the
	 * TBox or artificial) a corresponding element is created
	 * 
	 * @param map
	 * @param reasoner
	 * @return
	 */
	private Set<Element> createElements(OWLReasoner reasoner) {
		Set<Element> res = new HashSet<>();

		ontology.getClassesInSignature().forEach(cls -> {
			Element e;
			if (!(cls.isOWLNothing()) && reasoner.isSatisfiable(cls)) {
				if (mapper.getClassRepresentatives().containsKey(cls))
					e = mapper.getClassRepresentatives().get(cls);
				else {
					e = generator.getNextElement();
					mapper.addNewRepresentative(cls, e);
				}
				e.addType(cls);
				res.add(e);
			}
		});

		// Make sure there is a representative for top
		if (!mapper.getClassRepresentatives().containsKey(owlTools.getOWLTop()))
			mapper.addNewRepresentative(owlTools.getOWLTop(), generator.getNextElement());

		return res;
	}

	/**
	 * Classify the ontology and add all inferred axioms to the same ontology
	 * 
	 * @throws OWLOntologyCreationException
	 */
	private ElkReasoner classify() throws OWLOntologyCreationException {

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = (ElkReasoner) reasonerFactory.createReasoner(this.ontology);
		return reasoner;
	}

	/**
	 * Return a mapper of Qualified existential restrictions to new concept names.
	 * For every map entry, create and add an equivalence axiom to the ontology
	 * 
	 * @param subConcepts
	 * @return RestrictionMapper
	 */
	private void createMappersAndAxioms(Set<OWLClassExpression> subConcepts) {

		RestrictionMapper resMapper = mapper.getRestrictionMapper();
		ConjunctionMapper conjMapper = mapper.getConjunctionMapper();

		subConcepts.forEach(concept -> {
			if (concept instanceof OWLClass)
				return;

			OWLClass cls;
			OWLEquivalentClassesAxiom newAxiom = null;
			if (concept instanceof OWLObjectSomeValuesFrom) {
				OWLObjectSomeValuesFrom qer = (OWLObjectSomeValuesFrom) concept;

				if (!resMapper.getRestriction2Class().keySet().contains(qer)) {
					cls = generator.getNextConceptName();
					resMapper.addEntry(cls, qer);

					newAxiom = owlTools.getOWLEquivalenceAxiom(cls, qer);
				}
			} else if (concept instanceof OWLObjectIntersectionOf) {
				OWLObjectIntersectionOf conjunction = (OWLObjectIntersectionOf) concept;

				if (!conjMapper.getConjunction2Class().keySet().contains(conjunction)) {
					cls = generator.getNextConceptName();
					conjMapper.addEntry(cls, conjunction);

					newAxiom = owlTools.getOWLEquivalenceAxiom(cls, conjunction);
				}
			}
			addAxiom(newAxiom);
		});
	}

	/**
	 * Return a set of all sub-concepts in the ontology
	 * 
	 * @return{@code Set<OWLClassExpression>}
	 */
	private Set<OWLClassExpression> getAllSubConcepts() {

		Set<OWLClassExpression> res = new HashSet<>();

		this.ontology.getAxioms().stream().forEach(axiom -> {
			owlTools.getAsSubClassOf(axiom).forEach(subClassOf -> {
				res.addAll(owlTools.getSubConcepts(subClassOf.getSubClass()));
				res.addAll(owlTools.getSubConcepts(subClassOf.getSuperClass()));
			});
		});
		return res;
	}

	private void addAxiom(OWLAxiom axiom) {
		if (axiom != null)
			try {
				owlTools.addAxiom(axiom, this.ontology);
//				this.addedAxioms.add(axiom);
			} catch (OWLOntologyStorageException e) {
				e.printStackTrace();
			}
	}

	private void removePreviouslyAddedAxioms() {

		this.originalOntology.getAxioms().stream().forEach(axiom -> {
			if (!this.originalAxioms.contains(axiom)) {
				try {
					owlTools.removeAxiom(axiom, this.originalOntology);
				} catch (OWLOntologyStorageException e) {
					e.printStackTrace();
				}
			}
		});
	}
}
