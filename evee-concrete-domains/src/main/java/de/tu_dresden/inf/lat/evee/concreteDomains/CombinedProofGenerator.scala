package de.tu_dresden.inf.lat.evee.concreteDomains

import de.tu_dresden.inf.lat.evee.proofs.interfaces.{IProof, IProofGenerator}

import java.util.Optional

/**
 * @author Christian Alrabbaa
 */
trait CombinedProofGenerator [SENTENCE, ONTOLOGY] extends IProofGenerator[SENTENCE, ONTOLOGY]{

  def setOriginalOntology(ontology: ONTOLOGY): Unit

  def getProof(SENTENCE: SENTENCE): IProof[SENTENCE]
}
