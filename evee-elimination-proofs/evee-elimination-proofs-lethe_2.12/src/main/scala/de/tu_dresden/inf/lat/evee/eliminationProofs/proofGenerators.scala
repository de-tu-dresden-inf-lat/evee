package de.tu_dresden.inf.lat.evee.eliminationProofs

import de.tu_dresden.inf.lat.evee.eliminationProofs.adaptors.{LetheBasedForgetter, OWLApiBasedJustifier}
import de.tu_dresden.inf.lat.evee.eliminationProofs.minimal.{ApproximateProofMeasureAxiomSizeSum, ApproximateProofMeasureInferenceNumber, MinimalForgettingBasedProofGenerator, ProofEvaluatorInferenceNumber, SymbolMinimalForgettingBasedProofGenerator}
import de.tu_dresden.inf.lat.dltools.ALCHTBoxFilter
import de.tu_dresden.inf.lat.evee.general.tools.OWLOntologyFilterTool
import de.tu_dresden.inf.lat.evee.proofs.tools.RecursiveProofEvaluator
import de.tu_dresden.inf.lat.evee.proofs.tools.measures.{OWLAxiomSizeWeightedTreeSizeMeasure, TreeSizeMeasure}
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model.OWLAxiom

class LetheBasedHeuristicProofGenerator
  extends ForgettingBasedProofGenerator(
    LetheBasedForgetter.ALC_ABox(2000),
    new OWLOntologyFilterTool.ALCHFilter(),
//    ALCHTBoxFilter,
    OWLApiBasedJustifier.UsingHermiT(OWLManager.createOWLOntologyManager()),
    skipSteps = true
  )

class LetheBasedSymbolMinimalProofGenerator
extends SymbolMinimalForgettingBasedProofGenerator(
    LetheBasedForgetter.ALC_ABox(2000),
  new OWLOntologyFilterTool.ALCHFilter(),
//    ALCHTBoxFilter,
    OWLApiBasedJustifier.UsingHermiT(OWLManager.createOWLOntologyManager()),
    skipSteps = true
)

class LetheBasedSizeMinimalProofGenerator
extends MinimalForgettingBasedProofGenerator(
  //ProofMeasureInferenceNumber,
  ProofEvaluatorInferenceNumber,
  //new RecursiveProofEvaluator[OWLAxiom](new TreeSizeMeasure[OWLAxiom]),
  new ApproximateProofMeasureInferenceNumber,
  LetheBasedForgetter.ALC_ABox(2000),
  new OWLOntologyFilterTool.ALCHFilter(),
//  ALCHTBoxFilter,
  OWLApiBasedJustifier.UsingHermiT(OWLManager.createOWLOntologyManager())
)

class LetheBasedWeightedSizeMinimalProofGenerator
  extends MinimalForgettingBasedProofGenerator(
    //ProofMeasureAxiomSizeSum,
    new RecursiveProofEvaluator[OWLAxiom](new OWLAxiomSizeWeightedTreeSizeMeasure()),
    new ApproximateProofMeasureAxiomSizeSum,
    LetheBasedForgetter.ALC_ABox(2000),
    new OWLOntologyFilterTool.ALCHFilter(),
//    ALCHTBoxFilter,
    OWLApiBasedJustifier.UsingHermiT(OWLManager.createOWLOntologyManager())
  )

class LetheBasedALCHProofGenerator
  extends ForgettingBasedProofGenerator(
    LetheBasedForgetter.ALCH(),
    new OWLOntologyFilterTool.ALCHFilter(),
//    ALCHTBoxFilter,
    OWLApiBasedJustifier.UsingHermiT(OWLManager.createOWLOntologyManager()),
    skipSteps = false)

class LetheBasedALCHProofGeneratorSkippingSteps
  extends ForgettingBasedProofGenerator(
    LetheBasedForgetter.ALCH(),
    new OWLOntologyFilterTool.ALCHFilter(),
//    ALCHTBoxFilter,
    OWLApiBasedJustifier.UsingHermiT(OWLManager.createOWLOntologyManager()),
    skipSteps = true)

class LetheBasedKBProofGenerator
  extends ForgettingBasedProofGenerator(
    LetheBasedForgetter.ALC_ABox(),
    new OWLOntologyFilterTool.ALCHFilter(),
//    ALCHTBoxFilter,
    OWLApiBasedJustifier.UsingHermiT(OWLManager.createOWLOntologyManager()),
    skipSteps = false
  )

class LetheBasedKBProofGeneratorSkippingSteps
  extends ForgettingBasedProofGenerator(
    LetheBasedForgetter.ALC_ABox(),
    new OWLOntologyFilterTool.ALCHFilter(),
//    ALCHTBoxFilter,
    OWLApiBasedJustifier.UsingHermiT(OWLManager.createOWLOntologyManager()),
    skipSteps = true
  )

/*
class FameBasedProofGenerator
  extends ForgettingBasedProofGenerator(
    new FameBasedForgetter(),
    OWLApiBasedJustifier.UsingHermiT(OWLManager.createOWLOntologyManager()),
    skipSteps = false
  )

class FameBasedProofGeneratorSkippingSteps
  extends ForgettingBasedProofGenerator(
    new FameBasedForgetter(),
    OWLApiBasedJustifier.UsingHermiT(OWLManager.createOWLOntologyManager()),
    skipSteps = true
  )*/