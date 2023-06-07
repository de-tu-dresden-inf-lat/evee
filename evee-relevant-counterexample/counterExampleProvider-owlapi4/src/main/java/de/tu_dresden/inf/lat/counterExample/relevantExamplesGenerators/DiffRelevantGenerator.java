package de.tu_dresden.inf.lat.counterExample.relevantExamplesGenerators;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import de.tu_dresden.inf.lat.counterExample.ELKModelGenerator;
import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import com.github.jsonldjava.shaded.com.google.common.collect.Sets;

import de.tu_dresden.inf.lat.prettyPrinting.formatting.SimpleOWLFormatter;
import de.tu_dresden.inf.lat.counterExample.TypeExtender;
import de.tu_dresden.inf.lat.model.data.Element;
import de.tu_dresden.inf.lat.model.data.EntryPair;
import de.tu_dresden.inf.lat.model.data.Relation;
import de.tu_dresden.inf.lat.model.tools.GeneralTools;

public class DiffRelevantGenerator extends RelevantCounterExample {
	protected final Logger logger = Logger.getLogger(DiffRelevantGenerator.class);

	public DiffRelevantGenerator(ELKModelGenerator elkCounterModelGenerator) throws OWLOntologyCreationException {
		super(elkCounterModelGenerator);
	}

	public Set<Element> generate() {
		Map<Element, Set<OWLClassExpression>> elementsToRemovedTypes = new HashMap<>();
		logger.info("Extracting diff relevant counter example for " + SimpleOWLFormatter.format(this.conclusion));
		Instant start = Instant.now();

		Element lHSElement = new Element(this.model.getMapper().getLHSRepresentativeElement().getName());
		lHSElement.addTypes(this.model.getMapper().getLHSRepresentativeElement().getTypes());
		lHSElement.addRelations(this.model.getMapper().getLHSRepresentativeElement().getRelations()/* ) */);

		Element rHSElement = new Element(this.model.getMapper().getRHSRepresentativeElement().getName());
		rHSElement.addTypes(this.model.getMapper().getRHSRepresentativeElement().getTypes());
		rHSElement.addRelations(this.model.getMapper().getRHSRepresentativeElement().getRelations()/* ) */);

		Set<Element> tmp = Sets.newHashSet(lHSElement, rHSElement);
		Set<Element> typeDiffModel = new HashSet<>();

		while (!tmp.isEmpty()) {
			tmp = this.addSuccessorsOfAll(tmp, typeDiffModel);
		}

		this.cleanReverseEdges(typeDiffModel);

		Element finalizedLHSElement = getElementFrom(lHSElement, typeDiffModel);
		Element finalizedRHSElement = getElementFrom(rHSElement, typeDiffModel);

		Map<Relation, Set<Boolean>> edgeFoundMap = new HashMap<>();

		Map<Element, Set<OWLClassExpression>> typeCommonMap = new HashMap<>();
		typeCommonMap.put(finalizedLHSElement, Sets.newHashSet(this.model.getMapper().getAliasLHS()));
		for (OWLClassExpression t : finalizedLHSElement.getTypes()) {
			if (rHSElement.getTypes().contains(t))
				typeCommonMap.get(finalizedLHSElement).add(t);
		}

		Set<Element> reachableFromLHSElement = Sets.newHashSet(finalizedLHSElement);
		Set<EntryPair<Relation, Relation>> processed = new HashSet<EntryPair<Relation, Relation>>();

		Set<Element> reachableFromA = Sets.newHashSet(getElementFrom(finalizedLHSElement, typeDiffModel));
		Set<Element> reachableFromB = Sets.newHashSet(getElementFrom(finalizedRHSElement, typeDiffModel));

		Set<Relation> explored = new HashSet<>();
		fillReachableElementsSet(finalizedLHSElement, reachableFromA, typeDiffModel, explored);
		explored = new HashSet<>();
		fillReachableElementsSet(finalizedRHSElement, reachableFromB, typeDiffModel, explored);
		Set<Element> reachableFromBoth = new HashSet<>(reachableFromA);
		reachableFromBoth.retainAll(reachableFromB);

		trackCommon(finalizedLHSElement, finalizedRHSElement, edgeFoundMap, typeCommonMap, reachableFromLHSElement,
				processed, reachableFromBoth);

		// Filter out the common reachable elements
		Set<Element> onlyReachableFromLHSElement = filterReachableElements(reachableFromLHSElement, finalizedRHSElement,
				typeDiffModel);
//		System.out.println("only from lhs");
//		onlyReachableFromLHSElement.forEach(System.out::println);

//		System.out.println("***MAP***");
//		this.model.getMapper().getRestrictionMapper().getClass2Restriction().entrySet().forEach(System.out::println);

		for (Element e : typeDiffModel) {
			elementsToRemovedTypes.put(e, new HashSet<>());
//			if (reachableFromLHSElement.contains(e)) {
			if (onlyReachableFromLHSElement.contains(e)) {
				for (Relation r : Sets.newHashSet(e.getRelations())) {
					if (!r.isForward())
						continue;
					if (!evaluateFound(r, edgeFoundMap)) {
						e.removeRelation(r);
						getElementFrom(r.getElement2(), typeDiffModel).removeRelation(
								getElementFrom(r.getElement2(), typeDiffModel).getRelations().stream().filter(
										rel -> rel.getRoleName().equals(r.getRoleName()) && rel.getElement1().equals(e))
										.collect(Collectors.toList()).get(0));

//						System.out.println("current element" + e);
						this.model.getElementUpdatedTypeToEdgeMap().get(e).stream().map(EntryPair::getFirstArg)
								.forEach(exp -> {

									elementsToRemovedTypes.get(e).add(exp);

									exp = getRestrictionFromAlias(exp);

									Set<EntryPair<OWLClassExpression, Relation>> toRemove;
									Set<EntryPair<OWLClassExpression, Relation>> toBeUpdated;
									Set<Relation> backRelations = e.getRelations().stream().filter(x -> x.isBackward())
											.collect(Collectors.toSet()); // System.out.println("Backrelations"); //
//									System.out.println(backRelations); //
//									System.out.println("edge Label to remove " + exp);
									for (Relation backR : backRelations) {
										toBeUpdated = this.model.getElementUpdatedTypeToEdgeMap()
												.get(backR.getElement1()); // System.out.println("tobe updated"); //
//										System.out.println(toBeUpdated);
										toRemove = toBeUpdated.stream()
												.filter(x -> x.getSecondArg().getRoleName().equals(backR.getRoleName())
														&& x.getSecondArg().getElement2().equals(e))
												.collect(Collectors.toSet()); //
//										System.out.println(toRemove);

										for (EntryPair<OWLClassExpression, Relation> entry : toRemove) {
											toBeUpdated.remove(entry);

											// UpdateLoopTypes(backR.getElement(), exp, entry.getFirstArg());
											OWLClassExpression newConcept = makeConcept(exp,
													getRestrictionFromAlias(entry.getFirstArg())); //
//											System.out.println("NEW CONCEPT -> " + newConcept);

											// record what is the new meaning of the alias in the current element
											this.model.getElementLabelToNewConceptMap().get(backR.getElement1())
													.add(new EntryPair<>(entry.getFirstArg(), newConcept));

											EntryPair<OWLClassExpression, Relation> pair = new EntryPair<>(newConcept,
													entry.getSecondArg());
											toBeUpdated.add(pair);

//											System.out.println("===Updating===");
//											System.out.println(getRestrictionFromAlias(entry.getFirstArg()));
//											System.out.println("with");
//											System.out.println(pair);
										}

									}

								});

						//

//						// Add all types that correspond to paths
//						System.out.println("heeeere " + e.getName());
//						elementsToRemovedTypes.get(e).addAll(this.model.getElementLabelToEdgeMap().get(e).stream()
//								.map(EntryPair::getFirstArg).collect(Collectors.toSet()));
//						System.out.println("z->" + elementsToRemovedTypes.get(e));
//						Set<EntryPair<OWLClassExpression, Relation>> toRemove = this.model.getElementLabelToEdgeMap()
//								.get(e).stream().collect(Collectors.toSet());
//						for (EntryPair<OWLClassExpression, Relation> pair : toRemove) {
//							System.out.println(pair);
//							this.model.getElementLabelToEdgeMap().get(e).remove(pair);
//						}
					}
				}
//				if (onlyReachableFromLHSElement.contains(e)) {
				for (OWLClassExpression exp : Sets.newHashSet(e.getTypes()))
					if (!typeCommonMap.get(e).contains(exp)) {
						if (exp.equals(owlTools.getOWLTop()))
							continue;
						e.removeType(exp);
						elementsToRemovedTypes.get(e).add(exp);

//						propagateBackwards(exp, e);

						// Replaced with propagate backwards

						Set<EntryPair<OWLClassExpression, Relation>> toRemove;
						Set<EntryPair<OWLClassExpression, Relation>> toBeUpdated;
						Set<Relation> backRelations = e.getRelations().stream().filter(x -> x.isBackward())
								.collect(Collectors.toSet());
//						System.out.println("Label to remove " + exp);
						for (Relation backR : backRelations) {
							toBeUpdated = this.model.getElementUpdatedTypeToEdgeMap().get(backR.getElement1());
							toRemove = toBeUpdated.stream()
									.filter(x -> x.getSecondArg().getRoleName().equals(backR.getRoleName())
											&& x.getSecondArg().getElement2().equals(e))
									.collect(Collectors.toSet());

							for (EntryPair<OWLClassExpression, Relation> entry : toRemove) {
								toBeUpdated.remove(entry);

								// UpdateLoopTypes(backR.getElement(), exp, entry.getFirstArg());
								OWLClassExpression newConcept = makeConcept(exp,
										getRestrictionFromAlias(entry.getFirstArg()));
								EntryPair<OWLClassExpression, Relation> pair = new EntryPair<>(newConcept,
										entry.getSecondArg());
								toBeUpdated.add(pair);

//								System.out.println("===Updating===");
//								System.out.println(getRestrictionFromAlias(entry.getFirstArg()));
//								System.out.println("with");
//								System.out.println(pair);
							}

						}

					}

			}
		}

//		System.out.println("+++++++Map of updated types to concepts+++++++");
//		for (Element e : this.model.getElementUpdatedTypeToEdgeMap().keySet()) {
//			System.out.println("element" + e);
//			System.out.println(this.model.getElementUpdatedTypeToEdgeMap().get(e));
//		}

//		System.out.println("diff coarse before type extension");
//		typeDiffModel.forEach(System.out::println);
//		System.out.println("full raw before type extension");
//		this.model.getRawModelElements().forEach(System.out::println);

		TypeExtender te = new TypeExtender(model, typeDiffModel);
//		System.out.println(elementsToRemovedTypes.entrySet());
		te.ExtendTypes(elementsToRemovedTypes);

//		model.getMapper().getRestrictionMapper().getClass2Restriction().entrySet().forEach(System.out::println);

//		System.out.println("diff coarse After type extension");
//		typeDiffModel.forEach(System.out::println);

		Instant finish = Instant.now();
		logger.info("Total " + GeneralTools.getDuration(start, finish));
		logger.info("Coarse Diff model has been generated!\nTotal number of domain elements = " + typeDiffModel.size()
				+ "\nTotal number of edges = " + this.getEdgeCount(typeDiffModel));

		return typeDiffModel;
	}

	/**
	 * Common parts based on diff relevant counter example
	 * 
	 * @param lHSElement
	 * @param rHSElement
	 * @param edgeFoundMap
	 * @param typeFoundMap
	 * @param elementsReachableFromLHSElement
	 * @param processed
	 * @param reachableFromBoth
	 */
	private void trackCommon(Element lHSElement, Element rHSElement, Map<Relation, Set<Boolean>> edgeFoundMap,
			Map<Element, Set<OWLClassExpression>> typeFoundMap, Set<Element> elementsReachableFromLHSElement,
			Set<EntryPair<Relation, Relation>> processed, Set<Element> reachableFromBoth) {

		Set<Relation> cRelations;
		Set<OWLClassExpression> commonTypes;
		Element r1Element;
		Set<Relation> lhsElementRelations = lHSElement.getRelations().stream().filter(x -> x.isForward())
				.collect(Collectors.toSet());
		EntryPair<Relation, Relation> processedPair;

		Set<Relation> rhsElementRelations;
//		System.out.println("Processing the following pair -> " + lHSElement + ", " + rHSElement);

		for (Relation r1 : lhsElementRelations) {

			rhsElementRelations = rHSElement.getRelations().stream().filter(x -> x.isForward())
					.collect(Collectors.toSet());

			cRelations = rhsElementRelations.stream().filter(x -> x.getRoleName().equals(r1.getRoleName()))
					.collect(Collectors.toSet());
			commonTypes = new HashSet<>();

//			System.out.println("Common Relations" + cRelations);

			if (cRelations.isEmpty()) {
//				System.out.println("Recording -> " + r1 + " = " + false);
				updateMap(r1, false, edgeFoundMap);
			} else {
//				System.out.println("Recording -> " + r1 + " = " + true);
//				System.out.println("Reasons -> " + cRelations);
				updateMap(r1, true, edgeFoundMap);
//				this.relationReasons.updateMap(r1, cRelations);
			}

			r1Element = this.model.getFinilizedElement(r1.getElement2());
			elementsReachableFromLHSElement.add(r1Element);

			if (!reachableFromBoth.contains(r1Element)) {
				for (Relation r2 : cRelations) {

					Element r2Element = this.model.getFinilizedElement(r2.getElement2());
					commonTypes.addAll(r1Element.getTypes().stream().filter(x -> r2Element.getTypes().contains(x))
							.collect(Collectors.toSet()));

					processedPair = new EntryPair<Relation, Relation>(r1, r2);
					if (processed.contains(processedPair))
						continue;
					processed.add(processedPair);

					trackCommon(r1Element, r2Element, edgeFoundMap, typeFoundMap, elementsReachableFromLHSElement,
							processed, reachableFromBoth);
				}
			} else {
				commonTypes = r1Element.getTypes();
			}
			if (typeFoundMap.containsKey(r1Element))
				typeFoundMap.get(r1Element).addAll(commonTypes);
			else
				typeFoundMap.put(r1Element, new HashSet<>(commonTypes));
		}
//
//		System.out.println("edge found map");
//		edgeFoundMap.entrySet().forEach(System.out::println);
//
//		System.out.println("type found map");
//		typeFoundMap.entrySet().forEach(System.out::println);

	}

	private void propagateBackwards(OWLClassExpression conceptNameToRemove, Element destinationElement) {
//		System.out.println("Destination Element = " + destinationElement);
		Element sourceElement;
		OWLClassExpression updatedConcept;
		Set<EntryPair<OWLClassExpression, Relation>> toBeUpdated;
		Optional<EntryPair<OWLClassExpression, Relation>> entryToRemoveOpt;
		for (Relation r : destinationElement.getRelations()) {
			if (r.isBackward()) {
				sourceElement = r.getElement1();
				for (OWLClassExpression type : sourceElement.getTypes()) {
					if (type.equals(owlTools.getOWLTop()))
						continue;

					OWLClassExpression originalConcept = getConceptFromAlias(type);
					updatedConcept = makeConcept(conceptNameToRemove, originalConcept);

					toBeUpdated = this.model.getElementUpdatedTypeToEdgeMap().get(sourceElement);

//					System.out.println("Type = " + type + " -> " + originalConcept);

					entryToRemoveOpt = toBeUpdated.stream()
							.filter(x -> x.getFirstArg().equals(type) || x.getFirstArg().equals(originalConcept))
							.findFirst();

					if (entryToRemoveOpt.isPresent()) {

						toBeUpdated.remove(entryToRemoveOpt.get());

						EntryPair<OWLClassExpression, Relation> pair = new EntryPair<>(updatedConcept, r);

						toBeUpdated.add(pair);

//						System.out.println("===Updating===");
//						System.out.println(originalConcept);
//						System.out.println("with");
//						System.out.println(pair);

						// record what is the new meaning of the alias in the current element
						this.model.getElementLabelToNewConceptMap().get(sourceElement)
								.add(new EntryPair<>(type, updatedConcept));
					}

				}

				propagateBackwards(conceptNameToRemove, sourceElement);
			}
		}
	}
}
