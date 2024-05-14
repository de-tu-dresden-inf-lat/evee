package de.tu_dresden.inf.lat.counterExample.relevantExamplesGenerators;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import de.tu_dresden.inf.lat.counterExample.ELKModelGenerator;
import de.tu_dresden.inf.lat.counterExample.data.Tracker;
import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLClassExpression;
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

public class DiffRelevantGenerator extends RelevantCounterExampleGenerator {
	protected final Logger logger = Logger.getLogger(DiffRelevantGenerator.class);

	public DiffRelevantGenerator(ELKModelGenerator elkCounterModelGenerator) throws OWLOntologyCreationException {
		super(elkCounterModelGenerator);
	}

	public Set<Element> generate() {
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

		Set<Element> reachableFromB = Sets.newHashSet(getElementFrom(finalizedRHSElement, typeDiffModel));
		Set<Element> reachableFromA = Sets.newHashSet(getElementFrom(finalizedLHSElement, typeDiffModel));

		Set<Relation> explored = new HashSet<>();
		fillReachableElementsSet(finalizedRHSElement, reachableFromB, typeDiffModel, explored);

		explored = new HashSet<>();
		fillReachableElementsSet(finalizedLHSElement, reachableFromA, typeDiffModel, explored);

		Set<Element> reachableFromBoth = Sets.newHashSet(reachableFromB);
		reachableFromBoth.retainAll(reachableFromA);

		Set<EntryPair<Relation, Relation>> processed = new HashSet<>();

		Tracker tracker = new Tracker();
		filterTypes(finalizedLHSElement, finalizedRHSElement, typeDiffModel, reachableFromBoth, tracker, processed);

		removeRelationsAndModifyTypes(typeDiffModel, tracker, reachableFromBoth);

		Instant finish = Instant.now();
		logger.info("Total " + GeneralTools.getDuration(start, finish));
		logger.info("Coarse Diff model has been generated!\nTotal number of domain elements = " + typeDiffModel.size()
				+ "\nTotal number of edges = " + this.getEdgeCount(typeDiffModel));

		return typeDiffModel;
	}

	private void removeRelationsAndModifyTypes(Set<Element> typeDiffModel, Tracker tracker,
											   Set<Element> reachableFromBoth) {
		for(Relation r : tracker.getRelationExists().keySet()){
			if(reachableFromBoth.contains(r.getElement1()))
				continue;
			if(!tracker.checkRelationJustified(r)){
				getElementFrom(r.getElement1(), typeDiffModel).removeRelation(r);
			}else {
				//change this from types implied to relation implied
				if (!tracker.getImpliedRelations().contains(r))/*(!tracker.checkTypesImplied(r.getElement2()))*/{
					if(!reachableFromBoth.contains(r.getElement2())) {
						// In case the collected types are a subset of the types of element2 of the current edge, this
						// means element1 is connected to each of the elements that resulted in the collected types
						Set<OWLClassExpression> types = tracker.getTypesToKeep(r.getElement2());

						if(types.size()>1 && getElementFrom(r.getElement2(), typeDiffModel).getTypes().containsAll(types))
							getElementFrom(r.getElement1(), typeDiffModel).removeRelation(r);

						else
							getElementFrom(r.getElement2(), typeDiffModel).getTypes().retainAll(types);
					}
					else{
						if (getElementFrom(r.getElement1(), typeDiffModel).getRelations().stream().filter(x->!x.equals(r) &&
								x.getRoleName().equals(r.getRoleName()) &&
								getElementFrom(x.getElement2(),typeDiffModel).getTypes().size()>1).count()>=1)

							getElementFrom(r.getElement1(), typeDiffModel).removeRelation(r);
					}
				}
			}
		}
	}

	private void filterTypes(Element left, Element right, Set<Element> modelTmp, Set<Element> reachableFromBoth,
							 Tracker tracker, Set<EntryPair<Relation, Relation>> processed){

		Set<Relation> candidates;
		EntryPair<Relation, Relation> processedEntry;
		for(Relation rLeft : left.getRelations()){
			if(!rLeft.isForward())
				continue;
			if (reachableFromBoth.contains(left))
				continue;

			candidates =
					right.getRelations().stream().filter(rRight -> rLeft.getRoleName().equals(rRight.getRoleName()) && rRight.isForward())
					.collect(Collectors.toSet());

			if (candidates.isEmpty()) {
				tracker.addToRelationExists(rLeft, false);
			}
			else{
				tracker.addToRelationExists(rLeft, true);

				boolean isImplied = candidates.stream().anyMatch(rRight -> checkImplication(rLeft, rRight, modelTmp));
				if(isImplied) {
					tracker.addToImpliedRelations(rLeft);
					tracker.addToTypesImplied(rLeft.getElement2(), true);
				}
				else {
					tracker.addToTypesImplied(rLeft.getElement2(), false);
					candidates.stream().map(Relation::getElement2).forEach(e->
							tracker.addToTypesNeededCollector(rLeft.getElement2(),e)
					);
				}

				// Since we filter types when there is no direct implication between node labels, we have to iterate
				// over the full set of candidates
				for(Relation candidate: candidates){
					processedEntry = new EntryPair<>(rLeft, candidate);
					if (processed.contains(processedEntry))
						continue;
					processed.add(processedEntry);

					filterTypes(getElementFrom(rLeft.getElement2(), modelTmp), getElementFrom(candidate.getElement2()
							, modelTmp), modelTmp, reachableFromBoth, tracker, processed);
				}
			}
		}
	}

	private boolean checkImplication(Relation rLeft, Relation rRight, Set<Element> modelTmp) {
		assert rLeft.getRoleName().equals(rRight.getRoleName()): "The role name must be the same";

		Element l = getElementFrom(rLeft.getElement2(), modelTmp);
		Element r = getElementFrom(rRight.getElement2(), modelTmp);

		Set<OWLClassExpression> lClassNames =
				Sets.newHashSet(l.getTypes()).stream().filter(t->
						!this.model.getMapper().getRestrictionMapper().getClass2Restriction().containsKey(t.asOWLClass()) &&
						!this.model.getMapper().getConjunctionMapper().getClass2Conjunction().containsKey(t.asOWLClass())).collect(Collectors.toSet());

		Set<OWLClassExpression> rClassNames =
				Sets.newHashSet(r.getTypes()).stream().filter(t->
						!this.model.getMapper().getRestrictionMapper().getClass2Restriction().containsKey(t.asOWLClass()) &&
						!this.model.getMapper().getConjunctionMapper().getClass2Conjunction().containsKey(t.asOWLClass())).collect(Collectors.toSet());

		return rClassNames.containsAll(lClassNames);
	}
}
