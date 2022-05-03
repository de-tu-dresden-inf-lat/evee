package de.tu_dresden.inf.lat.evee.forgettingBasedProofs.minimal

import com.typesafe.scalalogging.Logger
import de.tu_dresden.inf.lat.dltools.DLFilter
import de.tu_dresden.inf.lat.evee.forgettingBasedProofs.ForgettingBasedProofGenerator
import de.tu_dresden.inf.lat.evee.forgettingBasedProofs.dataStructures.{Forgetter, Justifier}
import de.tu_dresden.inf.lat.evee.forgettingBasedProofs.tools.SearchTreeProgressTracker
import de.tu_dresden.inf.lat.prettyPrinting.formatting.SimpleOWLFormatter
import de.tu_dresden.inf.lat.evee.proofs.data.exceptions.{ProofGenerationCancelledException, ProofGenerationFailedException}
import org.semanticweb.HermiT
import org.semanticweb.owlapi.model.parameters.Imports
import org.semanticweb.owlapi.model.{OWLAxiom, OWLClass, OWLClassExpression, OWLEntity, OWLObjectProperty, OWLObjectRestriction, OWLObjectSomeValuesFrom, OWLOntology, OWLQuantifiedObjectRestriction}

import scala.collection.{JavaConverters, mutable}
import scala.collection.JavaConverters.{asScalaIteratorConverter, asScalaSetConverter}

class SymbolMinimalForgettingBasedProofGenerator(var forgetter: Forgetter,
                                                 filter: DLFilter,
                                                 justifier: Justifier,
                                                 skipSteps: Boolean = true,
                                                 varyJustifications: Boolean = false)
//                                           proofMeasure: ProofMeasure,
//                                           approximateProofMeasure: ApproximateProofMeasure)
  extends ForgettingBasedProofGenerator(new RobustForgetter(forgetter), filter, justifier, skipSteps) {

  val logger = Logger[SymbolMinimalForgettingBasedProofGenerator]

  if(skipSteps)
    logger.debug("skipping steps on")
  if(varyJustifications)
    logger.debug("varying also justifications")

  this.forgetter = new RobustForgetter(forgetter)

  var signatureSize = 0
  var maxProgress = 0L

  var searchTreeProgressTracker: SearchTreeProgressTracker = _// = new SearchTreeProgressTracker(progressTrackers,1,0)

  override def computeOntologySteps(): Unit = {

    forgettingCache.clear()

    logger.debug(s"toProve: ${SimpleOWLFormatter.format(toProve)}")
    val initial = justifier.justify(JavaConverters.asScalaSet(ontology.getLogicalAxioms(Imports.INCLUDED)), toProve)

    logger.debug("Initial: ")
    logger.debug(s"${initial.map(SimpleOWLFormatter.format).mkString("\n")}")
    logger.debug("")

    setSignatureSize(initial)

    bestProof(initial, Set()) match {
      case Some(x) => {
        this.ontologySteps = x._1.reverse
        this.signatureSteps = (null :: x._2).reverse

        // do the rest in the classical way to eliminate the roles
        /*while(!doneForgettingAndJustifying) {
        forgetAndJustify()
        progressTrackers.increment()
      }*/

        logger.debug(s"signatureSteps: ${signatureSteps}")
        logger.debug("ontologySteps:")
        var count = 0
        ontologySteps.foreach { step =>
          count += 1
          logger.debug(s"Step ${count}")
          logger.debug("----------------------")
          logger.debug(s"${step.map(SimpleOWLFormatter.format).mkString("\n")}")
          logger.debug("----------")
        }
      }
      case None if canceled =>
        throw new ProofGenerationCancelledException("Proof generation cancelled");
      case None if !canceled =>
        throw new ProofGenerationFailedException("No best proof found!")
    }

    maxSet=false
  }

  /**
   * Sets the signature size, and with it also the maximal value for the progress trackers
   * We over-approximate the number of nodes in the search tree. Let n be the signature size.
   * Then there are n! many paths in the search (each branching by the number of remaining symbols).
   * We multiply this by their maximal path length of n, obtaining n*n! as maximal progress.
   * Since roles are eliminated separately, we add the number of roles in the end, plus a constant of
   * 2 for the proof reconstruction step.
   */
  def setSignatureSize(axioms: Set[OWLAxiom]): Unit ={
    signatureSize = axioms
      .flatMap(_.getClassesInSignature.asScala)
      .filter(e => !e.isTopEntity && !e.isBottomEntity && !targetSignature(e))
      .size

    //println("Assuming maximal search depth of "+signatureSize)

    val roleNumber = axioms
      .flatMap(_.getObjectPropertiesInSignature.asScala)
      .size

    //maxProgress = signatureSize*MathTools.factorial(signatureSize  )

    //progressTrackers.setMax( maxProgress + roleNumber + 2)

    searchTreeProgressTracker = new SearchTreeProgressTracker(progressTrackers,signatureSize+roleNumber,2)

    maxSet=true
  }


  def bestProof(premises: Set[OWLAxiom],
                done: Set[OWLEntity],
                bound: Option[Integer] = None): Option[(List[Set[OWLAxiom]],List[OWLEntity])] = {

    logger.trace(s"enter with bound ${bound}")
    logger.trace(s"premises are : ${premises.map(SimpleOWLFormatter.format).mkString("[", ", ", "]")}")
    if(bound.exists(_<=0)) {
      logger.trace("bound reached - leave")
      return None
    } else if(finished(premises, done)) {
      logger.trace("We are finished in")
      logger.trace("leave")
      return Some((List(premises),List()))
    } else {


      var currentBound = bound
      var currentBestProof: Option[(List[Set[OWLAxiom]],List[OWLEntity])] = None
      val nextOptions = nextSteps(premises,done).toList.sortBy(x => approximateStepCost(x._1))

      if(nextOptions.isEmpty) {
        logger.trace("We are finished in")
        logger.trace("leave")
        return Some((List(premises),List()))
      }

      logger.trace(s"done: ${done.size} ${done.map(SimpleOWLFormatter.format).mkString("[", ", ", "]")}")

      logger.trace(s"currentBound: ${currentBound}")

      logger.trace(s"${nextOptions.size} next options to explore")

      var previousProgress = progressTrackers.getProgress



      if(!done.isEmpty)
        progressTrackers.setMessage("trying "+done.map(SimpleOWLFormatter.format(_)).mkString(", "))


      nextOptions.foreach(nextOption => {
        progressTrackers.increment() // increment by one before going down the search tree
        if(!canceled)
          bestProof(nextOption._1, done + nextOption._2, currentBound.map(_-1))
          .foreach { bestPair => // first list: sequence of ontologies, second list: names forgotten

            logger.trace(s"oldBound: ${currentBound}")
            logger.trace(s"oldBestProof: ${currentBestProof.map(_._2)}")
            currentBestProof = Some((premises::bestPair._1, nextOption._2::bestPair._2))
            val oldBound = currentBound
            currentBound = Some(bestPair._1.size) // note: x_1
            logger.trace(s"nextBound: ${currentBound}")
            logger.trace(s"nextBestProof: ${currentBestProof.map(_._2)}")
            if(!oldBound.forall(_>currentBound.get))
              throw new AssertionError("bound increased!")
        }
        // increment by current level size when coming back from search:
        previousProgress = searchTreeProgressTracker.increment(previousProgress, done.size+1)
      })


      if(currentBestProof.isEmpty)
        logger.trace("no proof found")

      logger.trace("leave")
      currentBestProof
    }

  }

  val forgettingCache = new mutable.HashMap[Set[OWLEntity], Set[OWLAxiom]]()

  def nextSteps(premises: Iterable[OWLAxiom], done: Set[OWLEntity]): Iterable[(Set[OWLAxiom],OWLEntity)] = {
    premises.flatMap(x => JavaConverters.asScalaSet(x.getSignature))
      .filterNot(inTarget)
//      .filterNot(x => x.isTopEntity || x.isBottomEntity)
      .filterNot(done)
      .filter(acceptable(_,premises))
      .flatMap { name =>
        val signature = done + name
        var nextAxioms = {
          forgettingCache.getOrElseUpdate(
            signature,
            forgetter.forget(premises, name)
          )
        }
        if(!varyJustifications) {
          nextAxioms = justifier.justify(nextAxioms, toProve)
          if(nextAxioms.isEmpty)
            throw new ProofGenerationFailedException("entailments lost during forgetting operation");

          Some(nextAxioms, name)
        } else {
          justifier.justifyAll(nextAxioms, toProve).map((_,name))
        }
      }
  }

  def acceptable(e: OWLEntity, axioms: Iterable[OWLAxiom]) = {
    e.isOWLClass ||
      axioms.forall(
        _.getNestedClassExpressions
          .asScala
          .forall(_ match {
            case restriction: OWLQuantifiedObjectRestriction =>
              !restriction.getProperty.equals(e.asInstanceOf[OWLObjectProperty]) ||
                restriction.getFiller.isOWLThing ||
                restriction.getFiller.isOWLNothing
            case _ => true
          }))
  }

  def inTarget(e: OWLEntity) = e match {
    case c: OWLClass => c.isTopEntity || c.isBottomEntity || targetSignature(c)
    //case c: OWLObjectProperty => true
    case _ => false
  }

  def finished(axioms: Set[OWLAxiom], processed: Set[OWLEntity]) =
    axioms.forall(x => JavaConverters.asScalaSet(x.getSignature)
      .forall(c => inTarget(c) || processed(c) ))


  def approximateStepCost(axioms:Iterable[OWLAxiom]) =
    axioms.flatMap(x => JavaConverters.asScalaSet(x.getSignature)).filterNot(inTarget).size

}

class RobustForgetter(forgetter: Forgetter) extends Forgetter {
  val logger = Logger[RobustForgetter]

  override def forget(axioms: Iterable[OWLAxiom], name: OWLEntity): Set[OWLAxiom] = {
    try {
      logger.trace("Forget "+SimpleOWLFormatter.format(name))
      val ui = forgetter.forget(axioms, name)
      val before = axioms.toSet[OWLAxiom].flatMap(_.getSignature.asScala)
      if(!ui.flatMap(_.getSignature.asScala).forall(before)) {
        logger.trace(s"definers introduced - take as failed")
        return axioms.toSet
      } else
        return ui
    } catch {
      case e: Exception =>
        e.printStackTrace()
        logger.error(s"could not forget ${name} due exception: ${e}, ${e.getMessage}")
        axioms.toSet
    }
  }
}