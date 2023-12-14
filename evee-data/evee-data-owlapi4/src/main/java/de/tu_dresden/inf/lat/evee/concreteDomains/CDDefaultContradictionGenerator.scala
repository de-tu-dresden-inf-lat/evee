package de.tu_dresden.inf.lat.evee.concreteDomains

import de.tu_dresden.inf.lat.evee.concreteDomains.exceptions.CDException
import org.apache.commons.math3.fraction.BigFraction

/**
 * @author Christian Alrabbaa
 */
object CDDefaultContradictionGenerator {
  val defaultLCBF: LinearConstraint[BigFraction] = new LinearConstraint[BigFraction](Map.empty, BigFraction.ONE)
  val defaultLCD: LinearConstraint[Double] = new LinearConstraint[Double](Map.empty, 1)
  val defaultCD2: CD2Contradiction.type = CD2Contradiction

  def getDefaultContradiction[T <: CDConstraint](value : T): T = {
    value match {
      case v:LinearConstraint[_] =>
        if(v.getType.equals(BigFraction.ONE.getClass))
          return defaultLCBF.asInstanceOf[T]
        defaultLCD.asInstanceOf[T]
      case _: CD2Predicate => defaultCD2.asInstanceOf[T]
      case _ => throw CDException("Could not recognise the concrete domain constraint -> " + value)
    }
  }
}
