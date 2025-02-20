package de.tu_dresden.inf.lat.evee.nemo.parser;

import java.util.List;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import de.tu_dresden.inf.lat.evee.proofs.data.Inference;
import de.tu_dresden.inf.lat.evee.proofs.data.Proof;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInference;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProof;

public class NemoProofParser {

    private AbstractAtomParser atomParser;

    public NemoProofParser(){}

    public NemoProofParser(AbstractAtomParser atomParser){
        this.atomParser = atomParser;
    }

    public void setAtomParser(AbstractAtomParser atomParser){
        this.atomParser = atomParser;
    }

    // TODO move method somewhere else?
    public String subClassAxiomToNemoString(OWLSubClassOfAxiom axiom){
        String superClass = axiom.getSuperClass().asOWLClass().getIRI().toString();
        String subClass = axiom.getSubClass().asOWLClass().getIRI().toString();

        return String.format("mainSubClassOf(<%s>,<%s>)", subClass, superClass);
    }

    /*
     * parses IProof<String> to IProof<OWLAxiom>
     * a AtomParser needs to be set before calling this method (via constructor or setAtomParser())
     */
    public IProof<OWLAxiom> toProofOWL(IProof<String> proofStr){
        if (atomParser == null)
            throw new IllegalStateException("no AtomParser configured");

        IInference<OWLAxiom> currentInf;
        List<OWLAxiom> currentPremise;
        OWLAxiom currentConclusion;

        String finalConc = proofStr.getFinalConclusion();
        IProof<OWLAxiom>  proof = new Proof<>(atomParser.toOwlAxiom(finalConc));

        for(IInference<String> infStr:proofStr.getInferences()){
            currentConclusion = atomParser.toOwlAxiom(infStr.getConclusion());
            currentPremise = infStr.getPremises().stream().map(x->atomParser.toOwlAxiom(x)).collect(Collectors.toList());
            currentInf = new Inference<>(currentConclusion,infStr.getRuleName(),currentPremise);

            proof.addInference(currentInf);
        }

       // atomParser.printCache();
        return proof;
    }

}
