package de.tu_dresden.inf.lat.evee.concreteDomains.diffDomain

import de.tu_dresden.inf.lat.evee.concreteDomains.ConstraintNetwork
import de.tu_dresden.inf.lat.evee.proofs.data.Inference
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInference
import de.tu_dresden.inf.lat.evee.proofs.tools.ProofTools
import org.semanticweb.owlapi.model.OWLDataProperty

import scala.collection.JavaConverters.seqAsJavaListConverter
import scala.collection.mutable

/**
 * Assume at most one unary constraint per node - complete for finding some proof, incomplete for finding all proofs.
 */
class DiffNetworkSimple extends ConstraintNetwork[DiffConstraint] {

  private var continueEvenIfInconsistent = false //false

  private var derived = Set[DiffConstraint]()
  private var inferences = Set[IInference[DiffConstraint]]()


  private val nodes = new mutable.HashMap[OWLDataProperty, DiffUnaryPredicate]()
  private val edges = new mutable.HashMap[OWLDataProperty,
    mutable.Map[OWLDataProperty,DiffSum]]()

  private var inconsistent: Boolean = false


  def setContinueEvenIfInconsistent() = {
    this.continueEvenIfInconsistent=true
  }

  def fill(predicates: Iterable[DiffConstraint]) = {
    inferences ++= predicates.map(new Inference(_, ProofTools.ASSERTED_1, List[DiffConstraint]().asJava))
    derived ++= predicates
    predicates.foreach(p => p match {
      case DiffContradiction => ;
      case DiffGreaterThan(d, _) =>
        update(d, p.asInstanceOf[DiffUnaryPredicate])
      case DiffEqual(d, _) =>
        update(d, p.asInstanceOf[DiffUnaryPredicate])
      case DiffSum(d1, _, d2) =>
        update(d1, d2, p.asInstanceOf[DiffSum])
    })
  }


  def propagate(): Unit = {

      deriveEdgesFromUnary()
      //println("Step 1: "+derived)

      mirrorEdges() // add corresponding backward edges
      //println("Step 2: " + derived)

      addTautologicalEdges() // add x+0-x for all used x

      propagateEdges() // derives all implicit edges from existing edges
      // this does not make new inferences in the previous steps possible:
      // first depends only on unary predicates,
      // all edges added by second step are added here as well
      //println("Step 3: " + derived)

      propagateValues() // propagate the values along the edges
      // this also does not make new inferences in the previous steps possible:
      // deriveEdgesFromUnary() would just add edges that were already there (we assume all implicit egdes to be derived  by propagateEdges)
      // the other only depend on binary predicates, which are not added by propagateValues
      //println("Step 4: "+derived)
  }

  def derivedPredicates() = {
    if(inconsistent)
      derived + DiffContradiction
    else
      derived
  }

  def derivationStructure() = inferences

  private def deriveEdgesFromUnary() = {

    // derive edges from values
    nodes.keys.foreach(n1 =>
      nodes.get(n1).foreach(p1 => p1 match {
        case DiffEqual(_, v1) =>
          nodes.keys.foreach(n2 =>
            nodes.get(n2).foreach(p2 => p2 match {
              case DiffEqual(_, v2) =>
                val newPred = DiffSum(n1, (v2 - v1), n2)
                update(n1,n2, newPred)
                derived += newPred
                inferences += new Inference(newPred, CD2Rules.RULE_DIFFERENCE_FROM_CONSTANTS, List(p1,p2).asJava)
              case _: DiffGreaterThan => ;
            }))
        case _: DiffGreaterThan => ;
      }))
  }

  private def mirrorEdges(): Unit = {
    edges.keys.foreach(n1 =>
    edges(n1).keys.foreach(n2 => {
      val newPred = DiffSum(n2, -edges(n1)(n2).diff, n1)
      update(n2,n1,newPred)
      derived += newPred
      inferences += new Inference(newPred, DiffRules.RULE_INVERSE, List(edges(n1)(n2)).asJava)
    }))
  }

  private def addTautologicalEdges(): Unit = {
    edges.keySet.union(nodes.keySet).foreach(n1 => {
      val newPred = DiffSum(n1, 0, n1)
      update(n1,n1,newPred)
      derived += newPred
      inferences += new Inference(newPred, DiffRules.RULE_ZERO, List.empty.asJava)
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
          val newPred = propagate(edges(n1)(n2),edges(n2)(n3))
          derived += newPred
          inferences += new Inference(newPred, DiffRules.RULE_TRANSITIVITY, List(edges(n1)(n2), edges(n2)(n3)).asJava)
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
              case DiffEqual(_, _) => DiffRules.RULE_PROPAGATE_EQUAL
              case DiffGreaterThan(_, _) => DiffRules.RULE_PROPAGATE_GREATER_THAN
            }
            inferences += new Inference(newPred, ruleName, List(unary1, edges(n1)(n2)).asJava)
            update(n2, newPred)
          })))
  }

  private def propagate(pred1: DiffSum, pred2: DiffSum): DiffSum =
    DiffSum(
      pred1.property1,
      pred1.diff + pred2.diff,
      pred2.property2)

  private def propagate(unary1: DiffUnaryPredicate,
                predicate: DiffSum
               ): DiffUnaryPredicate =
    unary1 match {
      case DiffEqual(property1, value1) =>
        DiffEqual(predicate.property2, value1 + predicate.diff)

      case DiffGreaterThan(property1, value1) =>
        DiffGreaterThan(predicate.property2, value1 + predicate.diff)
    }

  private def update(d: OWLDataProperty, predicate: DiffUnaryPredicate): Unit = {
    if(!nodes.contains(d))
      nodes.put(d,predicate)
    else (nodes(d), predicate) match {
      case (same1,same2) if same1.equals(same2) => ; // no update
      case (_: DiffEqual, _: DiffEqual) =>
        inconsistent = true
        inferences += new Inference(DiffContradiction, DiffRules.RULE_DIFFERENT_CONSTANTS, List(nodes(d), predicate).asJava)
      case (DiffGreaterThan(_,v1), DiffGreaterThan(_,v2)) =>
        if(v1<=v2)
          nodes.put(d,predicate)
      case (DiffGreaterThan(_,v1), DiffEqual(_,v2)) =>
        if(v1<v2)
          nodes.put(d,predicate)
        else {
          inconsistent = true
          inferences += new Inference(DiffContradiction, DiffRules.RULE_CONSTANT_TOO_SMALL, List(predicate, nodes(d)).asJava)
        }
      case (DiffEqual(_,v1), DiffGreaterThan(_,v2)) =>
        if(v1<=v2) {
          inferences += new Inference(DiffContradiction, DiffRules.RULE_CONSTANT_TOO_SMALL, List(nodes(d), predicate).asJava)
          inconsistent = true
        }
    }
  }

  private def update(d1: OWLDataProperty, d2: OWLDataProperty, p: DiffSum) = {
    if(!edges.contains(d1))
      edges.put(d1, new mutable.HashMap[OWLDataProperty, DiffSum]())
    edges(d1).get(d2) match {
      case None => edges(d1).put(d2,p)
      case Some(other) if !other.equals(p) => {
        inconsistent = true
        inferences += new Inference(DiffContradiction, DiffRules.RULE_DIFFERENT_DIFFERENCES, List(other, p).asJava)
      }
      case Some(p) => ;
    }
  }

  private var negativeNodes: mutable.Map[OWLDataProperty, mutable.Set[DiffUnaryPredicate]] = _
  private var negativeEdges: mutable.Map[OWLDataProperty, mutable.Map[OWLDataProperty, mutable.Set[DiffSum]]] = _

  def generateSolution(negativePredicates: Iterable[DiffConstraint]): Map[OWLDataProperty, Double] = {
    // "complete" the negative constraints w.r.t. the positive edges in the constraint network
    negativeNodes = new mutable.HashMap[OWLDataProperty, mutable.Set[DiffUnaryPredicate]]()
    negativeEdges = new mutable.HashMap[OWLDataProperty, mutable.Map[OWLDataProperty, mutable.Set[DiffSum]]]()
    fillNegativeGraphAndMirror(negativePredicates)
    applyPositiveEdgesToNegativeGraph()

    var properties = derived.flatMap(_.getOWLDataProperties).union(negativePredicates.flatMap(_.getOWLDataProperties).toSet)

    var solution = Map[OWLDataProperty, Double]()

    while (properties.nonEmpty) {
      // choose a new value for an un-assigned variable and update the constraints
      val (x, v) = chooseAssignment(properties)
      reducePositive(x, v)
      reduceNegative(x, v)
      properties -= x
      solution += (x -> v)
    }

    return solution
  }

  private def fillNegativeGraphAndMirror(negative: Iterable[DiffConstraint]): Unit = {
    negative.foreach {
      case DiffContradiction => ;
      case p@DiffGreaterThan(x, _) => updateNegativeNode(x, p)
      case p@DiffEqual(x, _) => updateNegativeNode(x, p)
      case p@DiffSum(x, d, y) => {
        updateNegativeEdge(x, y, p)
        updateNegativeEdge(y, x, DiffSum(y, -d, x))
      }
    }
  }

  private def updateNegativeNode(x: OWLDataProperty, p: DiffUnaryPredicate): Unit = {
    if (!negativeNodes.contains(x))
      negativeNodes.put(x, new mutable.HashSet[DiffUnaryPredicate]())
    negativeNodes(x).add(p)
  }

  private def updateNegativeEdge(x: OWLDataProperty, y: OWLDataProperty, p: DiffSum): Unit = {
    if(!negativeEdges.contains(x))
      negativeEdges.put(x, new mutable.HashMap[OWLDataProperty, mutable.Set[DiffSum]]())
    if (!negativeEdges(x).contains(y))
      negativeEdges(x).put(y, new mutable.HashSet[DiffSum]())
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

  private def chooseAssignment(properties: Set[OWLDataProperty]): (OWLDataProperty, Double) = {
    // If there is a constraint x=v, then assign v to x
    for (n1 <- nodes.keys) {
      nodes(n1) match {
        case DiffEqual(x, v) => return (x, v)
        case other => ;
      }
    }

    // If there are no constraints of the form x=v, choose a random variable x
    // and collect all relevant constraints about x
    val x = properties.head
    val lowerBoundExclusive = (nodes.get(x) collect { case DiffGreaterThan(_, e) => e })
      .reduceOption(_ max _)
    val upperBoundInclusive = (negativeNodes.getOrElse(x, Set.empty) collect { case DiffGreaterThan(_, e) => e })
      .reduceOption(_ min _)
    assert(lowerBoundExclusive.isEmpty || upperBoundInclusive.isEmpty || lowerBoundExclusive.get < upperBoundInclusive.get)
    // Collect all inequalities between the lower and upper bounds (if they exist)
    // and also disallow the lower bound (which is strict)
    var forbiddenValues = (negativeNodes.getOrElse(x, Set.empty) collect { case DiffEqual(_, e) if
      (lowerBoundExclusive.isEmpty || e > lowerBoundExclusive.get) &&
      (upperBoundInclusive.isEmpty || e <= upperBoundInclusive.get) => e
    })
      .union(lowerBoundExclusive.toSet)

    // Find a value in between the lower and upper bounds and not equal to one of the forbidden values
    def allowedValue: Double = {
      if (upperBoundInclusive.isDefined && !forbiddenValues.contains(upperBoundInclusive.get))
        upperBoundInclusive.get
      else {
        if (forbiddenValues.size == 0)
          return 0d
        if (forbiddenValues.size == 1)
          return forbiddenValues.head + 1d
        val lowest = forbiddenValues.min
        val nextLowest = forbiddenValues.filter(_ > lowest).min
        (lowest + nextLowest) / 2
      }
    }

    (x, allowedValue)
  }

  private def reduceNegative(x: OWLDataProperty, v: Double): Unit = {
    // Remove all constraints that only talk about x
    negativeNodes.remove(x)
    negativeEdges.remove(x)
    negativeEdges.keys.foreach(y => {
      negativeEdges(y).getOrElse(x, Set.empty).foreach {
        // Eliminate x from sum constraints
        case DiffSum(_, d, _) => updateNegativeNode(y, DiffEqual(y, v - d))
      }
      negativeEdges(y).remove(x)
    })
  }

  private def reducePositive(x: OWLDataProperty, v: Double): Unit = {
    // Remove all constraints that only talk about x
    nodes.remove(x)
    edges.remove(x)
    edges.keys.foreach(y => {
      edges(y).get(x).foreach {
        // Eliminate x from sum constraints
        case DiffSum(_, d, _) => update(y, DiffEqual(y, v - d))
      }
      edges(y).remove(x)
    })
  }

}
