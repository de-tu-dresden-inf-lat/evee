package scala.de.tu_dresden.inf.lat.evee.eliminationProofs.fame

import de.tu_dresden.inf.lat.evee.eliminationProofs.{FameBasedHeuristicProofGenerator, FameBasedSizeMinimalProofGenerator, FameBasedSymbolMinimalProofGenerator, FameBasedWeightedSizeMinimalProofGenerator}
import de.tu_dresden.inf.lat.evee.general.tools.BasicProgressBar
import de.tu_dresden.inf.lat.prettyPrinting.formatting.SimpleOWLFormatterCl
import org.junit.Test
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model.IRI

import java.io.File
import scala.io.Source

class DiscoveredFameBugsTests {

  // Issue 36
  @Test
  def testSpicyPizzaBug(): Unit = {

    val manager = OWLManager.createOWLOntologyManager()
    val factory = manager.getOWLDataFactory()
    val ontology = manager.loadOntology(IRI.create("http://protege.stanford.edu/ontologies/pizza/pizza.owl"))

    val axiom =  factory.getOWLEquivalentClassesAxiom(
      factory.getOWLClass(IRI.create("http://www.co-ode.org/ontologies/pizza/pizza.owl#SpicyPizza")),
      factory.getOWLClass(IRI.create("http://www.co-ode.org/ontologies/pizza/pizza.owl#SpicyPizzaEquivalent"))
    )

    val proofGenerator1 = new FameBasedHeuristicProofGenerator()

    proofGenerator1.setOntology(ontology)

    val proof1 = proofGenerator1.getProof(axiom)

    print(proof1)


    val proofGenerator2 = new FameBasedSizeMinimalProofGenerator()

    proofGenerator2.setOntology(ontology)

    val proof2 = proofGenerator2.getProof(axiom)

    print(proof2)


    val proofGenerator3 = new FameBasedSymbolMinimalProofGenerator()

    proofGenerator3.setOntology(ontology)

    val proof3 = proofGenerator3.getProof(axiom)

    print(proof3)
  }

  // Issue ??
  //@Test
  def testPizzaBug(): Unit = {

    val manager = OWLManager.createOWLOntologyManager()
    val factory = manager.getOWLDataFactory()
    val ontology = manager.loadOntology(IRI.create("http://protege.stanford.edu/ontologies/pizza/pizza.owl"))

    val axiom = factory.getOWLSubClassOfAxiom(
      factory.getOWLClass(IRI.create("http://www.co-ode.org/ontologies/pizza/pizza.owl#FourSeasons")),
      factory.getOWLClass(IRI.create("http://www.co-ode.org/ontologies/pizza/pizza.owl#MeatyPizza"))
    )

    val proofGenerator = new FameBasedSizeMinimalProofGenerator()

    proofGenerator.setOntology(ontology)

    val proof = proofGenerator.getProof(axiom)

    print(proof)
  }

  // Issue #21
  //@Test
  def testLUBMBug() = {

    val manager = OWLManager.createOWLOntologyManager()
    val factory = manager.getOWLDataFactory()
    val ontStream = getClass.getClassLoader.getResourceAsStream("univ-bench.owl.xml")
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

    val proofGenerator = new FameBasedSymbolMinimalProofGenerator()

    proofGenerator.setOntology(ontology)
    val proof = proofGenerator.getProof(axiom)
    println(proof)
  }

  // Related to Issue #21
  @Test
  def testLUBMBug2() = {

    val manager = OWLManager.createOWLOntologyManager()
    val factory = manager.getOWLDataFactory()
    val ontStream = getClass.getClassLoader.getResourceAsStream("univ-bench.owl.xml")
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

    val proofGenerator = new FameBasedHeuristicProofGenerator()

    proofGenerator.setOntology(ontology)
    val proof = proofGenerator.getProof(axiom)
    println(proof)
  }
  // Issue #26
  //@Test
  def testMatchError() = {

    val manager = OWLManager.createOWLOntologyManager()
    val factory = manager.getOWLDataFactory()
    val ontStream = getClass.getClassLoader.getResourceAsStream("dco-debugit.debugit-core-ontology.1.owl.xml")
    val ontology = manager.loadOntologyFromOntologyDocument(ontStream)

    val proofGenerator = new FameBasedWeightedSizeMinimalProofGenerator()

    val axiom = factory.getOWLSubClassOfAxiom(
      factory.getOWLClass(IRI.create("http://purl.org/imbi/dco/dco#VentilatorAssociatedPneumoniaProcess")),
      factory.getOWLClass(IRI.create("http://purl.org/biotop/biotop.owl#NoncanonicalProcessualEntity"))
    )

    proofGenerator.addProgressTracker(new BasicProgressBar())

    proofGenerator.setOntology(ontology)

    val proof = proofGenerator.getProof(axiom)
    println(proof)
  }

  // Issue #28
  @Test
  def testLostWhileForgetting(): Unit ={
    val manager = OWLManager.createOWLOntologyManager()
    val factory = manager.getOWLDataFactory()
    val ontStream = getClass.getClassLoader.getResourceAsStream("spo.skin-physiology-ontology.1.owl.xml")
    val ontology = manager.loadOntologyFromOntologyDocument(ontStream)

    val proofGenerator = new FameBasedWeightedSizeMinimalProofGenerator()

    val axiom = factory.getOWLSubClassOfAxiom(
      factory.getOWLClass(IRI.create("http://www.semanticweb.org/ontologies/2008/8/MultiscaleSkinPhysiologyOntology.owl#SebaceousGland")),
      factory.getOWLObjectIntersectionOf(
        factory.getOWLClass(IRI.create("http://www.semanticweb.org/ontologies/2008/8/MultiscaleSkinPhysiologyOntology.owl#Gland")),
        factory.getOWLObjectSomeValuesFrom(
          factory.getOWLObjectProperty(IRI.create("http://www.obofoundry.org/ro/ro.owl#agent_in")),
          factory.getOWLClass(IRI.create("http://www.semanticweb.org/ontologies/2008/8/SPO_lightweight_merged.owl#SecretionByLysis"))
        )
      )
    )

    proofGenerator.setOntology(ontology)
    val proof = proofGenerator.getProof(axiom)
  }


  // related to Issue #28
  @Test
  def testLostWhileForgettingHeuristic(): Unit ={
    val manager = OWLManager.createOWLOntologyManager()
    val factory = manager.getOWLDataFactory()
    val ontStream = getClass.getClassLoader.getResourceAsStream("spo.skin-physiology-ontology.1.owl.xml")
    val ontology = manager.loadOntologyFromOntologyDocument(ontStream)

    val proofGenerator = new FameBasedHeuristicProofGenerator()

    val axiom = factory.getOWLSubClassOfAxiom(
      factory.getOWLClass(IRI.create("http://www.semanticweb.org/ontologies/2008/8/MultiscaleSkinPhysiologyOntology.owl#SebaceousGland")),
      factory.getOWLObjectIntersectionOf(
        factory.getOWLClass(IRI.create("http://www.semanticweb.org/ontologies/2008/8/MultiscaleSkinPhysiologyOntology.owl#Gland")),
        factory.getOWLObjectSomeValuesFrom(
          factory.getOWLObjectProperty(IRI.create("http://www.obofoundry.org/ro/ro.owl#agent_in")),
          factory.getOWLClass(IRI.create("http://www.semanticweb.org/ontologies/2008/8/SPO_lightweight_merged.owl#SecretionByLysis"))
        )
      )
    )

    proofGenerator.setOntology(ontology)
    val proof = proofGenerator.getProof(axiom)
    println(proof)
  }


  // based on bug observed in protege plugin
  @Test
  def wineBug1(): Unit ={
    val proofGenerator = new FameBasedSizeMinimalProofGenerator()
    val manager = OWLManager.createOWLOntologyManager()
    val ontology = manager
      .loadOntology(IRI.create("http://www.w3.org/TR/owl-guide/wine.rdf"));
    val factory = manager.getOWLDataFactory()

    val lhs = factory.getOWLClass(IRI.create("http://www.w3.org/TR/2003/PR-owl-guide-20031209/food#Wine"))
    val rhs = factory.getOWLClass(IRI.create("http://www.w3.org/TR/2003/PR-owl-guide-20031209/food#PotableLiquid"))

    val otherWine = factory.getOWLClass(IRI.create("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#Wine"))


    val formatter = new SimpleOWLFormatterCl()
    formatter.setReferenceOntology(ontology)
    println("lhs wine: "+formatter.format(lhs))
    println("other wine: "+formatter.format(otherWine))


    proofGenerator.setOntology(ontology)
    val proof = proofGenerator.proveSubsumption(lhs,rhs)

    println(proof)
  }

  // Issue #39
  @Test
  def testPizzaBug2Size(): Unit = {

    val manager = OWLManager.createOWLOntologyManager()
    val factory = manager.getOWLDataFactory()
    val ontology = manager.loadOntology(IRI.create("http://protege.stanford.edu/ontologies/pizza/pizza.owl"))

    val axiom = factory.getOWLSubClassOfAxiom(
      factory.getOWLClass(IRI.create("http://www.co-ode.org/ontologies/pizza/pizza.owl#Country")),
      factory.getOWLClass(IRI.create("http://www.co-ode.org/ontologies/pizza/pizza.owl#DomainConcept"))
    )

    val proofGenerator = new FameBasedSizeMinimalProofGenerator()
    proofGenerator.setOntology(ontology)
    print(proofGenerator.getProof(axiom))
  }

  @Test
  def testPizzaBug2WeightedSize(): Unit = {

    val manager = OWLManager.createOWLOntologyManager()
    val factory = manager.getOWLDataFactory()
    val ontology = manager.loadOntology(IRI.create("http://protege.stanford.edu/ontologies/pizza/pizza.owl"))

    val axiom = factory.getOWLSubClassOfAxiom(
      factory.getOWLClass(IRI.create("http://www.co-ode.org/ontologies/pizza/pizza.owl#Country")),
      factory.getOWLClass(IRI.create("http://www.co-ode.org/ontologies/pizza/pizza.owl#DomainConcept"))
    )

    val proofGenerator = new FameBasedWeightedSizeMinimalProofGenerator()
    proofGenerator.setOntology(ontology)
    print(proofGenerator.getProof(axiom))
  }

}
