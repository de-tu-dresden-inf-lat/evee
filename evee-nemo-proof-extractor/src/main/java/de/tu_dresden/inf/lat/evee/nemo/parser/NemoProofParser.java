package de.tu_dresden.inf.lat.evee.nemo.parser;

import java.util.List;
import java.util.stream.Collectors;

import de.tu_dresden.inf.lat.evee.proofs.data.exceptions.ProofGenerationException;
import org.semanticweb.owlapi.model.*;

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

    public void setAtomParser(AbstractAtomParser atomParser){
        this.atomParser = atomParser;
    }

    public String subClassAxiomToNemoString(OWLSubClassOfAxiom axiom){
        String superClass = formatTopAndBottom(axiom.getSuperClass().asOWLClass());
        String subClass = formatTopAndBottom(axiom.getSubClass().asOWLClass());

        return String.format("mainSubClassOf(%s,%s)", subClass, superClass);
    }

    private String EquivClassAxiomToNemString(OWLEquivalentClassesAxiom axiom) {
        List<OWLClassExpression> classes = axiom.getClassExpressionsAsList();

        return String.format("mainEquivClass(%s, %s)", formatTopAndBottom(classes.get(0)),
                formatTopAndBottom(classes.get(1)));
    }

    private String formatTopAndBottom(OWLClassExpression clsExpression) {
        if (clsExpression.isOWLNothing())
            return "<http://www.w3.org/2002/07/owl#Nothing>";
        if (clsExpression.isOWLThing())
            return "<http://www.w3.org/2002/07/owl#Thing>";
        return clsExpression.toString();
    }

    public String axiomToNemoString(OWLAxiom axiom) throws ProofGenerationException {
        if (axiom.isOfType(AxiomType.SUBCLASS_OF))
            return subClassAxiomToNemoString((OWLSubClassOfAxiom) axiom);
        if (axiom.isOfType(AxiomType.EQUIVALENT_CLASSES))
            return EquivClassAxiomToNemString((OWLEquivalentClassesAxiom) axiom);
            
        throw new ProofGenerationException("Axiom type is not supported by this proof generator!");
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

            IInference<OWLAxiom> parsedInf = new Inference<>(conclusion, infStr.getRuleName(), premises); 
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
}
