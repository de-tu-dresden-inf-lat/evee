package de.tu_dresden.inf.lat.evee.nemo.parser;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import de.tu_dresden.inf.lat.evee.nemo.parser.exceptions.ConceptTranslationError;
import de.tu_dresden.inf.lat.evee.nemo.parser.tools.ParsingHelper;
import de.tu_dresden.inf.lat.evee.proofs.data.Inference;
import de.tu_dresden.inf.lat.evee.proofs.data.Proof;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInference;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProof;

public class NemoProofParser {

    //Set<List<String>> tripleAtomsArgs = null; //TODO: wat is zis?
   //  = new TripleAtomsParser(Collections.emptySet());
    private final ParsingHelper helper = ParsingHelper.getInstance();
    private final TripleAtomsParser tripleParser;
    private final AbstractAtomParser atomParser;

    
    public NemoProofParser(AbstractAtomParser atomParser, TripleAtomsParser tripleParser) {
        this.tripleParser = tripleParser;
        this.atomParser = atomParser;
    }

    // TODO move method somewhere else?
    public String subClassAxiomToNemoString(OWLSubClassOfAxiom axiom){
        String superClass = axiom.getSuperClass().asOWLClass().getIRI().toString();
        String subClass = axiom.getSubClass().asOWLClass().getIRI().toString();

        return String.format("mainSubClassOf(<%s>,<%s>)", subClass, superClass);
    }

    public IProof<OWLAxiom> toProofOWL(IProof<String> proofStr){
        IInference<OWLAxiom> currentInf;
        List<OWLAxiom> currentPremise;
        OWLAxiom currentConclusion;

        tripleParser.setTripleFacts(getPlaceholderTriples(proofStr.getInferences()));

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
        
        return atomParser.parse(helper.getPredicateName(atom), helper.getPredicateArguments(atom));
    }

    public Set<List<String>> getPlaceholderTriples(List<IInference<String>> inferences){

        Set<List<String>> relevantTriples = new HashSet<List<String>>();

        for(IInference<String> inf : inferences){
            String conc = inf.getConclusion();

            if(helper.isRdfTriple(conc) && helper.containsPlaceholders(conc)){
                relevantTriples.add(helper.getPredicateArguments(conc));
            }
            else if(helper.getPredicateName(conc).equals(TripleAtomsParser.repOf) && 
                    helper.countPlaceholders(conc) == 2){ //TODO optimize
                
                List<String> args = helper.getPredicateArguments(conc);
                
                //to match triple format
                args.add(1, helper.getPredicateName(conc));

                relevantTriples.add(args);
            }
        }
        
        return relevantTriples;
    }
}
