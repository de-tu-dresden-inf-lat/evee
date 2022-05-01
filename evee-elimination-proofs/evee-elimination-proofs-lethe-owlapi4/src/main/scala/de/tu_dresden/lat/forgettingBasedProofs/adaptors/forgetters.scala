package de.tu_dresden.inf.lat.forgettingBasedProofs.adaptors

import java.util.Collections
import de.tu_dresden.inf.lat.forgettingBasedProofs.dataStructures.{Forgetter, Justifier}
import de.tu_dresden.inf.lat.prettyPrinting.formatting.SimpleOWLFormatter
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model.{IRI, OWLAxiom, OWLClass, OWLEntity, OWLObjectProperty, OWLOntology, OWLOntologyManager, OWLSubObjectPropertyOfAxiom}
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory
import uk.ac.man.cs.lethe.forgetting.{AlchTBoxForgetter, IOWLForgetter, ShKnowledgeBaseForgetter, ShqTBoxForgetter}

import scala.collection.JavaConverters._

object LetheBasedForgetter {

  def ALCH(timeout: Long = -1) = {
    val internal = new AlchTBoxForgetter()
    if(timeout>0) {
      internal.useTimeout(timeout)
    }
    new LetheBasedForgetter(internal)
  }

  def ALC_ABox(timeout: Long = -1) = {
    val internal = new ShKnowledgeBaseForgetter()
    if(timeout>0) {
      internal.useTimeout(timeout)
    }
    new LetheBasedForgetter(internal)
  }

  def SHQ = new LetheBasedForgetter(new ShqTBoxForgetter())


}

class LetheBasedForgetter(internalForgetter: IOWLForgetter) extends Forgetter {

  val ontologyManager = OWLManager.createOWLOntologyManager

  override def forget(axioms: Iterable[OWLAxiom], name: OWLEntity): Set[OWLAxiom] = {

    val axiomSet = axioms.toSet

    val ontology = ontologyManager.createOntology(axiomSet.asJava)

    internalForgetter.forget(ontology, Collections.singleton(name)).getAxioms().asScala.toSet
  }
}
