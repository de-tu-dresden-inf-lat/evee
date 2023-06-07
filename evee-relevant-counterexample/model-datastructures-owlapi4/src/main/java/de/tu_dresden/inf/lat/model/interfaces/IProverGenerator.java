package de.tu_dresden.inf.lat.model.interfaces;

import java.io.FileOutputStream;
import java.io.IOException;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

/**
 * @author Christian Alrabbaa
 *
 */
public interface IProverGenerator extends IGenerator {

	/**
	 * Translate relevant OWL axioms to MetTel Calculus rules
	 * 
	 * @param ontology  an instance of OWLOntology
	 * @param outStream an instance of FileOutputStream
	 */
	abstract void OWL2MetRules(OWLOntology ontology, OWLEntity lhs, FileOutputStream outStream);

	/**
	 * @param ontStr
	 * @return OWLOntology
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws OWLOntologyCreationException
	 */
	abstract void generateProver(OWLOntology ontStr, OWLAxiom axiom)
			throws IOException, InterruptedException, OWLOntologyCreationException;

}
