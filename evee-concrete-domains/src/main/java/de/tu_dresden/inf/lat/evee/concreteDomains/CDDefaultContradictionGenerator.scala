package de.tu_dresden.inf.lat.evee.concreteDomains

import de.tu_dresden.inf.lat.evee.concreteDomains.exceptions.CDException
import de.tu_dresden.inf.lat.evee.concreteDomains.linearDomain.LinearConstraint
import de.tu_dresden.inf.lat.evee.concreteDomains.multDomain.{MultConstraint, MultContradiction}
import de.tu_dresden.inf.lat.evee.concreteDomains.diffDomain.{DiffConstraint, DiffContradiction}
import org.apache.commons.math3.fraction.BigFraction

/**
 * @author Christian Alrabbaa
 */
object CDDefaultContradictionGenerator {
  val defaultLCBF: LinearConstraint[BigFraction] = new LinearConstraint[BigFraction](Map.empty, BigFraction.ONE)
  val defaultLCD: LinearConstraint[Double] = new LinearConstraint[Double](Map.empty, 1)
  val defaultDiff: DiffContradiction.type = DiffContradiction
  val defaultMult: MultContradiction.type = MultContradiction

  def getDefaultContradiction[T <: CDConstraint](value : T): T = {
    value match {
      case v:LinearConstraint[_] =>
        if(v.getType.equals(BigFraction.ONE.getClass))
          return defaultLCBF.asInstanceOf[T]
        defaultLCD.asInstanceOf[T]
      case _: DiffConstraint => defaultDiff.asInstanceOf[T]
      case _: MultConstraint => defaultMult.asInstanceOf[T]
      case _ => throw CDException("Could not recognise the concrete domain constraint -> " + value)
    }
  }
}
