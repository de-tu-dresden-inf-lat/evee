package de.tu_dresden.inf.lat.evee.nemo;

import de.tu_dresden.inf.lat.evee.nemo.parser.NemoOwlParser;
import de.tu_dresden.inf.lat.evee.nemo.parser.tools.ParsingHelper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.List;

import org.junit.Test;
import org.semanticweb.owlapi.model.OWLAxiom;

/**
 * @author Christian Alrabbaa
 *
 */
public class ParsingTests {

    private String[] testInput = {
        "mainSubClassOf(<http://www.example.com/test/A>, <http://www.example.com/test/B>)",
        "http://rulewerk.semantic-web.org/inferred/subClassOf(<http://www.example.com/test/C>,<http://www.example.com/test/D>)",
        "unkownPredicate(<ArgumentA>, <ArgumentB>)"
    };

    @Test
    public void testToOWLAxiom(){
        NemoOwlParser parser = NemoOwlParser.getInstance();

        for (String axiom : testInput) {
            OWLAxiom owlAxiom = parser.toOWlAxiom(axiom);
            System.out.println(owlAxiom + "\n");
        }
    }    

    @Test
    public void TestHelperGetPredicateName(){
        ParsingHelper helper = ParsingHelper.getInstance();

        String[] expected = {
            "mainSubClassOf",
            "http://rulewerk.semantic-web.org/inferred/subClassOf",
            "unkownPredicate"
        };

        for (int i = 0; i < testInput.length; i++) {
            assertEquals(expected[i], helper.getPredicateName(testInput[i]));
        }
    }

    @Test
    public void TestHelperGetPredicateArgs(){
        ParsingHelper helper = ParsingHelper.getInstance();

        String[][] expected = {
            {"<http://www.example.com/test/A>", "<http://www.example.com/test/B>"},
            {"<http://www.example.com/test/C>", "<http://www.example.com/test/D>"},
            {"<ArgumentA>", "<ArgumentB>"},
        };

        for (int i = 0; i < testInput.length; i++) {
            List<String> args = helper.getPredicateArguments(testInput[i]);

            assertFalse("result of input " + testInput[i] +" is empty", args.isEmpty());
            assertEquals(expected[i][0], args.get(0));
            assertEquals(expected[i][1], args.get(1));
        }
    }


}
