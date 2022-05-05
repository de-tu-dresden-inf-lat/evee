package de.tu_dresden.lat.forgettingBasedProofs

import de.tu_dresden.inf.lat.dltools.ALCHTBoxFilter
import de.tu_dresden.inf.lat.forgettingBasedProofs.adaptors.{LetheBasedForgetter, OWLApiBasedJustifier}
import de.tu_dresden.inf.lat.forgettingBasedProofs.minimal.{ApproximateProofMeasureAxiomSizeSum, MinimalForgettingBasedProofGenerator, SymbolMinimalForgettingBasedProofGenerator}
import de.tu_dresden.inf.lat.forgettingBasedProofs.{ForgettingBasedProofGenerator, LetheBasedHeuristicProofGenerator, LetheBasedSizeMinimalProofGenerator, LetheBasedSymbolMinimalProofGenerator, LetheBasedWeightedSizeMinimalProofGenerator}
import de.tu_dresden.inf.lat.proofs.json.JsonProofParser
import de.tu_dresden.inf.lat.proofs.tools.RecursiveProofEvaluator
import de.tu_dresden.inf.lat.proofs.tools.measures.{OWLAxiomSizeWeightedTreeSizeMeasure, TreeSizeMeasure}
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model.OWLAxiom

import java.io.File
import java.util

object ProofsForTask {
  def main(args: Array[String]) = {
    val filename = args(0)

    val manager = OWLManager.createOWLOntologyManager()

    val task = new JsonProofParser().fromFile(new File(args(0)))

    val ontology = manager.createOntology()
    val axioms = new util.HashSet(task.getInferences().get(0).getPremises)
    manager.addAxioms(ontology, axioms)

    val evaluator = new RecursiveProofEvaluator[OWLAxiom](new OWLAxiomSizeWeightedTreeSizeMeasure())

    val proofGen1 = new ForgettingBasedProofGenerator(
      LetheBasedForgetter.ALC_ABox(10000),
      ALCHTBoxFilter,
      OWLApiBasedJustifier.UsingHermiT(OWLManager.createOWLOntologyManager()),
      skipSteps = false
    )
    val proofGen2 = new MinimalForgettingBasedProofGenerator(
      //ProofMeasureAxiomSizeSum,
      evaluator,
      new ApproximateProofMeasureAxiomSizeSum,
      LetheBasedForgetter.ALC_ABox(10000),
      ALCHTBoxFilter,
      OWLApiBasedJustifier.UsingHermiT(OWLManager.createOWLOntologyManager()),
      skipSteps = false
    )
    val proofGen3 = new SymbolMinimalForgettingBasedProofGenerator(
      LetheBasedForgetter.ALC_ABox(10000),
      ALCHTBoxFilter,
      OWLApiBasedJustifier.UsingHermiT(OWLManager.createOWLOntologyManager()),
      skipSteps = false,
      varyJustifications = true
    )

    proofGen1.setOntology(ontology)
    proofGen2.setOntology(ontology)
    proofGen3.setOntology(ontology)


    println("heuristic...")
    val proof1 = proofGen1.getProof(task.getFinalConclusion)
    println("value: "+evaluator.evaluate(proof1))
    println("weighted size...")
    val proof2 = proofGen2.getProof(task.getFinalConclusion)
    println("value: "+evaluator.evaluate(proof2))
    println("symbol minimal...")
    val proof3 = proofGen3.getProof(task.getFinalConclusion)
    println("value: "+evaluator.evaluate(proof3))

    println("Heuristic:")
    println("===========")
    println(proof1)
    println()
    println("Size Minimal:")
    println("======================")
    println(proof2)
    println()
    println("Symbol Minimal:")
    println("===============")
    println(proof3)
    println()

    print("Tree sizes: ")
    println(evaluator.evaluate(proof1), evaluator.evaluate(proof2), evaluator.evaluate(proof3))

  }
}
