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
        // TODO add isEntailed or something??
        return axiom instanceof OWLSubClassOfAxiom;
    }

    @Override
    public IProof<OWLAxiom> getProof(OWLAxiom axiom) throws ProofGenerationException {
        NemoOwlParser parser = NemoOwlParser.getInstance();

        //TODO type check axiom
        String nemoAxiom = parser.subClassAxiomToNemoString((OWLSubClassOfAxiom) axiom);

        IProof<String> proof;
        try{
            proof = reasoner.proof(nemoAxiom);
        }catch(Exception e) {
            throw new ProofGenerationException(e);
        }

        return parser.nemoProoftoProofOWL(proof);
    }
    
}
