package de.tu_dresden.inf.lat.counterExample.relevantExamplesGenerators;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import de.tu_dresden.inf.lat.counterExample.ELKModelGenerator;
import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import com.github.jsonldjava.shaded.com.google.common.collect.Sets;

import de.tu_dresden.inf.lat.prettyPrinting.formatting.SimpleOWLFormatter;
import de.tu_dresden.inf.lat.model.data.Element;
import de.tu_dresden.inf.lat.model.tools.GeneralTools;

/**
 * @author Christian Alrabbaa
 *
 *         Generate type-b relevant counter example extracted from the canonical
 *         model
 */
public class BetaRelevantGenerator extends RelevantCounterExample {
	private final Logger logger = Logger.getLogger(BetaRelevantGenerator.class);

	public BetaRelevantGenerator(ELKModelGenerator elkCounterModelGenerator) throws OWLOntologyCreationException {
		super(elkCounterModelGenerator);
	}

	public Set<Element> generate() {
		logger.info("Extracting type-b relevant counter example for " + SimpleOWLFormatter.format(this.conclusion));
		Instant start = Instant.now();

		Element lHSElement = new Element(this.model.getMapper().getLHSRepresentativeElement().getName());
		lHSElement.addTypes(this.model.getMapper().getLHSRepresentativeElement().getTypes());
		lHSElement.addRelations(FilterIncomingEdges(this.model.getMapper().getLHSRepresentativeElement()));

		// TODO what about A<=Bot
		Element rHSElement = new Element(this.model.getMapper().getRHSRepresentativeElement().getName());
		rHSElement.addTypes(this.model.getMapper().getRHSRepresentativeElement().getTypes());
		rHSElement.addRelations(FilterIncomingEdges(this.model.getMapper().getRHSRepresentativeElement()));

		Set<Element> tmp = Sets.newHashSet(lHSElement, rHSElement);
		Set<Element> typeBModel = new HashSet<>();

		while (!tmp.isEmpty()) {
			tmp = addSuccessorsOfAll(tmp, typeBModel);
		}

//		System.out.println("lhs -> " + this.model.getMapper().getAliasLHS() + ", its element -> "
//				+ this.model.getMapper().getLHSRepresentativeElement().getName());
//
//		System.out.println("rhs -> " + this.model.getMapper().getAliasRHS() + ", its element -> "
//				+ this.model.getMapper().getRHSRepresentativeElement().getName());
//
//		System.out.println("Beta coarse");
//		typeBModel.forEach(System.out::println);

		cleanReverseEdges(typeBModel);

		Instant finish = Instant.now();
		logger.info("Total " + GeneralTools.getDuration(start, finish));
		logger.info("Coarse Type-b model has been generated!\nTotal number of domain elements = " + typeBModel.size()
				+ "\nTotal number of edges = " + this.getEdgeCount(typeBModel));

		return typeBModel;
	}

}
