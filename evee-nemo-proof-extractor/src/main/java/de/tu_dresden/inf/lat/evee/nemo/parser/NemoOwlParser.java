package de.tu_dresden.inf.lat.evee.nemo.parser;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import de.tu_dresden.inf.lat.evee.nemo.parser.exceptions.ConceptTranslationError;
import de.tu_dresden.inf.lat.evee.nemo.parser.tools.ParsingHelper;
import de.tu_dresden.inf.lat.evee.proofs.data.Inference;
import de.tu_dresden.inf.lat.evee.proofs.data.Proof;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInference;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProof;

public class NemoOwlParser {

    //Set<List<String>> tripleAtomsArgs = null; //TODO: wat is zis?
    private final TripleAtomsParser tripleParser = new TripleAtomsParser(Collections.emptySet());
    private final ELAtomsParser elParser = ELAtomsParser.getInstance();
    private final ParsingHelper helper = ParsingHelper.getInstance();
    
    private NemoOwlParser() {}

	private static class LazyHolder {
		static NemoOwlParser instance = new NemoOwlParser();
	}

	public static NemoOwlParser getInstance() {
		return LazyHolder.instance;
	}

    public String subClassAxiomToNemoString(OWLSubClassOfAxiom axiom){
        String superClass = axiom.getSuperClass().asOWLClass().getIRI().toString();
        String subClass = axiom.getSubClass().asOWLClass().getIRI().toString();

        return String.format("mainSubClassOf(<%s>,<%s>)", subClass, superClass);
    }

    public IProof<OWLAxiom> toProofOWL(IProof<String> proofStr){
        IInference<OWLAxiom> currentInf;
        List<OWLAxiom> currentPremise;
        OWLAxiom currentConclusion;

        String finalConc = proofStr.getFinalConclusion();
        IProof<OWLAxiom>  proof = new Proof<>(toOWlAxiom(finalConc));

        for(IInference<String> infStr:proofStr.getInferences()){
            currentConclusion = toOWlAxiom(infStr.getConclusion());
            currentPremise = infStr.getPremises().stream().map(x->toOWlAxiom(x)).collect(Collectors.toList());
            currentInf = new Inference<>(currentConclusion,infStr.getRuleName(),currentPremise);

            proof.addInference(currentInf);
        }

        return proof;
    }

    public OWLAxiom toOWlAxiom(String atom){
        if(helper.getPredicateName(atom).equals(TripleAtomsParser.triple)) {
            try {
                return tripleParser.parse(helper.getPredicateArguments(atom));
            } catch (ConceptTranslationError e) {
                throw new RuntimeException(e); //TODO this cant be good
            }
        }
        
        return elParser.parse(helper.getPredicateName(atom), helper.getPredicateArguments(atom));
    }
}
