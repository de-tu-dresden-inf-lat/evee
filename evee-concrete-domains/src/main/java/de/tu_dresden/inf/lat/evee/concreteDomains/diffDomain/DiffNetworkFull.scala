package de.tu_dresden.inf.lat.evee.concreteDomains.diffDomain

import de.tu_dresden.inf.lat.evee.concreteDomains.ConstraintNetwork
import de.tu_dresden.inf.lat.evee.concreteDomains.tools.Numbers.FractionOps
import de.tu_dresden.inf.lat.evee.proofs.data.Inference
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInference
import de.tu_dresden.inf.lat.evee.proofs.tools.ProofTools
import org.apache.commons.math3.fraction.BigFraction
import org.semanticweb.owlapi.model.OWLDataProperty

import scala.collection.JavaConverters.seqAsJavaListConverter
import scala.collection.mutable

/**
 * For proofs: derives every constraint that follows from the given ones along a path that visits no data property
 * twice, and records every step between them (see [[DiffSteps.between]]). The paths go along the given differences
 * in both directions and along the differences of the given values. If the differences are consistent, these are all
 * constraints the rules derive; otherwise the rules derive infinitely many, e.g. x + 2 = x, x + 3 = x, ... from
 * x + 1 = y and y + 1 = x. The number of paths can grow exponentially with the number of data properties.
 */
class DiffNetworkFull extends ConstraintNetwork[DiffConstraint] {

  private var given = Seq[DiffConstraint]()
  private var derived = Set[DiffConstraint]()
  private var inferences = Set[IInference[DiffConstraint]]()

  override def fill(predicates: Iterable[DiffConstraint]): Unit = given ++= predicates

  /** it always derives all constraints */
  override def setContinueEvenIfInconsistent(): Unit = ()

  override def propagate(): Unit = {
    val sums = given.collect { case s: DiffSum => s }
    val unary = given.collect { case u: DiffUnaryConstraint => u }
    val equals = unary.collect { case e: DiffEqual => e }
    val edges: Map[OWLDataProperty, Seq[(BigFraction, OWLDataProperty)]] =
      (sums.flatMap(s => Seq((s.property1, s.diff, s.property2), (s.property2, -s.diff, s.property1))) ++
        (for (e1 <- equals; e2 <- equals if e1.property != e2.property)
          yield (e1.property, e2.value - e1.value, e2.property)))
        .distinct.groupBy(_._1).mapValues(_.map(edge => (edge._2, edge._3))).toMap

    val paths = mutable.Set[DiffSum]()
    def visit(start: OWLDataProperty, current: OWLDataProperty, weight: BigFraction, visited: Set[OWLDataProperty]): Unit =
      for ((diff, next) <- edges.getOrElse(current, Nil)) {
        // a path back to the start is a cycle; it ends there
        if (next == start || !visited.contains(next))
          paths += DiffSum(start, weight + diff, next)
        if (!visited.contains(next))
          visit(start, next, weight + diff, visited + next)
      }
    val properties = given.flatMap(_.getOWLDataProperties).distinct
    properties.foreach(x => visit(x, x, BigFraction.ZERO, Set(x)))

    val definitions = properties.map(x => DiffSum(x, BigFraction.ZERO, x))
    val allSums = (sums ++ paths ++ definitions).toSet
    val values =
      (for (e <- equals; s <- allSums if s.property1 == e.property) yield DiffEqual(s.property2, e.value + s.diff)) ++
        (for (g <- unary.collect { case g: DiffGreaterThan => g }; s <- allSums if s.property1 == g.property)
          yield DiffGreaterThan(s.property2, g.bound + s.diff))
    derived = given.toSet ++ allSums ++ values

    inferences = given.map(c => new Inference[DiffConstraint](c, ProofTools.ASSERTED_1, List[DiffConstraint]().asJava)).toSet ++
      definitions.filterNot(given.contains).map(d =>
        new Inference[DiffConstraint](d, DiffRules.RULE_ZERO, List(withProperty(d.property1)).asJava)) ++
      DiffSteps.between(derived)
  }

  /** a given constraint with the data property, which therefore has a value */
  private def withProperty(property: OWLDataProperty): DiffConstraint =
    given.filter(_.getOWLDataProperties.contains(property)).minBy(_.toString)

  override def derivedPredicates(): Set[DiffConstraint] =
    if (inferences.exists(_.getConclusion == DiffContradiction)) derived + DiffContradiction else derived

  override def derivationStructure(): Set[IInference[DiffConstraint]] = inferences

  override def generateSolution(negativePredicates: Iterable[DiffConstraint]): Map[OWLDataProperty, BigFraction] =
    throw new UnsupportedOperationException("DiffNetworkFull is only for proofs")
}
