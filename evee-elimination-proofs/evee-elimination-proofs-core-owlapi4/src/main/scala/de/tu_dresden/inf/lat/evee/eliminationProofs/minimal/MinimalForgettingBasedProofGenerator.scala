package de.tu_dresden.inf.lat.evee.eliminationProofs.minimal

import com.typesafe.scalalogging.Logger
import de.tu_dresden.inf.lat.dltools.DLFilter
import de.tu_dresden.inf.lat.evee.eliminationProofs.dataStructures.{Forgetter, Justifier}
import de.tu_dresden.inf.lat.evee.eliminationProofs.tools.{SearchTreeProgressTracker, TidyForgettingBasedProofs}
import de.tu_dresden.inf.lat.evee.eliminationProofs.{Constants, ForgettingBasedProofGenerator}
import de.tu_dresden.inf.lat.prettyPrinting.formatting.{SimpleOWLFormatter, SimpleOWLFormatterCl}
import de.tu_dresden.inf.lat.evee.proofs.data.Inference
import de.tu_dresden.inf.lat.evee.proofs.data.exceptions.ProofGenerationCancelledException
import de.tu_dresden.inf.lat.evee.proofs.interfaces._
import de.tu_dresden.inf.lat.evee.proofs.tools.ProgressTrackerCollection
import org.semanticweb.HermiT
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model._
import org.semanticweb.owlapi.model.parameters.Imports

import java.util
import scala.collection.JavaConverters.{asScalaSetConverter, iterableAsScalaIterableConverter, seqAsJavaListConverter, setAsJavaSetConverter}
import scala.collection.{JavaConverters, mutable}


class MinimalSignatureAndForgettingBasedProofGenerator(_measure: IProofEvaluator[OWLAxiom],
                                                       approximateMeasure: ApproximateProofMeasure,
                                                       forgetter: Forgetter,
                                                       filter: DLFilter,
                                                       justifier: Justifier,
                                                       skipSteps: Boolean = true)
  extends MinimalForgettingBasedProofGenerator(
    new SignatureOptimisedProofEvaluator(_measure, Set()),
    approximateMeasure,
    forgetter,
    filter,
    justifier,
    skipSteps)
    with ISimpleSignatureBasedProofGenerator[OWLEntity, OWLClass, OWLAxiom, OWLOntology] {

  override def setSignature(javaSignature: util.Collection[OWLEntity]): Unit = {
    val signature = JavaConverters.collectionAsScalaIterable(javaSignature).toSet
    measure = new SignatureOptimisedProofEvaluator(_measure, signature)
    //approximateMeasure.knownSignature=signature
  }
}

class MinimalForgettingBasedProofGenerator(var measure: IProofEvaluator[OWLAxiom],
                                           approximateMeasure: ApproximateProofMeasure,
                                           forgetter: Forgetter,
                                           filter: DLFilter,
                                           justifier: Justifier,
                                           var skipSteps: Boolean = true)
  extends IProofGenerator[OWLAxiom, OWLOntology] with ISimpleProofGenerator[OWLClass, OWLAxiom, OWLOntology] {

  val logger = Logger[MinimalForgettingBasedProofGenerator]

  val manager = OWLManager.createOWLOntologyManager()

  val heuristicProver = new ForgettingBasedProofGenerator(forgetter, filter, justifier)
  val progressTrackers = new ProgressTrackerCollection()
  val forgettingCache = new mutable.HashMap[Set[OWLEntity], Set[OWLAxiom]]()
  var targetSignature: Set[OWLEntity] = _
  var targetAxiom: OWLAxiom = _
  var ontology: OWLOntology = _
  var factory: OWLDataFactory = _
  var searchTreeProgressTracker: SearchTreeProgressTracker = _
  var bestProofs: Map[Set[OWLAxiom], ExtendableProof[OWLAxiom]] = Map()
  var searchDepthSet = false

  val formatter = new SimpleOWLFormatterCl()
  val tidyProofs = new TidyForgettingBasedProofs(formatter)

  //override def setReasoner(owlReasoner: OWLReasoner): Unit = {}

  private var canceled = false

  def setSkipSteps(skipSteps: Boolean) =
    this.skipSteps=skipSteps

  override def cancel(): Unit = {
    println("Canceling!")
    canceled = true
  }

  override def addProgressTracker(tracker: IProgressTracker): Unit =
    progressTrackers.addProgressTracker(tracker)

  val knownSupport = new mutable.HashMap[OWLAxiom,Boolean]()

  override def supportsProof(axiom: OWLAxiom): Boolean = {
    knownSupport.getOrElseUpdate(axiom, {
      val reasoner = new HermiT.Reasoner.ReasonerFactory().createReasoner(ontology)

      reasoner.isEntailed(axiom)
    })
  }

  override def setOntology(owlOntology: OWLOntology): Unit = {
    ontology = filter.filteredCopy(owlOntology, manager)

    println("Ontology changed!")

    factory = manager.getOWLDataFactory

    formatter.setReferenceOntology(owlOntology)

    knownSupport.clear()
  }

  override def getProof(axiom: OWLAxiom): IProof[OWLAxiom] = {
    targetSignature = axiom.getSignature().asScala.toSet
    targetAxiom = axiom
    prove()
  }

  override def proveSubsumption(owlClass: OWLClass, owlClass1: OWLClass): IProof[OWLAxiom] = {
    targetSignature = Set(owlClass, owlClass1)
    targetAxiom = factory.getOWLSubClassOfAxiom(owlClass, owlClass1)

    prove()
  }

  override def proveEquivalence(owlClass: OWLClass, owlClass1: OWLClass): IProof[OWLAxiom] = {
    targetSignature = Set(owlClass, owlClass1)
    targetAxiom = factory.getOWLEquivalentClassesAxiom(owlClass, owlClass1)

    prove()
  }

  def initSearchTreeProgressTracker(signature: Set[OWLEntity]) = {
    searchTreeProgressTracker = new SearchTreeProgressTracker(progressTrackers, signature.size, 2)
  }

  /**
   * replace the premises of the given proof with inferences to these using the given set of axioms.
   */
  def extendProof(_proof: ExtendableProof[OWLAxiom], axioms: Set[OWLAxiom], name: OWLEntity)
  : ExtendableProof[OWLAxiom] = {

    var proof = _proof

    if(skipSteps) {
      var remove = List[IInference[OWLAxiom]]()
      var add = List[IInference[OWLAxiom]]()
      _proof.getPremises().foreach { premise =>
          _proof.inferencesWithPremise(premise).foreach(inference => {
            val altPremises = justifier.justify(axioms, inference.getConclusion)
            val forgotten = formatter.format(name)
            val ruleName =
              if(inference.getRuleName.contains(forgotten))
                inference.getRuleName
              else
                Constants.RULE_NAME_PREFIX + formatter.format(name) +", " +
                inference.getRuleName.replaceAll(Constants.RULE_NAME_PREFIX,"")

            val alternative = new Inference(
              inference.getConclusion,
              ruleName,
              altPremises.toList.asJava)
            if (inference.getPremises.size() >= altPremises.size) {
              remove ::= inference
              add ::= alternative
              logger.debug("Replace "+inference+ " with "+alternative)
            }
          })
      }
      val newInferences = (_proof.getInferences.asScala.toSet -- remove) ++ add
      proof = new ExtendableProof[OWLAxiom](_proof.getFinalConclusion)
      proof.addPremises(_proof.getPremises())
      proof.addInferences(newInferences.asJava)
    }

    if(!skipSteps)
      proof = _proof.copy


    proof.getPremises().foreach { premise =>
        val just = justifier.justify(axioms, premise)
        if (!just.contains(premise)) {
          proof.addInference(new Inference(premise, Constants.RULE_NAME_PREFIX + formatter.format(name),
            JavaConverters.seqAsJavaList(just.toSeq)))
          proof.addPremises(just)
        }
      }

    return proof
  }

  def nextSteps(axioms: Iterable[OWLAxiom], signature: Set[OWLEntity], eliminated: Set[OWLEntity])
  : Iterable[(Set[OWLAxiom], OWLEntity)] = {
    signature
      .map { name =>
        //val signature = eliminated + name
        //val restSignature = axioms.flatMap(a => JavaConverters.asScalaSet(a.getClassesInSignature())).toSet -- signature
        val nextAxioms = //{
        //forgettingCache.getOrElseUpdate(
        //  restSignature,
          forgetter.forget(axioms, name)
        //)
        //  }
        //nextAxioms = justifier.justify(nextAxioms, toProve)
        if(!justifier.entailed(nextAxioms,targetAxiom)) {
          logger.warn(s"something got lost while forgetting ${name}")
          (axioms.toSet,name)
        } else
          (nextAxioms, name)
      }
  }

  def finished(axioms: Set[OWLAxiom]) =
    axioms.forall(x => JavaConverters.asScalaSet(x.getClassesInSignature)
      .forall(inTarget))

  def inTarget(c: OWLEntity) =
    c.isTopEntity || c.isBottomEntity || targetSignature(c)

  def approximateStepCost(axioms: Iterable[OWLAxiom]) =
    approximateMeasure.evaluate(axioms)

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

  private def prove(): IProof[OWLAxiom] = {

    canceled = false
    bestProofs = Map()

    searchDepthSet = false
    progressTrackers.setProgress(0)

    val optProof = generateProof()

    if(optProof.isEmpty && canceled)
      throw new ProofGenerationCancelledException("Proof generation cancelled");

    val proof = optProof.get

    proof.assertAllPremises()
    //println("Now we assert all premises:")
    //println(proof)
    tidyProofs.tidy(proof)
  }
  //axioms.flatMap(x => JavaConverters.asScalaSet(x.getClassesInSignature)).filterNot(inTarget).size

  private def generateProof(axioms: Set[OWLAxiom] =
                            JavaConverters.asScalaSet(
                              ontology.getLogicalAxioms(Imports.INCLUDED)).toSet,
                            eliminated: Set[OWLEntity] = Set(),
                            bound: Option[Double] = None)
  : Option[ExtendableProof[OWLAxiom]] = {


    if (!eliminated.isEmpty)
      progressTrackers.setMessage("trying " + eliminated.map(formatter.format(_)).mkString(", "))


    //println("\n\n Entering with bound: "+bound)
    //println(" Forgotten: "+eliminated.map(SimpleOWLFormatter.format))

    if (bound.exists(_ < 1))
      return None

    val justification = justifier.justify(axioms, targetAxiom)


    val signature = justification.flatMap(x => JavaConverters.asScalaSet(x.getSignature))
      .filterNot(inTarget)
      .filter(acceptable(_, justification))
      .filterNot(eliminated)

    if (!searchDepthSet) {
      initSearchTreeProgressTracker(justification.flatMap(x => JavaConverters.asScalaSet(x.getSignature)).filterNot(inTarget))
      searchDepthSet = true
    }

    //println("Our current axioms are:" )
    //println(justification.map(SimpleOWLFormatter.format))
    //println(justification.map(SimpleOWLFormatter.format).mkString("\n"))

    //println("****************************************************************")
    //println("Our current table of best proofs (1): ")
    //bestProofs.keys.foreach(key => {
    //  println(" for " + key.map(SimpleOWLFormatter.format))
    //  println("We got: " + bestProofs(key))
    //})
    //println("****************************************************************")
    if (bestProofs.contains(justification)) {
      //   println("I have already found a proof from this!")
      /*    println("It is: ")
          println(bestProofs(justification))
          println("--------------------- that was the one!")*/
      if (!bound.exists(_ < measure.evaluate(bestProofs(justification))))
        return Some(bestProofs(justification))
      else {
        //   println("But it's bad!")
        return None
      }
    }

    val minStepCost = approximateMeasure.lowerApproximation(justification)

    if (bound.exists(_ < minStepCost)) {
      //println("lower approximation says we can't make it anymore")
      //println("("+minStepCost+")")
      return None
    }

    // whatever proof we get can't be better than the one step
    //val oneStep = new Inference(targetAxiom, "conclusion",
    //  JavaConverters.seqAsJavaList(justification.toSeq))
    val oneStep = new ExtendableProof[OWLAxiom](targetAxiom)
    oneStep.addPremises(justification)

    val stepCost = measure.evaluate(oneStep)

    //println("Cost of this step: "+stepCost)
    //println("Cost of this step: at least "+minStepCost)

    //if(bound.exists(_<=stepCost))
    //  return None

    //System.out.println("We continue\n")

    if (signature.isEmpty) { // we then done proving
      // only roles left - continue with heuristic prover
      //val proof =
      //  if(justification.exists(!_.getObjectPropertiesInSignature.isEmpty)) {
      val ont = manager.createOntology(justification.asJava)

      heuristicProver.setOntology(ont)

      val restProof = heuristicProver.getProof(targetAxiom)

      val proof = new ExtendableProof[OWLAxiom](targetAxiom)
      restProof.getInferences.forEach(inf =>
        if (!inf.getRuleName.equals("asserted"))
          proof.addInference(inf)
      )
      proof.addPremises(justification)


      /*  proof
      } else {

        val proof = new ExtendableProof(targetAxiom)

        if (!justification.contains(targetAxiom))
          proof.addInference(
            new Inference(targetAxiom, "conclusion",
              JavaConverters.seqAsJavaList(justification.toSeq)))

        proof.addPremises(justification)
        proof
      }*/

      //println(" and we already found a proof:")

      //println(proof)

      return Some(proof)

    } else {

      var previousProgress = progressTrackers.getProgress()
      progressTrackers.increment()

      // we need to search for the next best proof

      var bestProof: Option[ExtendableProof[OWLAxiom]] = None
      var bestBound = bound //.map(_-minStepCost)//stepCost)
      var bestName: Option[OWLEntity] = None

      //println("Initial bound for next step: "+bestBound)

      nextSteps(justification, signature, eliminated)
        .toSeq
        .sortBy(x => approximateStepCost(x._1))
        .foreach { pair =>
          logger.trace(s" Forgotten: ${eliminated.map(SimpleOWLFormatter.format)}")
          logger.trace(s"forget ${pair._2}")
          if (!canceled) {
            val stepCost = justification.filter(!pair._1(_)).size // need to at least add those to the resulting proof
            generateProof(pair._1, eliminated + pair._2, bestBound.map(_ - stepCost)) match {
              case None => ;
              case Some(_proof) =>
                //println("Before copying:")
                //println(_proof)
                val proof = extendProof(_proof, justification, pair._2)

                //println("news on on level "+eliminated.map(SimpleOWLFormatter.format))
                //println("old bound was: "+bestBound)
                //println("found a better proof via "+SimpleOWLFormatter.format(pair._2)+": ")
                //println(proof)
                //println("the size is: "+measure.evaluate(proof))
                val value = measure.evaluate(proof)
                if(bestBound.isEmpty || bestBound.exists(_>value)) {
                  bestProof = Some(proof)
                  bestBound = Some(measure.evaluate(proof))
                  //println("next bound: "+bestBound)
                  bestName = Some(pair._2)
                }
            }
          }

          previousProgress = searchTreeProgressTracker.increment(previousProgress, eliminated.size + 1)

        }

      //println("news on on level "+eliminated.map(SimpleOWLFormatter.format))
      //println("Search is done at this point")

      /*bestProof.foreach(proof => proof.getPremises().foreach{ premise =>
        val just = justifier.justify(axioms, premise)
        if(!just.contains(premise)) {
          proof.addInference(new Inference(premise, "forget "+SimpleOWLFormatter.format(bestConcept.get),
            JavaConverters.seqAsJavaList(just.toSeq)))
          proof.addPremises(just)
        }
        // TODO: here is where we would skip!
      })*/

      //bestProof.foreach(p => extendProof(p,justification, bestConcept.get))

      //System.out.println("From the justification:")
      //println(justification.map(SimpleOWLFormatter.format))
      //System.out.println("This is our best proof:" )
      //println(bestProof)
      if (bestProof.isDefined) {
        assert(bestProof.get.getPremises().forall(justification))
      }

      bestProof.foreach(p => bestProofs = bestProofs + ((justification, p.copy)))

      //println("================================================================")
      //println("Our current table of best proofs (2): ")
      //bestProofs.keys.foreach(key => {
      //  println(" for " + key.map(SimpleOWLFormatter.format))
      //  println("We got: " + bestProofs(key))
      //})
      //println("================================================================")


      bestProof
    }
  }

  override def successful(): Boolean = {
    return !canceled;
  }
}
