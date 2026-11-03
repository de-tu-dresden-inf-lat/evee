package de.tu_dresden.inf.lat.evee.concreteDomains.multDomain

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
 * twice, and records every step between them (see [[MultSteps.between]]). The paths go along the given factors in
 * both directions and along the factors between the given positive values. If the factors are consistent, these are
 * all constraints the rules derive; otherwise the rules derive infinitely many, e.g. 4 * x = x, 8 * x = x, ... from
 * 2 * x = y and 1 * y = x. The number of paths can grow exponentially with the number of data properties.
 */
class MultNetworkFull extends ConstraintNetwork[MultConstraint] {

  private var given = Seq[MultConstraint]()
  private var derived = Set[MultConstraint]()
  private var inferences = Set[IInference[MultConstraint]]()

  override def fill(constraints: Iterable[MultConstraint]): Unit = given ++= constraints

  /** it always derives all constraints */
  override def setContinueEvenIfInconsistent(): Unit = ()

  override def propagate(): Unit = {
    val products = given.collect { case m: Multiplication => m }
    val unary = given.collect { case u: MultUnaryConstraint => u }
    val equals = unary.collect { case e: MultEqual => e }
    val edges: Map[OWLDataProperty, Seq[(BigFraction, OWLDataProperty)]] =
      (products.flatMap(m => Seq((m.property1, m.factor, m.property2), (m.property2, m.factor.reciprocal(), m.property1))) ++
        (for (e1 <- equals; e2 <- equals
              if e1.property != e2.property && e1.value > BigFraction.ZERO && e2.value > BigFraction.ZERO)
          yield (e1.property, e2.value / e1.value, e2.property)))
        .distinct.groupBy(_._1).mapValues(_.map(edge => (edge._2, edge._3))).toMap

    val paths = mutable.Set[Multiplication]()
    def visit(start: OWLDataProperty, current: OWLDataProperty, weight: BigFraction, visited: Set[OWLDataProperty]): Unit =
      for ((factor, next) <- edges.getOrElse(current, Nil)) {
        // a path back to the start is a cycle; it ends there
        if (next == start || !visited.contains(next))
          paths += Multiplication(start, weight * factor, next)
        if (!visited.contains(next))
          visit(start, next, weight * factor, visited + next)
      }
    val properties = given.flatMap(_.getOWLDataProperties).distinct
    properties.foreach(x => visit(x, x, BigFraction.ONE, Set(x)))

    val definitions = properties.map(x => Multiplication(x, BigFraction.ONE, x))
    val allProducts = (products ++ paths ++ definitions).toSet
    val values =
      (for (e <- equals; m <- allProducts if m.property1 == e.property) yield MultEqual(m.property2, e.value * m.factor)) ++
        (for (l <- unary.collect { case l: MultLessThan => l }; m <- allProducts if m.property1 == l.property)
          yield MultLessThan(m.property2, l.bound * m.factor))
    derived = given.toSet ++ allProducts ++ values

    inferences = given.map(c => new Inference[MultConstraint](c, ProofTools.ASSERTED_1, List[MultConstraint]().asJava)).toSet ++
      definitions.filterNot(given.contains).map(d =>
        new Inference[MultConstraint](d, MultRules.RULE_ZERO, List(withProperty(d.property1)).asJava)) ++
      MultSteps.between(derived)
  }

  /** a given constraint with the data property, which therefore has a value */
  private def withProperty(property: OWLDataProperty): MultConstraint =
    given.filter(_.getOWLDataProperties.contains(property)).minBy(_.toString)

  override def derivedPredicates(): Set[MultConstraint] =
    if (inferences.exists(_.getConclusion == MultContradiction)) derived + MultContradiction else derived

  override def derivationStructure(): Set[IInference[MultConstraint]] = inferences

  override def generateSolution(negativePredicates: Iterable[MultConstraint]): Map[OWLDataProperty, BigFraction] =
    throw new UnsupportedOperationException("MultNetworkFull is only for proofs")
}
