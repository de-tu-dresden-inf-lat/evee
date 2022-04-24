package de.tu_dresden.inf.lat.proofs.interfaces;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import de.tu_dresden.inf.lat.proofs.data.exceptions.ProofGenerationCancelledException;

public interface ISimpleProofGenerator<SYMBOL,SENTENCE,THEORY> extends IHasProgressTracker {

    void setOntology(THEORY ontology);

    IProof<SENTENCE> proveSubsumption(SYMBOL lhs, SYMBOL rhs) throws ProofGenerationCancelledException;

    IProof<SENTENCE> proveEquivalence(SYMBOL lhs, SYMBOL rhs) throws ProofGenerationCancelledException;
}
