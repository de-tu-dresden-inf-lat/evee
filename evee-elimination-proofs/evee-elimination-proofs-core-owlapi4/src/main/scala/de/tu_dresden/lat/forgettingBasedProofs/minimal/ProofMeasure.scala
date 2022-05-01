package de.tu_dresden.inf.lat.forgettingBasedProofs.minimal

import de.tu_dresden.inf.lat.forgettingBasedProofs.tools.OntologyTools
import de.tu_dresden.inf.lat.prettyPrinting.formatting.SimpleOWLFormatter
import de.tu_dresden.inf.lat.proofs.data.Proof
import de.tu_dresden.inf.lat.proofs.interfaces.{IInference, IProof}
import org.semanticweb.owlapi.model.{OWLAxiom, OWLEntity}
import de.tu_dresden.inf.lat.proofs.interfaces.IProofEvaluator

import scala.collection.JavaConverters
import scala.collection.convert.ImplicitConversions.`collection AsScalaIterable`

/*trait ProofMeasure {
  def evaluate(proof: IProof[OWLAxiom]): Double
  def evaluate(step: IInference[OWLAxiom]): Double
}*/


class SignatureOptimisedProofEvaluator(proofEvaluator: IProofEvaluator[OWLAxiom], signature: Set[OWLEntity])
  extends IProofEvaluator[OWLAxiom] {

  println("Hey guys, my signature is: "+signature.map(SimpleOWLFormatter.format))

  override def evaluate(proof: IProof[OWLAxiom]): Double = {
  //  println("  --- The proof: "+proof)
  //  println("  .... Filters to: "+filterProof(proof))

    proofEvaluator.evaluate(filterProof(proof))
  }

  private def filterProof(proof: IProof[OWLAxiom]): IProof[OWLAxiom] = {

    if(proof.getInferences.isEmpty)
      return proof

    val newProof = proof match {
      case proof: ExtendableProof[OWLAxiom] => new ExtendableProof(proof.getFinalConclusion)
      case _ => new Proof(proof.getFinalConclusion)
    }

    addFilteredInferences(newProof, proof, proof.getFinalConclusion)
    /*(proof,newProof) match {
      case (proof: ExtendableProof[OWLAxiom], newProof: ExtendableProof[OWLAxiom]) =>
        newProof.addPremises(proof.getPremises())
    }*/
    newProof
  }

  private def addFilteredInferences(newProof: Proof[OWLAxiom],
                            proof: IProof[OWLAxiom],
                            axiom: OWLAxiom):Unit = {
    if(ignorePremises(axiom) || !proof.hasInferenceFor(axiom)) {
      (newProof) match {
        case newProof: ExtendableProof[OWLAxiom] => newProof.addPremises(Set(axiom))
      }
      return
    } else proof.getInferences(axiom).foreach(inference =>
      if(!newProof.getInferences.contains(inference)) {
        newProof.addInference(inference)
        inference.getPremises
          .foreach(addFilteredInferences(newProof,proof,_))
      })
  }

  override def evaluate(step: IInference[OWLAxiom]): Double = {
    if(ignorePremises(step.getConclusion))
      return 1
    else
      return proofEvaluator.evaluate(step)
  }

  private def ignorePremises(axiom: OWLAxiom) = {
    val result = JavaConverters.asScalaSet(axiom.getSignature).forall(signature)
    if(result) println("Ignore: "+SimpleOWLFormatter.format(axiom))
    result
  }

}

object ProofEvaluatorAxiomNumber extends IProofEvaluator[OWLAxiom] {
  override def evaluate(proof: IProof[OWLAxiom]) = proof match {
    case proof: ExtendableProof[OWLAxiom] => (proof.getPremises() ++
      proof.getInferences
        .flatMap(inf => inf.getPremises ++ Set(inf.getConclusion))
      ).size
    case _ =>
      proof.getInferences
        .flatMap (inf => inf.getPremises ++ Set (inf.getConclusion) )
        .size
  }
  override def evaluate(step: IInference[OWLAxiom]) = 1+step.getPremises.size
}

object ProofEvaluatorAxiomSizeSum extends IProofEvaluator[OWLAxiom] {

  override def evaluate(proof: IProof[OWLAxiom]) = proof match {
    case proof: ExtendableProof[OWLAxiom] => (proof.getPremises() ++
      proof.getInferences
        .flatMap(inf => inf.getPremises ++ Set(inf.getConclusion))
      ).toSeq
      .map(OntologyTools.size).sum
    case _ =>
      proof.getInferences
        .flatMap (inf => inf.getPremises ++ Set (inf.getConclusion) )
        .toSeq
        .map(OntologyTools.size).sum
  }

  override def evaluate(step: IInference[OWLAxiom]) =
    OntologyTools.size(step.getConclusion)+
      step.getPremises.map(OntologyTools.size).sum

}

object ProofEvaluatorInferenceNumber extends IProofEvaluator[OWLAxiom] {
  override def evaluate(proof: IProof[OWLAxiom]) = proof match {
    case proof: ExtendableProof[OWLAxiom] => proof.getInferences().size() + proof.getPremises().size
    case _ =>
      proof.getInferences.size()
  }

  override def evaluate(step: IInference[OWLAxiom]) = 1
}

/** tries to approximate the size of a proof according to a given measure  */
trait ApproximateProofMeasure {
  var knownSignature: Set[OWLEntity]
  def evaluate(step: IInference[OWLAxiom]): Double
  def evaluate(axioms: Iterable[OWLAxiom]): Double
  val approximationOf:  IProofEvaluator[OWLAxiom]

  def lowerApproximation(axioms: Iterable[OWLAxiom]): Double
}


class ApproximateProofMeasureAxiomSizeSum(var knownSignature: Set[OWLEntity] = Set())
  extends ApproximateProofMeasure  {

  override def evaluate(step: IInference[OWLAxiom]) = {
    val premises = JavaConverters.collectionAsScalaIterable(step.getPremises())
    evaluate(premises)
  }

  override def evaluate(axioms: Iterable[OWLAxiom]) = {
    val signature = axioms.toSet.flatMap(
      (x: OWLAxiom) => JavaConverters.asScalaSet(x.getClassesInSignature))
    axioms.toSeq.map(OntologyTools.size).sum * ((signature.filterNot(knownSignature).size - 2))
  }


  override def lowerApproximation(axioms: Iterable[OWLAxiom]) = {
    val signature = axioms.toSet.flatMap(
      (x: OWLAxiom) => JavaConverters.asScalaSet(x.getClassesInSignature))
    axioms.toSeq.map(OntologyTools.size).sum// + (signature.filterNot(knownSignature).size-2)*3
  }

  val approximationOf = ProofEvaluatorInferenceNumber
}


class ApproximateProofMeasureInferenceNumber(var knownSignature: Set[OWLEntity] = Set())
  extends ApproximateProofMeasure  {
  def evaluate(step: IInference[OWLAxiom]) = {
    val premises = JavaConverters.collectionAsScalaIterable(step.getPremises())
    evaluate(premises)
  }

  override def evaluate (axioms: Iterable[OWLAxiom]) = {
    val signature = axioms.toSet.flatMap(
      (x: OWLAxiom) => JavaConverters.asScalaSet(x.getClassesInSignature))
      .filterNot(knownSignature)
    axioms.size * (signature.size-2)
  }

  override def lowerApproximation(axioms: Iterable[OWLAxiom]) = {
    //val signature = axioms.toSet.flatMap(
    //  (x: OWLAxiom) => JavaConverters.asScalaSet(x.getClassesInSignature))
    //  .filterNot(knownSignature)
    axioms.filterNot(_.getSignature.forall(knownSignature)).size+1
    //axioms.size// + signature.size - 2
  }

  val approximationOf = ProofEvaluatorInferenceNumber
}


class ApproximateForgettingBasedProofDepthMeasure(var knownSignature: Set[OWLEntity] = Set())
  extends ApproximateProofMeasure {
  override val approximationOf: IProofEvaluator[OWLAxiom] = ProofEvaluatorInferenceNumber

  override def evaluate(step: IInference[OWLAxiom]) =
    evaluate(JavaConverters.collectionAsScalaIterable(step.getPremises))

  override def evaluate(axioms: Iterable[OWLAxiom]) =
    axioms
      .toSet.flatMap((x: OWLAxiom) => JavaConverters.asScalaSet(x.getSignature))
      .filterNot(knownSignature).size

  override def lowerApproximation(axioms: Iterable[OWLAxiom]): Double = {
    0
/*    axioms
      .toSet.flatMap((x: OWLAxiom) => JavaConverters.asScalaSet(x.getClassesInSignature))
      .filterNot(knownSignature).size -2 */
  }
}