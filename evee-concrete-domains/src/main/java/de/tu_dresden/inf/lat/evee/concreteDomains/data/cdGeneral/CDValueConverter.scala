package de.tu_dresden.inf.lat.evee.concreteDomains.data.cdGeneral

import org.apache.commons.math3.fraction.BigFraction
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model.OWLLiteral

object CDValueConverter {

  private val factory = OWLManager.getOWLDataFactory

  def toOWLLiteral(value: Any): OWLLiteral = {
    value match {
      case d: Double => factory.getOWLLiteral(d)
      case bf: BigFraction => factory.getOWLLiteral(bf.doubleValue())
    }
  }

}
