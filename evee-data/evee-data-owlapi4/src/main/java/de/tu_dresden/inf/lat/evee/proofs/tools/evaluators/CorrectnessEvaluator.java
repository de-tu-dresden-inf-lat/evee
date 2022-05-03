package de.tu_dresden.inf.lat.evee.proofs.tools.evaluators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.semanticweb.owlapi.model.OWLAxiom;

import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInference;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProof;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProofEvaluator;
import de.tu_dresden.inf.lat.evee.proofs.tools.ProofTools;

public class CorrectnessEvaluator implements IProofEvaluator<OWLAxiom> {

	Collection<? extends OWLAxiom> ontology;
	OWLAxiom goalAxiom;

	public void setTask(IInference<OWLAxiom> task) {
		this.ontology = task.getPremises();
		this.goalAxiom = task.getConclusion();
	}

	public void setOntology(Collection<? extends OWLAxiom> ontology) {
		this.ontology = ontology;
	}

	public void setGoalAxiom(OWLAxiom goalAxiom) {
		this.goalAxiom = goalAxiom;
	}

	@Override
	public double evaluate(IProof<OWLAxiom> proof) {
		boolean correct=true;
		if(!matchesTask(proof)) {
			System.out.println("does not match task");
			correct = false;
		}
		if(!isComplete(proof)) {
			System.out.println("incomplete");
			correct = false;
		}
		if(!isCorrect(proof)) {
			System.out.println("incorrect");
			correct = false;
		}
		//boolean correct = matchesTask(proof) && isComplete(proof) && isCorrect(proof);
		return correct ? 1d : 0d;
	}

	private boolean matchesTask(IProof<OWLAxiom> proof) {
		// check that the conclusion is as intended and the asserted axioms are a subset
		// of the premises of the task
		return proof.getFinalConclusion().equals(goalAxiom) && proof.getInferences().stream()
				.filter(ProofTools::isAsserted).map(IInference::getConclusion).allMatch(ontology::contains);
	}

	private boolean isCorrect(IProof<OWLAxiom> proof) {
		// check that all non-asserted inferences are actually correct entailments
		return proof.getInferences().stream().filter(inf -> !ProofTools.isAsserted(inf))
				.allMatch(inf -> {
					if(!ProofTools.isCorrect(inf)) {
						System.out.println("unsound inference: ");
						System.out.println(inf);
						return false;
					} else
						return true;
				});
	}

	private <S> boolean isComplete(IProof<S> proof) {
		// check that all axioms reachable from the final conclusion are derived by (at
		// least) one inference
		return isComplete(proof, proof.getFinalConclusion(), Collections.emptyList());
	}

	private <S> boolean isComplete(IProof<S> proof, S conclusion, List<S> trace) {
		if (trace.contains(conclusion)) {
			// cyclic inferences are not allowed
			return false;
		}
		Collection<IInference<S>> inferences = proof.getInferences(conclusion);
		List<S> newTrace = new ArrayList<>(trace);
		newTrace.add(conclusion);
		return inferences.stream()
				.anyMatch(inf -> inf.getPremises().stream().allMatch(ax -> isComplete(proof, ax, newTrace)));
	}

	@Override
	public String getDescription() {
		return "Proof is correct";
	}

}
