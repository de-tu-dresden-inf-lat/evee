/**
 * 
 */
package de.tu_dresden.inf.lat.proofs.tools.evaluators.inferences;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import de.tu_dresden.inf.lat.proofs.interfaces.IInference;
import de.tu_dresden.inf.lat.proofs.interfaces.IInferenceEvaluator;
import de.tu_dresden.inf.lat.proofs.tools.OWLTools;
import de.tu_dresden.inf.lat.proofs.tools.ProofTools;

/**
 * @author stefborg
 *
 */
public abstract class EntailedHiddenAxiomsEvaluator implements IInferenceEvaluator<OWLAxiom> {

	protected static Double checkEntailedHiddenAxioms(IInference<OWLAxiom> inf, Function<OWLClass, OWLAxiom> f) {
		OWLOntology o = OWLTools.createOntology(inf.getPremises().stream());
		OWLReasoner reasoner = OWLTools.createReasoner(o);
		Set<OWLClass> namedClasses = ProofTools.getSignature(inf).stream().filter(e -> e.isOWLClass())
				.map(e -> e.asOWLClass()).collect(Collectors.toSet());
		for (OWLClass cls : namedClasses) {
			OWLAxiom ax = f.apply(cls);
			if (inf.getConclusion().equals(ax) || inf.getPremises().contains(ax)) {
				// axiom is not hidden, but explicit
				break;
			}
			if (reasoner.isEntailed(ax)) {
				reasoner.dispose();
				OWLTools.manager.removeOntology(o);
				return 1d;
			}
		}
		reasoner.dispose();
		OWLTools.manager.removeOntology(o);
		return 0d;
	}

}
