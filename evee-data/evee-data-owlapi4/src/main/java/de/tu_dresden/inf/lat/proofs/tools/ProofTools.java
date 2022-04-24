package de.tu_dresden.inf.lat.proofs.tools;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;

import de.tu_dresden.inf.lat.proofs.interfaces.IInference;
import de.tu_dresden.inf.lat.proofs.interfaces.IProof;

public class ProofTools {

	public static <S> boolean isAsserted(IInference<S> inf) {
		return inf.getRuleName().equals("asserted") || inf.getRuleName().equals("Asserted Conclusion");
	}

	public static boolean isCorrect(IInference<OWLAxiom> inf) {
		return OWLTools.entails(inf.getPremises().stream(), inf.getConclusion());
	}

	public static Set<OWLEntity> getSignature(IInference<OWLAxiom> inf) {
		Set<OWLEntity> sig = OWLTools.getSignature(inf.getConclusion());
		sig.addAll(OWLTools.getSignature(inf.getPremises()));
		return sig;
	}

	public static Set<OWLEntity> getSignature(IProof<OWLAxiom> proof) {
		Set<OWLEntity> sig = new HashSet<>();
		proof.getInferences().stream().map(ProofTools::getSignature).forEach(sig::addAll);
		return sig;
	}

	public static <S> List<S> getSentences(IInference<S> inf) {
		List<S> list = new ArrayList<>(inf.getPremises());
		list.add(inf.getConclusion());
		return list;
	}

}
