package de.tu_dresden.inf.lat.evee.concreteDomains

import de.tu_dresden.inf.lat.evee.concreteDomains.diffDomain._
import de.tu_dresden.inf.lat.evee.concreteDomains.linearDomain.{GaussianEliminationProver, GaussianEliminationReasoner, ImplicationTracker, LinearConstraint, LinearConstraintInferenceChecker}
import de.tu_dresden.inf.lat.evee.concreteDomains.multDomain._
import org.apache.commons.math3.fraction.BigFraction
import de.tu_dresden.inf.lat.evee.proofs.proofGenerators.TreeProofGenerator
import org.junit.Assert.{assertEquals, assertTrue}
import org.junit.Test
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model.{IRI, OWLDataProperty}

import scala.collection.JavaConverters._

/** The proofs that the constraint reasoners generate for their own conclusions are correct. */
class ConstraintProofTest {

  private def property(name: String): OWLDataProperty =
    OWLManager.getOWLDataFactory.getOWLDataProperty(IRI.create(TestOntologies.NS + name))

  private val (x, y, z) = (property("x"), property("y"), property("z"))

  private def assertDiffProof(premises: Seq[DiffConstraint], goal: DiffConstraint): Unit = {
    val prover = new DiffReasoner()
    prover.setOntology(premises)
    ProofChecker.forConstraints(new DiffReasoner(), premises, goal).assertCorrect(prover.getProof(goal))
  }

  private def assertMultProof(premises: Seq[MultConstraint], goal: MultConstraint): Unit = {
    val prover = new MultReasoner()
    prover.setOntology(premises)
    ProofChecker.forConstraints(new MultReasoner(), premises, goal).assertCorrect(prover.getProof(goal))
  }

  /** also checks that the rule names of the linear combinations give their factors */
  private def assertLinearProof(premises: Seq[LinearConstraint[BigFraction]], goal: LinearConstraint[BigFraction]): Unit = {
    val prover = new GaussianEliminationProver()
    prover.setOntology(premises)
    val proof = prover.getProof(goal)
    ProofChecker.forConstraints(new GaussianEliminationReasoner(), premises, goal).assertCorrect(proof)
    for (inference <- proof.getInferences.asScala if inference.getRuleName.startsWith(ImplicationTracker.RULE_PREFIX))
      assertTrue("wrong factors: " + inference, LinearConstraintInferenceChecker.correctCDInference(inference))
  }

  private def fraction(n: Int) = new BigFraction(n)

  /** the linear equation with the given coefficients and constant */
  private def equation(coefficients: (OWLDataProperty, Int)*)(constant: Int) =
    LinearConstraint[BigFraction](coefficients.map { case (v, c) => v -> fraction(c) }.toMap, fraction(constant))

  @Test def diffSum(): Unit =
    assertDiffProof(Seq(DiffSum(x, fraction(1), y), DiffSum(y, fraction(2), z)), DiffSum(x, fraction(3), z))

  @Test def diffEqual(): Unit =
    assertDiffProof(Seq(DiffEqual(x, fraction(0)), DiffSum(x, fraction(1), y)), DiffEqual(y, fraction(1)))

  @Test def diffGreaterThan(): Unit =
    assertDiffProof(Seq(DiffGreaterThan(x, fraction(0)), DiffSum(x, fraction(1), y)), DiffGreaterThan(y, fraction(1)))

  @Test def diffBoundFromEqual(): Unit =
    assertDiffProof(Seq(DiffEqual(x, fraction(5))), DiffGreaterThan(x, fraction(3)))

  @Test def diffInconsistency(): Unit =
    assertDiffProof(Seq(DiffEqual(x, fraction(0)), DiffGreaterThan(x, fraction(1))), DiffContradiction)

  @Test def multProduct(): Unit =
    assertMultProof(Seq(Multiplication(x, fraction(2), y), Multiplication(y, fraction(3), z)),
      Multiplication(x, fraction(6), z))

  @Test def multLessThan(): Unit =
    assertMultProof(Seq(Multiplication(x, fraction(2), y), MultLessThan(y, fraction(6))), MultLessThan(x, fraction(3)))

  @Test def multBoundFromEqual(): Unit =
    assertMultProof(Seq(MultEqual(x, fraction(5))), MultLessThan(x, fraction(6)))

  @Test def linearSubstitution(): Unit =
    assertLinearProof(Seq(equation(x -> 1, y -> 1)(5), equation(x -> 1)(2)), equation(y -> 1)(3))

  @Test def linearCombination(): Unit =
    assertLinearProof(Seq(equation(x -> 2, y -> 3)(5), equation(x -> 1, y -> -1)(0)), equation(x -> 1)(1))

  @Test def linearScaling(): Unit =
    assertLinearProof(Seq(equation(x -> 2)(4)), equation(x -> 1)(2))

  @Test def linearThreeVariables(): Unit =
    assertLinearProof(Seq(equation(x -> 1, y -> -1)(1), equation(y -> 1, z -> -1)(2), equation(x -> 1)(3)),
      equation(z -> 1)(0))

  @Test def linearInconsistency(): Unit =
    assertLinearProof(Seq(equation(x -> 1)(1), equation(x -> 1)(2)), CDDefaultContradictionGenerator.defaultLCBF)

  /** def(x) is derived from a premise that mentions x */
  @Test def diffDefined(): Unit =
    assertDiffProof(Seq(DiffEqual(x, fraction(5))), DiffSum(x, fraction(0), x))

  @Test def multDefined(): Unit =
    assertMultProof(Seq(Multiplication(x, fraction(2), y)), Multiplication(y, fraction(1), y))

  @Test def linearDefined(): Unit =
    assertLinearProof(Seq(equation(x -> 1, y -> 1)(5)), equation(x -> 0)(0))

  @Test def multInconsistency(): Unit =
    assertMultProof(Seq(MultEqual(x, fraction(5)), MultLessThan(x, fraction(3))), MultContradiction)

  /** from qlinear/example.ofn: eliminating x first takes 3 steps, eliminating y first only 2 */
  private val system = Seq(equation(x -> 3, y -> 4)(22), equation(y -> -2, z -> 1)(12), equation(x -> 1, y -> 1, z -> 1)(10))
  private val combination = equation(y -> 3, z -> 1)(-16)

  /** the number of linear combinations in a proof of minimal tree size */
  private def linearSteps(premises: Seq[LinearConstraint[BigFraction]], maxVariablesForAllOrders: Int): Int = {
    val prover = new GaussianEliminationProver()
    prover.setMaxVariablesForAllOrders(maxVariablesForAllOrders)
    prover.setOntology(premises)
    TreeProofGenerator.getMinimalTreeSizeProof(prover.getProof(combination)).getInferences.asScala
      .count(_.getRuleName.startsWith(ImplicationTracker.RULE_PREFIX))
  }

  @Test def linearAllVariableOrders(): Unit = {
    assertLinearProof(system, combination)
    assertEquals(2, linearSteps(system, 3))
  }

  /** above the limit, the variables are eliminated in the order of their IRIs, however the constraints are written */
  @Test def linearFixedVariableOrder(): Unit = {
    assertEquals(3, linearSteps(system, 2))
    assertEquals(3, linearSteps(system.reverse.map(c => LinearConstraint(c.lhs.toSeq.reverse.toMap, c.rhs)), 2))
  }

  /** the proof of the goal for proofs over OWL, i.e. the complete network, or all paths up to the given limit */
  private def diffProof(premises: Seq[DiffConstraint], goal: DiffConstraint, maxVariablesForAllPaths: Int) = {
    val prover = new DiffReasoner()
    prover.setCompleteReasoning()
    prover.setMaxVariablesForAllPaths(maxVariablesForAllPaths)
    prover.setOntology(premises)
    val proof = prover.getProof(goal)
    ProofChecker.forConstraints(new DiffReasoner(), premises, goal).assertCorrect(proof)
    proof.getInferences.asScala.filterNot(i => i.getPremises.contains(i.getConclusion)).map(_.toString).toSet
  }

  /**
   * from qdiff/toyDrone.ofn: the value of z is also propagated from the derived value of y, whatever the names and
   * the order of the data properties
   */
  @Test def diffAllPropagations(): Unit =
    for (names <- Seq(Seq("x", "y", "z", "w"), Seq("w", "z", "y", "x"), Seq("b", "d", "a", "c"));
         maxVariablesForAllPaths <- Seq(0, ConstraintPropagationReasoner.DEFAULT_MAX_VARIABLES_FOR_ALL_PATHS)) {
      val Seq(x, y, z, w) = names.map(property)
      val premises = Seq(DiffEqual(x, fraction(0)), DiffSum(x, fraction(2), y), DiffSum(y, fraction(2), z),
        DiffSum(z, fraction(1), w), DiffGreaterThan(w, fraction(11)))
      for (order <- Seq(premises, premises.reverse)) {
        val steps = diffProof(order, DiffContradiction, maxVariablesForAllPaths)
        val step = new de.tu_dresden.inf.lat.evee.proofs.data.Inference[DiffConstraint](DiffEqual(z, fraction(4)),
          DiffRules.RULE_PROPAGATE_EQUAL, List[DiffConstraint](DiffEqual(y, fraction(2)), DiffSum(y, fraction(2), z)).asJava)
        assertTrue(names + ", " + maxVariablesForAllPaths + ": " + steps, steps.contains(step.toString))
      }
    }

  /** with contradicting differences, all paths give more derivations than the complete network, which are correct */
  @Test def diffContradictingDifferences(): Unit = {
    val premises = Seq(DiffSum(x, fraction(1), y), DiffSum(y, fraction(1), z), DiffSum(x, fraction(3), z))
    val complete = diffProof(premises, DiffContradiction, 0)
    val allPaths = diffProof(premises, DiffContradiction, 3)
    assertTrue((complete -- allPaths).toString, complete.subsetOf(allPaths))
    assertTrue(allPaths.size > complete.size)
  }

  /** the proof of the goal for proofs over OWL, i.e. the complete network, or all paths up to the given limit */
  private def multProof(premises: Seq[MultConstraint], goal: MultConstraint, maxVariablesForAllPaths: Int) = {
    val prover = new MultReasoner()
    prover.setCompleteReasoning()
    prover.setMaxVariablesForAllPaths(maxVariablesForAllPaths)
    prover.setOntology(premises)
    val proof = prover.getProof(goal)
    ProofChecker.forConstraints(new MultReasoner(), premises, goal).assertCorrect(proof)
    proof.getInferences.asScala.filterNot(i => i.getPremises.contains(i.getConclusion)).map(_.toString).toSet
  }

  /** the value of z is also propagated from the derived value of y, whatever the names and order of the properties */
  @Test def multAllPropagations(): Unit =
    for (names <- Seq(Seq("x", "y", "z", "w"), Seq("w", "z", "y", "x"), Seq("b", "d", "a", "c"));
         maxVariablesForAllPaths <- Seq(0, ConstraintPropagationReasoner.DEFAULT_MAX_VARIABLES_FOR_ALL_PATHS)) {
      val Seq(x, y, z, w) = names.map(property)
      val premises = Seq(MultEqual(x, fraction(1)), Multiplication(x, fraction(2), y), Multiplication(y, fraction(2), z),
        Multiplication(z, fraction(2), w), MultLessThan(w, fraction(5)))
      for (order <- Seq(premises, premises.reverse)) {
        val steps = multProof(order, MultContradiction, maxVariablesForAllPaths)
        val step = new de.tu_dresden.inf.lat.evee.proofs.data.Inference[MultConstraint](MultEqual(z, fraction(4)),
          MultRules.RULE_PROPAGATE_EQUAL, List[MultConstraint](MultEqual(y, fraction(2)), Multiplication(y, fraction(2), z)).asJava)
        assertTrue(names + ", " + maxVariablesForAllPaths + ": " + steps, steps.contains(step.toString))
      }
    }

  /** with contradicting factors, all paths give more derivations than the complete network, which are correct */
  @Test def multContradictingFactors(): Unit = {
    val premises = Seq(Multiplication(x, fraction(2), y), Multiplication(y, fraction(2), z), Multiplication(x, fraction(3), z))
    val complete = multProof(premises, MultContradiction, 0)
    val allPaths = multProof(premises, MultContradiction, 3)
    assertTrue((complete -- allPaths).toString, complete.subsetOf(allPaths))
    assertTrue(allPaths.size > complete.size)
  }
}
