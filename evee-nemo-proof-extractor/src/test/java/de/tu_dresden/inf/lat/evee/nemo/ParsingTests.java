package de.tu_dresden.inf.lat.evee.nemo;

import de.tu_dresden.inf.lat.evee.nemo.parser.ELAtomsParser;
import de.tu_dresden.inf.lat.evee.nemo.parser.TripleAtomsParser;
import de.tu_dresden.inf.lat.evee.nemo.parser.exceptions.ConceptTranslationError;
import de.tu_dresden.inf.lat.evee.nemo.parser.tools.ParsingHelper;
import de.tu_dresden.inf.lat.evee.proofs.data.Inference;
import de.tu_dresden.inf.lat.evee.proofs.data.Proof;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInference;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProof;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Christian Alrabbaa
 *
 */
public class ParsingTests {

    @Test
    public void parsingTest1()  {
        Set<List<String>> tripleAtomsArgs = null;
        IProof<String> proofStr = null;

        //Triple atoms Parser
        TripleAtomsParser tp = new TripleAtomsParser(tripleAtomsArgs);

        //EL atoms parser
        ELAtomsParser ep = ELAtomsParser.getInstance();

        IProof<OWLAxiom> proof;

        IInference<OWLAxiom> currentInf;
        List<OWLAxiom> currentPremise;
        OWLAxiom currentConclusion;

        proof = new Proof<>(toOWlAxiom(proofStr.getFinalConclusion(),tp,ep));
        for(IInference<String> infStr:proofStr.getInferences()){
            currentConclusion = toOWlAxiom(infStr.getConclusion(),tp,ep);
            currentPremise = infStr.getPremises().stream().map(x->toOWlAxiom(x,tp,ep)).collect(Collectors.toList());
            currentInf = new Inference<>(currentConclusion,infStr.getRuleName(),currentPremise);

            proof.addInference(currentInf);
        }

        proof.getInferences().forEach(System.out::println);
    }

    private OWLAxiom toOWlAxiom(String atom, TripleAtomsParser tp, ELAtomsParser ep){
        ParsingHelper helper = ParsingHelper.getInstance();
        if(helper.getPredicateName(atom).equals(TripleAtomsParser.triple)) {
            try {
                return tp.parse(helper.getPredicateArguments(atom));
            } catch (ConceptTranslationError e) {
                throw new RuntimeException(e);
            }
        }
        return ep.parse(helper.getPredicateName(atom), helper.getPredicateArguments(atom));
    }


}
