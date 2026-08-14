package de.tu_dresden.inf.lat.evee.concreteDomains.multDomain

import de.tu_dresden.inf.lat.evee.proofs.data.Inference
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInference
import de.tu_dresden.inf.lat.evee.proofs.tools.ProofTools
import de.tu_dresden.inf.lat.evee.concreteDomains.ConstraintNetwork

import org.apache.commons.math3.fraction.BigFraction
import org.semanticweb.owlapi.model.OWLDataProperty

import scala.collection.JavaConverters.seqAsJavaListConverter
import scala.collection.mutable

/**
 * TODO adapt rule names
 * TODO possible problem with precision?
 * TODO generateSolution probably needs adaptation
 */
class MultNetworkSimple extends ConstraintNetwork[MultConstraint] {

  private var continueEvenIfInconsistent = false

  private var derived = Set[MultConstraint]()
  private var inferences = Set[IInference[MultConstraint]]()


  private val nodes = new mutable.HashMap[OWLDataProperty, MultUnaryConstraint]()
  private val edges = new mutable.HashMap[OWLDataProperty,
    mutable.Map[OWLDataProperty,Multiplication]]()

  private var inconsistent: Boolean = false


  def setContinueEvenIfInconsistent() = {
    this.continueEvenIfInconsistent=true
  }

  def fill(constraints: Iterable[MultConstraint]) = {
    inferences ++= constraints.map(new Inference(_, ProofTools.ASSERTED_1, List[MultConstraint]().asJava))
    derived ++= constraints
    constraints.foreach(f = p => p match {
      case MultContradiction => ;
      case MultLessThan(d, _) =>
        update(d, p.asInstanceOf[MultUnaryConstraint])
      case MultEqual(d, _) =>
        update(d, p.asInstanceOf[MultUnaryConstraint])
      case Multiplication(d1, _, d2) =>
        update(d1, d2, p.asInstanceOf[Multiplication])
    })
  }


  def propagate(): Unit = {

    deriveEdgesFromUnary()
    
    mirrorEdges() // add corresponding backward edges

    addTautologicalEdges() // add x+0-x for all used x

    propagateEdges() // derives all implicit edges from existing edges
    // this does not make new inferences in the previous steps possible:
    // first depends only on unary predicates,
    // all edges added by second step are added here as well


    propagateValues() // propagate the values along the edges
    // this also does not make new inferences in the previous steps possible:
    // deriveEdgesFromUnary() would just add edges that were already there (we assume all implicit egdes to be derived  by propagateEdges)
    // the other only depend on binary predicates, which are not added by propagateValues
    //println("Step 4: "+derived)
  }

  def derivedPredicates() = {
    if(inconsistent)
      derived + MultContradiction
    else
      derived
  }

  def derivationStructure() = inferences

  private def deriveEdgesFromUnary() = {

    // derive edges from values
    nodes.keys.foreach(n1 =>
      nodes.get(n1).foreach(p1 => p1 match {
        case MultEqual(_, v1) if v1.getNumerator().signum()!=0 =>
          nodes.keys.foreach(n2 =>
            nodes.get(n2).foreach(p2 => p2 match {
              case MultEqual(_, v2) =>
                val quotient = v2.divide(v1)
                if(v1.getNumerator.signum()==1){ // otherwise the constraint is not allowed
                  val newPred = Multiplication(n1, quotient, n2)
                  update(n1, n2, newPred)
                  derived += newPred
                  inferences += new Inference(newPred, MultRules.RULE_FACTOR_FROM_CONSTANTS, List(p1, p2).asJava)
                }
              case _: MultLessThan => ;
            }))
        case _: MultLessThan => ;
      }))
  }

  private def mirrorEdges(): Unit = {
    edges.keys.foreach(n1 =>
      edges(n1).keys.foreach(n2 => {
        val newPred = Multiplication(n2, edges(n1)(n2).factor.reciprocal(), n1)
        update(n2,n1,newPred)
        derived += newPred
        inferences += new Inference(newPred, MultRules.RULE_INVERSE, List(edges(n1)(n2)).asJava)
      }))
  }

  private def addTautologicalEdges(): Unit = {
    edges.keySet.union(nodes.keySet).foreach(n1 => {
      val newPred = Multiplication(n1, BigFraction.ONE, n1)
      update(n1,n1,newPred)
      derived += newPred
      inferences += new Inference(newPred, MultRules.RULE_ZERO, List.empty.asJava)
    })
  }

  private def propagateEdges(): Unit = {
    var added=true
    while(added && (!inconsistent || continueEvenIfInconsistent)) {
      added=false
      edges.keys.foreach(n1 =>
        edges(n1).keys.foreach(n2 =>
          edges.get(n2).foreach(_.keys.foreach(n3 => {
            if(!edges(n1).contains(n3))
              added = true
            // proceed in any case since we may derive a contradiction
            val newPred = propagate(edges(n1)(n2), edges(n2)(n3))
            derived += newPred
            inferences += new Inference(newPred, MultRules.RULE_TRANSITIVITY, List(edges(n1)(n2), edges(n2)(n3)).asJava)
            update(n1,n3,newPred)
          }
          ))))
    }
  }

  private def propagateValues(): Unit = {
    if (!inconsistent||continueEvenIfInconsistent)
      edges.keys.foreach(n1 =>
        nodes.get(n1).foreach(unary1 =>
          edges(n1).keys.foreach(n2 => {
            val newPred = propagate(unary1, edges(n1)(n2))
            derived += newPred
            val ruleName = unary1 match {
              case MultEqual(_, _) => MultRules.RULE_PROPAGATE_EQUAL
              case MultLessThan(_, _) => MultRules.RULE_PROPAGATE_LESS_THAN
            }
            inferences += new Inference(newPred, ruleName, List(unary1, edges(n1)(n2)).asJava)
            update(n2, newPred)
          })))
  }

  private def propagate(pred1: Multiplication, pred2: Multiplication): Multiplication =
    Multiplication(
      pred1.property1,
      pred1.factor.multiply(pred2.factor),
      pred2.property2)

  private def propagate(unary1: MultUnaryConstraint,
                        constraint: Multiplication
                       ): MultUnaryConstraint =
    unary1 match {
      case MultEqual(property1, value1) =>
        MultEqual(constraint.property2, value1.multiply(constraint.factor))

      case MultLessThan(property1, value1) =>
        MultLessThan(constraint.property2, value1.multiply(constraint.factor))
    }

  private def update(d: OWLDataProperty, constraint: MultUnaryConstraint): Unit = {
    if(!nodes.contains(d))
      nodes.put(d,constraint)
    else (nodes(d), constraint) match {
      case (same1,same2) if same1.equals(same2) => ; // no update
      case (_: MultEqual, _: MultEqual) =>
        inconsistent = true
        inferences += new Inference(MultContradiction, MultRules.RULE_DIFFERENT_CONSTANTS, List(nodes(d), constraint).asJava)
      case (MultLessThan(_,v1), MultLessThan(_,v2)) =>
        if(v1.compareTo(v2)>=0)
          nodes.put(d,constraint)
      case (MultLessThan(_,v1), MultEqual(_,v2)) =>
        if(v1.compareTo(v2)>0)
          nodes.put(d,constraint)
        else {
          inconsistent = true
          inferences += new Inference(MultContradiction, MultRules.RULE_CONSTANT_TOO_LARGE, List(constraint, nodes(d)).asJava)
        }
      case (MultEqual(_,v1), MultLessThan(_,v2)) =>
        if(v1.compareTo(v2)>=0) {
          inferences += new Inference(MultContradiction, MultRules.RULE_CONSTANT_TOO_LARGE, List(nodes(d), constraint).asJava)
          inconsistent = true
        }
    }
  }

  private def update(d1: OWLDataProperty, d2: OWLDataProperty, p: Multiplication) = {
    if(!edges.contains(d1))
      edges.put(d1, new mutable.HashMap[OWLDataProperty, Multiplication]())
    edges(d1).get(d2) match {
      case None => edges(d1).put(d2,p)
      case Some(other) if !other.equals(p) => {
        inconsistent = true
        inferences += new Inference(MultContradiction, MultRules.RULE_DIFFERENT_FACTORS, List(other, p).asJava)
      }
      case Some(p) => ;
    }
  }

  private var negativeNodes: mutable.Map[OWLDataProperty, mutable.Set[MultUnaryConstraint]] = _
  private var negativeEdges: mutable.Map[OWLDataProperty, mutable.Map[OWLDataProperty, mutable.Set[Multiplication]]] = _

  def generateSolution(negativeConstraints: Iterable[MultConstraint]): Map[OWLDataProperty, Double] =
    throw new AssertionError("Not Implemented!")

  private def fillNegativeGraphAndMirror(negative: Iterable[MultConstraint]): Unit = {
    negative.foreach {
      case MultContradiction => ;
      case p@MultLessThan(x, _) => updateNegativeNode(x, p)
      case p@MultEqual(x, _) => updateNegativeNode(x, p)
      case p@Multiplication(x, d, y) => {
        updateNegativeEdge(x, y, p)
        updateNegativeEdge(y, x, Multiplication(y, d.reciprocal(), x))
      }
    }
  }

  private def updateNegativeNode(x: OWLDataProperty, p: MultUnaryConstraint): Unit = {
    if (!negativeNodes.contains(x))
      negativeNodes.put(x, new mutable.HashSet[MultUnaryConstraint]())
    negativeNodes(x).add(p)
  }

  private def updateNegativeEdge(x: OWLDataProperty, y: OWLDataProperty, p: Multiplication): Unit = {
    if(!negativeEdges.contains(x))
      negativeEdges.put(x, new mutable.HashMap[OWLDataProperty, mutable.Set[Multiplication]]())
    if (!negativeEdges(x).contains(y))
      negativeEdges(x).put(y, new mutable.HashSet[Multiplication]())
    negativeEdges(x)(y).add(p)
  }

  private def applyPositiveEdgesToNegativeGraph(): Unit = {
    edges.keys.foreach(n1 =>
      edges(n1).keys.foreach(n2 => {
        val pos = edges(n1)(n2)
        if (negativeNodes.contains(n1)) {
          negativeNodes(n1).foreach(neg =>
            updateNegativeNode(n1, propagate(neg, pos))
          )
        }
        if (negativeEdges.contains(n2)) {
          negativeEdges(n2).keys.foreach(n3 =>
            negativeEdges(n2)(n3).foreach(neg =>
              updateNegativeEdge(n1, n3, propagate(neg, pos))
            )
          )
        }
      })
    )
  }

  private def chooseAssignment(properties: Set[OWLDataProperty]): (OWLDataProperty, BigFraction) = {
    throw new AssertionError("Not implemented!") // TODO
  }

  private def reduceNegative(x: OWLDataProperty, v: BigFraction): Unit = {
    // Remove all constraints that only talk about x
    negativeNodes.remove(x)
    negativeEdges.remove(x)
    negativeEdges.keys.foreach(y => {
      negativeEdges(y).getOrElse(x, Set.empty).foreach {
        // Eliminate x from sum constraints
        case Multiplication(_, d, _) => updateNegativeNode(y, MultEqual(y, v.divide(d)))
      }
      negativeEdges(y).remove(x)
    })
  }

  private def reducePositive(x: OWLDataProperty, v:  BigFraction): Unit = {
    // Remove all constraints that only talk about x
    nodes.remove(x)
    edges.remove(x)
    edges.keys.foreach(y => {
      edges(y).get(x).foreach {
        // Eliminate x from sum constraints
        case Multiplication(_, d, _) => update(y, MultEqual(y, v.divide(d)))
      }
      edges(y).remove(x)
    })
  }

}
