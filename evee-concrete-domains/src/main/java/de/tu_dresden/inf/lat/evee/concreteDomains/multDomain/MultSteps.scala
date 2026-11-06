package de.tu_dresden.inf.lat.evee.concreteDomains.multDomain

import de.tu_dresden.inf.lat.evee.concreteDomains.tools.Numbers.FractionOps
import de.tu_dresden.inf.lat.evee.proofs.data.Inference
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInference
import org.apache.commons.math3.fraction.BigFraction

import scala.collection.JavaConverters.seqAsJavaListConverter

/** The steps of the rules of QMult between constraints that have been derived already. */
object MultSteps {

  /**
   * every step of a rule whose premises are among the constraints and whose conclusion is one of them or ⊥; steps
   * that derive a premise again (e.g. from def(x)) are left out
   */
  def between(constraints: Set[MultConstraint]): Set[IInference[MultConstraint]] = {
    val unary = constraints.toSeq.collect { case u: MultUnaryConstraint => u }
    val equals = unary.collect { case e: MultEqual => e }
    val lessThans = unary.collect { case l: MultLessThan => l }
    val products = constraints.toSeq.collect { case m: Multiplication => m }
    val productsFrom = products.groupBy(_.property1)
    val steps = Set.newBuilder[IInference[MultConstraint]]
    def step(conclusion: MultConstraint, rule: String, premises: MultConstraint*): Unit =
      if (!premises.contains(conclusion) && (conclusion == MultContradiction || constraints.contains(conclusion)))
        steps += new Inference[MultConstraint](conclusion, rule, premises.toList.asJava)

    // q * x = y holds for the values of x and y if q times the one is the other, e.g. for every q if both are 0; not
    // from one value of x alone, as def(x) and q * x = x have their own rules
    val equalsOf = equals.groupBy(_.property)
    for (m <- products if m.property1 != m.property2;
         e1 <- equalsOf.getOrElse(m.property1, Nil); e2 <- equalsOf.getOrElse(m.property2, Nil)
         if e1.value * m.factor == e2.value)
      step(m, MultRules.RULE_FACTOR_FROM_CONSTANTS, e1, e2)
    for (m <- products)
      step(Multiplication(m.property2, m.factor.reciprocal(), m.property1), MultRules.RULE_INVERSE, m)
    for (m1 <- products; m2 <- productsFrom.getOrElse(m1.property2, Nil))
      step(Multiplication(m1.property1, m1.factor * m2.factor, m2.property2), MultRules.RULE_TRANSITIVITY, m1, m2)
    for (e <- equals; m <- productsFrom.getOrElse(e.property, Nil))
      step(MultEqual(m.property2, e.value * m.factor), MultRules.RULE_PROPAGATE_EQUAL, e, m)
    for (l <- lessThans; m <- productsFrom.getOrElse(l.property, Nil))
      step(MultLessThan(m.property2, l.bound * m.factor), MultRules.RULE_PROPAGATE_LESS_THAN, l, m)

    for (e1 <- equals; e2 <- equals if e1.property == e2.property && e1.value < e2.value)
      step(MultContradiction, MultRules.RULE_DIFFERENT_CONSTANTS, e1, e2)
    for (e <- equals; l <- lessThans if l.property == e.property && e.value >= l.bound)
      step(MultContradiction, MultRules.RULE_CONSTANT_TOO_LARGE, e, l)
    // q1 * x = y and q2 * x = y with q1 ≠ q2 hold only for x = 0
    for (m1 <- products; m2 <- productsFrom(m1.property1)
         if m1.property2 == m2.property2 && m1.factor < m2.factor)
      step(MultEqual(m1.property1, BigFraction.ZERO), MultRules.RULE_DIFFERENT_FACTORS, m1, m2)
    steps.result()
  }
}
