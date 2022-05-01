package de.tu_dresden.inf.lat.forgettingBasedProofs.tools

import org.semanticweb.owlapi.model.{OWLAxiom, OWLClass, OWLClassExpression, OWLDisjointClassesAxiom, OWLDisjointUnionAxiom, OWLEquivalentClassesAxiom, OWLNaryBooleanClassExpression, OWLObjectAllValuesFrom, OWLObjectComplementOf, OWLObjectPropertyDomainAxiom, OWLObjectPropertyRangeAxiom, OWLObjectSomeValuesFrom, OWLQuantifiedObjectRestriction, OWLSubClassOfAxiom, OWLSubObjectPropertyOfAxiom}

object OntologyTools {

  def size(axioms: Iterable[OWLAxiom]): Int =
    axioms.toList.map(size).sum

  def size(axiom: OWLAxiom): Int =
    axiom match {
      case ax: OWLSubClassOfAxiom =>
        size(ax.getSubClass) + 1 + size(ax.getSuperClass)
      case ax: OWLEquivalentClassesAxiom =>
        var x= ax.getClassExpressions.size()-1
        ax.getClassExpressions.forEach(x+=size(_));
        x
      case ax: OWLDisjointClassesAxiom =>
        var x = ax.getClassExpressions.size()-1
        ax.getClassExpressions.forEach(x+=size(_))
        x
      case ax: OWLObjectPropertyDomainAxiom =>
        2+size(ax.getDomain)
      case _: OWLSubObjectPropertyOfAxiom =>
        3
      case ax: OWLObjectPropertyRangeAxiom =>
        2+size(ax.getRange)
      case ax: OWLDisjointUnionAxiom =>
        var x = ax.getClassExpressions.size()-1
        ax.getClassExpressions.forEach(x+=size(_))
        x
    }

  def size(cl: OWLClassExpression): Int = cl match {
    case a: OWLClass => 1
    case a: OWLObjectComplementOf => 1 + size(a.getOperand)
    case a: OWLQuantifiedObjectRestriction => 2+size(a.getFiller)
    case a: OWLNaryBooleanClassExpression =>
      var x= a.getOperandsAsList.size()-1
      a.getOperandsAsList.forEach(x+=size(_));
      x
  }
}
