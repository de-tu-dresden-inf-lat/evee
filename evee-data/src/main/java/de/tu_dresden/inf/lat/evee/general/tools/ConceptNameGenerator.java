/**
 * 
 */
package de.tu_dresden.inf.lat.evee.general.tools;

import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;

/**
 * @author stefborg
 *
 */
public class ConceptNameGenerator extends NameGenerator<OWLClass> {

	public ConceptNameGenerator(String prefix, boolean readable, Set<OWLEntity> sig) {
		super('A', 26, str -> OWLTools.odf.getOWLClass(IRI.create(str)), prefix, readable, sig);
	}

}
