package scala.de.tu_dresden.inf.lat.evee.forgettingBasedProofs

import de.tu_dresden.inf.lat.evee.forgettingBasedProofs.{LetheBasedALCHProofGenerator, LetheBasedALCHProofGeneratorSkippingSteps, LetheBasedSymbolMinimalProofGenerator}
import de.tu_dresden.inf.lat.evee.forgettingBasedProofs.adaptors.{LetheBasedForgetter, OWLApiBasedJustifier}
import de.tu_dresden.inf.lat.evee.proofs.data.AbstractSimpleOWLProofGenerator
import de.tu_dresden.inf.lat.evee.proofs.interfaces.{IInference, ISimpleProofGenerator}
import de.tu_dresden.inf.lat.evee.proofs.json.JsonProofParser
import de.tu_dresden.inf.lat.evee.proofs.tools.BasicProgressBar
import org.junit.Test
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model.{OWLAxiom, OWLClass, OWLEntity, OWLEquivalentClassesAxiom, OWLOntology, OWLSubClassOfAxiom}
import uk.ac.man.cs.lethe.internal.tools.formatting.SimpleOWLFormatter

import java.io.File
import java.util
import java.util.Collections
import scala.collection.mutable

class JustificationFileTests {

  //@Test
  def testALCFiles() = {
    val dir = new File(
      getClass()
        .getClassLoader()
        .getResource("problems")
//        .getResource("justifications-ALC-Bioportal")
        .getFile()
    )

    List(
     // new LetheBasedALCHProofGenerator(),
     // new LetheBasedALCHProofGeneratorSkippingSteps(),
 //    new LetheBasedHeuristicProofGenerator(),
     new LetheBasedSymbolMinimalProofGenerator()
      //new LetheBasedSizeMinimalProofGenerator()
    ).foreach{ proofGenerator =>

      proofGenerator.addProgressTracker(new BasicProgressBar)

      val tester = new ExampleFileTest(proofGenerator)

      tester.testDir(dir)
    }
  }

  //@Test
  def testELFiles() = {
    val dir = new File(
      getClass()
        .getClassLoader()
        .getResource("justificationsLPAR")
        .getFile()
    )

    val justifier =
      OWLApiBasedJustifier.UsingHermiT(OWLManager.createOWLOntologyManager())

    List(
     // new FameBasedProofGenerator(),
      new LetheBasedALCHProofGenerator(),
      new LetheBasedALCHProofGeneratorSkippingSteps(),
      /*new ForgettingBasedProofGenerator(
        LetheBasedForgetter.ALC_ABox(), justifier, skipSteps = false),
      new ForgettingBasedProofGenerator(
        LetheBasedForgetter.ALC_ABox(), justifier, skipSteps = true),*/
    ).foreach{ proofGenerator =>

      val tester = new ExampleFileTest(proofGenerator)

      tester.testDir(dir)
    }

  }
}

class ExampleFileTest(proofGenerator: ISimpleProofGenerator[OWLClass, OWLAxiom,OWLOntology]) {

  val parser = new JsonProofParser()

  val manager = OWLManager.createOWLOntologyManager()

  def testDir(dir: File) = {
    dir.listFiles().foreach(testFile)
  }

  def testFile(file: File) = {
    System.out.println("testing file "+file)

    val justificationProof = parser.fromFile(file)

    val ontology = manager.createOntology()

    val premiseSet : util.HashSet[OWLAxiom] = new util.HashSet[OWLAxiom]()
    justificationProof.getInferences().get(0).getPremises.forEach(x => premiseSet.add(x))
    manager.addAxioms(ontology, premiseSet)

    proofGenerator.setOntology(ontology)

    System.out.println("Ontology:")
    ontology.getAxioms().forEach(x => println(SimpleOWLFormatter.format(x)))
    System.out.println()
    System.out.println()

    val proof = justificationProof.getFinalConclusion match {
      case eq: OWLEquivalentClassesAxiom =>
        val classes = eq.getClassExpressionsAsList
        assert(classes.size()==2)
        proofGenerator.proveEquivalence(
          classes.get(0).asInstanceOf[OWLClass],
          classes.get(1).asInstanceOf[OWLClass])
      case subs: OWLSubClassOfAxiom =>
        proofGenerator.proveSubsumption(
          subs.getSubClass.asInstanceOf[OWLClass],
          subs.getSuperClass.asInstanceOf[OWLClass])
    }

    println(proof)
  }
}
