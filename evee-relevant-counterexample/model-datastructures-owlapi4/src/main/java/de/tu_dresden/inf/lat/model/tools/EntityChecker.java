package de.tu_dresden.inf.lat.model.tools;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.expression.OWLEntityChecker;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * @author Christian Alrabbaa
 *
 */
public class EntityChecker implements OWLEntityChecker {

	private static OWLDataFactory factory;
	private static OWLOntology ontology;

	public EntityChecker(OWLOntology ont) {
		factory = OWLManager.getOWLDataFactory();
		ontology = ont;
	}

	@Override
	public OWLAnnotationProperty getOWLAnnotationProperty(String arg0) {
		assert false : "Checking OWL Annotation Prorerty is not implemented";
		return null;
	}

	@Override
	public OWLClass getOWLClass(String arg0) {

		IRI possible = getIRI(arg0);
		if (ontology.containsClassInSignature(possible))
			return factory.getOWLClass(possible);

		if (arg0.equals("owl:Thing"))
			return factory.getOWLThing();

		if (arg0.equals("owl:Nothing"))
			return factory.getOWLNothing();

		return null;

	}

	@Override
	public OWLDataProperty getOWLDataProperty(String arg0) {
		assert false : "Checking OWL Data Property is not implemented";
		return null;
	}

	@Override
	public OWLDatatype getOWLDatatype(String arg0) {
		assert false : "Checking OWL Data Types is not implemented";
		return null;
	}

	@Override
	public OWLNamedIndividual getOWLIndividual(String arg0) {

		IRI possible = getIRI(arg0);
		if (ontology.containsIndividualInSignature(possible))
			return factory.getOWLNamedIndividual(possible);

		return null;
	}

	@Override
	public OWLObjectProperty getOWLObjectProperty(String arg0) {

		IRI possible = getIRI(arg0);
		if (ontology.containsObjectPropertyInSignature(possible))
			return factory.getOWLObjectProperty(possible);

		return null;

	}

	private static IRI getIRI(String iriStr) {
		if (iriStr.startsWith("<") && iriStr.endsWith(">"))
			return IRI.create(iriStr.substring(1, iriStr.length() - 1));
		return IRI.create(iriStr);
	}
}
