package de.tu_dresden.inf.lat.evee.concreteDomains.linearDomain
  
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInference

import org.apache.commons.math3.fraction.BigFraction
import org.semanticweb.owlapi.model.OWLDataProperty

import java.math.BigInteger

import scala.collection.JavaConverters.collectionAsScalaIterableConverter

/**
 * @author Stefan Borgwardt
 * @author Christian Alrabbaa
 */
object LinearConstraintInferenceChecker {

  def correctCDInference(inf: IInference[LinearConstraint]): Boolean = {
    if (inf.getPremises.size() == 0) return true
    if(inf.getRuleName.equals("[Contradiction]") || inf.getRuleName.equals("Contradiction")) {
      return inf.getPremises.size()==1 && inf.getPremises.iterator().next().isInconsistent && inf.getConclusion
      .isInconsistent}

    // the rule name lists the factors of the premises, e.g. "Linear combination [1, -1/3]"
    val name = inf.getRuleName
    if (!name.startsWith(ImplicationTracker.RULE_PREFIX) || !name.endsWith("]"))
      return false
    val factors = name.substring(ImplicationTracker.RULE_PREFIX.length, name.length - 1).split(',').map(fraction)
    if (factors.length != inf.getPremises.size())
      return false
    var sum = inf.getConclusion
    for ((premise, factor) <- inf.getPremises.asScala.zip(factors))
      sum = add(sum, multiply(factor.negate(), premise))

    if (!sum.lhs.values.forall(BigFraction.ZERO.equals(_)))
      return false
    //Inconsistency check is needed because default contradiction is not necessary the same original inconsistent
    // equation that the bottom concept represents
    if(inf.getConclusion.isInconsistent)
      !sum.rhs.equals(LinearConstraint.contradiction.rhs)
    else
      sum.rhs.equals(BigFraction.ZERO)
  }

  private def fraction(text: String): BigFraction = text.trim.split('/') match {
    case Array(numerator) => new BigFraction(new BigInteger(numerator.trim))
    case Array(numerator, denominator) => new BigFraction(new BigInteger(numerator.trim), new BigInteger(denominator.trim))
  }

  private def multiply(a: BigFraction, c: LinearConstraint) = LinearConstraint(c.lhs.mapValues(a.multiply), a.multiply(c.rhs))

  private def add(a: LinearConstraint, b: LinearConstraint) = {
    var map = collection.mutable.Map[OWLDataProperty, BigFraction]()
    for (prop <- a.lhs.keySet ++ b.lhs.keySet) {
      val aValue = a.lhs.getOrElse(prop, BigFraction.ZERO)
      val bValue = b.lhs.getOrElse(prop, BigFraction.ZERO)
      map += (prop -> aValue.add(bValue))
    }
    LinearConstraint(map.toMap, a.rhs.add(b.rhs))
  }
}
