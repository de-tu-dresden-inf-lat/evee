package de.tu_dresden.inf.lat.evee.concreteDomains

import de.tu_dresden.inf.lat.evee.concreteDomains.diffDomain._
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

  private def fraction(n: Int) = new BigFraction(n)

  @Test def diffSum(): Unit =
    assertDiffProof(Seq(DiffSum(x, 1, y), DiffSum(y, 2, z)), DiffSum(x, 3, z))

  @Test def diffEqual(): Unit =
    assertDiffProof(Seq(DiffEqual(x, 0), DiffSum(x, 1, y)), DiffEqual(y, 1))

  @Test def diffGreaterThan(): Unit =
    assertDiffProof(Seq(DiffGreaterThan(x, 0), DiffSum(x, 1, y)), DiffGreaterThan(y, 1))

  @Test def diffBoundFromEqual(): Unit =
    assertDiffProof(Seq(DiffEqual(x, 5)), DiffGreaterThan(x, 3))

  @Test def diffInconsistency(): Unit =
    assertDiffProof(Seq(DiffEqual(x, 0), DiffGreaterThan(x, 1)), DiffContradiction)

  @Test def multProduct(): Unit =
    assertMultProof(Seq(Multiplication(x, fraction(2), y), Multiplication(y, fraction(3), z)),
      Multiplication(x, fraction(6), z))

  @Test def multLessThan(): Unit =
    assertMultProof(Seq(Multiplication(x, fraction(2), y), MultLessThan(y, fraction(6))), MultLessThan(x, fraction(3)))

  @Test def multBoundFromEqual(): Unit =
    assertMultProof(Seq(MultEqual(x, fraction(5))), MultLessThan(x, fraction(6)))

  @Test def multInconsistency(): Unit =
    assertMultProof(Seq(MultEqual(x, fraction(5)), MultLessThan(x, fraction(3))), MultContradiction)
}
