/**
 * 
 */
package de.tu_dresden.inf.lat.proofs.tools.evaluators.inferences;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import de.tu_dresden.inf.lat.proofs.interfaces.IInference;
import de.tu_dresden.inf.lat.proofs.interfaces.IInferenceEvaluator;

/**
 * Determine whether the premises contain a value restriction on the left-hand
 * side of an inclusion.
 * 
 * @author stefborg
 */
public class C3UniversalImplicationEvaluator implements IInferenceEvaluator<OWLAxiom> {

	@Override
	public Double evaluate(IInference<OWLAxiom> inf) {
		Set<OWLSubClassOfAxiom> axioms = new HashSet<>();
		for (OWLAxiom ax : inf.getPremises()) {
			if (ax instanceof OWLSubClassOfAxiom) {
				axioms.add((OWLSubClassOfAxiom) ax);
			}
			if (ax instanceof OWLEquivalentClassesAxiom) {
				axioms.addAll(((OWLEquivalentClassesAxiom) ax).asOWLSubClassOfAxioms());
			}
		}
		for (OWLSubClassOfAxiom sco : axioms) {
			if (sco.getSubClass() instanceof OWLObjectAllValuesFrom) {
				return 1d;
			}
		}
		return 0d;
	}

	@Override
	public String getDescription() {
		return "C3: Contains left-hand value restriction";
	}

}
