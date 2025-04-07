package de.tu_dresden.inf.lat.evee.nemo.parser;

import java.util.List;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import de.tu_dresden.inf.lat.evee.nemo.parser.tools.ParsingHelper;
import de.tu_dresden.inf.lat.evee.proofs.data.Inference;
import de.tu_dresden.inf.lat.evee.proofs.data.Proof;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInference;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProof;
import de.tu_dresden.inf.lat.evee.proofs.tools.MinimalProofExtractor;
import de.tu_dresden.inf.lat.evee.proofs.tools.measures.TreeSizeMeasure;

public class NemoProofParser {

    private final ParsingHelper parsingHelper = ParsingHelper.getInstance();
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

        atomParser.initFacts(proofStr.getInferences());

        String finalConc = proofStr.getFinalConclusion();
        IProof<OWLAxiom>  proof = new Proof<>(atomParser.toOwlAxiom(finalConc));

        for(IInference<String> infStr:proofStr.getInferences()){
            OWLAxiom conclusion = atomParser.toOwlAxiom(infStr.getConclusion());
            if (conclusion == atomParser.getDefaultAxiom())
                continue;

            List<OWLAxiom> premises = infStr.getPremises().stream()
                .map(x->atomParser.toOwlAxiom(x))
                    .filter(x -> !(x == atomParser.getDefaultAxiom()))
                        .collect(Collectors.toList());

            IInference<OWLAxiom> parsedInf = new Inference<>(conclusion, parseRuleName(infStr.getRuleName()), premises); 
            proof.addInference(parsedInf);
        }

        return mininmizeProof(proof);
    }

    private IProof<OWLAxiom> mininmizeProof(IProof<OWLAxiom> proof){
        try {
            proof = new MinimalProofExtractor<>(new TreeSizeMeasure<OWLAxiom>()).extract(proof);
        }catch (Exception e){}

        return proof;
    }

    private String parseRuleName(String ruleStr){
        String ruleName = parsingHelper.getRuleName(ruleStr);
        if (ruleName.isEmpty())
            return ruleStr;
        
        return ruleName;
    }

}
