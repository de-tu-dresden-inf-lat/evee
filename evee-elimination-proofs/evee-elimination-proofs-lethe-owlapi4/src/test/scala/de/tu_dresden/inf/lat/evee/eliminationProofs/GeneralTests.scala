package scala.de.tu_dresden.inf.lat.evee.eliminationProofs

import de.tu_dresden.inf.lat.evee.eliminationProofs.adaptors.{LetheBasedForgetter, OWLApiBasedJustifier}
import de.tu_dresden.inf.lat.evee.eliminationProofs.dataStructures.Forgetter
import de.tu_dresden.inf.lat.evee.eliminationProofs.grid.ForgettingBasedGridProofGenerator
import de.tu_dresden.inf.lat.evee.eliminationProofs.minimal.{ApproximateProofMeasureAxiomSizeSum, ApproximateProofMeasureInferenceNumber, MinimalForgettingBasedProofGenerator, MinimalSignatureAndForgettingBasedProofGenerator, ProofEvaluatorAxiomNumber, ProofEvaluatorAxiomSizeSum, ProofEvaluatorInferenceNumber, SymbolMinimalForgettingBasedProofGenerator}
import de.tu_dresden.inf.lat.dltools.ALCHTBoxFilter
import de.tu_dresden.inf.lat.evee.eliminationProofs.{LetheBasedKBProofGeneratorSkippingSteps, ProofGraphVisualiser}
import de.tu_dresden.inf.lat.prettyPrinting.formatting.SimpleOWLFormatter
import de.tu_dresden.inf.lat.evee.proofs.interfaces.{IProofGenerator, ISignatureBasedProofGenerator, ISimpleProofGenerator, ISimpleSignatureBasedProofGenerator}
import de.tu_dresden.inf.lat.evee.proofs.proofGenerators.OWLSignatureBasedMinimalTreeProofGenerator
import de.tu_dresden.inf.lat.evee.proofs.tools.BasicProgressBar
import org.junit.Assert.{assertEquals, assertFalse}
import org.junit.{Before, Test}
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model.parameters.Imports
import org.semanticweb.owlapi.model.{IRI, OWLAxiom, OWLClass, OWLEntity, OWLOntology, OWLOntologyManager}
import uk.ac.man.cs.lethe.internal.dl.datatypes.OntologyFilter

import java.io.File
import scala.Predef.Set
import scala.collection.JavaConverters


class GeneralTests {

  val ontologyManager: OWLOntologyManager = OWLManager.createOWLOntologyManager();

  //@Test
  def testSignatureBasedMinimalProofGenerator(): Unit = {
    var forgetter: Forgetter = LetheBasedForgetter.ALC_ABox(2000)//  .ALC_ABox(1)
   // forgetter = new BeautifulForgetter(forgetter)
    val justifier = OWLApiBasedJustifier.UsingHermiT(ontologyManager)
    val measure = ProofEvaluatorInferenceNumber
    val approximateMeasure = new ApproximateProofMeasureInferenceNumber
    //val measure = ProofMeasureAxiomSizeSum
    //val approximateMeasure = new ApproximateProofMeasureAxiomSizeSum()

    var proofGenerator: ISignatureBasedProofGenerator[OWLEntity,OWLAxiom,OWLOntology] =
      new MinimalSignatureAndForgettingBasedProofGenerator(
      measure,approximateMeasure,forgetter,ALCHTBoxFilter, justifier)


    proofGenerator = new OWLSignatureBasedMinimalTreeProofGenerator(proofGenerator)

    testPizzaInferenceWithSignature(proofGenerator)
  }

  @Test
  def testMinimalSizeForgettingBasedProofGenerator(): Unit = {
    var forgetter: Forgetter = LetheBasedForgetter.ALC_ABox(2000)//  .ALC_ABox(1)
  //  forgetter = new BeautifulForgetter(forgetter)
    val justifier = OWLApiBasedJustifier.UsingHermiT(ontologyManager)
    //val measure = ProofMeasureInferenceNumber
    //val approximateMeasure = new ApproximateProofMeasureInferenceNumber
    val measure = ProofEvaluatorAxiomSizeSum
    val approximateMeasure = new ApproximateProofMeasureAxiomSizeSum()

    val proofGenerator = new MinimalForgettingBasedProofGenerator(
      measure,approximateMeasure,forgetter, ALCHTBoxFilter, justifier)

    proofGenerator.addProgressTracker(new BasicProgressBar())

    testWineInference(proofGenerator)
    //testPizzaInference(proofGenerator)
    //testAminoAcid(proofGenerator)
    //testLubmInference(proofGenerator)
  }

  //@Test
  def testForgettingBasedGridProofGenerator(): Unit = {
    val forgetter = LetheBasedForgetter.ALC_ABox(1000)//  .ALC_ABox(1)
    val justifier = OWLApiBasedJustifier.UsingHermiT(ontologyManager)

    val proofGenerator = new ForgettingBasedGridProofGenerator(forgetter,justifier)

    testWineInference(proofGenerator)
    testPizzaInference(proofGenerator)
    //testAminoAcid(proofGenerator)
  }

  @Test
  def testDepthMinimalForgettingBasedProofGenerator(): Unit = {
    val forgetter = LetheBasedForgetter.ALC_ABox(2000)//  .ALC_ABox(1)
    val justifier = OWLApiBasedJustifier.UsingHermiT(ontologyManager)

    val proofGenerator = new SymbolMinimalForgettingBasedProofGenerator(forgetter, ALCHTBoxFilter,
      justifier,skipSteps = false)

    proofGenerator.addProgressTracker(new BasicProgressBar)

    testWineInference(proofGenerator)
    //testPizzaInference(proofGenerator)
    //testAminoAcid(proofGenerator)
    //testLubmInference(proofGenerator)
  }

  @Test
  def testLetheBasedProofGenerator(): Unit = {
    val proofGenerator = new LetheBasedKBProofGeneratorSkippingSteps()
    proofGenerator.addProgressTracker(new BasicProgressBar())
    testWineInference(proofGenerator)
    testPizzaInference(proofGenerator)
    //testAminoAcid(proofGenerator)
    //testLubmInference(proofGenerator)
  }

  def testWineInference(proofGenerator: ISimpleProofGenerator[OWLClass,OWLAxiom,OWLOntology]): Unit = {
    val ontology = ontologyManager.loadOntologyFromOntologyDocument(IRI.create("http://www.w3.org/TR/owl-guide/wine.rdf"))
    val factory = ontologyManager.getOWLDataFactory

    val lhs = factory.getOWLClass(IRI.create("http://www.w3.org/TR/2003/PR-owl-guide-20031209/food#BlandFishCourse"))
    val rhs = factory.getOWLClass(IRI.create("http://www.w3.org/TR/2003/PR-owl-guide-20031209/food#FishCourse"))

    proofGenerator.setOntology(ontology)
    val proof = proofGenerator.proveSubsumption(lhs,rhs)

    ProofGraphVisualiser.drawProof(proof, "wine-proof-"+proofGenerator.getClass.getSimpleName+".png")
  }

  def testAminoAcid(proofGenerator: ISimpleProofGenerator[OWLClass,OWLAxiom,OWLOntology]): Unit = {
    val ontology = ontologyManager.loadOntologyFromOntologyDocument(IRI.create("https://raw.githubusercontent.com/TheOntologist/AminoAcidOntology/master/amino-acid.owl"))
    val factory = ontologyManager.getOWLDataFactory

    val glutamine = factory.getOWLClass(IRI.create("http://www.co-ode.org/ontologies/amino-acid/2006/05/18/amino-acid.owl#Q"))
    val largeAliphatic = factory.getOWLClass(IRI.create("http://www.co-ode.org/ontologies/amino-acid/2006/05/18/amino-acid.owl#LargeAliphaticAminoAcid"))

    proofGenerator.setOntology(ontology)
    val proof = proofGenerator.proveSubsumption(glutamine, largeAliphatic)

    ProofGraphVisualiser.drawProof(proof, "amino-acid-proof-"+proofGenerator.getClass.getSimpleName+".png")
  }

  def testPizzaInference(proofGenerator: ISimpleProofGenerator[OWLClass,OWLAxiom,OWLOntology]): Unit = {
    val ontology = ontologyManager.loadOntologyFromOntologyDocument(new File(Thread.currentThread().getContextClassLoader.getResource("pizza.owl").getPath))
    val factory = ontologyManager.getOWLDataFactory

    val lhs = factory.getOWLClass(IRI.create("http://www.co-ode.org/ontologies/pizza/pizza.owl#Margherita"))
    val rhs = factory.getOWLClass(IRI.create("http://www.co-ode.org/ontologies/pizza/pizza.owl#VegetarianPizzaEquivalent2"))

    proofGenerator.setOntology(ontology)
    val proof = proofGenerator.proveSubsumption(lhs,rhs)

    ProofGraphVisualiser.drawProof(proof, "pizza-proof-"+proofGenerator.getClass.getSimpleName+".png")
  }

  def testLubmInference(proofGenerator: IProofGenerator[OWLAxiom,OWLOntology]): Unit = {

    val ontology = ontologyManager.loadOntologyFromOntologyDocument(new File(Thread.currentThread().getContextClassLoader.getResource("univ-bench.owl").getPath))
    val factory = ontologyManager.getOWLDataFactory

    val ra = factory.getOWLClass(IRI.create("http://swat.cse.lehigh.edu/onto/univ-bench.owl#ResearchAssistant"))
    val person = factory.getOWLClass(IRI.create("http://swat.cse.lehigh.edu/onto/univ-bench.owl#Person"))
    val worksFor = factory.getOWLObjectProperty(IRI.create("http://swat.cse.lehigh.edu/onto/univ-bench.owl#worksFor"))
    val organization = factory.getOWLClass(IRI.create("http://swat.cse.lehigh.edu/onto/univ-bench.owl#Organization"))

    val goal = factory.getOWLSubClassOfAxiom(ra,factory.getOWLObjectIntersectionOf(person,factory.getOWLObjectSomeValuesFrom(worksFor,organization)))

    proofGenerator.setOntology(ontology)
    val proof = proofGenerator.getProof(goal)
      
    ProofGraphVisualiser.drawProof(proof, "lubm-proof-"+proofGenerator.getClass.getSimpleName+".png")
  }

  def testPizzaInferenceWithSignature(proofGenerator: ISignatureBasedProofGenerator[OWLEntity,OWLAxiom,OWLOntology])
  : Unit = {
    val ontology = ontologyManager.loadOntologyFromOntologyDocument(new File("pizza.owl"))
    val factory = ontologyManager.getOWLDataFactory

    val lhs = factory.getOWLClass(
      IRI.create("http://www.co-ode.org/ontologies/pizza/pizza.owl#Margherita"))
    val rhs = factory.getOWLClass(
      IRI.create("http://www.co-ode.org/ontologies/pizza/pizza.owl#VegetarianPizzaEquivalent2"))

    var signature =  // Set[OWLEntity]()
    JavaConverters.asScalaSet(ontology.getSignature(Imports.INCLUDED)).toSet

    signature -= rhs


    /*val topping = factory.getOWLClass(
      IRI.create("http://www.co-ode.org/ontologies/pizza/pizza.owl#PizzaTopping"))

    val pizza = factory.getOWLClass(
      IRI.create("http://www.co-ode.org/ontologies/pizza/pizza.owl#Pizza"))

    val hasTopping = factory.getOWLObjectProperty(
      IRI.create("http://www.co-ode.org/ontologies/pizza/pizza.owl#hasTopping"))

    signature += pizza

    signature += hasTopping

    signature += topping

    // add all stated subclasses of topping
    signature ++= statedSubClasses(topping,ontology)

    val subClasses: Set[Set[OWLClass]] = signature
      .collect[Set[OWLClass],Set[Set[OWLClass]]] { case cl: OWLClass => statedSubClasses(cl,ontology) }

    subClasses.foreach(signature++=_)

    // add all stated subclasses of what we asked for
    //signature ++= subClasses.flatten
    */


    System.out.println("Signature to be used is: "+signature.map(SimpleOWLFormatter.format))

    //assertFalse(signature.contains(lhs))
    assertFalse(signature.contains(rhs))

    proofGenerator.setOntology(ontology)
    proofGenerator.setSignature(JavaConverters.asJavaCollection(signature))

    val proof = proofGenerator.getProof(factory.getOWLSubClassOfAxiom(lhs,rhs))

    ProofGraphVisualiser.drawProof(proof, "pizza-proof-"+proofGenerator.getClass.getSimpleName+".png")
  }

  private def statedSubClasses(clazz: OWLClass, ontology: OWLOntology): Set[OWLClass] = {
    JavaConverters.asScalaSet(ontology.getSubClassAxiomsForSuperClass(clazz))
      .map(gci => gci.getSubClass)
      .collect{case cl: OWLClass => cl}.toSet
  }

}
