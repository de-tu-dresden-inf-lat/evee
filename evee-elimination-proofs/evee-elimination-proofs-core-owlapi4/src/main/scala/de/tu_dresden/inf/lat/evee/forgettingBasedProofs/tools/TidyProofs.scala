package de.tu_dresden.inf.lat.evee.forgettingBasedProofs.tools

import com.typesafe.scalalogging.Logger
import de.tu_dresden.inf.lat.evee.forgettingBasedProofs.Constants
import de.tu_dresden.inf.lat.prettyPrinting.formatting.{SimpleOWLFormatter, SimpleOWLFormatterCl}
import de.tu_dresden.inf.lat.evee.proofs.data.{Inference, Proof}
import de.tu_dresden.inf.lat.evee.proofs.interfaces.{IInference, IProof}
import org.semanticweb.owlapi.model.{OWLAxiom, OWLEntity}

import scala.collection.JavaConverters.{collectionAsScalaIterableConverter, setAsJavaSetConverter}

class TidyForgettingBasedProofs(formatter: SimpleOWLFormatterCl) {

  val logger = Logger[TidyForgettingBasedProofs]

  val ASSERTED_RULE = "asserted"
  val NO_SIG_RULE = "infer"
  val ELIMINATE_PREFIX = "eliminate "

  def tidy(proof: IProof[OWLAxiom]): IProof[OWLAxiom] = {

    val inferences = proof.getInferences().asScala.map(tidy).toSet.asJava

    new Proof[OWLAxiom](proof.getFinalConclusion, inferences)
  }

  def tidy(inference: IInference[OWLAxiom]): IInference[OWLAxiom] = {

    if(inference.getRuleName.equals(ASSERTED_RULE))
      return inference

    if(!inference.getRuleName.startsWith(ELIMINATE_PREFIX))
      return inference

    //if(!inference.getRuleName.startsWith(Constants.RULE_NAME_PREFIX))
    //  return inference

    val premiseSignature =
      inference
        .getPremises()
        .asScala
        .toSet[OWLAxiom]
        .flatMap(_.getSignature().asScala)
        .filterNot(x => x.isTopEntity || x.isBottomEntity)
        .map(getName(_))

    val conclusionsSignature =
      inference
        .getConclusion
        .getSignature
        .asScala
        .toSet[OWLEntity]

    val names =
      //premiseSignature.filterNot(conclusionsSignature(_))
      //  .map(getName(_))
       inference
       .getRuleName()
       .substring(Constants.RULE_NAME_PREFIX.size)
       .split(",")
        .map(_.trim())
        .filter(premiseSignature)

    val inferenceDescription =
      if(names.isEmpty)
        NO_SIG_RULE
      else
        Constants.RULE_NAME_PREFIX+names.mkString(", ")

    logger.trace(s"${inference.getRuleName} becomes ${inferenceDescription}")
    logger.trace(s"because the premise contains ${premiseSignature}")

    new Inference[OWLAxiom](inference.getConclusion, inferenceDescription, inference.getPremises)
  }

  def getName(entity: OWLEntity) =
    formatter.format(entity)
}
