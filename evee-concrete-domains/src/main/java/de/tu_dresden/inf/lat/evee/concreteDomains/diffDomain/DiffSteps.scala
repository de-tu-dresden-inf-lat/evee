package de.tu_dresden.inf.lat.evee.concreteDomains.diffDomain

import de.tu_dresden.inf.lat.evee.concreteDomains.tools.Numbers.FractionOps
import de.tu_dresden.inf.lat.evee.proofs.data.Inference
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInference

import scala.collection.JavaConverters.seqAsJavaListConverter

/** The steps of the rules of QDiff between constraints that have been derived already. */
object DiffSteps {

  /**
   * every step of a rule whose premises are among the constraints and whose conclusion is one of them or ⊥; steps
   * that derive a premise again (e.g. from def(x)) are left out
   */
  def between(constraints: Set[DiffConstraint]): Set[IInference[DiffConstraint]] = {
    val unary = constraints.toSeq.collect { case u: DiffUnaryConstraint => u }
    val equals = unary.collect { case e: DiffEqual => e }
    val greaterThans = unary.collect { case g: DiffGreaterThan => g }
    val sums = constraints.toSeq.collect { case s: DiffSum => s }
    val sumsFrom = sums.groupBy(_.property1)
    val steps = Set.newBuilder[IInference[DiffConstraint]]
    def step(conclusion: DiffConstraint, rule: String, premises: DiffConstraint*): Unit =
      if (!premises.contains(conclusion) && (conclusion == DiffContradiction || constraints.contains(conclusion)))
        steps += new Inference[DiffConstraint](conclusion, rule, premises.toList.asJava)

    for (e1 <- equals; e2 <- equals if e1.property != e2.property)
      step(DiffSum(e1.property, e2.value - e1.value, e2.property), DiffRules.RULE_DIFFERENCE_FROM_CONSTANTS, e1, e2)
    for (s <- sums)
      step(DiffSum(s.property2, -s.diff, s.property1), DiffRules.RULE_INVERSE, s)
    for (s1 <- sums; s2 <- sumsFrom.getOrElse(s1.property2, Nil))
      step(DiffSum(s1.property1, s1.diff + s2.diff, s2.property2), DiffRules.RULE_TRANSITIVITY, s1, s2)
    for (e <- equals; s <- sumsFrom.getOrElse(e.property, Nil))
      step(DiffEqual(s.property2, e.value + s.diff), DiffRules.RULE_PROPAGATE_EQUAL, e, s)
    for (g <- greaterThans; s <- sumsFrom.getOrElse(g.property, Nil))
      step(DiffGreaterThan(s.property2, g.bound + s.diff), DiffRules.RULE_PROPAGATE_GREATER_THAN, g, s)

    for (e1 <- equals; e2 <- equals if e1.property == e2.property && e1.value < e2.value)
      step(DiffContradiction, DiffRules.RULE_DIFFERENT_CONSTANTS, e1, e2)
    for (e <- equals; g <- greaterThans if g.property == e.property && g.bound >= e.value)
      step(DiffContradiction, DiffRules.RULE_CONSTANT_TOO_SMALL, e, g)
    for (s1 <- sums; s2 <- sumsFrom(s1.property1)
         if s1.property2 == s2.property2 && s1.diff < s2.diff)
      step(DiffContradiction, DiffRules.RULE_DIFFERENT_DIFFERENCES, s1, s2)
    steps.result()
  }
}
