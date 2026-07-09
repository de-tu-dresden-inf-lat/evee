package de.tu_dresden.inf.lat.model.interfaces;

import java.io.IOException;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import de.tu_dresden.inf.lat.exceptions.EntityCheckerException;

/**
 * @author Christian Alrabbaa
 */

public interface IModelGenerator extends IGenerator {

	/**
	 * Negate an axiom, and return an instantiation of it.
	 * 
	 * For example {@code (A <= C) -> (@x (A & ~C))}
	 * 
	 * @param generalAxiom an instance of OWWLAxiom
	 * @return The instantiation of the axiom in string format
	 */
	abstract String getConclusionAsAssertion(OWLAxiom generalAxiom);

	/**
	 * @param ontology
	 * @param axiom
	 * @param filePath
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws OWLOntologyCreationException
	 */
	abstract void getCounterModel(OWLOntology ontology, OWLAxiom axiom, String filePath)
			throws IOException, InterruptedException, OWLOntologyCreationException, EntityCheckerException;
}
