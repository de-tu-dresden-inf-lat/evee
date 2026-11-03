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
 * As [[MultNetworkSimple]], but allows more than one unary constraint per node, for complete derivation structures,
 * which may be much larger.
 *
 * The constraints are processed in a fixed order (sorted by their text, and in insertion order afterwards), so that
 * the derivation structure does not depend on hash codes. After the saturation, every step between the derived
 * constraints is recorded (see [[MultSteps.between]]), not only the step that first derived a constraint. Of two
 * different factors for the same pair of data properties, only the first one is propagated further.
 */
class MultNetworkComplete extends ConstraintNetwork[MultConstraint] {

  private var continueEvenIfInconsistent = true

  private var derived = Set[MultConstraint]()
  private var inferences = Set[IInference[MultConstraint]]()

  /** the constraints the network was filled with */
  private var given = Seq[MultConstraint]()

  private val nodes = new mutable.LinkedHashMap[OWLDataProperty, mutable.Set[MultUnaryConstraint]]()
    with mutable.MultiMap[OWLDataProperty, MultUnaryConstraint] {
    override protected def makeSet: mutable.Set[MultUnaryConstraint] = new mutable.LinkedHashSet[MultUnaryConstraint]()
  }
  private val edges = new mutable.LinkedHashMap[OWLDataProperty, mutable.Map[OWLDataProperty, Multiplication]]()

  private var inconsistent: Boolean = false

  def setContinueEvenIfInconsistent() = {
    this.continueEvenIfInconsistent = true
  }

  def fill(unsorted: Iterable[MultConstraint]) = {
    val constraints = unsorted.toSeq.sortBy(_.toString)
    given ++= constraints
    inferences ++= constraints.map(new Inference(_, ProofTools.ASSERTED_1, List[MultConstraint]().asJava))
    derived ++= constraints
    constraints.foreach {
      case MultContradiction => ;
      case p: MultUnaryConstraint => update(p)
      case p: Multiplication => update(p)
    }
  }

  def propagate(): Unit = {
    deriveEdgesFromUnary()
    mirrorEdges() // add corresponding backward edges
    addTautologicalEdges() // add x*1=x for all used x
    propagateEdges() // derives all implicit edges from existing edges
    propagateValues() // propagate the values along the edges

    // the steps above record only the steps they make, e.g. no value propagated from a value derived later
    inferences ++= MultSteps.between(derived)
    if (inferences.exists(_.getConclusion == MultContradiction))
      inconsistent = true
  }

  def derivedPredicates() = if (inconsistent) derived + MultContradiction else derived

  def derivationStructure() = inferences

  private def deriveEdgesFromUnary() =
    for (n1 <- nodes.keys.toList; e1 <- nodes(n1).toList.collect { case e: MultEqual => e } if e1.value > BigFraction.ZERO;
         // def(n1) is derived by addTautologicalEdges, not from n1 = v1 and itself
         n2 <- nodes.keys.toList if n2 != n1;
         e2 <- nodes(n2).toList.collect { case e: MultEqual => e } if e2.value > BigFraction.ZERO) {
      val newPred = Multiplication(n1, e2.value / e1.value, n2)
      update(newPred)
      derived += newPred
      inferences += new Inference(newPred, MultRules.RULE_FACTOR_FROM_CONSTANTS, List[MultConstraint](e1, e2).asJava)
    }

  private def mirrorEdges(): Unit =
    for (n1 <- edges.keys.toList; n2 <- edges(n1).keys.toList) {
      val newPred = Multiplication(n2, edges(n1)(n2).factor.reciprocal(), n1)
      update(newPred)
      derived += newPred
      inferences += new Inference(newPred, MultRules.RULE_INVERSE, List[MultConstraint](edges(n1)(n2)).asJava)
    }

  private def addTautologicalEdges(): Unit =
    for (n1 <- (edges.keys ++ nodes.keys).toList.distinct) {
      val newPred = Multiplication(n1, BigFraction.ONE, n1)
      update(newPred)
      derived += newPred
      // def(n1) holds because a given constraint mentions n1; there is nothing to derive if def(n1) is given itself
      if (!given.contains(newPred))
        inferences += new Inference(newPred, MultRules.RULE_ZERO, List(withProperty(n1)).asJava)
    }

  /** a given constraint with the data property, which therefore has a value */
  private def withProperty(property: OWLDataProperty): MultConstraint =
    given.filter(_.getOWLDataProperties.contains(property)).minBy(_.toString)

  private def propagateEdges(): Unit = {
    var added = true
    while (added && (!inconsistent || continueEvenIfInconsistent)) {
      added = false
      for (n1 <- edges.keys.toList; n2 <- edges(n1).keys.toList; n3 <- edges.get(n2).toList.flatMap(_.keys)) {
        if (!edges(n1).contains(n3))
          added = true
        // proceed in any case since we may derive a contradiction
        val newPred = Multiplication(n1, edges(n1)(n2).factor * edges(n2)(n3).factor, n3)
        derived += newPred
        inferences += new Inference(newPred, MultRules.RULE_TRANSITIVITY,
          List[MultConstraint](edges(n1)(n2), edges(n2)(n3)).asJava)
        update(newPred)
      }
    }
  }

  private def propagateValues(): Unit =
    if (!inconsistent || continueEvenIfInconsistent)
      for (n1 <- edges.keys.toList; unary1 <- nodes.getOrElse(n1, Set()).toList; n2 <- edges(n1).keys.toList) {
        val edge = edges(n1)(n2)
        val (newPred, ruleName) = unary1 match {
          case MultEqual(_, value) => (MultEqual(n2, value * edge.factor), MultRules.RULE_PROPAGATE_EQUAL)
          case MultLessThan(_, bound) => (MultLessThan(n2, bound * edge.factor), MultRules.RULE_PROPAGATE_LESS_THAN)
        }
        derived += newPred
        inferences += new Inference(newPred, ruleName, List[MultConstraint](unary1, edge).asJava)
        update(newPred)
      }

  private def update(constraint: MultUnaryConstraint): Unit = {
    val d = constraint match {
      case MultEqual(x, _) => x
      case MultLessThan(x, _) => x
    }
    nodes.getOrElse(d, Set()).foreach { node =>
      (node, constraint) match {
        case (same1, same2) if same1.equals(same2) => ; // all good
        case (e1: MultEqual, e2: MultEqual) =>
          inconsistent = true
          // the smaller value first, as in MultSteps
          inferences += new Inference(MultContradiction, MultRules.RULE_DIFFERENT_CONSTANTS,
            (if (e1.value < e2.value) List[MultConstraint](e1, e2) else List[MultConstraint](e2, e1)).asJava)
        case (l: MultLessThan, e: MultEqual) =>
          if (e.value >= l.bound) {
            inconsistent = true
            inferences += new Inference(MultContradiction, MultRules.RULE_CONSTANT_TOO_LARGE, List[MultConstraint](e, l).asJava)
          }
        case (e: MultEqual, l: MultLessThan) =>
          if (e.value >= l.bound) {
            inconsistent = true
            inferences += new Inference(MultContradiction, MultRules.RULE_CONSTANT_TOO_LARGE, List[MultConstraint](e, l).asJava)
          }
        case _ => // no inconsistency, we're fine
      }
    }
    nodes.addBinding(d, constraint)
  }

  private def update(p: Multiplication): Unit = {
    val (d1, d2) = (p.property1, p.property2)
    if (!edges.contains(d1))
      edges.put(d1, new mutable.LinkedHashMap[OWLDataProperty, Multiplication]())
    edges(d1).get(d2) match {
      case None => edges(d1).put(d2, p)
      case Some(other) if !other.equals(p) =>
        inconsistent = true
        // the smaller factor first, as in MultSteps
        inferences += new Inference(MultContradiction, MultRules.RULE_DIFFERENT_FACTORS,
          (if (other.factor < p.factor) List[MultConstraint](other, p) else List[MultConstraint](p, other)).asJava)
      case Some(_) => ;
    }
  }

  def generateSolution(negativeConstraints: Iterable[MultConstraint]): Map[OWLDataProperty, BigFraction] =
    throw new AssertionError("Not Implemented!")
}
