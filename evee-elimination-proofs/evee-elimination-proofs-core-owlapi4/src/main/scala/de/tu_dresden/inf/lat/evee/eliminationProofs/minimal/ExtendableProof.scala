package de.tu_dresden.inf.lat.evee.eliminationProofs.minimal

import de.tu_dresden.inf.lat.prettyPrinting.formatting.SimpleOWLFormatter
import de.tu_dresden.inf.lat.evee.proofs.data.{Inference, Proof}
import de.tu_dresden.inf.lat.evee.proofs.interfaces.{IInference, IProof}
import org.semanticweb.owlapi.model.OWLAxiom

import java.util
import java.util.Collections
import scala.collection.JavaConverters
import scala.collection.JavaConverters.iterableAsScalaIterableConverter

class ExtendableProof[T](conclusion: T) extends Proof[T](conclusion) {

  private var premises: Set[T] = Set()

  def this(proof: IProof[T]) {
    this(proof.getFinalConclusion)
    this.addInferences(proof.getInferences)
  }

  /**
   * Returns all inferences that have the given premise
   * @param premise
   * @return
   */
  def inferencesWithPremise(premise: T) = {
    getInferences().asScala.toSet[IInference[T]].filter(_.getPremises().contains(premise))
  }

  def addPremises(premises: Set[T]) = {
    this.premises ++= premises
  }

  def getPremises() = premises

  override def addInference(inference: IInference[T]): Unit = {
    super.addInference(inference)
    this.premises -= inference.getConclusion
  }

  def assertAllPremises() = {
    premises.foreach(premise =>
      addInference(new Inference(premise, "asserted", Collections.emptyList))
    )
  }

  def asProof(): Proof[T] =
    new Proof[T](conclusion, getInferences)

  override def toString() = {
    super.toString+"\n open premises: "+
      premises.map(x => SimpleOWLFormatter.format(x.asInstanceOf[OWLAxiom]))
        .mkString("[",", ","]")
  }

  def copy(): ExtendableProof[T] = {
    val result = new ExtendableProof[T](conclusion)
    getInferences.forEach(result.addInference)
    result.premises=premises
    result
  }

}
