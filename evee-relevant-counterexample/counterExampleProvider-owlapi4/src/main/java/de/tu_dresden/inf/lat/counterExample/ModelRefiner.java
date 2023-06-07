package de.tu_dresden.inf.lat.counterExample;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import de.tu_dresden.inf.lat.counterExample.data.CommonElements;
import de.tu_dresden.inf.lat.counterExample.data.ModelType;
import de.tu_dresden.inf.lat.counterExample.data.RelationReasons;
import org.apache.log4j.Logger;
import org.semanticweb.elk.owlapi.ElkReasoner;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import com.github.jsonldjava.shaded.com.google.common.collect.Sets;

import de.tu_dresden.inf.lat.counterExample.data.Loop;
import de.tu_dresden.inf.lat.counterExample.relevantExamplesGenerators.RelevantCounterExample;
import de.tu_dresden.inf.lat.model.data.Element;
import de.tu_dresden.inf.lat.model.data.ElkModel;
import de.tu_dresden.inf.lat.model.data.EntryPair;
import de.tu_dresden.inf.lat.model.data.Mapper;
import de.tu_dresden.inf.lat.model.data.Relation;
import de.tu_dresden.inf.lat.model.tools.ToOWLTools;

/**
 * @author Christian Alrabbaa
 *
 */
public class ModelRefiner {
	private Set<Relation> set;

	private static final Logger logger = Logger.getLogger(RelevantCounterExample.class);

//	private final OWLOntology ontology;
	private static final ToOWLTools owlTools = ToOWLTools.getInstance();
	private final ElkReasoner reasoner;

	public ModelRefiner(OWLOntology ontology) {
		// TODO improvement! the ontology should only contain declaration statements
//		this.ontology = ontology;
		reasoner = (ElkReasoner) new ElkReasonerFactory().createReasoner(ontology);
	}

//	public void refine(ElkModel model, Set<Element> coarseModel) throws OWLOntologyCreationException {
//		refine(model, coarseModel, false);
//	}

	public void refine(RelevantCounterExample generator, Set<Element> coarseModel, ModelType modelType)
			throws OWLOntologyCreationException {

		// TODO start from A and B first

		ElkModel model = generator.getElkModel();
		RelationReasons relationReasons = generator.getRelationReasons();

		CommonElements elementsReachableFromBoth = new CommonElements(coarseModel, model.getMapper(), modelType);

		LoopTracker loopTracker = new LoopTracker(model);

		// This does not work properly, see relevantC example
		// Don't remember why I have this
		if (modelType == ModelType.Diff || modelType == ModelType.FlatDiff)
			directRelationsFilter(model, coarseModel, relationReasons, elementsReachableFromBoth, loopTracker);

		Map<Element, Set<Relation>> toRemove;
		Map<Element, Set<EntryPair<Relation, Relation>>> firstImpliesSecond = new HashMap<>();
		boolean doesOneImplyTwo, doesTwoImplyOne;

//		Map<Element, Set<EntryPair<OWLClassExpression, Relation>>> m = model.getElementUpdatedTypeToEdgeMap();
//		Set<OWLClassExpression> implierConcepts, impliedConcepts;
//		Set<Relation> impliedRelations;

//		System.out.println("====inModelRefiner====");
//		model.getElementTypeToEdgeMap().entrySet().forEach(x -> {
//			System.out.println("In " + x.getKey());
//			model.getElementTypeToEdgeMap().get(x.getKey()).forEach(y -> {
//				System.out.println(" Concept " + SimpleOWLFormatter.format(y.getFirstArg()) + " is assigned to "
//						+ y.getSecondArg());
//			});
//		});
//		System.out.println("(*)(*)(*)(*");
//		model.getElementUpdatedTypeToEdgeMap().entrySet().forEach(x -> {
//			System.out.println("At " + x.getKey());
//			model.getElementUpdatedTypeToEdgeMap().get(x.getKey()).forEach(y -> {
//				System.out.println("Concept " + SimpleOWLFormatter.format(y.getFirstArg()) + " is assigned to "
//						+ y.getSecondArg());
//			});
//		});

		Set<Relation> processedRelations;

		// a pair <e1,e2> means that there are 2 edges (? -> e1), (? -> e2) that imply
		// each other, and we kept the edge that lead to e1
		Set<EntryPair<Element, Element>> TieBreakRecord = new HashSet<>();
		Set<EntryPair<Relation, Relation>> processedPairsOfRelations = new HashSet<>();

		for (Element e : coarseModel) {
//			System.out.println("Processing ----> " + e);
//			impliedRelations = new HashSet<>();
			toRemove = new HashMap<>();

			for (Relation implier : e.getRelations()) {
				if (!implier.isForward())
					continue;
				// This can cause not determinism, the order must not influence the result
//				if (impliedRelations.contains(implier))
//					continue;

//				implierConcepts = m.get(e).stream().filter(x -> x.getSecondArg().equals(implier))
//						.map(EntryPair::getFirstArg).collect(Collectors.toSet());

				for (Relation implied : e.getRelations()) {

					if (implier.equals(implied))
						continue;

					if (!implied.isForward())
						continue;

					EntryPair<Relation, Relation> processedPair = new EntryPair<>(implier, implied);
					if (processedPairsOfRelations.stream()
							.anyMatch(x -> x.getFirstArg().equals(processedPair.getSecondArg())
									&& x.getSecondArg().equals(processedPair.getFirstArg())))
						continue;

//					impliedConcepts = m.get(e).stream().filter(x -> x.getSecondArg().equals(implied))
//							.map(EntryPair::getFirstArg).collect(Collectors.toSet());
//					System.out.println("checking if " + implier + " implies " + implied);

//					System.out.println("checking if " + implierConcepts + " implies " + impliedConcepts);
//					isImplied = checkImplication(new ArrayList<>(implierConcepts), new ArrayList<>(impliedConcepts),
//							model);
//					System.out.println("Result = " + isImplied);

					processedRelations = new HashSet<>();

					doesOneImplyTwo = checkImplication(model, implier, implied);
					doesTwoImplyOne = checkImplication(model, implied, implier);

					if (doesOneImplyTwo && !doesTwoImplyOne) {
//						System.out.println("->");
//						System.out.println(implier);
//						System.out.println(implied);
						if (checkLoops(e, implier, implied, loopTracker)) {
							updateMap(e, new EntryPair<Relation, Relation>(implier, implied), firstImpliesSecond);
//							System.out.println("from " + e);
//							System.out.println("Because of " + implier);
//							System.out.println("i'm removing " + implied);
							updateMap(e, implied, toRemove);

//							impliedRelations.add(implied);

						}

					} else if (!doesOneImplyTwo && doesTwoImplyOne) {
//						System.out.println("<-");
//						System.out.println(implier);
//						System.out.println(implied);
						if (checkLoops(e, implier, implied, loopTracker)) {
							updateMap(e, new EntryPair<Relation, Relation>(implied, implier), firstImpliesSecond);
//							System.out.println("from " + e);
//							System.out.println("Because of " + implier);
//							System.out.println("i'm removing " + implied);
							updateMap(e, implier, toRemove);

//							impliedRelations.add(implied);

						}

					} else if (doesOneImplyTwo && doesTwoImplyOne) {
//						System.out.println("<->");
//						System.out.println(implier);
//						System.out.println(implied);
//						System.out.println("*****************here*************");
						if (checkLoops(e, implier, implied, loopTracker)) {

//							System.out.println("tie record");
//							TieBreakRecord.forEach(System.out::println);

							if (checkCommonStructure(implied, elementsReachableFromBoth, processedRelations)) {
								EntryPair<Element, Element> tiePair = new EntryPair<>(implied.getElement2(),
										implier.getElement2());
								EntryPair<Element, Element> notToUseTiePair = new EntryPair<>(implier.getElement2(),
										implied.getElement2());

								if (!TieBreakRecord.contains(notToUseTiePair)) {
									TieBreakRecord.add(tiePair);
//									System.out.println("Adding tie pair -> " + tiePair);
									updateMap(e, new EntryPair<Relation, Relation>(implied, implier),
											firstImpliesSecond);
//									System.out.println("from " + e);
//									System.out.println("Because of " + implied);
//									System.out.println("i'm removing " + implier);
									updateMap(e, implier, toRemove);
								}
//								else {
//									System.out.println("Adding tie pair -> " + tiePair);
//									updateMap(e, new EntryPair<Relation, Relation>(implier, implied),
//											firstImpliesSecond);
//									System.out.println("from " + e);
//									System.out.println("Because of " + implier);
//									System.out.println("i'm removing " + implied);
//									updateMap(e, implied, toRemove);
//								}
							} else {
//								System.out.println("here9");
								EntryPair<Element, Element> tiePair = new EntryPair<>(implier.getElement2(),
										implied.getElement2());
								EntryPair<Element, Element> notToUseTiePair = new EntryPair<>(implied.getElement2(),
										implier.getElement2());

								if (!TieBreakRecord.contains(notToUseTiePair)) {
									TieBreakRecord.add(tiePair);

									updateMap(e, new EntryPair<Relation, Relation>(implier, implied),
											firstImpliesSecond);
//									System.out.println("from the element " + e);
//									System.out.println("Because of " + implier);
//									System.out.println("i'm removing " + implied);
									updateMap(e, implied, toRemove);
								}
//								else {
//									updateMap(e, new EntryPair<Relation, Relation>(implied, implier),
//											firstImpliesSecond);
//									System.out.println("from " + e);
//									System.out.println("Because of " + implied);
//									System.out.println("i'm removing " + implier);
//									updateMap(e, implier, toRemove);
//								}
							}
						}
					}

				}
			}
			if (toRemove.containsKey(e)) {
				removeRedundantEdges(e, toRemove.get(e), coarseModel, firstImpliesSecond.get(e),
						elementsReachableFromBoth.getElements());
			}
		}

//it was here
		if (modelType == ModelType.Alpha) {
			removeUnreachableElements(coarseModel, model.getMapper().getLHSRepresentativeElement(),
					model.getMapper().getLHSRepresentativeElement());
		} else {
			removeUnreachableElements(coarseModel, model.getMapper().getLHSRepresentativeElement(),
					model.getMapper().getRHSRepresentativeElement());
		}

		for (Element e : coarseModel) {
			for (Relation r : Sets.newHashSet(e.getRelations())) {
				if (!coarseModel.contains(r.getElement2())) {
					e.removeRelation(r);
				}
			}
			e.removeTypes(model.getMapper());
		}

	}

	/**
	 * Remove all edges from LHS representative if they do not satisfy diff or flat
	 * diff
	 * 
	 * @param model
	 * @param coarseModel
	 * @param relationReasons
	 * @param elementsReachableFromBoth
	 * @param loopTracker
	 */
	private void directRelationsFilter(ElkModel model, Set<Element> coarseModel, RelationReasons relationReasons,
			CommonElements elementsReachableFromBoth, LoopTracker loopTracker) {
		Element lHSRep = getElementFrom(model.getMapper().getLHSRepresentativeElement(), coarseModel);
		Element rHSRep = getElementFrom(model.getMapper().getRHSRepresentativeElement(), coarseModel);

		Set<Relation> toRemove = new HashSet<>();

		for (Relation r : lHSRep.getRelations()) {
			Set<OWLClassExpression> conceptsMappedToRelation = model.getElementUpdatedTypeToEdgeMap().get(lHSRep)
					.stream().filter(x -> x.getSecondArg().equals(r))
					.map(x -> getConceptFromAlias(x.getFirstArg(), model)).collect(Collectors.toSet());

			for (OWLClassExpression exp : conceptsMappedToRelation) {
				if (isStronglyImpliedBySomeBConcept(exp, rHSRep.getTypes(), model))
					continue;

				if (isWeaklyImpliedBySomeBConcept(exp, rHSRep.getTypes(), model)) {
					if (isRedundant(r, makeAllTop(exp), lHSRep, model, elementsReachableFromBoth)) {
//						System.out.println("Redundant -> " + r);
						toRemove.add(r);
					}
//					System.out.println("Not redundant -> " + r);
				} else
					toRemove.add(r);
			}
		}

		for (Relation r : toRemove) {
			lHSRep.removeRelation(r);
			Optional<Relation> backRelationOpt = r.getElement2().getRelations().stream()
					.filter(x -> x.getElement1().equals(r.getElement1()) && x.getElement2().equals(r.getElement2())
							&& x.isBackward())
					.findFirst();
			if (backRelationOpt.isPresent()) {
				getElementFrom(r.getElement2(), coarseModel).removeRelation(backRelationOpt.get());
			}
		}
	}

	/**
	 * Check if there is another edge that fulfils the generalised concept
	 * 
	 * @param r
	 * @param generalisedConcept
	 * @param lHSRep
	 * @return
	 * @return
	 */
	private boolean isRedundant(Relation r, OWLClassExpression generalisedConcept, Element lHSRep, ElkModel model,
			CommonElements reachableFromBoth) {
		for (EntryPair<OWLClassExpression, Relation> exp2Rel : model.getElementUpdatedTypeToEdgeMap().get(lHSRep)) {
//			System.out.println("considering " + exp2Rel.getFirstArg());
			if (exp2Rel.getSecondArg().equals(r))
				continue;

			if (!reachableFromBoth.getElements().contains(r.getElement2()))
				continue;

			if (isEntailed(owlTools.getOWLSubClassOfAxiom(getConceptFromAlias(exp2Rel.getFirstArg(), model),
					generalisedConcept)))
				return true;
		}
		return false;

	}

	private boolean isStronglyImpliedBySomeBConcept(OWLClassExpression exp, Set<OWLClassExpression> types,
			ElkModel model) {
		for (OWLClassExpression type : types) {
//			System.out.println("checking -> "
//					+ SimpleOWLFormatter.format(owlTools.getOWLSubClassOfAxiom(getConceptFromAlias(type, model), exp)));
//			System.out.println(
//					"Result -> " + isEntailed(owlTools.getOWLSubClassOfAxiom(getConceptFromAlias(type, model), exp)));
			if (isEntailed(owlTools.getOWLSubClassOfAxiom(getConceptFromAlias(type, model), exp)))
				return true;
		}
		return false;
	}

	private boolean isWeaklyImpliedBySomeBConcept(OWLClassExpression exp, Set<OWLClassExpression> types,
			ElkModel model) {

		for (OWLClassExpression type : types) {
//			System.out.println("checking second chance-> " + SimpleOWLFormatter
//					.format(owlTools.getOWLSubClassOfAxiom(getConceptFromAlias(type, model), makeAllTop(exp))));
//			System.out.println("Result Weak-> "
//					+ isEntailed(owlTools.getOWLSubClassOfAxiom(getConceptFromAlias(type, model), makeAllTop(exp))));
			if (isEntailed(owlTools.getOWLSubClassOfAxiom(getConceptFromAlias(type, model), makeAllTop(exp)))) {
				return true;
			}
		}
		return false;
	}

	private OWLClassExpression makeAllTop(OWLClassExpression conceptFromAlias) {
		if (conceptFromAlias instanceof OWLClass)
			return owlTools.getOWLTop();

		if (conceptFromAlias instanceof OWLObjectSomeValuesFrom) {
			OWLObjectSomeValuesFrom c = (OWLObjectSomeValuesFrom) conceptFromAlias;
			return owlTools.getOWLExistentialRestriction(c.getProperty(), makeAllTop(c.getFiller()));
		}

		if (conceptFromAlias instanceof OWLObjectIntersectionOf) {
			OWLObjectIntersectionOf c = (OWLObjectIntersectionOf) conceptFromAlias;
			Set<OWLClassExpression> conjuncts = new HashSet<>();
			for (OWLClassExpression exp : c.asConjunctSet()) {
				conjuncts.add(makeAllTop(exp));
			}
			return owlTools.getOWLConjunction(conjuncts);
		}

		assert false : "something is wrong";
		return null;
	}

	private boolean checkImplication(ElkModel model, Relation implier, Relation implied) {
		boolean res;

		Set<OWLClassExpression> implierConcepts, impliedConcepts;

		Map<Element, Set<EntryPair<OWLClassExpression, Relation>>> m = model.getElementUpdatedTypeToEdgeMap();

		assert implier.getElement1().equals(implied.getElement1())
				: "Something is wrong! the 2 edges should share the same source";

		Element e = implier.getElement1();

		implierConcepts = m.get(e).stream().filter(x -> x.getSecondArg().equals(implier)).map(EntryPair::getFirstArg)
				.collect(Collectors.toSet());

		impliedConcepts = m.get(e).stream().filter(x -> x.getSecondArg().equals(implied)).map(EntryPair::getFirstArg)
				.collect(Collectors.toSet());

//		System.out.println("checking if "
//				+ implierConcepts.stream().map(x -> getRestrictionFromAlias(x, model)).collect(Collectors.toSet())
//				+ " implies "
//				+ impliedConcepts.stream().map(x -> getRestrictionFromAlias(x, model)).collect(Collectors.toSet()));
		res = checkImplication(new ArrayList<>(implierConcepts), new ArrayList<>(impliedConcepts), model);
//		System.out.println("Result = " + res);

		return res;
	}

	private boolean checkCommonStructure(Relation implied, CommonElements elementsReachableFromBoth,
			Set<Relation> processedRelations) {
//		System.out.println("common structure " + implied + " ->");
//		System.out.println("Checking implied -> " + implied);
		if (implied.isForward()) {
			if (!processedRelations.contains(implied)) {
				processedRelations.add(implied);
				if (!elementsReachableFromBoth.getElements().contains(implied.getElement2())) {
//					System.out.println(false);
					return false;
				} else {
					for (Relation r : implied.getElement2().getRelations()) {
						if (r.isForward())
							checkCommonStructure(r, elementsReachableFromBoth, processedRelations);
					}
				}
			}
		}
//		System.out.println(true);
		return true;
	}

	private boolean checkLoops(Element e, Relation implier, Relation implied, LoopTracker loopTracker) {
		// The assumption is that if an edge that lead to a loop implies another that
		// also leads to a loop, then the loops are taken care of by the implication
		// between the edges.
		// so we keep the edge that leads to a loop with the less elements
		Set<Loop> loopsImplier = loopTracker.getLoopsStartingWith(e, implier);
		Set<Loop> loopsImplied = loopTracker.getLoopsStartingWith(e, implied);

//		if (loopsImplier.isEmpty() || loopsImplied.isEmpty())
//			return true;

		if (!loopsImplier.isEmpty() && !loopsImplied.isEmpty()) {
			for (Loop lImplier : loopsImplier) {
				Set<Loop> lImpliedLarger = loopsImplied.stream().filter(x -> x.getLoopSize() >= lImplier.getLoopSize())
						.collect(Collectors.toSet());
				if (lImpliedLarger.isEmpty()) {
//					System.out.println("**********");
//					System.out.println(implier);
//					System.out.println(loopsImplier);
//					System.out.println(implied);
//					System.out.println("**********");
					return false;
				}
			}
		}

		return true;
	}

	private void removeRedundantEdges(Element e, Set<Relation> toRemove, Set<Element> coarseModel,
			Set<EntryPair<Relation, Relation>> firstImpliesSecond, Set<Element> reachableFromBothAandB) {

		for (EntryPair<Relation, Relation> p1 : firstImpliesSecond) {
			for (EntryPair<Relation, Relation> p2 : firstImpliesSecond) {
				if (p1.getFirstArg().equals(p2.getSecondArg()) && p1.getSecondArg().equals(p2.getFirstArg())) {
					if (!reachableFromBothAandB.contains(p1.getFirstArg().getElement2())) {
						toRemove.remove(p1.getFirstArg());
					}
				}
			}
		}

		toRemove.stream().forEach(x -> {
			if (e.getRelations().contains(x)) {
				e.removeRelation(x);
//				System.out.println(getElementFrom(x.getElement2(), coarseModel));
				getElementFrom(x.getElement2(), coarseModel)
						.removeRelation(getElementFrom(x.getElement2(), coarseModel).getRelations().stream()
								.filter(rel -> rel.getRoleName().equals(x.getRoleName()) && rel.getElement1().equals(e))
								.collect(Collectors.toList()).get(0));
			}
		});

	}

	private void updateMap(Element e, Relation r, Map<Element, Set<Relation>> map) {
		if (map.containsKey(e))
			map.get(e).add(r);
		else
			map.put(e, Sets.newHashSet(r));

	}

	private void updateMap(Element e, EntryPair<Relation, Relation> pair,
			Map<Element, Set<EntryPair<Relation, Relation>>> map) {
		if (map.containsKey(e))
			map.get(e).add(pair);
		else
			map.put(e, new HashSet<>(Arrays.asList(pair)));

	}

	private boolean checkImplication(List<OWLClassExpression> implierConcepts, List<OWLClassExpression> impliedConcepts,
			ElkModel model) {
		boolean[] redundancyTracker = new boolean[impliedConcepts.size()];
		Arrays.fill(redundancyTracker, false);

		OWLClassExpression originalImplierConcept, originalImpliedConcept;
		OWLSubClassOfAxiom testAxiom;
		Set<OWLClassExpression> alreadyImplied = new HashSet<>();

		for (OWLClassExpression implierConcept : implierConcepts) {
			originalImplierConcept = getRestrictionFromAlias(implierConcept, model.getMapper());
//			System.out.println("Original Implier Concept = " + originalImplierConcept);

			for (OWLClassExpression impliedConcept : impliedConcepts) {
				if (alreadyImplied.contains(impliedConcept))
					continue;

				originalImpliedConcept = getRestrictionFromAlias(impliedConcept, model.getMapper());
//				System.out.println("Original Implied Concept = " + originalImpliedConcept);
				testAxiom = owlTools.getOWLSubClassOfAxiom(originalImplierConcept, originalImpliedConcept);
//				System.out.println(
//						"Test axiom = " + SimpleOWLFormatter.format(testAxiom) + ", result = " + isEntailed(testAxiom));
				if (isEntailed(testAxiom)) {
					if (!redundancyTracker[impliedConcepts.indexOf(impliedConcept)]) {
						redundancyTracker[impliedConcepts.indexOf(impliedConcept)] = true;
						alreadyImplied.add(impliedConcept);
					}
				}
			}

			if (alreadyImplied.size() == impliedConcepts.size()) {
				break;
			}
		}

		return evaluateAND(redundancyTracker);
	}

	private boolean evaluateAND(boolean[] redundancyTracker) {
		for (int i = 0; i < redundancyTracker.length; i++) {
			if (redundancyTracker[i] == false)
				return false;
		}
		return true;
	}

	private OWLClassExpression getRestrictionFromAlias(OWLClassExpression alias, Mapper mapper) {
		if (mapper.getRestrictionMapper().getClass2Restriction().containsKey(alias)) {

			OWLObjectSomeValuesFrom restriction = (OWLObjectSomeValuesFrom) mapper.getRestrictionMapper()
					.getClass2Restriction().get(alias);
			OWLClassExpression filler = restriction.getFiller();

			return owlTools.getOWLExistentialRestriction(restriction.getProperty(),
					getRestrictionFromAlias(filler, mapper));
		}

		if (mapper.getConjunctionMapper().getClass2Conjunction().containsKey(alias)) {
			Set<OWLClassExpression> conjuncts = mapper.getConjunctionMapper().getClass2Conjunction().get(alias)
					.asConjunctSet();
			Set<OWLClassExpression> newConjuncts = new HashSet<>();
			for (OWLClassExpression conjunct : conjuncts) {
				newConjuncts.add(getRestrictionFromAlias(conjunct, mapper));
			}
			return owlTools.getOWLConjunction(newConjuncts);
		}
		return alias;
	}

	private Element getElementFrom(Element toFind, Set<Element> elements) {
		return elements.stream().filter(x -> x.equals(toFind)).collect(Collectors.toList()).get(0);
	}

	private void removeUnreachableElements(Set<Element> model, Element lHSElement, Element rHSElement) {
		Set<Element> reachableElements = new HashSet<>();
		Set<Element> tmp = Sets.newHashSet(getElementFrom(lHSElement, model), getElementFrom(rHSElement, model));

		while (!tmp.equals(reachableElements)) {
			for (Element e : tmp)
				reachableElements.add(e);

			for (Element e : reachableElements)
				tmp.addAll(e.getRelations().stream().filter(x -> x.isForward())
						.map(x -> getElementFrom(x.getElement2(), model)).collect(Collectors.toSet()));
		}

		model.retainAll(reachableElements);
	}

	private boolean isEntailed(OWLAxiom axiom) {
		return reasoner.isEntailed(axiom);
	}

	/**
	 * Return the OWLClassExpression mapped to the input alias
	 * 
	 * @param alias
	 * @return
	 */
	private OWLClassExpression getConceptFromAlias(OWLClassExpression alias, ElkModel model) {
		OWLClassExpression res = getRestrictionFromAlias(alias, model);

		if (!res.equals(alias)) {
			OWLObjectSomeValuesFrom restriction = (OWLObjectSomeValuesFrom) model.getMapper().getRestrictionMapper()
					.getClass2Restriction().get(alias);
			OWLClassExpression filler = restriction.getFiller();

			return owlTools.getOWLExistentialRestriction(restriction.getProperty(), getConceptFromAlias(filler, model));
		}

		res = getConjunctionFromAlias(alias, model);
		if (!res.equals(alias)) {
			Set<OWLClassExpression> conjuncts = model.getMapper().getConjunctionMapper().getClass2Conjunction()
					.get(alias).asConjunctSet();
			Set<OWLClassExpression> newConjuncts = new HashSet<>();
			for (OWLClassExpression conjunct : conjuncts) {
				newConjuncts.add(getConceptFromAlias(conjunct, model));
			}
			return owlTools.getOWLConjunction(newConjuncts);
		}

		return res;
	}

	private OWLClassExpression getRestrictionFromAlias(OWLClassExpression alias, ElkModel model) {
		if (model.getMapper().getRestrictionMapper().getClass2Restriction().containsKey(alias))
			return model.getMapper().getRestrictionMapper().getClass2Restriction().get(alias);
		return alias;
	}

	private OWLClassExpression getConjunctionFromAlias(OWLClassExpression alias, ElkModel model) {
		if (model.getMapper().getConjunctionMapper().getClass2Conjunction().containsKey(alias))
			return model.getMapper().getConjunctionMapper().getClass2Conjunction().get(alias);
		return alias;
	}

}
