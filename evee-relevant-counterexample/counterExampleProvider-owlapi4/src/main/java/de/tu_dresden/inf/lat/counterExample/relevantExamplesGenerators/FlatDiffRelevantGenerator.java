package de.tu_dresden.inf.lat.counterExample.relevantExamplesGenerators;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.tu_dresden.inf.lat.counterExample.ELKModelGenerator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import com.github.jsonldjava.shaded.com.google.common.collect.Sets;

import de.tu_dresden.inf.lat.prettyPrinting.formatting.SimpleOWLFormatter;
import de.tu_dresden.inf.lat.model.data.Element;
import de.tu_dresden.inf.lat.model.data.EntryPair;
import de.tu_dresden.inf.lat.model.data.Relation;
import de.tu_dresden.inf.lat.model.tools.GeneralTools;

/**
 * @author Christian Alrabbaa
 */
public class FlatDiffRelevantGenerator extends DiffRelevantGenerator {

	private final Logger logger = Logger.getLogger(FlatDiffRelevantGenerator.class);

	public FlatDiffRelevantGenerator(ELKModelGenerator elkCounterModelGenerator) throws OWLOntologyCreationException {
		super(elkCounterModelGenerator);
	}

	@Override
	public Set<Element> generate() {
		logger.info("Extracting flat relevant counter example for " + SimpleOWLFormatter.format(this.conclusion));
		Instant start = Instant.now();

		Level currentLevel = super.logger.getLevel();
		super.logger.setLevel(Level.OFF);

		Set<Element> typeFlatModel = super.generate();
		super.logger.setLevel(currentLevel);

		Element finalizedLHSElement = getElementFrom(this.model.getMapper().getLHSRepresentativeElement(),
				typeFlatModel);
		Element finalizedRHSElement = getElementFrom(this.model.getMapper().getRHSRepresentativeElement(),
				typeFlatModel);

		Map<Relation, Set<Boolean>> edgeFoundMap = new HashMap<>();
		Set<EntryPair<Element, Element>> processedPairsOfElements = new HashSet<>();

		Set<Element> reachableFromA = Sets.newHashSet(getElementFrom(finalizedLHSElement, typeFlatModel));
		Set<Element> reachableFromB = Sets.newHashSet(getElementFrom(finalizedRHSElement, typeFlatModel));

		Set<Relation> explored = new HashSet<>();
		fillReachableElementsSet(finalizedLHSElement, reachableFromA, typeFlatModel, explored);
		explored = new HashSet<>();
		fillReachableElementsSet(finalizedRHSElement, reachableFromB, typeFlatModel, explored);
		Set<Element> reachableFromBoth = new HashSet<>(reachableFromA);
		reachableFromBoth.retainAll(reachableFromB);

		trackCommon(typeFlatModel, finalizedLHSElement, finalizedRHSElement, edgeFoundMap, processedPairsOfElements,
				reachableFromBoth);

		Set<Element> trimPoints = new HashSet<>();
		Set<Relation> processedRelations = new HashSet<>();
		getTrimPoints(typeFlatModel, finalizedRHSElement, edgeFoundMap, trimPoints, reachableFromBoth,
				processedRelations);

		cutAtTrimPoints(typeFlatModel, trimPoints);

		Instant finish = Instant.now();
		logger.info("Total " + GeneralTools.getDuration(start, finish));
		logger.info("Coarse Flat model has been generated!\nTotal number of domain elements = " + typeFlatModel.size()
				+ "\nTotal number of edges = " + this.getEdgeCount(typeFlatModel));

		return typeFlatModel;
	}

	private void getTrimPoints(Set<Element> model, Element element, Map<Relation, Set<Boolean>> edgeFoundMap,
			Set<Element> trimPoints, Set<Element> reachableFromBoth, Set<Relation> processedRelations) {
		if (reachableFromBoth.contains(element))
			return;

		for (Relation r : element.getRelations()) {
			if ((!r.isForward()) || processedRelations.contains(r))
				continue;
			processedRelations.add(r);
			if (evaluateFound(r, edgeFoundMap))
				getTrimPoints(model, getElementFrom(r.getElement2(), model), edgeFoundMap, trimPoints,
						reachableFromBoth,
						processedRelations);
			else
				trimPoints.add(getElementFrom(r.getElement2(), model));
		}
	}

	private void cutAtTrimPoints(Set<Element> model, Set<Element> trimPoints) {
		for (Element e : trimPoints) {
			Sets.newHashSet(getElementFrom(e, model).getRelations()).stream().filter(Relation::isForward)
					.forEach(getElementFrom(e, model)::removeRelation);
		}
	}

	protected void trackCommon(Set<Element> model, Element lHSElement, Element rHSElement,
			Map<Relation, Set<Boolean>> edgeFoundMap, Set<EntryPair<Element, Element>> processed,
			Set<Element> reachableFromBoth) {

		Set<Relation> rhsElementRelations = rHSElement.getRelations().stream().filter(Relation::isForward)
				.collect(Collectors.toSet());
		Set<Relation> cRelations;
		Set<Relation> lhsElementRelations = lHSElement.getRelations().stream().filter(Relation::isForward)
				.collect(Collectors.toSet());

		EntryPair<Element, Element> processedPair;
		for (Relation r2 : rhsElementRelations) {

			cRelations = lhsElementRelations.stream().filter(x -> x.getRoleName().equals(r2.getRoleName()))
					.collect(Collectors.toSet());

			updateMap(r2, !cRelations.isEmpty(), edgeFoundMap);

			if (!reachableFromBoth.contains(r2.getElement2())) {
				for (Relation r1 : cRelations) {
					processedPair = new EntryPair<>(getElementFrom(r1.getElement2(), model),
							getElementFrom(r2.getElement2(), model));
					if (processed.contains(processedPair))
						continue;
					processed.add(processedPair);
					trackCommon(model, getElementFrom(r1.getElement2(), model), getElementFrom(r2.getElement2(), model),
							edgeFoundMap, processed, reachableFromBoth);
				}
			}
		}
	}
}
