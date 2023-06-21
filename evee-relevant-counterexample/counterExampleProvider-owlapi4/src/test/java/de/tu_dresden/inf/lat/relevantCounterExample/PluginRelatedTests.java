package de.tu_dresden.inf.lat.relevantCounterExample;

import com.github.jsonldjava.shaded.com.google.common.collect.Sets;
import de.tu_dresden.inf.lat.counterExample.data.ModelType;
import de.tu_dresden.inf.lat.counterExample.relevantExamplesGenerators.ELKRelevantCounterexampleGenerator;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.elk.owlapi.ElkReasoner;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import static org.junit.Assert.*;

public class PluginRelatedTests {
    private static OWLOntologyManager manager;
    private static OWLDataFactory factory;
    private static OWLOntology sushiOnt, pizzaOnt;

    @BeforeClass
    public static void init() throws OWLOntologyCreationException {
        manager = OWLManager.createOWLOntologyManager();
        factory = manager.getOWLDataFactory();

        sushiOnt = manager.loadOntologyFromOntologyDocument(
                Objects.requireNonNull(RelevantCounterExamplesTest.class.getClassLoader().getResourceAsStream(
                        "ontologies/Sushi_X.owl")));
        pizzaOnt =  manager.loadOntologyFromOntologyDocument(
                Objects.requireNonNull(RelevantCounterExamplesTest.class.getClassLoader().getResourceAsStream(
                        "ontologies/pizza.owl")));
    }

    @Test
    public void test1() {

        OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(factory.getOWLClass(IRI.create("http://www.example.org/sushi_x#California_maki")),
                factory.getOWLClass(IRI.create("http://www.example.org/sushi_x#Vegan_food")));

        ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
        ElkReasoner reasoner = reasonerFactory.createReasoner(sushiOnt);

        assertFalse(reasoner.isEntailed(conclusion));

        ELKRelevantCounterexampleGenerator cegAlpha = new ELKRelevantCounterexampleGenerator(ModelType.Beta);
        cegAlpha.setOntology(sushiOnt);
        cegAlpha.setObservation(Sets.newHashSet(conclusion));
        cegAlpha.setSignature(Collections.emptySet());

        cegAlpha.generateModel();
    }

    @Test
    public void test2() {

        OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(factory.getOWLClass(IRI.create("http://www.co-ode.org/ontologies/pizza/pizza.owl#Pizza")),
                factory.getOWLClass(IRI.create("http://www.co-ode.org/ontologies/pizza/pizza.owl#IceCream")));

        ELKRelevantCounterexampleGenerator cegAlpha = new ELKRelevantCounterexampleGenerator(ModelType.Alpha);
        ELKRelevantCounterexampleGenerator cegBeta = new ELKRelevantCounterexampleGenerator(ModelType.Beta);
        ELKRelevantCounterexampleGenerator cegDiff = new ELKRelevantCounterexampleGenerator(ModelType.Diff);
        ELKRelevantCounterexampleGenerator cegFlatDiff = new ELKRelevantCounterexampleGenerator(ModelType.FlatDiff);

        cegAlpha.setOntology(pizzaOnt);
        cegAlpha.setObservation(Sets.newHashSet(conclusion));
        cegAlpha.setSignature(Collections.emptySet());

        cegBeta.setOntology(pizzaOnt);
        cegBeta.setObservation(Sets.newHashSet(conclusion));
        cegBeta.setSignature(Collections.emptySet());

        cegDiff.setOntology(pizzaOnt);
        cegDiff.setObservation(Sets.newHashSet(conclusion));
        cegDiff.setSignature(Collections.emptySet());

        cegFlatDiff.setOntology(pizzaOnt);
        cegFlatDiff.setObservation(Sets.newHashSet(conclusion));
        cegFlatDiff.setSignature(Collections.emptySet());

        Set<OWLIndividualAxiom> modelAlpha =  cegAlpha.generateModel();
        Set<OWLIndividualAxiom> modelBeta =  cegBeta.generateModel();
        Set<OWLIndividualAxiom> modelDiff =  cegDiff.generateModel();
        Set<OWLIndividualAxiom> modelFlatDiff =  cegFlatDiff.generateModel();

        assertEquals(modelAlpha, modelBeta);
        assertEquals(modelBeta, modelDiff);
        assertEquals(modelDiff, modelFlatDiff);

        assertFalse(cegAlpha.isModelTypeReverted());
        assertTrue(cegBeta.isModelTypeReverted());
    }

    @Test
    public void test3() {

        OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(factory.getOWLClass(IRI.create("http://www.co-ode.org/ontologies/pizza/pizza.owl#Fiorentina")),
                factory.getOWLClass(IRI.create("http://www.co-ode.org/ontologies/pizza/pizza.owl#SpicyPizza")));

        ELKRelevantCounterexampleGenerator cegDiff = new ELKRelevantCounterexampleGenerator(ModelType.Diff);
        ELKRelevantCounterexampleGenerator cegFlatDiff = new ELKRelevantCounterexampleGenerator(ModelType.FlatDiff);

        cegDiff.setOntology(pizzaOnt);
        cegDiff.setObservation(Sets.newHashSet(conclusion));
        cegDiff.setSignature(Collections.emptySet());

        cegFlatDiff.setOntology(pizzaOnt);
        cegFlatDiff.setObservation(Sets.newHashSet(conclusion));
        cegFlatDiff.setSignature(Collections.emptySet());

        cegDiff.generateModel();
        cegFlatDiff.generateModel();

        assertEquals(2, cegDiff.getMarkedIndividuals().stream().filter(Objects::nonNull).count());
        assertEquals(2, cegFlatDiff.getMarkedIndividuals().stream().filter(Objects::nonNull).count());

        assertFalse(cegDiff.isModelTypeReverted());
        assertEquals(cegDiff.isModelTypeReverted(), cegFlatDiff.isModelTypeReverted());
    }
}
