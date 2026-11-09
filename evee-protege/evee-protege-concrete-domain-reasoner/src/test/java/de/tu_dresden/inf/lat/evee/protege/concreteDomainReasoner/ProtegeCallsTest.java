package de.tu_dresden.inf.lat.evee.protege.concreteDomainReasoner;

import de.tu_dresden.inf.lat.evee.concreteDomains.CDConstraint;
import de.tu_dresden.inf.lat.evee.concreteDomains.data.exceptions.ParsingException;
import de.tu_dresden.inf.lat.evee.concreteDomains.elkCD.ELKCDReasoner;
import de.tu_dresden.inf.lat.evee.concreteDomains.owlApi.ConstraintSizeWeightedTreeSizeMeasure;
import de.tu_dresden.inf.lat.evee.concreteDomains.owlApi.OWLCDProofGenerator;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInference;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProof;
import de.tu_dresden.inf.lat.evee.proofs.proofGenerators.MinimalTreeProofGenerator;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.PrefixDocumentFormat;
import org.semanticweb.owlapi.io.StringDocumentSource;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.InconsistentOntologyException;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.VersionInfo;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Replays the calls that Protégé makes to PACER and its proof services, on OWL API 4.5 as in Protégé (the library's
 * own tests run on OWL API 5): classify, query, edit and synchronise, prove with the running reasoner.
 */
public class ProtegeCallsTest {

    private static final String NS = "http://ex.org/o#";
    private static final OWLDataFactory factory = OWLManager.getOWLDataFactory();

    /** an ontology with the concrete domain, constraints (class name, constraint) and further axioms */
    private static OWLOntology ontology(OWLOntologyManager manager, String iri, String domain, String[][] constraints,
                                        String... axioms) throws OWLOntologyCreationException {
        StringBuilder text = new StringBuilder("Prefix(:=<" + NS + ">)\nPrefix(cd:=<http://tu-dresden.de/inf/lat/evee/cd#>)\n")
                .append("Ontology(<").append(iri).append(">\n");
        for (String axiom : axioms)
            if (axiom.startsWith("Import("))
                text.append(axiom).append("\n");
        if (domain != null)
            text.append("Annotation(cd:concreteDomain \"").append(domain).append("\")\n");
        for (String[] constraint : constraints)
            text.append("Declaration(Class(:").append(constraint[0]).append("))\n")
                    .append("AnnotationAssertion(cd:constraint :").append(constraint[0]).append(" \"")
                    .append(constraint[1]).append("\")\n");
        for (String axiom : axioms)
            if (!axiom.startsWith("Import("))
                text.append(axiom).append("\n");
        return manager.loadOntologyFromOntologyDocument(new StringDocumentSource(text.append(")\n").toString()));
    }

    private static OWLOntology ontology(String domain, String[][] constraints, String... axioms)
            throws OWLOntologyCreationException {
        return ontology(OWLManager.createOWLOntologyManager(), "http://ex.org/o", domain, constraints, axioms);
    }

    private static OWLClass cls(String name) {
        return factory.getOWLClass(IRI.create(NS + name));
    }

    private static OWLSubClassOfAxiom subClassOf(String sub, String sup) {
        return factory.getOWLSubClassOfAxiom(cls(sub), cls(sup));
    }

    /** the reasoner as Protégé creates it: with the factory of the plugin, buffering */
    private static OWLReasoner reasoner(OWLOntology o) {
        return new ProtegeCDReasonerFactory().getReasonerFactory().createReasoner(o);
    }

    /** x = 5, x + 1 = y and y > 4 */
    private static final String[][] CHAIN = {{"C0", ":x = 5"}, {"C1", ":x + 1 = :y"}, {"C2", ":y > 4"}};

    @Test
    public void runsOnOWLAPI4() {
        assertTrue(VersionInfo.getVersionInfo().getVersion(), VersionInfo.getVersionInfo().getVersion().startsWith("4.5"));
    }

    /** Protégé classifies with precomputeInferences and then asks for the class hierarchy */
    @Test
    public void classifyAndQuery() throws Exception {
        OWLOntology o = ontology("QDiff", CHAIN, "SubClassOf(:A ObjectIntersectionOf(:C0 :C1))");
        OWLReasoner reasoner = reasoner(o);
        try {
            reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
            assertTrue(reasoner.isConsistent());
            assertTrue(reasoner.getSuperClasses(cls("A"), false).getFlattened().contains(cls("C2")));
            assertTrue(reasoner.getSubClasses(cls("C2"), false).getFlattened().contains(cls("A")));
            assertTrue(reasoner.isEntailed(subClassOf("A", "C2")));
            assertFalse(reasoner.isEntailed(subClassOf("C2", "A")));
        } finally {
            reasoner.dispose();
        }
    }

    @Test
    public void allDomains() throws Exception {
        String[][][] cases = {
                {{"QMult"}, {"C0", ":x = 2"}, {"C1", "2 * :x = :y"}, {"C2", ":y < 5"}},
                {{"QLinear"}, {"C0", ":x = 2"}, {"C1", ":x + :y = 5"}, {"C2", ":y = 3"}}};
        for (String[][] c : cases) {
            OWLOntology o = ontology(c[0][0], Arrays.copyOfRange(c, 1, c.length),
                    "SubClassOf(:A ObjectIntersectionOf(:C0 :C1))");
            OWLReasoner reasoner = reasoner(o);
            try {
                reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
                assertTrue(c[0][0], reasoner.isEntailed(subClassOf("A", "C2")));
            } finally {
                reasoner.dispose();
            }
        }
    }

    /** the reasoner buffers changes until Protégé synchronises it with flush */
    @Test
    public void editAndSynchronise() throws Exception {
        OWLOntology o = ontology("QDiff", CHAIN, "SubClassOf(:A :C0)");
        OWLReasoner reasoner = reasoner(o);
        try {
            reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
            assertFalse(reasoner.isEntailed(subClassOf("A", "C2")));
            o.getOWLOntologyManager().addAxiom(o, subClassOf("A", "C1"));
            assertFalse("the change should not be visible before flush", reasoner.isEntailed(subClassOf("A", "C2")));
            reasoner.flush();
            reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
            assertTrue("the change should be visible after flush", reasoner.isEntailed(subClassOf("A", "C2")));
        } finally {
            reasoner.dispose();
        }
    }

    /** Protégé reports an inconsistent ontology if classification throws InconsistentOntologyException */
    @Test
    public void inconsistentOntology() throws Exception {
        OWLOntology o = ontology("QDiff", new String[][]{{"C0", ":x = 0"}, {"C1", ":x > 1"}},
                "ClassAssertion(:C0 :a)", "ClassAssertion(:C1 :a)");
        OWLReasoner reasoner = reasoner(o);
        try {
            assertFalse(reasoner.isConsistent());
            try {
                reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
                fail("classification should report the inconsistency");
            } catch (InconsistentOntologyException expected) {
            }
        } finally {
            reasoner.dispose();
        }
    }

    /** constraints of an imported ontology count, and its prefixes are used to show data properties */
    @Test
    public void importedConstraints() throws Exception {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        manager.loadOntologyFromOntologyDocument(new StringDocumentSource(
                "Prefix(d:=<http://ex.org/data#>)\nPrefix(cd:=<http://tu-dresden.de/inf/lat/evee/cd#>)\n"
                        + "Ontology(<http://ex.org/imported>\nDeclaration(Class(<" + NS + "C1>))\n"
                        + "AnnotationAssertion(cd:constraint <" + NS + "C1> \"d:x > 1\")\n)\n"));
        OWLOntology o = ontology(manager, "http://ex.org/o", "QDiff",
                new String[][]{{"C0", "<http://ex.org/data#x> = 5"}}, "Import(<http://ex.org/imported>)",
                "SubClassOf(:A :C0)");
        OWLReasoner reasoner = reasoner(o);
        try {
            assertTrue(reasoner.isEntailed(subClassOf("A", "C1")));
            assertEquals("http://ex.org/data#", OWLCDProofGenerator.prefixesOfImportsClosure(o).get("d:").get());
        } finally {
            reasoner.dispose();
        }
    }

    /** the proof services minimise the proofs of the running reasoner; derived constraints are shown as classes */
    @Test
    public void proveWithRunningReasoner() throws Exception {
        OWLOntology o = ontology("QDiff", CHAIN, "SubClassOf(:A ObjectIntersectionOf(:C0 :C1))");
        OWLReasoner reasoner = reasoner(o);
        try {
            reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
            IProof<OWLAxiom> proof = prove(o, reasoner, subClassOf("A", "C2"));
            assertEquals(subClassOf("A", "C2"), proof.getFinalConclusion());
            Set<String> shown = new HashSet<>();
            for (IInference<OWLAxiom> inference : proof.getInferences())
                for (OWLClass c : inference.getConclusion().getClassesInSignature())
                    if (c.getIRI().toString().startsWith(OWLCDProofGenerator.CONSTRAINT_CLASS_NAMESPACE()))
                        shown.add(c.getIRI().toString().substring(OWLCDProofGenerator.CONSTRAINT_CLASS_NAMESPACE().length()));
            assertTrue(shown.toString(), shown.contains(":y = 6"));
        } finally {
            reasoner.dispose();
        }
    }

    @SuppressWarnings("unchecked")
    private static IProof<OWLAxiom> prove(OWLOntology o, OWLReasoner reasoner, OWLAxiom goal) throws Exception {
        OWLCDProofGenerator generator = new OWLCDProofGenerator((ELKCDReasoner<CDConstraint>) reasoner);
        try {
            MinimalTreeProofGenerator<OWLAxiom, OWLOntology> minimal = new MinimalTreeProofGenerator<>(generator,
                    new ConstraintSizeWeightedTreeSizeMeasure(generator.constraints()));
            minimal.setOntology(o);
            IProof<OWLAxiom> proof = minimal.getProof(goal);
            assertFalse(proof.getInferences().isEmpty());
            return proof;
        } finally {
            generator.dispose();
        }
    }

    /** reasoning and proving leave the ontology, its prefixes and its manager as they were */
    @Test
    public void ontologyUnchanged() throws Exception {
        OWLOntology o = ontology("QDiff", CHAIN, "SubClassOf(:A ObjectIntersectionOf(:C0 :C1))");
        OWLOntologyManager manager = o.getOWLOntologyManager();
        Set<OWLAxiom> axioms = new HashSet<>(o.getAxioms());
        Set<OWLAnnotation> annotations = new HashSet<>(o.getAnnotations());
        Map<String, String> prefixes = new HashMap<>(
                ((PrefixDocumentFormat) manager.getOntologyFormat(o)).getPrefixName2PrefixMap());
        Set<OWLOntology> ontologies = new HashSet<>(manager.getOntologies());
        OWLReasoner reasoner = reasoner(o);
        try {
            reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
            prove(o, reasoner, subClassOf("A", "C2"));
        } finally {
            reasoner.dispose();
        }
        assertEquals(axioms, o.getAxioms());
        assertEquals(annotations, o.getAnnotations());
        assertEquals(prefixes, ((PrefixDocumentFormat) manager.getOntologyFormat(o)).getPrefixName2PrefixMap());
        assertEquals(ontologies, manager.getOntologies());
    }

    /** a constraint that cannot be read is reported by the factory (with a dialog in Protégé) and passed on */
    @Test
    public void factoryErrorIsPassedOn() throws Exception {
        OWLOntology o = ontology("QMult", new String[][]{{"C0", ":x - 0.5 * :y = 0"}});
        try {
            reasoner(o).dispose();
            fail("the reasoner should not be created");
        } catch (ParsingException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("in QMult"));
        }
    }
}
