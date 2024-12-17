package de.tu_dresden.inf.lat.evee.nemo;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import de.tu_dresden.inf.lat.evee.nemo.parser.NemoOwlParser;
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
    public void setOntology(OWLOntology ontology) {
        reasoner = new NemoReasoner(ontology);
    }

    @Override
    public IProof<OWLAxiom> getProof(OWLAxiom axiom) throws ProofGenerationException {
        NemoOwlParser parser = NemoOwlParser.getInstance();

        String nemoAxiom = parser.subClassAxiomToNemoString((OWLSubClassOfAxiom) axiom);

        IProof<String> proof;
        try{
            proof = reasoner.proof(nemoAxiom);
        }catch(Exception e) {
            throw new ProofGenerationException(e);
        }

        return parser.toProofOWL(proof);
    }

    @Override
    public boolean supportsProof(OWLAxiom axiom) {
        if (!(axiom instanceof OWLSubClassOfAxiom))
            return false;
    
        OWLSubClassOfAxiom subAxiom = (OWLSubClassOfAxiom) axiom;
        boolean subClassValid = subAxiom.getSubClass().asOWLClass().isOWLClass();
        boolean superClassValid = subAxiom.getSuperClass().asOWLClass().isOWLClass();

        return subClassValid && superClassValid;
    }

    @Override
	public void cancel() {
		// do nothing, assuming that NEMO is fast enough
	}

	@Override
	public boolean successful() {
		// return true, because we let NEMO run through to the end
		return true;
	}
    
}
