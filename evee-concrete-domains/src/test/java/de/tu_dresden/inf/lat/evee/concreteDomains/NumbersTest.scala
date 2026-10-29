package de.tu_dresden.inf.lat.evee.concreteDomains

import de.tu_dresden.inf.lat.evee.concreteDomains.tools.Numbers
import org.apache.commons.math3.fraction.BigFraction
import org.junit.Assert.assertEquals
import org.junit.Test

/** Decimal numbers in constraints are read exactly and displayed readably. */
class NumbersTest {

  @Test def parse(): Unit = {
    assertEquals(new BigFraction(1, 10), Numbers.parse("0.1"))
    assertEquals(new BigFraction(-5, 2), Numbers.parse("-2.50"))
    assertEquals(new BigFraction(3), Numbers.parse("+3"))
    assertEquals(new BigFraction(1, 1000), Numbers.parse(" 1e-3 "))
  }

  @Test def format(): Unit = {
    assertEquals("1/2", Numbers.format(new BigFraction(1, 2)))
    assertEquals("-5/2", Numbers.format(new BigFraction(-5, 2)))
    assertEquals("5", Numbers.format(new BigFraction(5)))
    assertEquals("0", Numbers.format(BigFraction.ZERO))
    assertEquals("100", Numbers.format(new BigFraction(100)))
    assertEquals("1/3", Numbers.format(new BigFraction(1, 3)))
    assertEquals("7/20", Numbers.format(new BigFraction(7, 20)))
    assertEquals("-1/6", Numbers.format(new BigFraction(-1, 6)))
  }
}
