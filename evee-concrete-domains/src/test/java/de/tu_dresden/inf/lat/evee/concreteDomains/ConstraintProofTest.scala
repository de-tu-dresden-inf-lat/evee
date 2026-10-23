package de.tu_dresden.inf.lat.evee.concreteDomains

import de.tu_dresden.inf.lat.evee.concreteDomains.diffDomain._
import de.tu_dresden.inf.lat.evee.concreteDomains.linearDomain.{GaussianEliminationProver, GaussianEliminationReasoner, LinearConstraint}
import de.tu_dresden.inf.lat.evee.concreteDomains.multDomain._
import org.apache.commons.math3.fraction.BigFraction
import org.junit.Test
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model.{IRI, OWLDataProperty}

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

  private def assertLinearProof(premises: Seq[LinearConstraint[BigFraction]], goal: LinearConstraint[BigFraction]): Unit = {
    val prover = new GaussianEliminationProver()
    prover.setOntology(premises)
    ProofChecker.forConstraints(new GaussianEliminationReasoner(), premises, goal).assertCorrect(prover.getProof(goal))
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

  @Test def multInconsistency(): Unit =
    assertMultProof(Seq(MultEqual(x, fraction(5)), MultLessThan(x, fraction(3))), MultContradiction)
}
