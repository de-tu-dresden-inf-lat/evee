package de.tu_dresden.inf.lat.model.data;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectInverseOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;

import com.github.jsonldjava.shaded.com.google.common.collect.Sets;

import de.tu_dresden.inf.lat.model.interfaces.IModel;
import de.tu_dresden.inf.lat.model.interfaces.IModelComponent;
import de.tu_dresden.inf.lat.model.tools.GeneralTools;

/**
 * @author Christian Alrabbaa
 *
 */
public class ElkModel implements IModel {
	private static final Logger logger = Logger.getLogger(ElkModel.class);
//	private static final ToOWLTools owlTools = ToOWLTools.getInstance();

	private final Set<Element> rawModelElements;
	private final Set<Element> finalizedModelElements;
	private final Mapper mapper;
	private final Map<Element, Element> raw2Finalized;

	private final Map<Element, Set<EntryPair<OWLClassExpression, Relation>>> elementTypeToEdge;
	private final Map<Element, Set<EntryPair<OWLClassExpression, Relation>>> elementUpdatedTypeToEdge;
	private final Map<Element, Set<EntryPair<OWLClassExpression, OWLClassExpression>>> elementLabelToNewConcept;

//	private final Map<OWLClassExpression, OWLClassExpression> OriginalLabel2NewExpression;
//	private final Set<OWLClass> loopLabels;

	public ElkModel(Set<Element> modelElements, Mapper mapper) {
		this.mapper = mapper;
		this.rawModelElements = modelElements;
		this.finalizedModelElements = new HashSet<>();
		this.raw2Finalized = new HashMap<>();

		this.elementTypeToEdge = new HashMap<>();
		this.elementUpdatedTypeToEdge = new HashMap<>();

		this.elementLabelToNewConcept = new HashMap<>();

//		this.OriginalLabel2NewExpression = new HashMap<>();
//		this.loopLabels = new HashSet<>();

		Element newE;
		for (Element e : modelElements) {
			newE = new Element(e.getName());
			newE.addTypes(e.getTypes());
			newE.addRelations(e.getRelations());
			this.finalizedModelElements.add(newE);
			this.raw2Finalized.put(e, newE);
		}

		finalizeModelElements();
	}

	public Set<Element> getRawModelElements() {
		return rawModelElements;
	}

	@Override
	public Set<Element> getFinalizedModelElements() {
		return finalizedModelElements;
	}

	public Mapper getMapper() {
		return mapper;
	}

	public Element getFinilizedElement(Element rawElement) {
		return this.raw2Finalized.get(rawElement);
	}

	public Map<Element, Set<EntryPair<OWLClassExpression, Relation>>> getElementTypeToEdgeMap() {
		return elementTypeToEdge;
	}

	public Map<Element, Set<EntryPair<OWLClassExpression, Relation>>> getElementUpdatedTypeToEdgeMap() {
		return elementUpdatedTypeToEdge;
	}

	public Map<Element, Set<EntryPair<OWLClassExpression, OWLClassExpression>>> getElementLabelToNewConceptMap() {
		return elementLabelToNewConcept;
	}

//	public Map<OWLClassExpression, OWLClassExpression> getOriginalLabel2NewExpression() {
//		return OriginalLabel2NewExpression;
//	}
//
//	public Set<OWLClass> getLoopLabels() {
//		return loopLabels;
//	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((finalizedModelElements == null) ? 0 : finalizedModelElements.hashCode());
		result = prime * result + ((mapper == null) ? 0 : mapper.hashCode());
		result = prime * result + ((raw2Finalized == null) ? 0 : raw2Finalized.hashCode());
		result = prime * result + ((rawModelElements == null) ? 0 : rawModelElements.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ElkModel other = (ElkModel) obj;
		if (finalizedModelElements == null) {
			if (other.finalizedModelElements != null)
				return false;
		} else if (!finalizedModelElements.equals(other.finalizedModelElements))
			return false;
		if (mapper == null) {
			if (other.mapper != null)
				return false;
		} else if (!mapper.equals(other.mapper))
			return false;
		if (raw2Finalized == null) {
			if (other.raw2Finalized != null)
				return false;
		} else if (!raw2Finalized.equals(other.raw2Finalized))
			return false;
		if (rawModelElements == null) {
			if (other.rawModelElements != null)
				return false;
		} else if (!rawModelElements.equals(other.rawModelElements))
			return false;
		return true;
	}

	/**
	 * These functions are responsible for translating the artificial concept names
	 * that refer to existential restrictions
	 */

	public Element finalizeElement(Element rawElement) {
		Set<Element> res = Sets.newHashSet(rawElement);

		UpdateElements(res);

		res.forEach(element -> element.removeTypes(mapper));

		return res.iterator().next();
	}

	private void finalizeModelElements() {
		Instant start = Instant.now(), finish;

		// create relations and add them to the corresponding elements
		logger.info("Instantiating relations");
		start = Instant.now();

		UpdateElements(this.finalizedModelElements);

		// Introduce new types to describe paths
		// Example: A->Er.C, C->Er.D. The map contains {A1->Er.C, A2->Er.D}, add the
		// following A3->Er.Er.D to the map, and A3 to the types of rep. A
//		ExtendTypes();

		// remove artificial concept names (for restrictions and LHS of the conclusion)

//		this.finalizedModelElements.forEach(System.out::println);
//		System.exit(0);

		finalizedModelElements.forEach(element -> element.removeTypes(mapper));

		finish = Instant.now();
		logger.info(GeneralTools.getDuration(start, finish));

	}

	private void UpdateElements(Set<Element> elements) {
		RestrictionMapper restrictionsMap = this.mapper.getRestrictionMapper();
		ConjunctionMapper conjunctionMapper = this.mapper.getConjunctionMapper();

		elements.forEach(element_1 -> {
			this.elementTypeToEdge.put(element_1, new HashSet<>());
			this.elementUpdatedTypeToEdge.put(element_1, new HashSet<>());
			this.elementLabelToNewConcept.put(element_1, new HashSet<>());
			element_1.getTypes().forEach(type -> {
				// index 0 original Direction,
				// index 1 opposite Direction
				// index 2 the filler.
				Object[] data = new Object[] { null, null, null };
				Relation r1, r2;

//				Set<OWLObjectSomeValuesFrom> expressions = new HashSet<>();
//
//				if (restrictionsMap.getClass2Restriction().containsKey(type)) {
//					expressions.add((restrictionsMap.getClass2Restriction().get(type)));
//				} else if ((conjunctionMapper.getClass2Conjunction().containsKey(type))) {
//					System.out.println("hrererere");
//					(conjunctionMapper.getClass2Conjunction().get(type)).asConjunctSet().stream().forEach(x -> {
//						System.out.println(x);
//						// expressions.add((OWLObjectSomeValuesFrom) x);
//					});
//					System.out.println("->");
//					System.out.println(expressions);
//				}

				if (restrictionsMap.getClass2Restriction().containsKey(type)) {
					OWLObjectSomeValuesFrom qer = restrictionsMap.getClass2Restriction().get(type);
//				for (OWLObjectSomeValuesFrom qer : expressions) {
					OWLObjectProperty prop = prepareRelation(qer, data);

					Element element_2 = getElementFrom(this.mapper.getRepresentativeOf((OWLClassExpression) data[2]),
							elements);

//					Removed it because it was causing problems and there is no advantage of it
//					if (element_2.equals(element_1))
//						data[0] = data[1] = true;

					r1 = new Relation(prop, element_1, element_2, (RelationDirection) data[0]);
					r2 = new Relation(prop, element_1, element_2, (RelationDirection) data[1]);

					element_1.addRelation(r1);
					element_2.addRelation(r2);

					EntryPair<OWLClassExpression, Relation> ep = new EntryPair<>(type, r1);
					if (this.elementTypeToEdge.containsKey(element_1)) {
						this.elementTypeToEdge.get(element_1).add(ep);
						this.elementUpdatedTypeToEdge.get(element_1).add(new EntryPair<>(type, r1));
					} else {
						this.elementTypeToEdge.put(element_1, new HashSet<>(Arrays.asList(ep)));
						this.elementUpdatedTypeToEdge.put(element_1,
								new HashSet<>(Arrays.asList(new EntryPair<>(type, r1))));
					}
				}
			});
		});

	}

	private Element getElementFrom(Element toFind, Set<Element> elements) {
		return elements.stream().filter(x -> x.equals(toFind)).collect(Collectors.toList()).get(0);
	}

	private OWLObjectProperty prepareRelation(OWLClassExpression type, Object[] data) {

		if (type instanceof OWLObjectSomeValuesFrom) {
			OWLObjectSomeValuesFrom qer = (OWLObjectSomeValuesFrom) type;
			OWLObjectPropertyExpression proExp = qer.getProperty();

			data[2] = qer.getFiller();

			if (proExp instanceof OWLObjectProperty) {
				data[0] = RelationDirection.Forward;
				data[1] = RelationDirection.Backward;
				return ((OWLObjectProperty) proExp);
			}
			if (proExp instanceof OWLObjectInverseOf) {
				data[0] = RelationDirection.Backward;
				data[1] = RelationDirection.Forward;
				return ((OWLObjectInverseOf) proExp).getNamedProperty();
			}
		}

		assert false : "It should not be something other than OWLObjectSomeValuesFrom, but it was "
				+ type.getClass().getSimpleName() + " -> " + type;
		return null;

	}

	/**
	 * Return a set of IModelComponents that corresponds to the input concept in the
	 * current Model
	 * 
	 * @param concept
	 * @return
	 */
	public Set<IModelComponent> getConceptModel(OWLClassExpression concept) {
		Set<IModelComponent> result = new HashSet<>();

		getConceptModel(concept, result);

		return result;
	}

	/**
	 * Fill the input set of IModelComponents
	 * 
	 * @param concept
	 * @param components
	 */
	private void getConceptModel(OWLClassExpression concept, Set<IModelComponent> components) {
		components.add(this.getFinilizedElement(this.mapper.getRepresentativeOf(concept)));

		if (concept instanceof OWLObjectIntersectionOf) {
			concept.asConjunctSet().forEach(x -> {
				if (x instanceof OWLObjectSomeValuesFrom)
					getConceptModel(x, components);
			});
		}

		else if (concept instanceof OWLObjectSomeValuesFrom) {
			OWLObjectSomeValuesFrom exsRes = (OWLObjectSomeValuesFrom) concept;
			Element finalizedRep = this.getFinilizedElement(this.mapper.getRepresentativeOf(exsRes));

			getConceptModel(exsRes.getFiller(), components);

			Element finalizedFillerRepresentative = this
					.getFinilizedElement(this.mapper.getRepresentativeOf(exsRes.getFiller()));

			if (finalizedFillerRepresentative != null) {
				finalizedRep.getRelations().stream().filter(r -> r.getElement2().equals(finalizedFillerRepresentative))
						.forEach(components::add);
			}

		}

		else if (!(concept instanceof OWLClass))
			assert false : "unexpected type of objects " + concept;

	}

}
