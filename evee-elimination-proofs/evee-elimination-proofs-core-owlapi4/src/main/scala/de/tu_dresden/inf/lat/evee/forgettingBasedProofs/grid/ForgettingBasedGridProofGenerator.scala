package de.tu_dresden.inf.lat.evee.forgettingBasedProofs.grid

import de.tu_dresden.inf.lat.evee.forgettingBasedProofs.ForgettingBasedProofGenerator
import de.tu_dresden.inf.lat.evee.forgettingBasedProofs.dataStructures.{Forgetter, Justifier}
import de.tu_dresden.inf.lat.prettyPrinting.formatting.SimpleOWLFormatter
import de.tu_dresden.inf.lat.evee.proofs.data.{AbstractSimpleOWLProofGenerator, Inference, Proof}
import de.tu_dresden.inf.lat.evee.proofs.interfaces.{IInference, IProof, IProofGenerator}
import de.tu_dresden.inf.lat.evee.proofs.tools.MinimalHypergraphProofExtractor
import org.semanticweb.owlapi.model.parameters.Imports
import org.semanticweb.owlapi.model.{OWLAxiom, OWLClass, OWLOntology}
import org.semanticweb.owlapi.reasoner.OWLReasoner

import scala.collection.{JavaConverters, mutable}

/**
 * Tries to generate a kind of "derivation structure", rather than just a single proof, from which then
 * minimal proofs may be extracted. For this, we produce sequences of ontologies where in each step, we
 * take the union of the results of forgetting each name, and repeating until we get no new inferences
 */
class ForgettingBasedGridProofGenerator(forgetter: Forgetter, justifier: Justifier)
  extends AbstractSimpleOWLProofGenerator {

  var ontology: OWLOntology =_

  override def setOntology(owlOntology: OWLOntology): Unit = this.ontology = owlOntology

  override def supportsProof(sentence: OWLAxiom): Boolean = true

  var foundInferences: mutable.Set[IInference[OWLAxiom]] = _
  var foundAxioms: mutable.Set[OWLAxiom]= _
  var foundProof: Boolean= _
  var currentAxioms: Set[OWLAxiom] = _
  var toProve: OWLAxiom = _
  var goalSignature: Set[OWLClass] = _
  var knownJustifications: mutable.Set[Set[OWLAxiom]] = _

  override def getProof(toProve: OWLAxiom): IProof[OWLAxiom] = {

    foundInferences = mutable.HashSet()
    foundAxioms = mutable.HashSet()
    foundProof = false
    currentAxioms = JavaConverters.asScalaSet(ontology.getAxioms(Imports.INCLUDED)).toSet
    knownJustifications = mutable.HashSet()
    //currentAxioms = justifier.justify(currentAxioms,toProve)

    currentAxioms.foreach(foundInferences +=
      new Inference[OWLAxiom](_, "asserted", JavaConverters.seqAsJavaList(Seq())))


    this.toProve = toProve
    goalSignature = JavaConverters.asScalaSet(toProve.getClassesInSignature).toSet


    while(!(foundProof && currentAxioms.forall(foundAxioms))){
      println("Hu!")
      currentAxioms = nextStep()
      foundAxioms ++= currentAxioms
    }

    val result = new Proof(toProve)
    result.addInferences(JavaConverters.asJavaCollection(foundInferences))

    MinimalHypergraphProofExtractor.makeUnique(result)
    //
    //result
  }


  private def nextStep(): Set[OWLAxiom] = {
    println("Current Axioms: ")
    println("=================")
    var result =  Set[OWLAxiom]()
    currentAxioms.foreach(ax => println(SimpleOWLFormatter.format(ax)))

    justifier.justifyAll(currentAxioms,toProve)
      .filterNot(knownJustifications(_))
      .foreach { axiomSet =>

        knownJustifications += axiomSet

        println("Current justification:")
        println(axiomSet.map(SimpleOWLFormatter.format).mkString("\n"))

      val inferrable = axiomSet
        .flatMap(x => JavaConverters.asScalaSet(x.getClassesInSignature()))
        .flatMap { name =>
          println(name+"..")
          forgetter.forget(axiomSet, name)
        }

      println("Inferrable: ")
      println(inferrable.map(SimpleOWLFormatter.format).mkString("\n"))

      val nextAxioms = inferrable// justifier.justify(inferrable, toProve)
      foundInferences ++= nextAxioms
        .filterNot(foundAxioms)
        .map { axiom =>

          val premises = justifier.justify(axiomSet, axiom)
          new Inference(axiom, "inferred", JavaConverters.seqAsJavaList(premises.toSeq))
        }

      checkFoundProof()

      result ++= nextAxioms
    }
    result
  }

  private def checkFoundProof() {
    val inSignature = currentAxioms
      .filter(_.getClassesInSignature.stream()
        .allMatch(goalSignature(_)))

    justifier.justifyIfPossible(inSignature, toProve) match {
      case Some(justification) =>
        foundProof = true
        foundInferences += new Inference[OWLAxiom](
          toProve, "", JavaConverters.seqAsJavaList(justification.toSeq))
      case None => ;
    }
  }

  override def setReasoner(owlReasoner: OWLReasoner): Unit = {}

  override def proveSubsumption(owlClass: OWLClass, owlClass1: OWLClass): IProof[OWLAxiom] = {
    getProof(ontology
      .getOWLOntologyManager()
      .getOWLDataFactory()
      .getOWLSubClassOfAxiom(owlClass,owlClass1));
  }

  override def proveEquivalence(owlClass: OWLClass, owlClass1: OWLClass): IProof[OWLAxiom] = {
    getProof(ontology
    .getOWLOntologyManager()
    .getOWLDataFactory()
    .getOWLEquivalentClassesAxiom(owlClass,owlClass1))
  }

  override def successful(): Boolean = {
    return true;
  }
}
