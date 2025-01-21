package de.tu_dresden.inf.lat.evee.nemo;

import de.tu_dresden.inf.lat.evee.nemo.parser.TripleAtomsParser;
import de.tu_dresden.inf.lat.evee.nemo.parser.exceptions.ConceptTranslationError;
import de.tu_dresden.inf.lat.evee.nemo.parser.tools.ParsingHelper;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;

/**
 * @author Christian Alrabbaa
 *
 */
public class ParsingTests {

    ParsingHelper helper = ParsingHelper.getInstance();

    private String[] predicateInput = {
        "mainSubClassOf(<http://www.example.com/test/A>, <http://www.example.com/test/B>)",
        "http://rulewerk.semantic-web.org/inferred/subClassOf(<http://www.example.com/test/C>,<http://www.example.com/test/D>)",
        "unkownPredicate(<ArgumentA>, <ArgumentB>)"
    };

    // @Test
    // public void testToOWLAxiom(){
    //     //AtomParser atomParser = new AtomParser()
    //     NemoProofParser parser = NemoProofParser.getInstance();

    //     for (String axiom : testInput) {
    //         OWLAxiom owlAxiom = parser.toOWlAxiom(axiom);
    //         System.out.println(owlAxiom + "\n");
    //     }
    // }
    
    @Test
    public void TestTriplePlaceholderParsing() throws ConceptTranslationError{

        Set<List<String>> parsingBase = new HashSet<>();
        //_:1 = Er.A
        parsingBase.add(helper.getPredicateArguments("TRIPLE(_:1, <http://www.w3.org/2002/07/owl#someValuesFrom>, <http://www.w3.org/2002/07/A>)"));
        parsingBase.add(helper.getPredicateArguments(	"TRIPLE(_:1, <http://www.w3.org/2002/07/owl#onProperty>, <http://www.w3.org/2002/07/r>)"));

        //_:2 = Er.(Es.B)   where Es.B = _:3
        parsingBase.add(helper.getPredicateArguments("TRIPLE(_:2, <http://www.w3.org/2002/07/owl#someValuesFrom>, _:3)"));
        parsingBase.add(helper.getPredicateArguments("TRIPLE(_:2, <http://www.w3.org/2002/07/owl#onProperty>, <http://www.w3.org/2002/07/r>)"));
        parsingBase.add(helper.getPredicateArguments("TRIPLE(_:3, <http://www.w3.org/2002/07/owl#someValuesFrom>, <http://www.w3.org/2002/07/B>)"));
        parsingBase.add(helper.getPredicateArguments("TRIPLE(_:3, <http://www.w3.org/2002/07/owl#onProperty>, <http://www.w3.org/2002/07/s>)"));

        //_:4 = A n B n C
        parsingBase.add(helper.getPredicateArguments("TRIPLE(_:4, <http://www.w3.org/2002/07/owl#intersectionOf>, _:5)"));
        parsingBase.add(helper.getPredicateArguments("TRIPLE(_:5, <http://www.w3.org/1999/02/22-rdf-syntax-ns#first>, <http://www.w3.org/2002/07/A>)"));
        parsingBase.add(helper.getPredicateArguments("TRIPLE(_:5, <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest>, _:6)"));
        parsingBase.add(helper.getPredicateArguments("TRIPLE(_:6, <http://www.w3.org/1999/02/22-rdf-syntax-ns#first>, <http://www.w3.org/2002/07/B>)"));
        parsingBase.add(helper.getPredicateArguments("TRIPLE(_:6, <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest>, _:7)"));
        parsingBase.add(helper.getPredicateArguments("TRIPLE(_:7, <http://www.w3.org/1999/02/22-rdf-syntax-ns#first>, <http://www.w3.org/2002/07/C>)"));
        parsingBase.add(helper.getPredicateArguments("TRIPLE(_:7, <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest>, <http://www.w3.org/1999/02/22-rdf-syntax-ns#nil>)"));


        TripleAtomsParser tripleParser = new TripleAtomsParser();
        tripleParser.setTripleFacts(parsingBase);

        OWLClassExpression placeholder1 = tripleParser.getConceptFromPlaceholder("_:1");
        OWLClassExpression placeholder2 = tripleParser.getConceptFromPlaceholder("_:2");
        OWLClassExpression placeholder3 = tripleParser.getConceptFromPlaceholder("_:3");
        OWLClassExpression placeholder4 = tripleParser.getConceptFromPlaceholder("_:4");


        System.out.println(placeholder1);
        assertTrue(placeholder1 instanceof OWLObjectSomeValuesFrom);

        System.out.println(placeholder2);
        assertTrue(placeholder2 instanceof OWLObjectSomeValuesFrom);

        System.out.println(placeholder3);
        assertTrue(placeholder3 instanceof OWLObjectSomeValuesFrom);

        System.out.println(placeholder4);
        assertTrue(placeholder4 instanceof OWLObjectIntersectionOf);
    }

    @Test
    public void TestHelperGetPredicateName(){

        String[] expected = {
            "mainSubClassOf",
            "http://rulewerk.semantic-web.org/inferred/subClassOf",
            "unkownPredicate"
        };

        for (int i = 0; i < predicateInput.length; i++) {
            assertEquals(expected[i], helper.getPredicateName(predicateInput[i]));
        }
    }

    @Test
    public void TestHelperGetPredicateArgs(){

        String[][] expected = {
            {"<http://www.example.com/test/A>", "<http://www.example.com/test/B>"},
            {"<http://www.example.com/test/C>", "<http://www.example.com/test/D>"},
            {"<ArgumentA>", "<ArgumentB>"},
        };

        for (int i = 0; i < predicateInput.length; i++) {
            List<String> args = helper.getPredicateArguments(predicateInput[i]);

            assertArrayEquals(expected[i], args.toArray());
        }
    }

    @Test
    public void TestHelperGetPlaceholders(){

        String[] input = {
            "synConj(_:8, <http://www.w3.org/2002/07/K>, _:7)",
            "prepareSco(_:8, <http://www.w3.org/2002/07/B>)",
            "TRIPLE(_:7, <http://www.w3.org/2002/07/owl#onProperty>, <http://www.w3.org/2002/07/r>)",
            "http://rulewerk.semantic-web.org/normalForm/conj(_:39, _:32, _:34)",
            "mainSubClassOf(<http://www.example.com/test/A>, <http://www.example.com/test/B>)",

        };

        String[][] expected = {
            {"_:8", "_:7"},
            {"_:8"},
            {"_:7"},
            {"_:39", "_:32", "_:34"},
            {}
        };

        for (int i = 0; i < input.length; i++) {
            List<String> placeholders = helper.getPlaceholders(input[i]);

            assertArrayEquals(expected[i], placeholders.toArray());
        }
    }

    @Test
    public void TestHelperIsTriple(){

        String[] input = {
            "TRIPLE(arg1, pred, arg2)",
            "TRIPLE(_:7, <http://www.w3.org/2002/07/owl#onProperty>, <http://www.w3.org/2002/07/r>)",
            "TRIPLE()",
            "TRIPLE(",
            "synConj(_:8, <http://www.w3.org/2002/07/K>, _:7)"
        };

        boolean [] expected = {true, true, true, false, false};

        for (int i = 0; i < input.length; i++) {
            assertEquals(expected[i], helper.isRdfTriple(input[i]));
        }
    }


}
