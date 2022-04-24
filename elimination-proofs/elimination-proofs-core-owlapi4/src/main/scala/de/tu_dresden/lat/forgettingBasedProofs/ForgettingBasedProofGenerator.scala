package de.tu_dresden.inf.lat.forgettingBasedProofs


import com.typesafe.scalalogging.Logger
import de.tu_dresden.inf.lat.dltools.DLFilter
import de.tu_dresden.inf.lat.forgettingBasedProofs.dataStructures.{Forgetter, Justifier}
import de.tu_dresden.inf.lat.forgettingBasedProofs.tools.{Counter, OntologyTools, TidyForgettingBasedProofs}
import de.tu_dresden.inf.lat.proofs.data.{AbstractSimpleOWLProofGenerator, Inference, Proof}
import de.tu_dresden.inf.lat.proofs.interfaces.{IProgressTracker, IProof, IProofGenerator, ISimpleProofGenerator}
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model._
import org.semanticweb.owlapi.reasoner.OWLReasoner
import de.tu_dresden.inf.lat.prettyPrinting.formatting.{ParsableOWLFormatter, SimpleOWLFormatter, SimpleOWLFormatterCl}
import de.tu_dresden.inf.lat.proofs.data.exceptions.ProofGenerationFailedException
import de.tu_dresden.inf.lat.proofs.tools.ProgressTrackerCollection
import org.semanticweb.HermiT
import org.semanticweb.owlapi.model.parameters.Imports

import java.util.concurrent.CancellationException
import scala.collection.JavaConverters._
import scala.collection.{Set, mutable}
import scala.concurrent.CancellationException

/**
 *
 * @param forgetter
 * @param justifier
 * @param skipSteps when constructing the proof from the sequence of ontologies, allow to skip ontologies in the
 *                    sequence if this leads to proof steps of similar or smaller size
 *
 * TODO If hiddenSignature is a relevant field for this class, then it should be an argument of the constructor
 * TODO with a default value (as for skipSteps)
 */
class ForgettingBasedProofGenerator(forgetter: Forgetter,
                                    filter: DLFilter,
                                    justifier: Justifier,
                                    skipSteps: Boolean = true,
                                    var hiddenSignature: Set[OWLEntity] = Set())
  extends IProofGenerator[OWLAxiom,OWLOntology] with ISimpleProofGenerator[OWLClass, OWLAxiom, OWLOntology] {

  private val logger = Logger[ForgettingBasedProofGenerator]

  val manager = OWLManager.createOWLOntologyManager()

  protected val progressTrackers: ProgressTrackerCollection = new ProgressTrackerCollection();

  protected var ontology: OWLOntology = _

  private var owlDataFactory: OWLDataFactory = _

  protected val formatter = new SimpleOWLFormatterCl()
  protected val tidyProofs = new TidyForgettingBasedProofs(formatter)

  protected var canceled: Boolean = false

  override def cancel(): Unit = {
    System.out.println("Canceling execution")
    canceled = true
  }
  override def toString() = {
    "ForgettingBasedProofGenerator("+forgetter+", "+justifier+", skipSteps="+skipSteps+")"
  }

  override def addProgressTracker(tracker: IProgressTracker): Unit =
    progressTrackers.addProgressTracker(tracker)

  private val knownSupport = new mutable.HashMap[OWLAxiom,Boolean]()

  override def supportsProof(axiom: OWLAxiom): Boolean = {
   // if(!super.supportsProof(axiom))
   //   return false

    knownSupport.getOrElseUpdate(axiom, {
      logger.debug(s"We check whether ${axiom}")
      logger.debug(s"is entailed by ${ontology.getLogicalAxioms().asScala.map(formatter.format(_))}")

      val reasoner = new HermiT.Reasoner.ReasonerFactory().createReasoner(ontology)

      reasoner.isEntailed(axiom)
    })
  }

  override def setOntology(owlOntology: OWLOntology): Unit = {

    println("Ontology changed!")

    ontology = filter.filteredCopy(owlOntology,manager)

    owlDataFactory = manager.getOWLDataFactory

    formatter.setReferenceOntology(owlOntology)

    knownSupport.clear()
  }

  //override def setReasoner(reasoner: OWLReasoner) = {
  //  // does nothing
  //}

  override def proveSubsumption(lhs: OWLClass, rhs: OWLClass) = {

    logger.debug("gonna prove that "+lhs+" is subsumed by "+rhs)

    targetSignature = Set(lhs,rhs)
    toProve = subClassOf(lhs,rhs)
    prove()
  }

  override def proveEquivalence(lhs: OWLClass, rhs: OWLClass) = {
    logger.debug("gonna prove that "+lhs+" is equivalent to "+rhs)

    targetSignature = Set(lhs,rhs)
    toProve = equivalence(lhs, rhs)
    prove()
  }

  def proveUnsatisfiability(concept: OWLClass) = {

    logger.debug("gonna prove that "+concept+" is unsatisfiable")

    toProve = unsatisfiable(concept)
    targetSignature = Set(concept)
    prove()
  }

  override def getProof(axiom: OWLAxiom) = {
    toProve = axiom
    targetSignature = axiom.getSignature().asScala.toSet[OWLEntity]

    prove()
  }

  /*def setTargetSignature(sig: Set[OWLEntity]) = {
    hiddenSignature = sig
  }*/

  var toProve: OWLAxiom = _
  var targetSignature: Set[OWLEntity] = _
  var ontologySteps: List[Set[OWLAxiom]] = _
  var signatureSteps: List[OWLEntity] = _
  var doneForgettingAndJustifying: Boolean = _
  var proof: IProof[OWLAxiom] = _

  var originalSignature: Set[OWLEntity] = _
  //var hiddenSignature: Set[OWLEntity] = _
  var adjacentSignature: Set[OWLEntity] = _

  var maxSet: Boolean = false

  protected def prove(): IProof[OWLAxiom] = {

    canceled = false

    progressTrackers.setProgress(0)

    originalSignature = ontology
      .getLogicalAxioms(Imports.INCLUDED)
      .iterator()
      .asScala
      .toSet[OWLAxiom].flatMap(_.getSignature.asScala)

    originalSignature ++= toProve.getSignature.asScala
    originalSignature ++= Set(owlDataFactory.getOWLThing,
      owlDataFactory.getOWLNothing,
      owlDataFactory.getOWLTopObjectProperty,
      owlDataFactory.getOWLBottomObjectProperty)


    computeOntologySteps()


    logger.debug(s"signatureSteps: ${signatureSteps}")
    logger.debug("ontologySteps: ")
    logger.debug(s"${ontologySteps}")
    if(canceled && ontologySteps==null)
      return null
    var count = 0
    assert(ontologySteps!=null)
    ontologySteps.foreach{step =>
      count+=1
      logger.debug(s"Step ${count}")
      logger.debug("----------------------")
      logger.debug(s"${step.map(formatter.format).mkString("\n")}")
      logger.debug("----------")
    }



    proof = new Proof(toProve)

    progressTrackers.setMessage("constructing proof")

    reconstructProofFor(toProve, ontologySteps, signatureSteps, List(null)) //signatureSteps.head))

    progressTrackers.increment()
    progressTrackers.increment() // we assume here that we increment by 2 for the reconstruction step

    progressTrackers.done()

    tidyProofs.tidy(proof)
  }

  var progressMax: Int = 0;

  private def setMaxToProgressTrackers(axioms: Set[OWLAxiom]) = {
    val signature = axioms
      .flatMap(_.getSignature.asScala)
      .filter(c =>
        !c.isBottomEntity &&
          !c.isTopEntity &&
          !(c.isInstanceOf[OWLClass] && targetSignature(c.asOWLClass())))

    progressMax = signature.size + 2
    progressTrackers.setMax(progressMax) // twice: once for forgetting, once for reconstructing
    maxSet=true
  }


  private def updateProgressTrackers(): Unit = {
    var nextOntology::_ = ontologySteps
    val signature = nextOntology
      .filter(!_.isInstanceOf[OWLDeclarationAxiom])
      .flatMap(_.getSignature.asScala)
      .filter(c =>
        !c.isBottomEntity &&
          !c.isTopEntity &&
          !(c.isInstanceOf[OWLClass] && targetSignature(c.asOWLClass())))

    logger.trace(s"Axioms: ${nextOntology}")
    logger.trace(s"Number of steps: ${ontologySteps.size}")
    logger.trace(s"Signature left: ${signature}")

    progressTrackers.setProgress(progressMax - signature.size-2)

  }

  protected def computeOntologySteps(): Unit = {
    ontologySteps = List(ontology.getLogicalAxioms(Imports.INCLUDED).iterator.asScala.toSet)
   // ontologySteps = List(unifySignature(ontology.getLogicalAxioms(Imports.INCLUDED).iterator.asScala.toSet,Set(toProve)))
    signatureSteps = List(null)


    doneForgettingAndJustifying = false

    maxSet=false;

    // find symbols that occur in axioms together with hiddenSignature symbols
    /*adjacentSignature = ontology
      .getLogicalAxioms(Imports.INCLUDED)
      .iterator()
      .asScala
      .toSet[OWLAxiom]
      .filter(
        _
          .getSignature()
          .asScala
          .toSet[OWLEntity]
          .exists(hiddenSignature.contains)
      )
      .flatMap(_.getSignature.asScala)
    adjacentSignature --= hiddenSignature
    */
    while(!doneForgettingAndJustifying && !canceled) {
      forgetAndJustify()
      updateProgressTrackers()
    }

    logger.debug(s"${if(canceled) "stopped execution" else "done with forgetting and justifying"}")
  }

  private def reconstructProofFor(axiom: OWLAxiom,
                                  prefix: Seq[Set[OWLAxiom]],
                                  prefixSig: Seq[OWLEntity],
                                  forgotSoFar: List[OWLEntity]): Unit = {
    logger.trace(s"prefix: ${prefixSig}")
    logger.trace(s"forgotSoFar: ${forgotSoFar}")
    if(!proof.hasInferenceFor(axiom)) prefix match {
      case Nil =>
        logger.trace(s"nothing left to prove for ${axiom}")
        val inference = new Inference(axiom, "asserted", List().asJava)
        proof.addInference(inference)
        logger.trace(s"new inference: ${inference}")
        // proof.setLeaf(axiom)
        // proof.setPremisses(axiom, Set())

      case prevOnt::rest if prevOnt.contains(axiom) ||
        justifier.justify(prevOnt, axiom) // make sure we don't justify with definers!
          .exists(_.getSignature
            .asScala
            .exists(!originalSignature(_))) =>
        logger.trace(s"${formatter.format(axiom)} occurs in ${prevOnt.map(formatter.format)}")
        logger.trace(s" or contains a definer: ${justifier.justify(prevOnt,axiom).filter(_.getSignature.asScala.exists(!originalSignature(_)))}")
        logger.trace("need to go one ontology further back")
        reconstructProofFor(axiom, rest, prefixSig.tail, List(prefixSig.head))

      case prevOnt:: rest if canceled && prevOnt.isEmpty =>
        logger.trace(s"computation was canceled here - go one step further back")
        reconstructProofFor(axiom, rest, prefixSig.tail, List(prefixSig.head))

      case prevOnt::rest =>
        //println("justifying "+SimpleOWLFormatter.format(axiom)+" in "+prevOnt.map(SimpleOWLFormatter.format))

        // TODO: try multiple justifications and choose the one which contains more of the hidden signature?
        var premises = justifier.justify(prevOnt, axiom).filter(notAddedTautology)

        if(premises.isEmpty){
          println("Something broke! empty justification")
          println(s"axiom: ${axiom}")
          println(s"axiom set: ${prevOnt}")
          println()
          assert(false)
          System.exit(1)
        }

        if (skipSteps) {
          // check whether we should skip
          rest match {
            case Nil => ;
            case prevPrevOnt::rest2 =>
              // TODO: try multiple justifications?
              val premises2 = justifier.justify(prevPrevOnt, axiom).filter(notAddedTautology)
              //if (OntologyTools.size(premises2) <= OntologyTools.size(premises)) {
              if (premises2.size <= premises.size) {
                  logger.trace(s"premise set\n${premises2.map(formatter.format)}\nis smaller than or equal to\n${premises.map(formatter.format)}")
                //println("("+OntologyTools.size(premises2)+" <= "+OntologyTools.size(premises)+")")
                reconstructProofFor(axiom, rest, prefixSig.tail, prefixSig.head::forgotSoFar)
                return
              }
          }
        }

        // no (more) skipping
        val ruleName =
          forgotSoFar match {
            case List(null) => "direct"
            case other =>
              //assert(!other.contains(null))
              Constants.RULE_NAME_PREFIX+forgotSoFar.filterNot(_==null).map(formatter.format(_)).mkString(", ")
          }

        val inference = new Inference(
          axiom,
          ruleName,
          premises.toList.asJava)

        logger.trace(s"new inference: ${inference}")

        proof.addInference(inference)

        // proof.setPremisses(axiom, premises)
        premises.foreach(reconstructProofFor(_,rest, prefixSig.tail, List(prefixSig.head)))
    }
  }

  private def notAddedTautology(axiom: OWLAxiom) = {
    axiom match {
      case sa: OWLSubClassOfAxiom =>
        !(sa.getSubClass().isInstanceOf[OWLClass] && sa.getSuperClass().isTopEntity)
      case _ => true
    }
  }

  protected def forgetAndJustify(): Unit ={

    var nextOntology::_ = ontologySteps

    logger.trace(s"current ontology: \n${nextOntology.map(formatter.format).mkString("\n")}\n")
    
    // TODO: try multiple justifications, choose the one that contains more of the hidden signature?
    val just = justifier.justify(nextOntology,toProve)

    if(!maxSet)
      setMaxToProgressTrackers(just)

    if(just.isEmpty)
      throw new ProofGenerationFailedException("Something got lost during forgetting!")

    //assert(just.size>0, "something got lost while forgetting")

    logger.trace(s"Justification: \n ${just.map(formatter.format).mkString("\n")}\n")

    selectNextSymbol(just) match {
      case Some(nextSymbol) => {

        progressTrackers.setMessage(Constants.RULE_NAME_PREFIX+formatter.format(nextSymbol))
        logger.trace(s"next symbol is ${nextSymbol}")
        logger.trace("forgetting ..")
        var forgettingResult = forgetter.forget(just, nextSymbol)

        var tryNext = nextSymbol
        var triedSymbols = Set[OWLEntity](tryNext)
        var allFailed = false

        while(!allFailed && (
          forgettingResult.exists(_.containsEntityInSignature(tryNext)) ||
            !justifier.entailed(forgettingResult, toProve))) {
          logger.trace(s"Failed forgetting ${tryNext}")
          logger.trace(s"${if(!justifier.entailed(forgettingResult, toProve)) "something got lost while forgetting!" else "symbol remained"}")
          selectNextSymbol(just, ignore = triedSymbols) match {
            case Some(next) =>
              tryNext = next
              triedSymbols += next
              logger.trace(s"next symbol is ${tryNext}")
              logger.trace("forgetting ..")
              forgettingResult = forgetter.forget(just, tryNext)
            case None =>
              allFailed = true
          }
        }

        if(!allFailed) {
          // the following is needed to make sure justifications work even if outside
          // signature
         // forgettingResult = unifySignature(forgettingResult, nextOntology)

          logger.trace("done")
          ontologySteps = forgettingResult :: ontologySteps
          signatureSteps = tryNext :: signatureSteps
        }
        else
          doneForgettingAndJustifying = true
      }
      case None => doneForgettingAndJustifying = true
    }
  }

  private def unifySignature(axioms: Set[OWLAxiom], other: Set[OWLAxiom]) = {
    var result = axioms
    val sig = axioms.flatMap(_.getSignature().iterator.asScala)
    result ++= other.flatMap(_.getSignature().iterator.asScala)
      .filterNot(x => sig(x) || x.isInstanceOf[OWLDatatype])
      .map{ _ match {
        case cl: OWLClass => owlDataFactory.getOWLSubClassOfAxiom(cl, owlDataFactory.getOWLThing)
        case pr: OWLObjectProperty => owlDataFactory.getOWLSubClassOfAxiom(owlDataFactory.getOWLThing,
          owlDataFactory.getOWLObjectAllValuesFrom(pr, owlDataFactory.getOWLThing))
        case pr: OWLDataProperty => owlDataFactory.getOWLSubClassOfAxiom(owlDataFactory.getOWLThing,
          owlDataFactory.getOWLDataAllValuesFrom(pr, owlDataFactory.getTopDatatype))
        case in: OWLNamedIndividual => owlDataFactory.getOWLClassAssertionAxiom(owlDataFactory.getOWLThing, in)
      }}


    result
  }

  def selectNextSymbol(axiomSet: Set[OWLAxiom], ignore: Set[OWLEntity] = Set()): Option[OWLEntity] = {
    val conceptNames = new Counter[OWLEntity]()
    val roleNames = new Counter[OWLEntity]()

    val nestedConceptNames = new Counter[OWLEntity]()
    val nestedRoleNames = new Counter[OWLObjectProperty]()
    var rolesWithFillers = Set[OWLObjectProperty]()

    val adjacentConceptNames = new Counter[OWLEntity]()
    val adjacentRoleNames = new Counter[OWLEntity]()

    val hiddenConceptNames = new Counter[OWLEntity]()
    val hiddenRoleNames = new Counter[OWLEntity]()

    axiomSet.foreach(_.getNestedClassExpressions().forEach{
      classExp =>

        classExp.getClassesInSignature().forEach(x =>
          if(originalSignature(x)) // we only select names in the original signature - other names would be introduced definers
                                   // and forgetting those would lead to an infinite loop
            /*if (adjacentSignature(x))
              adjacentConceptNames.add(x)
            else if (hiddenSignature(x))
              hiddenConceptNames.add(x)
            else*/
              conceptNames.add(x)
        )

        classExp.getObjectPropertiesInSignature().forEach(x =>
          if(originalSignature(x))
            /*if (adjacentSignature(x))
              adjacentRoleNames.add(x)
            else if (hiddenSignature(x))
              hiddenRoleNames.add(x)
            else*/
              roleNames.add(x)
        )

        classExp match {
          case e: OWLQuantifiedObjectRestriction =>
            if(!(e.getFiller.isOWLThing || e.getFiller.isOWLNothing))
              rolesWithFillers += e.getProperty().getObjectPropertiesInSignature().iterator().next()
            e.getFiller.getClassesInSignature().forEach(nestedConceptNames.add(_))
            e.getFiller.getObjectPropertiesInSignature().forEach(nestedRoleNames.add(_))
          case other => ; // do nothing
        }

    })


    selectNextSymbol(
      adjacentConceptNames,
      adjacentRoleNames,
      nestedConceptNames,
      nestedRoleNames,
      rolesWithFillers,
      ignore
    ) match {
      case Some(nextSymbol) => 
        Some(nextSymbol)
      case None =>
        selectNextSymbol(
          hiddenConceptNames,
          hiddenRoleNames,
          nestedConceptNames,
          nestedRoleNames,
          rolesWithFillers,
          ignore) match {
          case Some(nextSymbol) =>
            Some(nextSymbol)
          case None =>
            selectNextSymbol(conceptNames, roleNames, nestedConceptNames, nestedRoleNames, rolesWithFillers, ignore)
        }
    }

  }


  def selectNextSymbol(conceptNames: Counter[OWLEntity],
                       roleNames: Counter[OWLEntity],
                       nestedConceptNames: Counter[OWLEntity],
                       nestedRoleNames: Counter[OWLObjectProperty],
                       rolesWithFillers: Set[OWLObjectProperty],
                       ignore: Set[OWLEntity]): Option[OWLEntity] = {

    conceptNames.removeKeys(targetSignature)
    conceptNames.removeKey(owlDataFactory.getOWLThing)
    conceptNames.removeKey(owlDataFactory.getOWLNothing)

    conceptNames.removeKeys(ignore)
    roleNames.removeKeys(ignore)

    nestedConceptNames.removeKey(owlDataFactory.getOWLThing)
    nestedConceptNames.removeKey(owlDataFactory.getOWLNothing)
    nestedConceptNames.removeKeys(targetSignature)
    nestedConceptNames.removeKeys(ignore)

    //if((conceptNames.keys.size+roleNames.keys.size) <= targetSignature.size+1)
    //  None
    //else

    val rolesWithoutFillers = roleNames.filter(r => !rolesWithFillers(r.asInstanceOf[OWLObjectProperty]))

    val unnestedConcepts = conceptNames.filter(!nestedConceptNames.keys.toSet(_))
    val unnestedRoles = roleNames.filter(!nestedRoleNames.keys.toSet(_))

    if(!rolesWithoutFillers.empty)
      Some(rolesWithoutFillers.max)
    else if(!unnestedConcepts.empty)
      Some(unnestedConcepts.max)
    else if(!conceptNames.empty)
      Some(conceptNames.max)
 //     else if (!unnestedRoles.empty)
 //     Some(unnestedRoles.max)
    else if(!roleNames.empty)
      Some(roleNames.max)
    else
      None
  }

  private def equivalence(lhs: OWLClass, rhs: OWLClass): OWLAxiom = {
    owlDataFactory.getOWLEquivalentClassesAxiom(lhs,rhs)
  }

  private def subClassOf(lhs: OWLClass, rhs: OWLClass): OWLAxiom = {
    owlDataFactory.getOWLSubClassOfAxiom(lhs,rhs)
  }

  private def unsatisfiable(concept: OWLClass): OWLAxiom =
    owlDataFactory.getOWLSubClassOfAxiom(concept, owlDataFactory.getOWLNothing)

  override def successful(): Boolean = {
    return !canceled;
  }
}
