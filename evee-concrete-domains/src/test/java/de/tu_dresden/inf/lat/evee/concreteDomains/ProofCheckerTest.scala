package de.tu_dresden.inf.lat.evee.concreteDomains

import de.tu_dresden.inf.lat.evee.concreteDomains.ReasonerTestSupport.subClassOf
import de.tu_dresden.inf.lat.evee.concreteDomains.TestOntologies.{cls, ontology}
import de.tu_dresden.inf.lat.evee.concreteDomains.data.extendedDataStructure.parser.ExtendedOntologyParser
import de.tu_dresden.inf.lat.evee.concreteDomains.diffDomain.{DiffConstraint, DiffReasoner, DiffSum}
import de.tu_dresden.inf.lat.evee.concreteDomains.linearDomain.{GaussianEliminationReasoner, LinearConstraint}
import de.tu_dresden.inf.lat.evee.concreteDomains.multDomain.{MultConstraint, MultLessThan, MultReasoner, Multiplication}
import de.tu_dresden.inf.lat.evee.proofs.data.{Inference, Proof}
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInference
import de.tu_dresden.inf.lat.evee.proofs.tools.ProofTools
import org.apache.commons.math3.fraction.BigFraction
import org.junit.Assert.assertTrue
import org.junit.Test
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model.{IRI, OWLAxiom, OWLClass, OWLDataProperty, OWLOntology}

import scala.collection.JavaConverters._

/**
 * The proof checker finds the problems in proofs that combine OWL and concrete domain inferences, and in proofs
 * that consist only of constraints.
 */
class ProofCheckerTest {

  /** C0: x = 5, C1: x > 3, C2: x > 7 */
  private val example = ontology("QDiff",
    Seq("C0" -> ":x = 5", "C1" -> ":x > 3", "C2" -> ":x > 7"),
    "SubClassOf(:A :C0)", "SubClassOf(:C1 :B)")

  private def asserted(axiom: OWLAxiom): IInference[OWLAxiom] =
    new Inference[OWLAxiom](axiom, ProofTools.ASSERTED_1, List[OWLAxiom]().asJava)

  private def inference(conclusion: OWLAxiom, premises: OWLAxiom*): IInference[OWLAxiom] =
    new Inference[OWLAxiom](conclusion, "rule", premises.asJava)

  /** the problems found in a proof of the goal that has the given final conclusion and inferences */
  private def problems(ontology: OWLOntology, goal: OWLAxiom, finalConclusion: OWLAxiom,
                       inferences: IInference[OWLAxiom]*): List[String] = {
    val constraints = ExtendedOntologyParser.toExtendedOntology(ontology).constraintMap
      .asInstanceOf[Map[OWLClass, DiffConstraint]]
    ProofChecker.forOWL(ontology, constraints, new DiffReasoner(), goal)
      .problems(new Proof[OWLAxiom](finalConclusion, inferences.asJava))
  }

  private def problems(goal: OWLAxiom, inferences: IInference[OWLAxiom]*): List[String] =
    problems(example, goal, goal, inferences: _*)

  /** the problems found are exactly one for each expected message part */
  private def assertProblems(found: List[String], expected: String*): Unit =
    assertTrue("problems found:\n" + found.mkString("\n"),
      found.size == expected.size && expected.forall(part => found.exists(_.contains(part))))

  /** A ⊑ C0 (x = 5) implies A ⊑ C1 (x > 3), and C1 ⊑ B */
  private val correct = Seq(
    asserted(subClassOf("A", "C0")),
    inference(subClassOf("A", "C1"), subClassOf("A", "C0")),
    asserted(subClassOf("C1", "B")),
    inference(subClassOf("A", "B"), subClassOf("A", "C1"), subClassOf("C1", "B")))

  @Test def correctProof(): Unit =
    assertProblems(problems(subClassOf("A", "B"), correct: _*))

  @Test def wrongGoal(): Unit =
    assertProblems(problems(example, subClassOf("A", "C1"), subClassOf("A", "B"), correct: _*),
      "is not the goal")

  @Test def assertedAxiomNotInOntology(): Unit = assertProblems(problems(subClassOf("A", "B"),
    asserted(subClassOf("A", "C1")),
    asserted(subClassOf("C1", "B")),
    inference(subClassOf("A", "B"), subClassOf("A", "C1"), subClassOf("C1", "B"))),
    "is not asserted")

  @Test def missingInference(): Unit = assertProblems(problems(subClassOf("A", "B"),
    asserted(subClassOf("C1", "B")),
    inference(subClassOf("A", "B"), subClassOf("A", "C1"), subClassOf("C1", "B"))),
    "no complete and acyclic derivation")

  @Test def cyclicDerivation(): Unit = assertProblems(problems(subClassOf("A", "B"),
    inference(subClassOf("A", "C1"), subClassOf("A", "C1")),
    asserted(subClassOf("C1", "B")),
    inference(subClassOf("A", "B"), subClassOf("A", "C1"), subClassOf("C1", "B"))),
    "no complete and acyclic derivation")

  @Test def unsoundConcreteDomainInference(): Unit = assertProblems(problems(subClassOf("A", "C2"),
    asserted(subClassOf("A", "C0")),
    inference(subClassOf("A", "C2"), subClassOf("A", "C0"))),
    "unsound inference")

  @Test def unsoundOWLInference(): Unit = assertProblems(problems(subClassOf("A", "B"),
    asserted(subClassOf("A", "C0")),
    asserted(subClassOf("C1", "B")),
    inference(subClassOf("A", "B"), subClassOf("A", "C0"), subClassOf("C1", "B"))),
    "unsound inference")

  /** x + 0 = x holds exactly for the individuals that have a value for x */
  private val tautology = ontology("QDiff", Seq("C0" -> ":x = 5", "C3" -> ":x + 0 = :x"))

  @Test def tautologyForConstrainedFeature(): Unit = assertProblems(
    problems(tautology, subClassOf("C0", "C3"), subClassOf("C0", "C3"), inference(subClassOf("C0", "C3"))))

  @Test def tautologyForUnconstrainedFeature(): Unit = assertProblems(
    problems(tautology, subClassOf("A", "C3"), subClassOf("A", "C3"), inference(subClassOf("A", "C3"))),
    "unsound inference")

  private val Seq(x, y, z) = Seq("x", "y", "z")
    .map(name => OWLManager.getOWLDataFactory.getOWLDataProperty(IRI.create(TestOntologies.NS + name)))

  /**
   * the problems found in the proof of the goal that consists of the asserted premises and the given steps, each
   * from a list of premises to a conclusion
   */
  private def problems[C <: CDConstraint](reasoner: CDReasoner[C], premises: Seq[C], goal: C,
                                          steps: (Seq[C], C)*): List[String] = {
    val inferences: Seq[IInference[C]] =
      premises.map(p => new Inference[C](p, ProofTools.ASSERTED_1, List[C]().asJava)) ++
        steps.map { case (from, to) => new Inference[C](to, "rule", from.asJava) }
    ProofChecker.forConstraints(reasoner, premises, goal).problems(new Proof[C](goal, inferences.asJava))
  }

  @Test def unsoundDiffInference(): Unit = {
    val premises = Seq[DiffConstraint](DiffSum(x, 1, y), DiffSum(y, 2, z))
    assertProblems(problems(new DiffReasoner(), premises, DiffSum(x, 5, z), premises -> DiffSum(x, 5, z)),
      "unsound inference")
  }

  @Test def unsoundMultInference(): Unit = {
    val premises = Seq[MultConstraint](Multiplication(x, new BigFraction(2), y), MultLessThan(y, new BigFraction(6)))
    val goal = MultLessThan(x, new BigFraction(2))
    assertProblems(problems(new MultReasoner(), premises, goal, premises -> goal), "unsound inference")
  }

  private def linear(coefficients: (OWLDataProperty, Int)*)(constant: Int) =
    LinearConstraint[BigFraction](coefficients.map { case (p, c) => p -> new BigFraction(c) }.toMap, new BigFraction(constant))

  /** x + y = 5, x = 2 */
  private val linearPremises = Seq(linear(x -> 1, y -> 1)(5), linear(x -> 1)(2))

  @Test def linearInference(): Unit = assertProblems(problems(new GaussianEliminationReasoner(),
    linearPremises, linear(y -> 1)(3), linearPremises -> linear(y -> 1)(3)))

  @Test def unsoundLinearInference(): Unit = assertProblems(problems(new GaussianEliminationReasoner(),
    linearPremises, linear(y -> 1)(4), linearPremises -> linear(y -> 1)(4)),
    "unsound inference")

  @Test def linearContradiction(): Unit = {
    val premises = linearPremises :+ linear(y -> 1)(4)
    val contradiction = linear()(1) // 0 = 1
    assertProblems(problems(new GaussianEliminationReasoner(), premises, contradiction, premises -> contradiction))
  }

  @Test def concreteDomainContradiction(): Unit = {
    val contradicting = ontology("QDiff", Seq("C0" -> ":x = 5", "C2" -> ":x > 7"),
      "SubClassOf(:A :C0)", "SubClassOf(:A :C2)")
    val unsatisfiable = OWLManager.getOWLDataFactory.getOWLSubClassOfAxiom(cls("A"), OWLManager.getOWLDataFactory.getOWLNothing)
    assertProblems(problems(contradicting, unsatisfiable, unsatisfiable,
      asserted(subClassOf("A", "C0")),
      asserted(subClassOf("A", "C2")),
      inference(unsatisfiable, subClassOf("A", "C0"), subClassOf("A", "C2"))))
  }
}
