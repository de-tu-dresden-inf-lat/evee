package de.tu_dresden.inf.lat.evee.concreteDomains

import de.tu_dresden.inf.lat.evee.concreteDomains.data.exceptions.ParsingException
import de.tu_dresden.inf.lat.evee.concreteDomains.tools.Numbers
import org.apache.commons.math3.fraction.BigFraction

/**
 * The tokens of a constraint, read from left to right. There are three kinds of tokens:
 *  - names of data properties: full IRIs in angle brackets, prefixed names such as ex:x or :x, and plain names such
 *    as x; names may contain hyphens, so a minus sign after a name needs a space before it,
 *  - numbers without sign: decimals such as 0.5 or 1e-3, and fractions such as 1/3,
 *  - the operators + - * = < > (also − for minus).
 * Whitespace between tokens is optional.
 */
class ConstraintTokens(input: String) {

  private sealed trait Token { def position: Int }
  private case class NameToken(text: String, position: Int) extends Token
  private case class NumberToken(value: BigFraction, text: String, position: Int) extends Token
  private case class OperatorToken(text: String, position: Int) extends Token

  private val TokenPattern = ("""\s*(?:""" +
    """(<[^<>\s]*>)""" +
    """|([\p{L}_:](?:[\p{L}\p{N}_\-.:%]*[\p{L}\p{N}_:%])?)""" +
    """|((?:\d+(?:\.\d*)?|\.\d+)(?:[eE][+-]?\d+)?(?:/\d+)?)""" +
    """|([-+*=<>−]))""").r

  private val tokens: Vector[Token] = {
    val result = Vector.newBuilder[Token]
    var position = 0
    while (input.substring(position).trim.nonEmpty) {
      val m = TokenPattern.findPrefixMatchOf(input.substring(position)).getOrElse(
        throw new ParsingException(s"unexpected '${input.substring(position).trim.head}' at position ${start(position)}"))
      val tokenStart = position + m.group(0).length - m.group(0).dropWhile(_.isWhitespace).length
      result += (
        if (m.group(1) != null || m.group(2) != null) NameToken(m.group(0).trim, tokenStart)
        else if (m.group(3) != null) NumberToken(parseNumber(m.group(3)), m.group(3), tokenStart)
        else OperatorToken(m.group(4).replace('−', '-'), tokenStart))
      position += m.end
    }
    result.result()
  }

  private def start(position: Int) = position + input.substring(position).takeWhile(_.isWhitespace).length + 1

  private def parseNumber(text: String): BigFraction = text.split('/') match {
    case Array(value) => Numbers.parse(value)
    case Array(numerator, denominator) =>
      val d = Numbers.parse(denominator)
      if (d.equals(BigFraction.ZERO)) throw new ParsingException(s"division by zero in $text")
      Numbers.parse(numerator).divide(d)
  }

  private var index = 0

  private def next: Option[Token] = tokens.lift(index)

  /** fails with a message that says what was expected and what was found instead */
  def fail(expected: String): Nothing = next match {
    case Some(token) =>
      val text = token match {
        case NameToken(t, _) => t
        case NumberToken(_, t, _) => t
        case OperatorToken(t, _) => t
      }
      throw new ParsingException(s"expected $expected, but found '$text' at position ${token.position + 1}")
    case None => throw new ParsingException(s"expected $expected, but the constraint ends")
  }

  /** reads the operator if it comes next */
  def accept(operator: String): Boolean = next match {
    case Some(OperatorToken(`operator`, _)) => index += 1; true
    case _ => false
  }

  def expect(operator: String): Unit = if (!accept(operator)) fail(s"'$operator'")

  /** whether a number, possibly with a sign, comes next */
  def atNumber: Boolean = next match {
    case Some(NumberToken(_, _, _)) => true
    case Some(OperatorToken("+" | "-", _)) => tokens.lift(index + 1).exists(_.isInstanceOf[NumberToken])
    case _ => false
  }

  /** reads a number, possibly with a sign */
  def number(): BigFraction = {
    val sign = if (accept("-")) BigFraction.MINUS_ONE else { accept("+"); BigFraction.ONE }
    next match {
      case Some(NumberToken(value, _, _)) => index += 1; value.multiply(sign)
      case _ => fail("a number")
    }
  }

  /** reads a name as written: a full IRI in angle brackets, a prefixed name or a plain name */
  def name(): String = next match {
    case Some(NameToken(text, _)) => index += 1; text
    case _ => fail("a data property")
  }

  /** checks that all tokens have been read */
  def end(): Unit = if (next.isDefined) fail("the end of the constraint")
}
