package scala.de.tu_dresden.inf.lat.forgettingBasedProofs

import de.tu_dresden.inf.lat.forgettingBasedProofs.adaptors.{LetheBasedForgetter, OWLApiBasedJustifier}
import de.tu_dresden.inf.lat.forgettingBasedProofs.minimal.SymbolMinimalForgettingBasedProofGenerator
import de.tu_dresden.inf.lat.dltools.ALCHTBoxFilter
import de.tu_dresden.inf.lat.forgettingBasedProofs.{ProofFormatter, ProofGraphVisualiser}
import org.junit.Test
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model.IRI

import java.io.File

class BasicTest {

    //@Test
    def test() = {

      val ontologyManager = OWLManager.createOWLOntologyManager()
      val forgetter = LetheBasedForgetter.ALC_ABox(1000)//  .ALC_ABox(1)
      val justifier = OWLApiBasedJustifier.UsingHermiT(ontologyManager)
      val ontology = ontologyManager.loadOntologyFromOntologyDocument(new File(Thread.currentThread().getContextClassLoader.getResource("pizza.owl").getPath))
      //val proofGenerator = new CompleteForgettingBasedProofGenerator(ontology, forgetter, justifier, true)
      //val proofGenerator = new ForgettingBasedProofGenerator(forgetter,justifier)
      val proofGenerator = new SymbolMinimalForgettingBasedProofGenerator(forgetter,ALCHTBoxFilter, justifier)
      proofGenerator.setOntology(ontology)


      val dataFactory = ontologyManager.getOWLDataFactory()

      val lhs = dataFactory.getOWLClass(IRI.create("http://www.co-ode.org/ontologies/pizza/pizza.owl#Margherita"))
      val rhs = dataFactory.getOWLClass(IRI.create("http://www.co-ode.org/ontologies/pizza/pizza.owl#VegetarianPizzaEquivalent2"))

      val proof = proofGenerator.proveSubsumption(lhs,rhs)

      //proofGenerator.proveUnsatisfiability(dataFactory.getOWLClass(IRI.create("http://www.co-ode.org/ontologies/pizza/pizza.owl#IceCream")))

      println("Done!")

      println()
      println("------------------------------------------")
      println()
      println("WE PRESENT TO YOU NOW")
      println("...")
      println("THE PROOF!")
      println()
      println()
      ProofFormatter.format(proof)

      ProofGraphVisualiser.drawProof(proof, "Margherita is vegetarian.png")

    /*  val visualiser = new ProofDotVisualizer()

      visualiser.setFileName("Margherita is a VegetarianPizza")

      visualiser.format(proofGenerator.proof)

     */
    }
}
