package de.tu_dresden.inf.lat.evee.concreteDomains.tools

import java.math.{BigDecimal, BigInteger}

import org.apache.commons.math3.fraction.BigFraction

/**
 * Exact rational numbers for the concrete domains, represented as BigFraction. Constraints must not be parsed via
 * Double, which changes decimal numbers like 0.1 and makes the reasoners derive wrong consequences.
 */
object Numbers {

  /** the exact value of a decimal number such as "-0.25" or "3" */
  def parse(decimal: String): BigFraction = {
    val value = new BigDecimal(decimal.trim)
    if (value.scale <= 0) new BigFraction(value.toBigIntegerExact)
    else new BigFraction(value.unscaledValue, BigInteger.TEN.pow(value.scale))
  }

  /** an integer such as "3", or a fraction in lowest terms such as "-1/3" */
  def format(value: BigFraction): String =
    if (value.getDenominator == BigInteger.ONE) value.getNumerator.toString
    else value.getNumerator + "/" + value.getDenominator

  /** arithmetic and comparison operators for BigFraction */
  implicit class FractionOps(val a: BigFraction) extends AnyVal {
    def +(b: BigFraction): BigFraction = a.add(b)
    def -(b: BigFraction): BigFraction = a.subtract(b)
    def *(b: BigFraction): BigFraction = a.multiply(b)
    def /(b: BigFraction): BigFraction = a.divide(b)
    def unary_- : BigFraction = a.negate()
    def <(b: BigFraction): Boolean = a.compareTo(b) < 0
    def <=(b: BigFraction): Boolean = a.compareTo(b) <= 0
    def >(b: BigFraction): Boolean = a.compareTo(b) > 0
    def >=(b: BigFraction): Boolean = a.compareTo(b) >= 0
    def max(b: BigFraction): BigFraction = if (a >= b) a else b
    def min(b: BigFraction): BigFraction = if (a <= b) a else b
  }
}
