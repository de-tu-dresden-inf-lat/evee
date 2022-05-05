package de.tu_dresden.inf.lat.evee.forgettingBasedProofs.adaptors

import forgetting.Fame

import java.util.Collections
import de.tu_dresden.inf.lat.evee.forgettingBasedProofs.dataStructures.{Forgetter, Justifier}
import de.tu_dresden.inf.lat.prettyPrinting.formatting.SimpleOWLFormatter
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model._

import scala.collection.JavaConverters._

object FamePlusBasedForgetter extends Forgetter {
  val forgetter = new Fame()

  val ontologyManager = OWLManager.createOWLOntologyManager

  override def forget(axioms: Iterable[OWLAxiom], name: OWLEntity): Set[OWLAxiom] = {

    val ontology = ontologyManager.createOntology(axioms.toSet.asJava)

    val resultOnt: OWLOntology = name match {
      case cl: OWLClass =>
        forgetter.FameRC(Set().asJava, Set(cl).asJava, ontology)
      case prp: OWLObjectProperty =>
        forgetter.FameRC(Set(prp).asJava, Set().asJava, ontology)
      case _ => throw new UnsupportedOperationException()
    }

    //if (resultOnt.getSignature().contains(name))
    //  return null

    resultOnt.getAxioms().asScala.toSet
  }
}

