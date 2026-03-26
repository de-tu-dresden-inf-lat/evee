package de.tu_dresden.inf.lat.evee.proofs.tools;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.tu_dresden.inf.lat.evee.general.tools.OWLTools;
import de.tu_dresden.inf.lat.evee.proofs.data.Proof;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;

import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInference;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProof;

public class ProofTools {

	public static String ASSERTED_1 = "asserted";
	public static String ASSERTED_2 = "Asserted Conclusion";
	public static String ASSERTED_3 = "Asserted";

	/**
	 * TODO a bit unclean - it would be better to have this as property in the inference object (isAsserted)
	 */
	public static <S> boolean isAsserted(IInference<S> inf) {
		return inf.getRuleName().equals(ASSERTED_1) || inf.getRuleName().equals(ASSERTED_2) || inf.getRuleName().equals(ASSERTED_3);
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


	public static <SENTENCE> IProof<SENTENCE> restrictToReachable(IProof<SENTENCE> proof) {
		Set<IInference<SENTENCE>> reachableInf = new HashSet<>();
		fillReachableInferences(proof, proof.getFinalConclusion(), reachableInf);
		return new Proof<SENTENCE>(proof.getFinalConclusion(), reachableInf);
	}

	public static <SENTENCE> Set<SENTENCE> reachableAssertions(IProof<SENTENCE> proof) {
		Set<IInference<SENTENCE>> reachableInf = new HashSet<>();
		fillReachableInferences(proof, proof.getFinalConclusion(), reachableInf);
		Set<SENTENCE> reachableAss = new HashSet<>();
		for(IInference<SENTENCE> inf:reachableInf){
			if(ProofTools.isAsserted(inf))
				reachableAss.add(inf.getConclusion());
		}
		return reachableAss;
	}

	public static <SENTENCE> void fillReachableInferences(
			IProof<SENTENCE> proof, SENTENCE conclusion, Set<IInference<SENTENCE>> toFill) {
		fillReachableInferences(proof, conclusion, toFill, new HashSet<>());
	}

	private static <SENTENCE> void fillReachableInferences(
			IProof<SENTENCE> proof, SENTENCE conclusion, Set<IInference<SENTENCE>> toFill, Set<SENTENCE> processed) {
		if(processed.contains(conclusion))
			return;
		else {
			processed.add(conclusion);
			for(IInference<SENTENCE> inf: proof.getInferences(conclusion)){
				toFill.add(inf);
				inf.getPremises().forEach(p -> fillReachableInferences(proof,p,toFill, processed));
			}
			return;
		}
	}

}
