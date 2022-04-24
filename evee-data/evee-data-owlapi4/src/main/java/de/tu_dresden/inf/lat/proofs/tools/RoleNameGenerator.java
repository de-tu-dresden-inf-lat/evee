/**
 * 
 */
package de.tu_dresden.inf.lat.proofs.tools;

import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectProperty;

/**
 * @author stefborg
 *
 */
public class RoleNameGenerator extends NameGenerator<OWLObjectProperty> {

	public RoleNameGenerator(String prefix, boolean readable, Set<OWLEntity> sig) {
		super('r', 9, str -> OWLTools.odf.getOWLObjectProperty(IRI.create(str)), prefix, readable, sig);
	}

}
