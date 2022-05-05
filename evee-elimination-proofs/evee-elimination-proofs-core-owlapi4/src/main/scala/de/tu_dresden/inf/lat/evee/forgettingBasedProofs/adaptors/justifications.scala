package de.tu_dresden.inf.lat.evee.forgettingBasedProofs.adaptors

import java.util.Collections
import org.semanticweb.HermiT
import com.clarkparsia.owlapi.explanation.DefaultExplanationGenerator
import com.clarkparsia.owlapi.explanation.util.{ExplanationProgressMonitor, SilentExplanationProgressMonitor}
import de.tu_dresden.inf.lat.evee.forgettingBasedProofs.dataStructures.{Forgetter, Justifier}
import de.tu_dresden.inf.lat.prettyPrinting.formatting.SimpleOWLFormatter
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model.{IRI, OWLAxiom, OWLClass, OWLEntity, OWLObjectProperty, OWLOntology, OWLOntologyManager, OWLSubObjectPropertyOfAxiom}
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory

import scala.collection.JavaConverters._


object OWLApiBasedJustifier {

  def UsingHermiT( manager: OWLOntologyManager)  =
    new OWLApiBasedJustifier(new HermiT.Reasoner.ReasonerFactory, manager)

}

/**
 * Extend the DefaultExplanationGenerator to also support role inclusions.
 */
class ExtendedDefaultExplanationGenerator(ontologyManager: OWLOntologyManager,
                                          reasonerFactory: OWLReasonerFactory,
                                          ontology: OWLOntology,
                                          progressMonitor: ExplanationProgressMonitor)
extends DefaultExplanationGenerator(ontologyManager, reasonerFactory, ontology, progressMonitor){

  val factory = ontologyManager.getOWLDataFactory

  val nominal = factory.getOWLNamedIndividual(IRI.create("ANYTHING"))

  // need to add an axiom to match signature with fresh individual name
  val addedAxiom = factory.getOWLClassAssertionAxiom(factory.getOWLThing, nominal)
  ontology.getOWLOntologyManager.addAxiom(ontology,addedAxiom);
  //ontology.addAxiom(addedAxiom)

  override def getExplanation(axiom: OWLAxiom) = axiom match {
    case ri: OWLSubObjectPropertyOfAxiom => {
      val concept = factory.getOWLObjectIntersectionOf(
        factory.getOWLObjectHasValue(ri.getSubProperty, nominal),
        factory.getOWLObjectComplementOf(
          factory.getOWLObjectHasValue(ri.getSuperProperty, nominal)
        )
      )
      var result = getExplanation(concept)
      result.remove(addedAxiom)
      result
    }
    case _ => super.getExplanation(axiom)
  }
}

class OWLApiBasedJustifier(reasonerFactory: OWLReasonerFactory,
                           ontologyManager: OWLOntologyManager) extends Justifier {

  val factory = ontologyManager.getOWLDataFactory

  override def entailed(axioms: Iterable[OWLAxiom], axiom: OWLAxiom): Boolean = {
    val reasoner = reasonerFactory.createReasoner(ontologyManager.createOntology(axioms.toSet.asJava))
    return reasoner.isEntailed(axiom)
  }

  override def justify(axioms: Iterable[OWLAxiom], axiom: OWLAxiom): Set[OWLAxiom] = {

    // TODO: it is still possible that axioms get into the justification only to
    // provide the signature. The real fix would be to adapt the axiom, not to add declaration axioms

    var axioms2 = Set[OWLAxiom]() ++ axioms;
    axiom.getSignature().forEach(s => axioms2 += factory.getOWLDeclarationAxiom(s))

    //println("Justify "+SimpleOWLFormatter.format(axiom))
    //println("In "+axioms2.map(SimpleOWLFormatter.format))

    val explanationGenerator =
      new ExtendedDefaultExplanationGenerator(
        ontologyManager,
        reasonerFactory,
        ontologyManager.createOntology(axioms2.toSet.asJava),
        new SilentExplanationProgressMonitor()
      )

    val explanation = explanationGenerator.getExplanation(axiom).asScala.toSet

    /*println("Justification for "+SimpleOWLFormatter.format(axiom))
    println(" in "+axioms.map(SimpleOWLFormatter.format))
    println(" is "+explanation.map(SimpleOWLFormatter.format))
    */
    explanation
  }


  override def justifyAll(axioms: Iterable[OWLAxiom], axiom: OWLAxiom): Set[Set[OWLAxiom]] = {
    val explanationGenerator =
      new ExtendedDefaultExplanationGenerator(
        ontologyManager,
        reasonerFactory,
        ontologyManager.createOntology(axioms.toSet.asJava),
        new SilentExplanationProgressMonitor()
      )

    explanationGenerator.getExplanations(axiom).asScala.toSet
      .map((x: java.util.Set[OWLAxiom]) => x.asScala.toSet)
  }

  override def toString() = {
    "OWLApiBasedJustifier("+reasonerFactory+")"
  }

  override def justifyIfPossible(axioms: Iterable[OWLAxiom], axiom: OWLAxiom)
  : Option[Set[OWLAxiom]] = {
    val ontology = ontologyManager.createOntology(axioms.toSet.asJava)
    val reasoner = reasonerFactory.createReasoner(ontology)

    if(reasoner.isEntailed(axiom))
      return Some(justify(axioms,axiom))
    else
      return None
  }
}
