package de.tu_dresden.inf.lat.evee.proofs.data;

import java.util.Collection;

import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProgressTracker;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProofGenerator;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.ISignatureBasedProofGenerator;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.ISimpleProofGenerator;

public abstract class AbstractSignatureBasedProofGeneratorDecorator<SYMBOL, SENTENCE, ONTOLOGY>
		implements ISignatureBasedProofGenerator<SYMBOL, SENTENCE, ONTOLOGY> {

	protected IProofGenerator<SENTENCE, ONTOLOGY> innerGenerator;

	protected Collection<SYMBOL> knownSignature;

	public AbstractSignatureBasedProofGeneratorDecorator(IProofGenerator<SENTENCE, ONTOLOGY> innerGenerator) {
		this.innerGenerator = innerGenerator;
	}

	@Override
	public void setSignature(Collection<SYMBOL> knownSignature) {
		this.knownSignature = knownSignature;
		if(innerGenerator instanceof ISignatureBasedProofGenerator) {
			System.out.println("Handing over");
			((ISignatureBasedProofGenerator<SYMBOL,SENTENCE,ONTOLOGY>) innerGenerator).setSignature(knownSignature);
		}
	}

	@Override
	public void setOntology(ONTOLOGY ontology) {
		innerGenerator.setOntology(ontology);
	}

	@Override
	public boolean supportsProof(SENTENCE axiom) {
		return innerGenerator.supportsProof(axiom);
	}

	@Override
	public void addProgressTracker(IProgressTracker tracker) {
		innerGenerator.addProgressTracker(tracker);
	}

	@Override
	public void cancel() {
		innerGenerator.cancel();
	}

}
