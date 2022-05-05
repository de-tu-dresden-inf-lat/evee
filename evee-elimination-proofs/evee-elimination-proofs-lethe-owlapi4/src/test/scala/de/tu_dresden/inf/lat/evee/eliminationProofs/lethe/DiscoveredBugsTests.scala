package scala.de.tu_dresden.inf.lat.evee.eliminationProofs.lethe

import org.junit.Test
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model.IRI
import de.tu_dresden.inf.lat.evee.eliminationProofs.{LetheBasedHeuristicProofGenerator, LetheBasedSymbolMinimalProofGenerator, LetheBasedWeightedSizeMinimalProofGenerator}

class DiscoveredLetheBugsTests {

  // Issue #21
  @Test
  def testLUBMBug() = {

    val manager = OWLManager.createOWLOntologyManager()
    val factory = manager.getOWLDataFactory()
    val ontStream = getClass.getClassLoader.getResourceAsStream("univ-bench.owl")
    val ontology = manager.loadOntologyFromOntologyDocument(ontStream)

    val axiom = factory.getOWLSubClassOfAxiom(
      factory.getOWLClass(IRI.create("http://swat.cse.lehigh.edu/onto/univ-bench.owl#ResearchAssistant")),
      factory.getOWLObjectIntersectionOf(
        factory.getOWLClass(IRI.create("http://swat.cse.lehigh.edu/onto/univ-bench.owl#Person")),
        factory.getOWLObjectSomeValuesFrom(
          factory.getOWLObjectProperty(IRI.create("http://swat.cse.lehigh.edu/onto/univ-bench.owl#worksFor")),
          factory.getOWLClass(IRI.create("http://swat.cse.lehigh.edu/onto/univ-bench.owl#Organization"))
        )
      )
    )

    val proofGenerator = new LetheBasedSymbolMinimalProofGenerator()

    proofGenerator.setOntology(ontology)
    val proof = proofGenerator.getProof(axiom)
    println(proof)
  }

  // Related to Issue #21
  @Test
  def testLUBMBug2() = {

    val manager = OWLManager.createOWLOntologyManager()
    val factory = manager.getOWLDataFactory()
    val ontStream = getClass.getClassLoader.getResourceAsStream("univ-bench.owl")
    val ontology = manager.loadOntologyFromOntologyDocument(ontStream)

    val axiom = factory.getOWLSubClassOfAxiom(
      factory.getOWLClass(IRI.create("http://swat.cse.lehigh.edu/onto/univ-bench.owl#ResearchAssistant")),
      factory.getOWLObjectIntersectionOf(
        factory.getOWLClass(IRI.create("http://swat.cse.lehigh.edu/onto/univ-bench.owl#Person")),
        factory.getOWLObjectSomeValuesFrom(
          factory.getOWLObjectProperty(IRI.create("http://swat.cse.lehigh.edu/onto/univ-bench.owl#worksFor")),
          factory.getOWLClass(IRI.create("http://swat.cse.lehigh.edu/onto/univ-bench.owl#Organization"))
        )
      )
    )

    val proofGenerator = new LetheBasedHeuristicProofGenerator()

    proofGenerator.setOntology(ontology)
    val proof = proofGenerator.getProof(axiom)
    println(proof)
  }
}
