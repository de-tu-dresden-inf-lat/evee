package de.tu_dresden.inf.lat.evee.eliminationProofs.dataStructures


import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model.{OWLAxiom, OWLClass, OWLEntity, OWLOntology}
//import uk.ac.man.cs.lethe.beautification.OWLAxiomBeautifier

import scala.collection.{JavaConverters, mutable}



/**
 * Abstraction for proof generator:
 */
/*trait ProofGenerator {

  def proveSubsumption(lhs: OWLClass, rhs: OWLClass): Proof

  def proveUnsatisfiability(concept: OWLClass): Proof

  def proveEquivalence(lhs: OWLClass, rhs: OWLClass): Proof
}

/**
 * Internal structure of proofs used.
 *
 * @param finalConclusion what is proven by the proof.
 */

class Proof(val finalConclusion: OWLAxiom) {

  private val leafs = new mutable.HashSet[OWLAxiom]()

  private val premiseMap = new mutable.HashMap[OWLAxiom, Iterable[OWLAxiom]]()

  def premisses(axiom: OWLAxiom): Iterable[OWLAxiom] =
    premiseMap(axiom)

  def hasNode(axiom: OWLAxiom) =
    premiseMap.contains(axiom)

  def setPremisses(axiom: OWLAxiom, premisses: Iterable[OWLAxiom]) = {

    println("premises for "+SimpleOWLFormatter.format(axiom))
    println("  are "+premisses.map(SimpleOWLFormatter.format))

    premiseMap.put(axiom, premisses)
  }

  def setLeaf(axiom: OWLAxiom)  ={
    leafs.add(axiom)
  }

  def isLeaf(axiom: OWLAxiom) =
    leafs.contains(axiom)
}*/

/**
 * Abstraction for forgetting component (only single names, from sets of axioms)
 */
trait Forgetter {

  def forget(axioms: Iterable[OWLAxiom], name: OWLEntity): Set[OWLAxiom]
}

/*class BeautifulForgetter(forgetter: Forgetter,
                         referenceOntology: OWLOntology
                         = OWLManager.createOWLOntologyManager().createOntology())
  extends Forgetter {

  val beautifier = new OWLAxiomBeautifier()

  override def forget(axioms: Iterable[OWLAxiom], name: OWLEntity): Set[OWLAxiom] = {
    forgetter.forget(axioms,name).flatMap(x =>
      JavaConverters.asScalaSet(
        beautifier.beautify(x,referenceOntology)))
  }
}*/

/**
 * Abstraction for justification.
 */
trait Justifier {
  def justify(axioms: Iterable[OWLAxiom], axiom: OWLAxiom): Set[OWLAxiom]
  def justifyIfPossible(axioms: Iterable[OWLAxiom], axiom: OWLAxiom): Option[Set[OWLAxiom]]
  def justifyAll(axioms: Iterable[OWLAxiom], axiom: OWLAxiom): Set[Set[OWLAxiom]]
  def entailed(axioms: Iterable[OWLAxiom], axiom: OWLAxiom): Boolean
}