/**
 * 
 */
package de.tu_dresden.inf.lat.proofs.tools.evaluators.inferences;

import org.semanticweb.owlapi.model.OWLAxiom;

import de.tu_dresden.inf.lat.proofs.interfaces.IInference;
import de.tu_dresden.inf.lat.proofs.tools.OWLTools;

/**
 * Check whether a concept name is entailed to be equivalent to bottom, without
 * explicitly being declared to be so.
 * 
 * @author stefborg
 *
 */
public class C5SynonymOfNothingEvaluator extends EntailedHiddenAxiomsEvaluator {

	@Override
	public Double evaluate(IInference<OWLAxiom> inf) {
		return checkEntailedHiddenAxioms(inf, cls -> OWLTools.odf.getOWLSubClassOfAxiom(cls, OWLTools.odf.getOWLNothing()));
	}

	@Override
	public String getDescription() {
		return "C5: Bottom has a synonym";
	}

}
