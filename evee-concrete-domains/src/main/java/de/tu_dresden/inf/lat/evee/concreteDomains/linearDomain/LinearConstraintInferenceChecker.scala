package de.tu_dresden.inf.lat.evee.concreteDomains.linearDomain
  
import de.tu_dresden.inf.lat.evee.concreteDomains.CDDefaultContradictionGenerator
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInference

import org.apache.commons.math3.fraction.{BigFraction, BigFractionFormat}
import org.semanticweb.owlapi.model.OWLDataProperty

import scala.collection.JavaConverters.collectionAsScalaIterableConverter

/**
 * @author Stefan Borgwardt
 * @author Christian Alrabbaa
 */
object LinearConstraintInferenceChecker {

  def correctCDInference(inf: IInference[LinearConstraint[BigFraction]]): Boolean = {
    if (inf.getPremises.size() == 0) return true
    if(inf.getRuleName.equals("[Contradiction]") || inf.getRuleName.equals("Contradiction")) {
      return inf.getPremises.size()==1 && inf.getPremises.iterator().next().isInconsistent && inf.getConclusion
      .isInconsistent}

    var name = inf.getRuleName
    name = name.substring(1, name.length - 1)
    val factors = name.split(',')
    var i = 0
    var sum = inf.getConclusion
    for (premise <- inf.getPremises.asScala) {
      val factor = BigFractionFormat.getProperInstance.parse(factors(i))
      sum = add(sum, multiply(factor.negate(), premise))
      i = i + 1
    }

    val result = {
      if (!sum.lhs.values.forall(BigFraction.ZERO.equals(_)))
        return false
      //Inconsistency check is needed because default contradiction is not necessary the same original inconsistent
      // equation that the bottom concept represents
      if(inf.getConclusion.isInconsistent)
        !sum.rhs.equals(CDDefaultContradictionGenerator.defaultLCBF.rhs)
      else
        sum.rhs.equals(BigFraction.ZERO)
    }

    if(!result) {
      println("Wrong CD inference")
      println(inf)
    }
    result
  }

  private def multiply(a: BigFraction, c: LinearConstraint[BigFraction]) = LinearConstraint[BigFraction](c.lhs.mapValues(a.multiply), a.multiply(c.rhs))

  private def add(a: LinearConstraint[BigFraction], b: LinearConstraint[BigFraction]) = {
    var map = collection.mutable.Map[OWLDataProperty, BigFraction]()
    for (prop <- a.lhs.keySet ++ b.lhs.keySet) {
      val aValue = a.lhs.getOrElse(prop, BigFraction.ZERO)
      val bValue = b.lhs.getOrElse(prop, BigFraction.ZERO)
      map += (prop -> aValue.add(bValue))
    }
    LinearConstraint(map.toMap, a.rhs.add(b.rhs))
  }
}
