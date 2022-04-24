package de.tu_dresden.inf.lat.proofs.interfaces;

import java.util.Collection;

public interface ISignatureBasedProofGenerator<SYMBOL, SENTENCE, ONTOLOGY> extends IProofGenerator<SENTENCE, ONTOLOGY> {

	void setSignature(Collection<SYMBOL> knownSignature);
	
}
