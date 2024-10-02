package de.tu_dresden.inf.lat.counterExample.tools;

import java.util.Collections;
import java.util.Set;

import org.semanticweb.elk.owlapi.ElkReasoner;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

/**
 * @author Christian Alrabbaa
 *
 */
public class Segmenter {

	private static final int superConcepts = -1;
	private static final int subConcepts = 0;
	private static final ModuleType type = ModuleType.BOT;

	private Segmenter() {
	}

	public static Set<OWLAxiom> getSegment(OWLOntology ontology, Set<OWLEntity> concept) {

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = reasonerFactory.createReasoner(ontology);
		SyntacticLocalityModuleExtractor extractor = new SyntacticLocalityModuleExtractor(
				OWLManager.createOWLOntologyManager(), ontology, type);

		return extractor.extract(concept, superConcepts, subConcepts, reasoner);
	}

	public static OWLOntology getStarModule(OWLOntology ontology, Set<OWLEntity> concept, IRI iri)
			throws OWLOntologyCreationException {

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = reasonerFactory.createReasoner(ontology);
		SyntacticLocalityModuleExtractor extractor = new SyntacticLocalityModuleExtractor(
				OWLManager.createOWLOntologyManager(), ontology, ModuleType.STAR);
		return extractor.extractAsOntology(concept, iri, 0, 0, reasoner);
	}

	public static OWLOntology getSegmentAsOntology(OWLOntology ontology, Set<OWLEntity> concept, IRI iri)
			throws OWLOntologyCreationException {

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = reasonerFactory.createReasoner(ontology);
		SyntacticLocalityModuleExtractor extractor = new SyntacticLocalityModuleExtractor(
				OWLManager.createOWLOntologyManager(), ontology, type);
		return extractor.extractAsOntology(concept, iri, superConcepts, subConcepts, reasoner);
	}

	public static Set<OWLAxiom> getSegment(OWLOntology ontology, OWLEntity concept) {

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = reasonerFactory.createReasoner(ontology);
		SyntacticLocalityModuleExtractor extractor = new SyntacticLocalityModuleExtractor(
				OWLManager.createOWLOntologyManager(), ontology, type);
		return extractor.extract(Collections.singleton(concept), superConcepts, subConcepts, reasoner);
	}

	public static OWLOntology getSegmentAsOntology(OWLOntology ontology, OWLEntity concept, IRI iri)
			throws OWLOntologyCreationException {

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = reasonerFactory.createReasoner(ontology);
		SyntacticLocalityModuleExtractor extractor = new SyntacticLocalityModuleExtractor(
				OWLManager.createOWLOntologyManager(), ontology, type);
		return extractor.extractAsOntology(Collections.singleton(concept), iri, superConcepts, subConcepts, reasoner);
	}
}
