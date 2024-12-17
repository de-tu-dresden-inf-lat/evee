package de.tu_dresden.inf.lat.evee.nemo;

import de.tu_dresden.inf.lat.evee.nemo.parser.ELAtomsParser;
import de.tu_dresden.inf.lat.evee.nemo.parser.NemoOwlParser;
import de.tu_dresden.inf.lat.evee.nemo.parser.TripleAtomsParser;
import de.tu_dresden.inf.lat.evee.nemo.parser.exceptions.ConceptTranslationError;
import de.tu_dresden.inf.lat.evee.nemo.parser.tools.ParsingHelper;
import de.tu_dresden.inf.lat.evee.proofs.data.Inference;
import de.tu_dresden.inf.lat.evee.proofs.data.Proof;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInference;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProof;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Christian Alrabbaa
 *
 */
public class ParsingTests {

    private String[] testInput = {
        "mainSubClassOf(<http://www.example.com/test/A>, <http://www.example.com/test/B>)",
        "http://rulewerk.semantic-web.org/inferred/subClassOf(<http://www.example.com/test/A>, <http://www.example.com/test/B>)"
    };

    @Test
    public void testToOWLAxiom(){
        NemoOwlParser parser = NemoOwlParser.getInstance();

        for (String axiom : testInput) {
            OWLAxiom owlAxiom = parser.toOWlAxiom(axiom);
            System.out.println(owlAxiom + "\n");
        }
    }


}
