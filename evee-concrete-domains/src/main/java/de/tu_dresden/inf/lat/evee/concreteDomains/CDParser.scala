package de.tu_dresden.inf.lat.evee.concreteDomains

import de.tu_dresden.inf.lat.evee.concreteDomains.{CDConstraint, CDMultPredicate}
import org.semanticweb.owlapi.model.OWLClass

import java.io.File

trait CDParser[CD <: CDConstraint] {
  def parse(file: File): Map[OWLClass, CD]
}
