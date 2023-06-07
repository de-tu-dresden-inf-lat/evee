package de.tu_dresden.inf.lat.counterExample;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;

import de.tu_dresden.inf.lat.model.data.Element;
import de.tu_dresden.inf.lat.model.data.ElkModel;
import de.tu_dresden.inf.lat.model.data.EntryPair;
import de.tu_dresden.inf.lat.model.data.Relation;
import de.tu_dresden.inf.lat.model.tools.ObjectGenerator;
import de.tu_dresden.inf.lat.model.tools.ToOWLTools;

/**
 * @author Christian Alrabbaa
 *
 *         The purpose of this class is to extend the elements of a coarse model
 *         with new types that describe concepts corresponding to paths. These
 *         new labels are needed for the ModelRefiner to remove redundancy from
 *         the coarse model
 */
public class TypeExtender {

	private static final Logger logger = Logger.getLogger(TypeExtender.class);
	private static final ToOWLTools owlTools = ToOWLTools.getInstance();
	private static final ObjectGenerator generator = ObjectGenerator.getInstance();

	private final ElkModel model;
	private final Set<Element> coarseModel;

	public TypeExtender(ElkModel model, Set<Element> coarseModel) {
		this.model = model;
		this.coarseModel = coarseModel;
	}

	/**
	 * extend the types of every element with fresh types that describe all paths
	 * starting from that element
	 * 
	 * @param elements
	 */
	public void ExtendTypes(Map<Element, Set<OWLClassExpression>> elementsToRemovedTypes) {
//		System.out.println("====inTypeExtender====");
//		elementsToRemovedTypes.entrySet().forEach(x -> {
//			System.out.println("from " + x.getKey());
//			elementsToRemovedTypes.get(x.getKey()).forEach(System.out::println);
//		});
//		model.getElementTypeToEdgeMap().entrySet().forEach(System.out::println);
//		System.out.println("----");
//		model.getElementUpdatedTypeToEdgeMap().entrySet().forEach(System.out::println);
//		model.getElementLabelToNewConceptMap().entrySet().forEach(System.out::println);
//		System.out.println(elementsToRemovedTypes.entrySet());
//		System.out.println("========");
		int maxNestingDepth = getMaxNestingDepthFromRHSRep();// getMaxNestingDepth();
//		System.out.println("Max Nesting -> " + maxNestingDepth);

		OWLClassExpression originalExpression;
		Set<OWLObjectSomeValuesFrom> newTypeExpressions = new HashSet<>();
		OWLObjectSomeValuesFrom newTypeExpression;
		Relation correspondingRelation;
		Map<Element, Set<EntryPair<OWLClass, OWLObjectSomeValuesFrom>>> tmpRestrictionsMap = new HashMap<>();
		Map<OWLObjectSomeValuesFrom, OWLClass> newExpressionToAlias = new HashMap<>();

		for (Element e : this.coarseModel) {
			// TODO the extension happens after finding the relevant part, so we can't use
			// the original types, we should use the remaining ones only
			Element rawE = getElementFrom(e, this.model.getRawModelElements());
			tmpRestrictionsMap.put(e, new HashSet<>());
//			newTypeExpressions = new HashSet<>();
			for (OWLClassExpression t : rawE.getTypes()) {
//				System.out.println("coarse element -> " + e);
//				System.out.println("Raw element -> " + rawE);
//				System.out.println("Type -> " + t);
				if (getConceptFromAlias(t).equals(t) && !e.getTypes().contains(t)) {
//					System.out.println("at element " + e);
//					System.out.println("Skipping " + t + " because it was removed as not relevant");
					continue;
				}
//
				Set<OWLClassExpression> modifiedTypeSinglton = this.model.getElementLabelToNewConceptMap().get(e)
						.stream().filter(x -> x.getFirstArg().equals(t)).map(EntryPair::getSecondArg)
						.collect(Collectors.toSet());

				if (!modifiedTypeSinglton.isEmpty()) {
					assert modifiedTypeSinglton.size() == 1 : "it should exactly be one";
					originalExpression = modifiedTypeSinglton.iterator().next();
				} else
					originalExpression = getConceptFromAlias(t);

				if (!(originalExpression instanceof OWLObjectSomeValuesFrom))
					continue;

				// If there is a type that refers to a removed edge, it should be ignored
				boolean cont = true;
				for (Relation r : this.model.getElementTypeToEdgeMap().get(e).stream()
						.filter(x -> x.getFirstArg().equals(t)).map(EntryPair::getSecondArg)
						.collect(Collectors.toSet())) {

					if (e.getRelations().contains(r)) {
						cont = false;
						break;
					}

				}
				if (cont)
					continue;

				OWLObjectProperty correspondingRole = ((OWLObjectSomeValuesFrom) originalExpression).getProperty()
						.getNamedProperty();
				OWLClassExpression correspondingConceptAlias = getAliasFromConcept(
						((OWLObjectSomeValuesFrom) originalExpression).getFiller());

				correspondingRelation = getElementFrom(e, this.model.getFinalizedModelElements()).getRelations()
						.stream()
						.filter(x -> x.getRoleName().equals(correspondingRole) && x.getElement2().equals(
								this.model.getMapper().getClassRepresentatives().get(correspondingConceptAlias)))
						.collect(Collectors.toList()).get(0);

				Map<Element, Integer> ocurrences = new HashMap<>();
				newTypeExpression = (OWLObjectSomeValuesFrom) getNewTypeExpression(originalExpression, maxNestingDepth,
						elementsToRemovedTypes, 0, ocurrences);
//				System.out.println("Original Expression -> " + originalExpression);
//				System.out.println("New Expression -> " + newTypeExpression);

				if (newTypeExpressions.contains(newTypeExpression)) {
					tmpRestrictionsMap.get(e).add(new EntryPair<OWLClass, OWLObjectSomeValuesFrom>(
							newExpressionToAlias.get(newTypeExpression), newTypeExpression));
					this.model.getElementTypeToEdgeMap().get(e)
							.add(new EntryPair<>(newTypeExpression, correspondingRelation));
					this.model.getElementUpdatedTypeToEdgeMap().get(e)
							.add(new EntryPair<>(newTypeExpression, correspondingRelation));
				} else {

					newTypeExpressions.add(newTypeExpression);
					OWLClass alias = generator.getNextConceptName();

					newExpressionToAlias.put(newTypeExpression, alias);

					this.model.getMapper().getRestrictionMapper().addEntry(alias, newTypeExpression);

					tmpRestrictionsMap.get(e)
							.add(new EntryPair<OWLClass, OWLObjectSomeValuesFrom>(alias, newTypeExpression));

//					this.model.getOriginalLabel2NewExpression().put(correspondingConceptAlias, alias);
//					checkIfThereIsALoop((OWLClass) correspondingConceptAlias, newTypeExpression);

//					System.out.println("~~~~~~~~~~~~");
//					System.out.println("At " + e.getName() + "The concept "
//							+ getConceptFromAlias(correspondingConceptAlias) + " (" + correspondingConceptAlias + ") "
//							+ " leads to " + SimpleOWLFormatter.format(newTypeExpression));

					this.model.getElementTypeToEdgeMap().get(e)
							.add(new EntryPair<>(newTypeExpression, correspondingRelation));
					this.model.getElementUpdatedTypeToEdgeMap().get(e)
							.add(new EntryPair<>(newTypeExpression, correspondingRelation));

				}

//				this.OriginalLabel2NewExpression.put(correspondingConceptAlias, alias);
//				checkIfThereIsALoop((OWLClass) correspondingConceptAlias, newTypeExpression);
//
//				System.out.println("At " + e.getName() + "The concept " + getConceptFromAlias(correspondingConceptAlias)
//						+ " (" + correspondingConceptAlias + ") " + " leads to "
//						+ SimpleOWLFormatter.format(newTypeExpression));
//
//				this.elementLabelToEdge.get(e).add(new EntryPair<>(newTypeExpression, correspondingRelation));
			}
//			newTypeExpressions.forEach(restriction -> {
//				OWLClass alias = generator.getNextConceptName();
//				tmpRestrictionsMap.put(e, new EntryPair<OWLClass, OWLObjectSomeValuesFrom>(alias, restriction));
//			});
		}

//		System.out.println("new expressions");
		tmpRestrictionsMap.keySet().forEach(x -> {
			tmpRestrictionsMap.get(x).forEach(y -> {
//				System.out.println("adding to " + x.getName() + ": " + y.getFirstArg() + " -> "
//						+ SimpleOWLFormatter.format(y.getSecondArg()));
				x.addType(y.getFirstArg());
				/* getElementFrom(x, this.model.getFinalizedModelElements()) */
				x.addType(y.getFirstArg());
				// this.model.getMapper().getRestrictionMapper().addEntry(x.getValue().getFirstArg(),
				// x.getValue().getSecondArg());
			});
		});

//		System.out.println("loop labels");
//		this.model.getLoopLabels().forEach(System.out::println);
	}

	private OWLClassExpression getNewTypeExpression(OWLClassExpression expression, int maxNestingDepth,
			Map<Element, Set<OWLClassExpression>> elementsToRemovedTypes, int counter,
			Map<Element, Integer> representativesOccurencesCounters) {
		Set<OWLClassExpression> newTypeExpressions = new HashSet<>();
		OWLClassExpression filler, cToUse;
		Element representative;

		counter++;

		if (maxNestingDepth <= 0)
			return expression;

		if (expression instanceof OWLObjectSomeValuesFrom) {
			filler = ((OWLObjectSomeValuesFrom) expression).getFiller();

//			System.out.println("Filler " + filler);

			representative = this.model.getMapper().getClassRepresentatives().get(getAliasFromConcept(filler));

			if (representativesOccurencesCounters.containsKey(representative))
				representativesOccurencesCounters.put(representative,
						representativesOccurencesCounters.get(representative) + 1);
			else
				representativesOccurencesCounters.put(representative, 1);

//			System.out.println("representative = " + representative);
//			System.out.println("Depth " + maxNestingDepth);
			maxNestingDepth = maxNestingDepth - 1;
			if (representativesOccurencesCounters.get(representative) <= counter) {

				for (OWLClassExpression tToUse : representative.getTypes()) {

//					System.out.println("going deeper with " + tToUse);
//					System.out.println("Concept from " + tToUse + " = " + getConceptFromAlias(tToUse));

//					if (maxNestingDepth <= 0) {
//						System.out.println("AAAAAAAAAAAAAA");
//						break;
//					}

					cToUse = filterRemovedConceptName(tToUse, representative, elementsToRemovedTypes);

//					System.out.println("tTOUse = " + tToUse);
//					System.out.println("cTOUse = " + cToUse);

//					if (elementsToRemovedTypes.get(representative).contains(tToUse)) {
//						System.out.println(expression);
//						System.out.println(representative);
////						tToUse = this.model.getElementLabelToNewConceptMap().get(representative).stream()
////								.filter(x -> x.getFirstArg().equals(tToUse)).map(EntryPair::getSecondArg)
////								.collect(Collectors.toSet()).iterator().next();
//						System.out.println("BBBBBBBBBBBBBB");
//						continue;
//					}
//					if (newTypeExpressions.contains(getConceptFromAlias(cToUse))) {
//						System.out.println("CCCCCCCCCCC");
//						continue;
//					}
					if (!cToUse.equals(owlTools.getOWLTop())) {
						// This is wrong because A<=Er.A
//						if (getConceptFromAlias(tToUse).equals(tToUse))
//							continue;
						newTypeExpressions.add(getNewTypeExpression(cToUse, maxNestingDepth, elementsToRemovedTypes,
								counter, representativesOccurencesCounters));
					}

				}
			}

//			if (newTypeExpressions.size() == 0) {
//				representative = this.model.getMapper().getClassRepresentatives().get(getAliasFromConcept(expression));
//				System.out.println("Expression -> " + expression);
//				System.out.println("rep -> " + representative);
//				elementsToRemovedTypes.entrySet().forEach(System.out::println);
//				for (OWLClassExpression t : representative.getTypes()) {
//					if (!elementsToRemovedTypes.get(representative).contains(t))
//						newTypeExpressions.add(t);
//				}
//			}

			if (newTypeExpressions.size() == 0 && expression instanceof OWLObjectSomeValuesFrom) {
//				System.out.println("Expression " + expression);
//				System.out.println("Leads to TOP");
				newTypeExpressions.add(owlTools.getOWLTop());
			} else if (newTypeExpressions.size() == 0 && !(expression instanceof OWLObjectSomeValuesFrom)) {
//				System.out.println("Expression " + expression);
//				System.out.println("Leads to " + expression);
				OWLClassExpression lastExp = filterRemovedConceptName(expression, representative,
						elementsToRemovedTypes);
				if (lastExp instanceof OWLObjectSomeValuesFrom)
					newTypeExpressions.addAll(expression.asConjunctSet());
				else
					newTypeExpressions.add(expression);
			}

			if (newTypeExpressions.size() == 1) {
//				System.out.println("Expression " + expression);
//				System.out.println("Leads to " + SimpleOWLFormatter.format(owlTools.getOWLExistentialRestriction(
//						((OWLObjectSomeValuesFrom) expression).getProperty(), newTypeExpressions.iterator().next())));
				return owlTools.getOWLExistentialRestriction(((OWLObjectSomeValuesFrom) expression).getProperty(),
						newTypeExpressions.iterator().next());
			}

//			System.out.println("Expression " + expression);
//			System.out.println("Leads to " + SimpleOWLFormatter
//					.format(owlTools.getOWLExistentialRestriction(((OWLObjectSomeValuesFrom) expression).getProperty(),
//							owlTools.getOWLConjunction(newTypeExpressions))));
			return owlTools.getOWLExistentialRestriction(((OWLObjectSomeValuesFrom) expression).getProperty(),
					owlTools.getOWLConjunction(newTypeExpressions));
		}

		return expression;
	}

	private OWLClassExpression filterRemovedConceptName(OWLClassExpression tToUse, Element representative,
			Map<Element, Set<OWLClassExpression>> elementsToRemovedTypes) {
		OWLClassExpression cToUse = getConceptFromAlias(tToUse);

//		if (cToUse instanceof OWLClass) {
//			System.out.println("HERE");
//			System.out.println(representative);
		if (elementsToRemovedTypes.containsKey(representative))
			if (elementsToRemovedTypes.get(representative).contains(cToUse)
					|| elementsToRemovedTypes.get(representative).contains(tToUse))
				return owlTools.getOWLTop();
//		}

		if (cToUse instanceof OWLObjectIntersectionOf) {
			Set<OWLClassExpression> filteredConjuncts = new HashSet<>();
			for (OWLClassExpression exp : cToUse.asConjunctSet()) {
				if (exp instanceof OWLClass) {
					if (!elementsToRemovedTypes.get(representative).contains(exp))
						filteredConjuncts.add(exp);
				}
				if (exp instanceof OWLObjectSomeValuesFrom) {
					OWLClassExpression filler = ((OWLObjectSomeValuesFrom) exp).getFiller();
					OWLObjectPropertyExpression property = ((OWLObjectSomeValuesFrom) exp).getProperty();

					return owlTools.getOWLExistentialRestriction(property,
							filterRemovedConceptName(filler,
									this.model.getMapper().getRepresentativeOf(getAliasFromConcept(filler)),
									elementsToRemovedTypes));

//					if (!elementsToRemovedTypes.get(representative).contains(exp))
//						filteredConjuncts.add(exp);
				}
				// assumption is that if a conjunct is an existential restriction, its filler
				// will be filtered later when the representative of that filler is being used
				else
					filteredConjuncts.add(exp);
			}

			if (filteredConjuncts.isEmpty())
				return owlTools.getOWLTop();

			if (filteredConjuncts.size() == 1)
				return filteredConjuncts.iterator().next();

			return owlTools.getOWLConjunction(filteredConjuncts);
		}

		return cToUse;

	}

	/**
	 * Return the length of the longest path in the model that starts from the RHS
	 * representative
	 * 
	 * @return
	 */
	private int getMaxNestingDepthFromRHSRep() {
		int max = 0;
		Set<Relation> processed;
		processed = new HashSet<>();

		Element representative = this.model.getMapper().getRHSRepresentativeElement();

		if (representative != null) {
			max = getMaxPathLength(this.model.getFinilizedElement(representative), 0, processed);
		}
		return max;
	}

	/**
	 * Return the length of the longest path in the model
	 * 
	 * @return
	 */
	private int getMaxNestingDepth() {
		int max = 0, tmp;
		Set<Relation> processed;
		for (Element e : this.model.getFinalizedModelElements()) {
			processed = new HashSet<>();
			tmp = getMaxPathLength(e, 0, processed);
			if (tmp > max)
				max = tmp;
		}
		return max;
	}

	/**
	 * Return the length of the longest path starting with the input element
	 * 
	 * @param element
	 * @param length
	 * @param processed
	 * @return
	 */
	private int getMaxPathLength(Element element, int length, Set<Relation> processed) {
		int max = length, tmp;
		for (Relation r : element.getRelations()) {
			if (r.isForward()) {
				if (processed.contains(r))
					continue;
				processed.add(r);
				tmp = getMaxPathLength(r.getElement2(), length + 1, processed);
				if (tmp > max)
					max = tmp;
			}
		}
		return max;
	}

	/**
	 * Return the OWLClassExpression mapped to the input alias
	 * 
	 * @param alias
	 * @return
	 */
	private OWLClassExpression getConceptFromAlias(OWLClassExpression alias) {
		OWLClassExpression res = getRestrictionFromAlias(alias);

		if (!res.equals(alias)) {
			OWLObjectSomeValuesFrom restriction = (OWLObjectSomeValuesFrom) this.model.getMapper()
					.getRestrictionMapper().getClass2Restriction().get(alias);
			OWLClassExpression filler = restriction.getFiller();

			return owlTools.getOWLExistentialRestriction(restriction.getProperty(), getConceptFromAlias(filler));
		}

		res = getConjunctionFromAlias(alias);
		if (!res.equals(alias)) {
			Set<OWLClassExpression> conjuncts = this.model.getMapper().getConjunctionMapper().getClass2Conjunction()
					.get(alias).asConjunctSet();
			Set<OWLClassExpression> newConjuncts = new HashSet<>();
			for (OWLClassExpression conjunct : conjuncts) {
				newConjuncts.add(getConceptFromAlias(conjunct));
			}
			return owlTools.getOWLConjunction(newConjuncts);
		}

		return res;
	}

	/**
	 * Return the alias mapped to the input OWLClassExpression
	 * 
	 * @param expression
	 * @return
	 */
	private OWLClassExpression getAliasFromConcept(OWLClassExpression expression) {
		OWLClassExpression res = getAliasFromRestriction(expression);
		if (!res.equals(expression))
			return res;

		res = getAliasFromConjunction(expression);
		if (!res.equals(expression))
			return res;

		return expression;
	}

	private OWLClassExpression getRestrictionFromAlias(OWLClassExpression alias) {
		if (this.model.getMapper().getRestrictionMapper().getClass2Restriction().containsKey(alias))
			return this.model.getMapper().getRestrictionMapper().getClass2Restriction().get(alias);
		return alias;
	}

	private OWLClassExpression getAliasFromRestriction(OWLClassExpression alias) {
		if (this.model.getMapper().getRestrictionMapper().getRestriction2Class().containsKey(alias))
			return this.model.getMapper().getRestrictionMapper().getRestriction2Class().get(alias);
		return alias;
	}

	private OWLClassExpression getConjunctionFromAlias(OWLClassExpression alias) {
		if (this.model.getMapper().getConjunctionMapper().getClass2Conjunction().containsKey(alias))
			return this.model.getMapper().getConjunctionMapper().getClass2Conjunction().get(alias);
		return alias;
	}

	private OWLClassExpression getAliasFromConjunction(OWLClassExpression alias) {
		if (this.model.getMapper().getConjunctionMapper().getConjunction2Class().containsKey(alias))
			return this.model.getMapper().getConjunctionMapper().getConjunction2Class().get(alias);
		return alias;
	}

	private Element getElementFrom(Element toFind, Set<Element> elements) {
		return elements.stream().filter(x -> x.equals(toFind)).collect(Collectors.toList()).get(0);
	}
}
