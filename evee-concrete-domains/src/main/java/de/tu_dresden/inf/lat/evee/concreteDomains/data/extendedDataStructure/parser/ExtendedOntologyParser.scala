package de.tu_dresden.inf.lat.evee.concreteDomains.data.extendedDataStructure.parser

import de.tu_dresden.inf.lat.evee.concreteDomains.data.extendedDataStructure.ExtendedOntology
import de.tu_dresden.inf.lat.evee.concreteDomains.diffDomain.DiffConstraint
import de.tu_dresden.inf.lat.evee.concreteDomains.linearDomain.LinearConstraint

import de.tu_dresden.inf.lat.evee.concreteDomains.diffDomain.parser.DiffConstraintParser
import de.tu_dresden.inf.lat.evee.concreteDomains.linearDomain.parser.LinearConstraintParser
import de.tu_dresden.inf.lat.evee.concreteDomains.data.extendedDataStructure._
  
import de.tu_dresden.inf.lat.evee.concreteDomains.preprocess.RedundancyRemover

import org.apache.commons.math3.fraction.BigFraction
import org.semanticweb.owlapi.apibinding.OWLManager

import java.io.File

object ExtendedOntologyParser {

  def doubleParse(ontologyFile: File, constraintsFile: File): ExtendedOntology[LinearConstraint[Double]] =
    parse(ontologyFile, constraintsFile, _.toDouble)

  def bigParse(s: String): BigFraction = {
    val d = s.toDouble
    if (d == 0d)
      BigFraction.ZERO
    else
      new BigFraction(d)
  }

  def bigParse(ontologyFile: File, constraintsFile: File): ExtendedOntology[LinearConstraint[BigFraction]] = {
    parse(ontologyFile, constraintsFile, bigParse)
  }

  def parse[T](ontologyFile: File, constraintsFile: File, numberParser: String => T): ExtendedOntology[LinearConstraint[T]] = {
    val ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(ontologyFile)

    val constraintParser = new LinearConstraintParser(ontology, numberParser)

    val constraints = constraintParser.parse(constraintsFile)

    val extOnt = ExtendedOntology(ontology, constraints)

    RedundancyRemover.getInstance().makeConstraintNamesUnique(extOnt)
  }

  def diffParse(ontologyFile: File, constraintsFile: File): ExtendedOntology[DiffConstraint] = {
    val ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(ontologyFile)

    val constraintParser = new DiffConstraintParser(ontology)

    val constraints = constraintParser.parse(constraintsFile)

    val extOnt = ExtendedOntology(ontology, constraints)

    RedundancyRemover.getInstance().makeConstraintNamesUnique(extOnt)
  }

}
