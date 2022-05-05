package scala.de.tu_dresden.inf.lat.evee.eliminationProofs

import java.io.File
import de.tu_dresden.inf.lat.evee.eliminationProofs.adaptors.{LetheBasedForgetter, OWLApiBasedJustifier}
import de.tu_dresden.inf.lat.dltools.ALCHTBoxFilter
import de.tu_dresden.inf.lat.evee.eliminationProofs.ForgettingBasedProofGenerator
import de.tu_dresden.inf.lat.evee.proofs.json.{JsonProofParser, JsonProofWriter}
import org.junit.Test
import org.junit.Assert.assertEquals
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model.{IRI, OWLAxiom}
import uk.ac.man.cs.lethe.internal.dl.filters.OWLOntologyFilters
import org.scalatest.junit.JUnitSuite

class ProofOutputTest extends JUnitSuite {

  @Test
  def testProofOutput(): Unit = {

    println("Parsing ontology...")
    val manager = OWLManager.createOWLOntologyManager()
    val ontology = manager.loadOntologyFromOntologyDocument(new File(Thread.currentThread().getContextClassLoader.getResource("pizza.owl").getPath))

    println("Restricting to ALCH")
    OWLOntologyFilters.restrictToALCH(ontology)

    val forgetter = LetheBasedForgetter.ALC_ABox()
    val justifier = OWLApiBasedJustifier.UsingHermiT(ontology.getOWLOntologyManager)

    val proofGenerator = new ForgettingBasedProofGenerator(forgetter, ALCHTBoxFilter, justifier, true)

    proofGenerator.setOntology(ontology)

    val factory = manager.getOWLDataFactory


    val proof = proofGenerator.proveSubsumption(
      factory.getOWLClass(IRI.create("http://www.co-ode.org/ontologies/pizza/pizza.owl#Margherita")),
      factory.getOWLClass(IRI.create("http://www.co-ode.org/ontologies/pizza/pizza.owl#VegetarianPizzaEquivalent1"))
    )

    val proofWriter = new JsonProofWriter[OWLAxiom]()
    val proofString = proofWriter.toString(proof)

    println(proofString)

    val proofParser = new JsonProofParser()
    val parsedProof = proofParser.parseProof(proofString)

    assertEquals(proof, parsedProof)
  }
}
