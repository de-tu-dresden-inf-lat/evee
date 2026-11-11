package de.tu_dresden.inf.lat.evee.concreteDomains.owlApi

import de.tu_dresden.inf.lat.evee.concreteDomains.CDConstraint
import de.tu_dresden.inf.lat.evee.proofs.interfaces.{IInference, IRecursiveMeasure}
import de.tu_dresden.inf.lat.evee.proofs.tools.measures.OWLAxiomSizeMeasurer
import org.semanticweb.owlapi.model.{OWLAxiom, OWLClass}

import scala.collection.JavaConverters._

/**
 * The tree size weighted by the sizes of the axioms, as OWLAxiomSizeWeightedTreeSizeMeasure, except that a class with
 * a constraint counts with the size of its constraint (CDConstraint.size) instead of 1.
 *
 * @param constraints the constraints of the classes, such as [[OWLCDProofGenerator.constraints]]
 */
class ConstraintSizeWeightedTreeSizeMeasure(constraints: scala.collection.Map[OWLClass, CDConstraint])
  extends IRecursiveMeasure[OWLAxiom] {

  private val axiomSizeMeasurer = new OWLAxiomSizeMeasurer()

  private def size(axiom: OWLAxiom): Double =
    axiom.accept(axiomSizeMeasurer).doubleValue + OWLCDProofGenerator.classOccurrences(axiom)
      .flatMap(constraints.get).map(_.size - 1).sum

  override def leafValue(sentence: OWLAxiom): Double = size(sentence)

  override def edgeValue(inference: IInference[OWLAxiom], childValues: java.util.List[java.lang.Double]): Double =
    size(inference.getConclusion) + childValues.asScala.map(_.doubleValue).sum

  override def getDescription: String = "Weighted tree size, with the sizes of the constraints"
}
