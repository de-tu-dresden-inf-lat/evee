package de.tu_dresden.inf.lat.evee.nemo;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import de.tu_dresden.inf.lat.evee.proofs.data.exceptions.ProofGenerationException;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProof;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProofGenerator;

public class NemoProofGenerator implements IProofGenerator<OWLAxiom, OWLOntology>{

    private NemoReasoner reasoner;

    public NemoProofGenerator(){}

    public NemoProofGenerator(OWLOntology ontology){
        reasoner = new NemoReasoner(ontology);
    }

    @Override
    public boolean successful() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'successful'");
    }

    @Override
    public void setOntology(OWLOntology ontology) {
        reasoner = new NemoReasoner(ontology);
    }

    @Override
    public boolean supportsProof(OWLAxiom axiom) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'supportsProof'");
    }

    @Override
    public IProof<OWLAxiom> getProof(OWLAxiom axiom) throws ProofGenerationException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getProof'");
    }
    
}
