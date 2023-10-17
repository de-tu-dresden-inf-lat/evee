package de.tu_dresden.inf.lat.evee.concreteDomains

import de.tu_dresden.inf.lat.evee.proofs.data.AbstractExtendedAxiom
import de.tu_dresden.inf.lat.prettyPrinting.formatting.SimpleOWLFormatter
import org.semanticweb.owlapi.model.{OWLAxiom, OWLClass}

/**
 * @author Christian Alrabbaa
 */

case class ExtendedAxiom[CD_CONSTRAINT <: CDConstraint](axiom: OWLAxiom, map: Map[OWLClass, CD_CONSTRAINT])
  extends AbstractExtendedAxiom{

  def getAllConstraints: Iterable[CD_CONSTRAINT] = map.values

  def constraintFor(cls: OWLClass): CD_CONSTRAINT = map(cls)

  def constraintNames: Set[OWLClass] = map.keySet

  override def toString: String = ConstraintNamesFormatter.format(axiom)

  override def getJSONString: String = {
    val a = ConstraintNamesFormatter.format(axiom)

    val m = scala.collection.mutable.Map[String, CD_CONSTRAINT]()
    map.foreach(e=>m+=SimpleOWLFormatter.format(e._1) -> e._2)

    "axiom: " + a + ", map: " + m.mkString(",")
  }
}
