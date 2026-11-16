package de.tu_dresden.inf.lat.evee.nemo;

import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/** The ontology given to Nemo gets the axioms for the goal expressions, but the user's ontology stays unchanged. */
public class OntologyUnchangedTest {

    @Test
    public void goalAxiomsOnlyInTheExportedCopy() throws Exception {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLClass a = factory.getOWLClass(IRI.create("http://ex.org/A"));
        OWLClass b = factory.getOWLClass(IRI.create("http://ex.org/B"));
        OWLClass goal = factory.getOWLClass(IRI.create("http://ex.org/Goal"));
        OWLOntology ontology = manager.createOntology(IRI.create("http://ex.org/o"));
        manager.addAxiom(ontology, factory.getOWLSubClassOfAxiom(a, b));
        Set<OWLAxiom> before = new HashSet<>(ontology.getAxioms());

        Path importDir = new NemoReasoner().prepareImportDir(ontology, Collections.singleton(goal));

        assertEquals(before, ontology.getAxioms());
        File exported = importDir.resolve("ont.ttl").toFile();
        OWLOntology copy = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(exported);
        assertTrue(copy.containsAxiom(factory.getOWLSubClassOfAxiom(a, b)));
        assertTrue(copy.containsAxiom(factory.getOWLSubClassOfAxiom(goal, goal)));
        exported.delete();
        importDir.toFile().delete();
    }
}
