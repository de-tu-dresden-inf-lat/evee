package de.tu_dresden.inf.lat.evee.concreteDomains.tools

import de.tu_dresden.inf.lat.evee.concreteDomains.{CDConstraint, ConstraintNamesFormatter}
import de.tu_dresden.inf.lat.evee.concreteDomains.data.extendedDataStructure.ExtendedAxiom

import de.tu_dresden.inf.lat.evee.proofs.data.{Inference, Proof}
import de.tu_dresden.inf.lat.evee.proofs.interfaces.{IInference, IProof}

import de.tu_dresden.inf.lat.prettyPrinting.formatting.SimpleOWLFormatter

import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model._

import scala.collection.JavaConverters.{asScalaBufferConverter, asScalaSetConverter, seqAsJavaListConverter}
import scala.collection.{JavaConverters, mutable}

/**
 * @author Christian Alrabbaa
 */

class OWLClass2CDPDecoder[CD_CONSTRAINT<:CDConstraint]() {

  def decode(proof: IProof[ExtendedAxiom[CD_CONSTRAINT]]): IProof[ExtendedAxiom[CD_CONSTRAINT]] = {
    val factory = OWLManager.createOWLOntologyManager().getOWLDataFactory

    val finalConclusion = decodeAxiom(proof.getFinalConclusion, factory)
    val res = new Proof[ExtendedAxiom[CD_CONSTRAINT]](finalConclusion)
    var explicitConclusion: Option[ExtendedAxiom[CD_CONSTRAINT]] = None

    proof.getInferences().asScala.foreach(x => {
      val fc = decodeAxiom(x.getConclusion, factory)
      val ps = x.getPremises.asScala.map(decodeAxiom(_, factory))
      res.addInference(new Inference[ExtendedAxiom[CD_CONSTRAINT]](fc, decodeRuleName(x, factory),
        JavaConverters.seqAsJavaList(ps)))

      if(x.getConclusion.equals(proof.getFinalConclusion))
        if(!fc.axiom.equals(x.getConclusion.axiom))
          explicitConclusion = Some(fc)
    })

    //An artificial inference is added in case the RHS of the final conclusion is a name referring to a CD.
    //This clarifies the last step in the proof
//    extendProof(res, explicitConclusion, factory)

    res
  }

  private def getMapFromInference(inference: IInference[ExtendedAxiom[CD_CONSTRAINT]]):Map[OWLClass, CD_CONSTRAINT]  = {
    var res = Map[OWLClass, CD_CONSTRAINT]()
    inference.getPremises.asScala.union(List(inference.getConclusion)).foreach(p=>
      p.map.foreach(e=>
        if(!res.contains(e._1))
          res += e._1 -> e._2
        )
    )
    res
  }

  def decodeRuleName(inference: IInference[ExtendedAxiom[CD_CONSTRAINT]], factory: OWLDataFactory): String = {
    if(!inference.getRuleName.contains("#") && !inference.getRuleName.contains(":") )
      return inference.getRuleName

    val conceptStr = inference.getRuleName.trim.substring(inference.getRuleName.lastIndexOf(" ") + 1)
    val conceptStrClean = SimpleOWLFormatter.format({
      val conceptName = factory.getOWLClass(IRI.create(conceptStr))
      val m = getMapFromInference(inference)

      if (m.contains(conceptName))
        ConstraintNamesFormatter.formatClassExpression(getConstraintAsOWLClass(m(conceptName), factory))
      else conceptName
    })

    inference.getRuleName.replace(conceptStr, conceptStrClean)
  }

  def decodeAxiom(extendedAxiom: ExtendedAxiom[CD_CONSTRAINT], factory: OWLDataFactory): ExtendedAxiom[CD_CONSTRAINT] = {
    if (!extendedAxiom.axiom.isInstanceOf[OWLSubClassOfAxiom])
      return extendedAxiom

    val nameToConstraint = mutable.Map[OWLClass, OWLClass]()
    val n = mutable.Map[OWLClass, CD_CONSTRAINT]()

    val subClsOf = extendedAxiom.axiom.asInstanceOf[OWLSubClassOfAxiom]
    val lhs = decodeConcept(subClsOf.getSubClass, extendedAxiom.map, nameToConstraint, factory)
    val rhs = decodeConcept(subClsOf.getSuperClass, extendedAxiom.map, nameToConstraint, factory)

    val a = factory.getOWLSubClassOfAxiom(lhs, rhs)
    val m = filterMap(extendedAxiom.map,a)

    m.keySet.foreach(key=>{
      n += nameToConstraint(key) -> m(key)
    })
    new ExtendedAxiom[CD_CONSTRAINT](a, n.toMap)
  }

  private def decodeConcept(clsExp: OWLClassExpression, map: Map[OWLClass, CD_CONSTRAINT], newMap: mutable
  .Map[OWLClass, OWLClass], factory: OWLDataFactory): OWLClassExpression = {
    clsExp match {
      case conceptName: OWLClass =>
        decodeConceptName(conceptName, map, newMap, factory)
      case exiRes: OWLObjectSomeValuesFrom =>
        factory.getOWLObjectSomeValuesFrom(exiRes.getProperty, decodeConcept(exiRes.getFiller, map, newMap,factory))
      case conj: OWLObjectIntersectionOf =>
        factory.getOWLObjectIntersectionOf(JavaConverters.setAsJavaSet(conj.asConjunctSet().asScala.map(decodeConcept(_, map, newMap,factory)).toSet))
      case _ => clsExp
    }
  }

  private def decodeConceptName(conceptName: OWLClass, map: Map[OWLClass, CD_CONSTRAINT], newMap: mutable
  .Map[OWLClass, OWLClass], factory: OWLDataFactory) : OWLClassExpression = {
    if (!map.contains(conceptName))
      return conceptName

    val newConceptName = getConstraintAsOWLClass(map(conceptName), factory)
    newMap += conceptName -> newConceptName
    newConceptName
  }

  private def getConstraintAsOWLClass(constraint: CD_CONSTRAINT, factory: OWLDataFactory): OWLClass = {
    factory.getOWLClass(IRI.create("[" + constraint + "]"))
  }

  def filterMap(map: Map[OWLClass, CD_CONSTRAINT], a: OWLSubClassOfAxiom): Map[OWLClass, CD_CONSTRAINT] = {
    val res = mutable.Map[OWLClass, CD_CONSTRAINT]()
    Tools.getConceptNamesInAxiom(a).asScala.foreach(k => {
      if (map.contains(k))
        res(k) = map(k)
    })
    res.toMap
  }

  private def extendProof(ds: IProof[ExtendedAxiom[CD_CONSTRAINT]],
                          explicitConclusion: Option[ExtendedAxiom[CD_CONSTRAINT]], factory: OWLDataFactory) = {
    if (explicitConclusion.nonEmpty) {
      val decodedRHS = explicitConclusion.get.axiom.asInstanceOf[OWLSubClassOfAxiom].getSuperClass

      val originalRHS = ds.getFinalConclusion.axiom.asInstanceOf[OWLSubClassOfAxiom].getSuperClass

      val mapAxiom = new ExtendedAxiom[CD_CONSTRAINT](
        factory.getOWLSubClassOfAxiom(decodedRHS, originalRHS),
        ds.getFinalConclusion.map)

      ds.addInference(
        new Inference[ExtendedAxiom[CD_CONSTRAINT]](
          ds.getFinalConclusion,
          "Superclass Normalisation",
          List(explicitConclusion.get, mapAxiom).asJava))

      ds.addInference(
        new Inference[ExtendedAxiom[CD_CONSTRAINT]](
          mapAxiom,
          "Constraint Mapping",
          List.empty.asJava))
    }
  }
}
