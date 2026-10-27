package de.tu_dresden.inf.lat.evee.concreteDomains

import de.tu_dresden.inf.lat.evee.concreteDomains.data.exceptions.ParsingException
import org.semanticweb.owlapi.formats.PrefixDocumentFormat
import org.semanticweb.owlapi.model.{IRI, OWLAnnotationSubject, OWLClass, OWLDataProperty, OWLOntology}

/**
 * Reads the constraints of a concrete domain from the cd:constraint annotations of an ontology. Each concrete domain
 * defines its grammar in parse; names of data properties are resolved with the prefixes of the ontology.
 */
trait ConstraintParser[+CD <: CDConstraint] {

  def ontology: OWLOntology

  protected lazy val factory = ontology.getOWLOntologyManager.getOWLDataFactory

  private lazy val prefixes: Option[PrefixDocumentFormat] =
    Option(ontology.getOWLOntologyManager.getOntologyFormat(ontology)).collect { case f: PrefixDocumentFormat => f }

  /** reads one constraint of this concrete domain from the tokens */
  protected def parse(tokens: ConstraintTokens): CD

  def toConstraint(constraint: String): CD = {
    val tokens = new ConstraintTokens(constraint)
    val result = parse(tokens)
    tokens.end()
    result
  }

  /** the class that the constraint is attached to, with the constraint */
  def classConstraint(subject: OWLAnnotationSubject, constraint: String): (OWLClass, CD) = {
    val owlClass = subject match {
      case iri: IRI => factory.getOWLClass(iri)
      case other => throw new ParsingException(s"""The constraint "$constraint" has to be attached to a class, not to $other""")
    }
    val parsed =
      try toConstraint(constraint)
      catch {
        case e: ParsingException =>
          throw new ParsingException(s"""Cannot read the constraint "$constraint" of ${owlClass.getIRI}: ${e.getMessage}""")
      }
    owlClass -> parsed
  }

  /**
   * reads def(x) if it comes next, which says that the data property x has a value; each concrete domain expresses
   * this by one of its constraints
   */
  protected def definedProperty(tokens: ConstraintTokens): Option[OWLDataProperty] =
    if (tokens.acceptCall("def")) {
      val x = property(tokens)
      tokens.expect(")")
      Some(x)
    } else None

  /** reads the name of a data property: a full IRI, a prefixed name, or a plain name in the default namespace */
  protected def property(tokens: ConstraintTokens): OWLDataProperty = property(tokens.name())

  /** the data property with the given name, as written in a constraint */
  protected def property(name: String): OWLDataProperty = factory.getOWLDataProperty(resolve(name))

  private def resolve(name: String): IRI =
    if (name.startsWith("<"))
      IRI.create(name.substring(1, name.length - 1))
    else {
      val prefixedName = if (name.contains(":")) name else ":" + name
      val prefix = prefixedName.substring(0, prefixedName.indexOf(':') + 1)
      prefixes match {
        case Some(format) if format.containsPrefixMapping(prefix) => format.getIRI(prefixedName)
        case Some(_) => throw new ParsingException(s"the prefix $prefix of $name is not defined in the ontology")
        case None => throw new ParsingException(s"cannot resolve $name, since the ontology defines no prefixes; " +
          "use a full IRI in angle brackets")
      }
    }
}
