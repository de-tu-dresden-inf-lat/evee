package de.tu_dresden.inf.lat.evee.eliminationProofs

import de.tu_dresden.inf.lat.dltools.ALCTBoxFilter
import de.tu_dresden.inf.lat.evee.eliminationProofs.adaptors.{FamePlusBasedForgetter, OWLApiBasedJustifier}
import de.tu_dresden.inf.lat.evee.eliminationProofs.minimal.{ApproximateProofMeasureAxiomSizeSum, ApproximateProofMeasureInferenceNumber, MinimalForgettingBasedProofGenerator, ProofEvaluatorAxiomSizeSum, SymbolMinimalForgettingBasedProofGenerator}
import de.tu_dresden.inf.lat.evee.proofs.tools.RecursiveProofEvaluator
import de.tu_dresden.inf.lat.evee.proofs.tools.measures.{OWLAxiomSizeWeightedTreeSizeMeasure, TreeSizeMeasure}
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model.OWLAxiom


class FameBasedHeuristicProofGenerator
  extends ForgettingBasedProofGenerator(
    FamePlusBasedForgetter,
    ALCTBoxFilter,
    OWLApiBasedJustifier.UsingHermiT(OWLManager.createOWLOntologyManager()),
    skipSteps = true
  )

class FameBasedSymbolMinimalProofGenerator
extends SymbolMinimalForgettingBasedProofGenerator(
    FamePlusBasedForgetter,
    ALCTBoxFilter,
    OWLApiBasedJustifier.UsingHermiT(OWLManager.createOWLOntologyManager()),
    skipSteps = false
)

class FameBasedSizeMinimalProofGenerator
extends MinimalForgettingBasedProofGenerator(
  new RecursiveProofEvaluator[OWLAxiom](new TreeSizeMeasure[OWLAxiom]()),
  new ApproximateProofMeasureInferenceNumber,
  FamePlusBasedForgetter,
  ALCTBoxFilter,
  OWLApiBasedJustifier.UsingHermiT(OWLManager.createOWLOntologyManager())
)

class FameBasedWeightedSizeMinimalProofGenerator
  extends MinimalForgettingBasedProofGenerator(
    new RecursiveProofEvaluator[OWLAxiom](new OWLAxiomSizeWeightedTreeSizeMeasure()),
    new ApproximateProofMeasureAxiomSizeSum,
    FamePlusBasedForgetter,
    ALCTBoxFilter,
    OWLApiBasedJustifier.UsingHermiT(OWLManager.createOWLOntologyManager())
  )
