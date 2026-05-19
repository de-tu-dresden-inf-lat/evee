/**
 * 
 */
package de.tu_dresden.inf.lat.evee.proofs.tools.evaluators.inferences;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInference;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInferenceEvaluator;
import de.tu_dresden.inf.lat.evee.general.tools.OWLTools;

/**
 * Determine whether there is a domain restriction on r that is used in a weird
 * way, i.e., without applying it to an existential restriction over r.
 * 
 * @author stefborg
 *
 */
public class C6DomainAndNoExistentialEvaluator implements IInferenceEvaluator<OWLAxiom> {

	@Override
	public Double evaluate(IInference<OWLAxiom> inf) {
		OWLOntology o = OWLTools.createOntology(inf.getPremises().stream());
		OWLReasoner reasoner = OWLTools.createReasoner(o);
		for (OWLAxiom ax : inf.getPremises()) {
			if (ax instanceof OWLObjectPropertyDomainAxiom) {
				OWLObjectPropertyExpression r = ((OWLObjectPropertyDomainAxiom) ax).getProperty();
				OWLClassExpression rtop = OWLTools.odf.getOWLObjectSomeValuesFrom(r, OWLTools.odf.getOWLThing());
				if (!inf.getPremises().stream().flatMap(prem -> prem.getNestedClassExpressions().stream())
						.map(expr -> OWLTools.odf.getOWLSubClassOfAxiom(expr, rtop))
						.anyMatch(sco -> reasoner.isEntailed(sco))) {
					reasoner.dispose();
					OWLTools.manager.removeOntology(o);
					return 1d;
				}
			}
		}
		reasoner.dispose();
		OWLTools.manager.removeOntology(o);
		return 0d;
	}

	@Override
	public String getDescription() {
		return "C6: Domain restriction without existential restriction";
	}

}
