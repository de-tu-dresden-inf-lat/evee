package de.tu_dresden.inf.lat.evee.nemo;

import de.tu_dresden.inf.lat.evee.nemo.parser.tools.OWLHelper;
import org.semanticweb.owlapi.model.*;

import de.tu_dresden.inf.lat.evee.nemo.parser.ELKAtomParser;
import de.tu_dresden.inf.lat.evee.nemo.parser.EnvelopeAtomParser;
import de.tu_dresden.inf.lat.evee.nemo.parser.TextbookAtomParser;
import de.tu_dresden.inf.lat.evee.nemo.parser.exceptions.ConceptTranslationError;
import de.tu_dresden.inf.lat.evee.nemo.parser.NemoProofParser;
import de.tu_dresden.inf.lat.evee.proofs.data.exceptions.*;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProof;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProofGenerator;

public class NemoProofGenerator implements IProofGenerator<OWLAxiom, OWLOntology>{

    private NemoReasoner reasoner;
    private NemoProofParser parser;
    private ECalculus calculus = ECalculus.ENVELOPE;

    public NemoProofGenerator(OWLOntology ontology){
        reasoner = new NemoReasoner(ontology);
        parser = new NemoProofParser();
        setCalculusParser(calculus);
    }

    public NemoProofGenerator(){
        parser = new NemoProofParser();
        reasoner = new NemoReasoner();
        setCalculusParser(calculus);
    }

    @Override
    public void setOntology(OWLOntology ontology) {
        reasoner.setOntology(ontology);
    }

    public void setCalculus(ECalculus calc){ 
        this.calculus = calc;
        setCalculusParser(calc);
    }

    public void setNemoExecPath(String path){
        reasoner.setNemoExecPath(path);
    }

    @Override
    public IProof<OWLAxiom> getProof(OWLAxiom axiom) throws ProofGenerationException {
        //Extend the ontology with the top level concepts in the input axiom
        reasoner.setOptionalGoalExpressions(OWLHelper.getInstance().getTopLevelClassExpressions(axiom));

        String nemoAxiom = parser.axiomToNemoString(axiom);

        IProof<String> proof;
        try{
            proof = reasoner.proof(nemoAxiom, calculus);
        }catch(Exception e) {
            throw new ProofGenerationException(e);
        }

        //TODO idealy checked in supportsProof
        if (proof.getFinalConclusion().isEmpty())
            throw new ProofNotSupportedException("axiom could not be derived");

        IProof<OWLAxiom> parsedProof;
        try{
            parsedProof = parser.toProofOWL(proof);
        } catch (ConceptTranslationError e){
            throw new ProofGenerationException(e);
        }
        
        return parsedProof;
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

        boolean subClassValid = subAxiom.getSubClass().isNamed();
        boolean superClassValid = subAxiom.getSuperClass().isNamed();

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

    private void setCalculusParser(ECalculus calc){
        switch (calc) {
            case ELK:
                parser.setAtomParser(new ELKAtomParser());
                break;
            case TEXTBOOK:
                parser.setAtomParser(new TextbookAtomParser());
                break;
            case ENVELOPE:
                parser.setAtomParser(new EnvelopeAtomParser());
                break;
            default:
                break;
        }
    }
}
