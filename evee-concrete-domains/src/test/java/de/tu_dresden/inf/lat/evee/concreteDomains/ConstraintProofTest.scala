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
  private def assertLinearProof(premises: Seq[LinearConstraint], goal: LinearConstraint): Unit = {
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
    LinearConstraint(coefficients.map { case (v, c) => v -> fraction(c) }.toMap, fraction(constant))

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
    assertLinearProof(Seq(equation(x -> 1)(1), equation(x -> 1)(2)), LinearConstraint.contradiction)

  /** def(x) is derived from a premise that mentions x */
  @Test def diffDefined(): Unit =
    assertDiffProof(Seq(DiffEqual(x, fraction(5))), DiffSum(x, fraction(0), x))

  @Test def multDefined(): Unit =
    assertMultProof(Seq(Multiplication(x, fraction(2), y)), Multiplication(y, fraction(1), y))

  @Test def linearDefined(): Unit =
    assertLinearProof(Seq(equation(x -> 1, y -> 1)(5)), equation(x -> 0)(0))

  /** the rules for bounds of QMult name its relation < (not > as in QDiff) */
  @Test def multBoundRuleNames(): Unit = {
    def rules(premise: MultConstraint, goal: MultConstraint) = {
      val prover = new MultReasoner()
      prover.setOntology(Seq(premise))
      prover.getProof(goal).getInferences.asScala.map(_.getRuleName).toSet
    }
    assertTrue(rules(MultEqual(x, fraction(5)), MultLessThan(x, fraction(6))).contains("Introduce <"))
    assertTrue(rules(MultLessThan(x, fraction(5)), MultLessThan(x, fraction(6))).contains("Weaken <"))
  }

  @Test def multInconsistency(): Unit =
    assertMultProof(Seq(MultEqual(x, fraction(5)), MultLessThan(x, fraction(3))), MultContradiction)

  /** from qlinear/example.ofn: eliminating x first takes 3 steps, eliminating y first only 2 */
  private val system = Seq(equation(x -> 3, y -> 4)(22), equation(y -> -2, z -> 1)(12), equation(x -> 1, y -> 1, z -> 1)(10))
  private val combination = equation(y -> 3, z -> 1)(-16)

  /** the number of linear combinations in a proof of minimal tree size */
  private def linearSteps(premises: Seq[LinearConstraint], maxVariablesForAllOrders: Int): Int = {
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

  /**
   * The constraints of the proof in all variable orders: the given ones and the goal as written, the others with
   * integer coefficients without a common divisor and a positive first coefficient (with the variables sorted by IRI);
   * no constraint occurs twice in different scalings.
   */
  private def assertIntegerForm(premises: Seq[LinearConstraint], goal: LinearConstraint): Unit = {
    val prover = new GaussianEliminationProver()
    prover.setOntology(premises)
    val proof = prover.getProof(goal)
    val constraints = proof.getInferences.asScala.flatMap(i => i.getPremises.asScala :+ i.getConclusion).toSet
    def entries(c: LinearConstraint) =
      c.lhs.toSeq.sortBy(_._1.getIRI.toString).map(_._2).filterNot(_ == BigFraction.ZERO) :+ c.rhs
    for (c <- constraints -- premises - goal; values = entries(c).filterNot(_ == BigFraction.ZERO)) {
      assertTrue("not integer: " + c, values.forall(_.getDenominator == java.math.BigInteger.ONE))
      assertEquals("common divisor: " + c, java.math.BigInteger.ONE,
        values.map(_.getNumerator).foldLeft(java.math.BigInteger.ZERO)(_ gcd _))
      assertTrue("negative first coefficient: " + c, values.head.compareTo(BigFraction.ZERO) > 0)
    }
    val byMultiple = constraints.groupBy(c => entries(c).map(_.divide(entries(c).find(_ != BigFraction.ZERO).get)))
    for ((_, scalings) <- byMultiple)
      assertEquals("in different scalings: " + scalings, 1, scalings.size)
  }

  @Test def linearIntegerForm(): Unit = {
    assertLinearProof(system, combination)
    assertIntegerForm(system, combination)
  }

  /** a derived constraint that is a multiple of the goal is the goal as written, here 2y = 6 instead of y = 3 */
  @Test def linearGivenForm(): Unit = {
    val premises = Seq(equation(x -> 1, y -> 1)(5), equation(x -> 1)(2))
    assertLinearProof(premises, equation(y -> 2)(6))
    assertIntegerForm(premises, equation(y -> 2)(6))
  }

  /** the network that saturate uses, with or without complete reasoning, and with the limit for all paths */
  private def network(reasoner: ConstraintPropagationReasoner[_ <: CDConstraint, _], complete: Boolean, limit: Int,
                      saturate: => Unit): Class[_] = {
    if (complete)
      reasoner.setCompleteReasoning()
    reasoner.setMaxVariablesForAllPaths(limit)
    saturate
    reasoner.lastNetwork.getClass
  }

  /** pure reasoning uses the simple network, even with a limit for all paths; proofs the complete network or all paths */
  @Test def networkSelection(): Unit = {
    val diffPremises = Seq(DiffEqual(x, fraction(0)), DiffSum(x, fraction(1), y))
    def diff(complete: Boolean, limit: Int) = { val r = new DiffReasoner(); network(r, complete, limit, r.saturate(diffPremises)) }
    assertEquals(classOf[DiffNetworkSimple], diff(complete = false, 6))
    assertEquals(classOf[DiffNetworkComplete], diff(complete = true, 0))
    assertEquals(classOf[DiffNetworkFull], diff(complete = true, 6))
    val multPremises = Seq(MultEqual(x, fraction(1)), Multiplication(x, fraction(2), y))
    def mult(complete: Boolean, limit: Int) = { val r = new MultReasoner(); network(r, complete, limit, r.saturate(multPremises)) }
    assertEquals(classOf[MultNetworkSimple], mult(complete = false, 6))
    assertEquals(classOf[MultNetworkComplete], mult(complete = true, 0))
    assertEquals(classOf[MultNetworkFull], mult(complete = true, 6))
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

  /**
   * different factors 4 and 3 between x and z hold only for x = 0 (not ⊥, as QMult is over all rational numbers); all
   * paths give more derivations than the complete network, which are correct
   */
  @Test def multDifferentFactors(): Unit = {
    val premises = Seq(Multiplication(x, fraction(2), y), Multiplication(y, fraction(2), z), Multiplication(x, fraction(3), z))
    assertTrue("the premises should be consistent", new MultReasoner().consistent(premises))
    val complete = multProof(premises, MultEqual(x, fraction(0)), 0)
    val allPaths = multProof(premises, MultEqual(x, fraction(0)), 3)
    assertTrue((complete -- allPaths).toString, complete.subsetOf(allPaths))
    assertTrue(allPaths.size > complete.size)
  }

  /** the proofs of the simple and the complete network, and of all paths; not from ⊥ unless that is the goal */
  private def assertMultProofs(premises: Seq[MultConstraint], goal: MultConstraint): Unit = {
    if (goal != MultContradiction)
      assertTrue("the premises should be consistent", new MultReasoner().consistent(premises))
    assertMultProof(premises, goal)
    multProof(premises, goal, 0)
    multProof(premises, goal, 3)
  }

  /** 4 * x = x holds for x = 0 */
  @Test def multZeroFromFactor(): Unit = assertMultProofs(Seq(Multiplication(x, fraction(4), x)), MultEqual(x, fraction(0)))

  /** x = y = 0 satisfy q * x = y for every factor q */
  @Test def multZeroSatisfiesAllFactors(): Unit =
    assertMultProofs(Seq(Multiplication(x, fraction(2), y), Multiplication(x, fraction(3), y)), Multiplication(x, fraction(5), y))

  /** 0 < 0 does not hold */
  @Test def multZeroBelowBound(): Unit =
    assertMultProofs(Seq(Multiplication(x, fraction(4), x), MultLessThan(x, fraction(0))), MultContradiction)

  /** there is a positive factor between two negative values */
  @Test def multNegativeValues(): Unit =
    assertMultProofs(Seq(MultEqual(x, fraction(-2)), MultEqual(y, fraction(-4))), Multiplication(x, fraction(2), y))
}
