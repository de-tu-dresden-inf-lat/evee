package de.tu_dresden.inf.lat.evee.nemo;

import org.semanticweb.owlapi.model.*;

import de.tu_dresden.inf.lat.evee.nemo.parser.ELKAtomParser;
import de.tu_dresden.inf.lat.evee.nemo.parser.NemoProofParser;
import de.tu_dresden.inf.lat.evee.proofs.data.exceptions.ProofGenerationException;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProof;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProofGenerator;

public class NemoProofGenerator implements IProofGenerator<OWLAxiom, OWLOntology>{

    private NemoReasoner reasoner;
    private NemoProofParser parser;
//    private OWLOntology ontology;

    public NemoProofGenerator(OWLOntology ontology){
        reasoner = new NemoReasoner(ontology);
        parser = new NemoProofParser();
    }

    public NemoProofGenerator(){
        parser = new NemoProofParser();
    }

    @Override
    public void setOntology(OWLOntology ontology) {
//        this.ontology = ontology;
        reasoner = new NemoReasoner(ontology);
    }

    @Override
    public IProof<OWLAxiom> getProof(OWLAxiom axiom) throws ProofGenerationException {
        String nemoAxiom = parser.axiomToNemoString(axiom);

        IProof<String> proof;
        try{
            proof = reasoner.proof(nemoAxiom);
        }catch(Exception e) {
            throw new ProofGenerationException(e);
        }

        if (proof.getFinalConclusion().isEmpty())
            throw new ProofGenerationException("axiom could not be derived");

        parser.setAtomParser(new ELKAtomParser());
        
        return parser.toProofOWL(proof);   
    }

    @Override
    public boolean supportsProof(OWLAxiom axiom) {
        OWLSubClassOfAxiom subAxiom;

        if (axiom instanceof OWLSubClassOfAxiom)
            subAxiom = (OWLSubClassOfAxiom) axiom;
        else if (axiom.isOfType(AxiomType.EQUIVALENT_CLASSES))
            subAxiom = ((OWLEquivalentClassesAxiom) axiom).asOWLSubClassOfAxioms().iterator().next();
        else
            return false;

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
