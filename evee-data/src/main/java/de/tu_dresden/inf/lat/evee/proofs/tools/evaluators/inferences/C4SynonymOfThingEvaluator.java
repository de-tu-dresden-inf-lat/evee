/**
 * 
 */
package de.tu_dresden.inf.lat.evee.proofs.tools.evaluators.inferences;

import org.semanticweb.owlapi.model.OWLAxiom;

import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInference;
import de.tu_dresden.inf.lat.evee.general.tools.OWLTools;

/**
 * Check whether a concept name is entailed to be equivalent to top, without
 * explicitly being declared to be so.
 * 
 * @author stefborg
 *
 */
public class C4SynonymOfThingEvaluator extends EntailedHiddenAxiomsEvaluator {

	@Override
	public Double evaluate(IInference<OWLAxiom> inf) {
		return checkEntailedHiddenAxioms(inf, cls -> OWLTools.odf.getOWLSubClassOfAxiom(OWLTools.odf.getOWLThing(), cls));
	}

	@Override
	public String getDescription() {
		return "C4: Top has a synonym";
	}

}
