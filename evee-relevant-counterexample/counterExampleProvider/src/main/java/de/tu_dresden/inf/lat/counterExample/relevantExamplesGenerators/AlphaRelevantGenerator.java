package de.tu_dresden.inf.lat.counterExample.relevantExamplesGenerators;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import de.tu_dresden.inf.lat.counterExample.ELKModelGenerator;
import de.tu_dresden.inf.lat.model.data.Element;
import de.tu_dresden.inf.lat.model.tools.GeneralTools;
import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import com.github.jsonldjava.shaded.com.google.common.collect.Sets;

import de.tu_dresden.inf.lat.prettyPrinting.formatting.SimpleOWLFormatter;

/**
 * @author Christian Alrabbaa
 *
 *         Generate type-a relevant counter example extracted from the canonical
 *         model
 */
public class AlphaRelevantGenerator extends RelevantCounterExampleGenerator {

	private final Logger logger = Logger.getLogger(AlphaRelevantGenerator.class);

	public AlphaRelevantGenerator(ELKModelGenerator elkCounterModelGenerator) throws OWLOntologyCreationException {
		super(elkCounterModelGenerator);
	}

	public Set<Element> generate() {
		logger.info("Extracting type-a relevant counter example for " + SimpleOWLFormatter.format(this.conclusion));
		Instant start = Instant.now();

		Element lHSElement = new Element(this.model.getMapper().getLHSRepresentativeElement().getName());
		lHSElement.addTypes(this.model.getMapper().getLHSRepresentativeElement().getTypes());
		lHSElement.addRelations(FilterIncomingEdges(this.model.getMapper().getLHSRepresentativeElement()));

		Set<Element> tmp = Sets.newHashSet(lHSElement);
		Set<Element> typeAModel = new HashSet<>();

		while (!tmp.isEmpty()) {
			tmp = addSuccessorsOfAll(tmp, typeAModel);
		}

		cleanReverseEdges(typeAModel);

		Instant finish = Instant.now();
		logger.info("Total " + GeneralTools.getDuration(start, finish));
		logger.info("Coarse Type-a model has been generated!\nTotal number of domain elements = " + typeAModel.size()
				+ "\nTotal number of edges = " + this.getEdgeCount(typeAModel));

		return typeAModel;
	}

}
