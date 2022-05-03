/**
 * 
 */
package de.tu_dresden.inf.lat.evee.proofs.tools;

import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

/**
 * @author stefborg
 *
 */
public class IndividualNameGenerator extends NameGenerator<OWLNamedIndividual> {

	public IndividualNameGenerator(String prefix, boolean readable, Set<OWLEntity> sig) {
		super('a', 17, str -> OWLTools.odf.getOWLNamedIndividual(IRI.create(str)), prefix, readable, sig);
	}

}
