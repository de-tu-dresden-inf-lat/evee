package de.tu_dresden.inf.lat.evee.concreteDomains

import de.tu_dresden.inf.lat.evee.concreteDomains.ReasonerTestSupport.subClassOf
import de.tu_dresden.inf.lat.evee.concreteDomains.TestOntologies.{load, ontology, owlClass}
import de.tu_dresden.inf.lat.evee.concreteDomains.elkCD.ELKCDReasoner
import de.tu_dresden.inf.lat.evee.concreteDomains.owlApi.{ConstraintSizeWeightedTreeSizeMeasure, OWLCDProofGenerator, OWLCDReasonerFactory}
import de.tu_dresden.inf.lat.evee.proofs.proofGenerators.MinimalTreeProofGenerator
import org.junit.Assert.{assertEquals, assertFalse, assertTrue}
import org.junit.Test
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model._
import org.semanticweb.owlapi.reasoner.InferenceType

import scala.collection.JavaConverters._

/**
 * Reasoning and proving leave the user's ontologies as they are: their axioms, annotations, entities, IDs and prefixes,
 * and the ontologies of their manager. The fresh classes of the proofs (auxiliary classes and classes that show
 * constraints) occur only in the proofs.
 */
class OntologyUnchangedTest {

  private val factory = OWLManager.getOWLDataFactory

  private case class Snapshot(axioms: Set[OWLAxiom], annotations: Set[OWLAnnotation], signature: Set[OWLEntity],
                              id: OWLOntologyID, prefixes: Map[String, String])

  private def snapshot(o: OWLOntology) = Snapshot(o.getAxioms().asScala.toSet, o.getAnnotations.asScala.toSet,
    o.getSignature().asScala.toSet, o.getOntologyID, OWLCDProofGenerator.prefixesOf(o))

  /** the snapshots of all ontologies of the manager, which also shows whether ontologies were added or removed */
  private def snapshots(manager: OWLOntologyManager): Map[OWLOntologyID, Snapshot] =
    manager.getOntologies().asScala.map(o => o.getOntologyID -> snapshot(o)).toMap

  /**
   * what Protégé does: classify with PACER (buffering), query, also about an expression that is not in the ontology
   * (as the DL query tab does), prove with the running reasoner (both renderings, with minimisation), and also prove
   * with a reasoner of the generator's own
   */
  private def reasonAndProve(o: OWLOntology, goal: OWLSubClassOfAxiom): Unit = {
    val reasoner = new OWLCDReasonerFactory().createReasoner(o).asInstanceOf[ELKCDReasoner[CDConstraint]]
    try {
      reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY)
      assertTrue(goal + " should be entailed", reasoner.isEntailed(goal))
      val r = factory.getOWLObjectProperty(IRI.create(TestOntologies.NS + "r"))
      assertTrue(reasoner.isSatisfiable(factory.getOWLObjectSomeValuesFrom(r,
        factory.getOWLObjectIntersectionOf(goal.getSubClass, goal.getSuperClass))))
      prove(o, goal, reasoner)
    } finally reasoner.dispose()
    prove(o, goal, null)
  }

  private def prove(o: OWLOntology, goal: OWLSubClassOfAxiom, reasoner: ELKCDReasoner[CDConstraint]): Unit =
    for (inputClassesAsConstraints <- Seq(false, true)) {
      val generator = new OWLCDProofGenerator(reasoner)
      try {
        generator.setInputClassesAsConstraints(inputClassesAsConstraints)
        val minimal = new MinimalTreeProofGenerator[OWLAxiom, OWLOntology](generator,
          new ConstraintSizeWeightedTreeSizeMeasure(generator.constraints))
        minimal.setOntology(o)
        assertFalse(minimal.getProof(goal).getInferences.isEmpty)
      } finally generator.dispose()
    }

  private def assertUnchanged(o: OWLOntology, goal: OWLSubClassOfAxiom): Unit = {
    val before = snapshots(o.getOWLOntologyManager)
    reasonAndProve(o, goal)
    assertEquals(before, snapshots(o.getOWLOntologyManager))
  }

  private def goal(o: OWLOntology, sub: String, sup: String) =
    factory.getOWLSubClassOfAxiom(owlClass(o, sub), owlClass(o, sup))

  @Test def diff(): Unit = {
    val o = load("qdiff/toyDrone2.ofn")
    assertUnchanged(o, goal(o, "Drone", "Hexicopter"))
  }

  @Test def linear(): Unit = {
    val o = load("qlinear/coffee.ofn")
    assertUnchanged(o, goal(o, "Order", "Ristretto"))
  }

  @Test def mult(): Unit = {
    val o = ontology("QMult", Seq("C0" -> ":x = 2", "C1" -> "2 * :x = :y", "C2" -> ":y < 5"),
      "SubClassOf(:A ObjectIntersectionOf(:C0 :C1))")
    assertUnchanged(o, subClassOf("A", "C2"))
  }

  /** the constraints of the imported ontology are used, and both ontologies stay unchanged */
  @Test def imports(): Unit = {
    val manager = OWLManager.createOWLOntologyManager()
    ontology(manager, "http://ex.org/imported", null, Seq("C1" -> ":x > 1"))
    val o = ontology(manager, "http://ex.org/o", "QDiff", Seq("C0" -> ":x = 5"),
      "Import(<http://ex.org/imported>)", "SubClassOf(:A :C0)")
    assertUnchanged(o, subClassOf("A", "C1"))
  }

  /** after an edit and synchronising (flush), as in Protégé, the edited ontology stays as the user left it */
  @Test def editAndSynchronise(): Unit = {
    val o = ontology("QDiff", Seq("C0" -> ":x = 5", "C1" -> ":x > 1", "C2" -> ":x + 1 = :y", "C3" -> ":y > 4"),
      "SubClassOf(:A :C0)")
    val reasoner = new OWLCDReasonerFactory().createReasoner(o).asInstanceOf[ELKCDReasoner[CDConstraint]]
    try {
      assertTrue(reasoner.isEntailed(subClassOf("A", "C1")))
      o.getOWLOntologyManager.addAxiom(o, subClassOf("A", "C2"))
      val edited = snapshots(o.getOWLOntologyManager)
      reasoner.flush()
      assertTrue(reasoner.isEntailed(subClassOf("A", "C3")))
      prove(o, subClassOf("A", "C3"), reasoner)
      assertEquals(edited, snapshots(o.getOWLOntologyManager))
    } finally reasoner.dispose()
  }
}
