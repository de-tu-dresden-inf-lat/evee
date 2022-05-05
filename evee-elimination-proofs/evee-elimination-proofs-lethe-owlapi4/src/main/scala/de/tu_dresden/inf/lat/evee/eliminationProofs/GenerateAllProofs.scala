package de.tu_dresden.inf.lat.evee.eliminationProofs

import java.io.{File, PrintWriter}
import org.semanticweb.owlapi.apibinding.OWLManager

import java.io.File
import de.tu_dresden.inf.lat.evee.eliminationProofs.adaptors.{LetheBasedForgetter, OWLApiBasedJustifier}
import de.tu_dresden.inf.lat.dltools.ALCHTBoxFilter
import de.tu_dresden.inf.lat.evee.proofs.data.AbstractSimpleOWLProofGenerator
import de.tu_dresden.inf.lat.evee.proofs.interfaces.{IProof, ISimpleProofGenerator, IProofWriter}
import de.tu_dresden.inf.lat.evee.proofs.json.JsonProofWriter
import org.semanticweb.HermiT.ReasonerFactory

import scala.collection.JavaConverters._
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model.{IRI, OWLAxiom, OWLClass, OWLClassExpression, OWLOntology}
import org.semanticweb.owlapi.reasoner.Node
import uk.ac.man.cs.lethe.internal.dl.datatypes.OntologyFilter
import uk.ac.man.cs.lethe.internal.dl.filters.OWLOntologyFilters

import scala.io.Source

object GenerateAllProofs {

  val lastFile = "last"

  def printUsageAndExit() = {
    println("Usage: ")
    println("de.tu_dresden.inf.lat.evee.eliminationProofs.GenerateALLProofs ONTOLOGY_PATH")
    System.exit(1)
  }

  def main(args: Array[String]) = {

    if(args.length!=1)
      printUsageAndExit()

    val file = new File(args(0))

    if(!file.exists()){
      println("File "+file+" does not exist!")
      printUsageAndExit()
    }

    println("Generating all proofs for "+file)

    println("Parsing ontology...")
    val ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(file)

    println("Restricting to ALCH")
    OWLOntologyFilters.restrictToALCH(ontology)

    val proofGenerator = getProofGenerator(ontology)

    val eliminationProofs = new GenerateAllProofs(proofGenerator)

    if(new File(lastFile).exists()){
      val resource = Source.fromFile(lastFile)
      try{
        val lastSubsumption = Source.fromFile(lastFile).getLines.next
        eliminationProofs.continueAt = Some(lastSubsumption)
      } finally
        resource.close()
    }


    val proofWriter = new JsonProofWriter[OWLAxiom]()

    val proofsDir = new File(file.getName+" proofs")
    proofsDir.mkdir()

    eliminationProofs.generateProofs(ontology, proofWriter, proofsDir+"/")
  }

  private def getProofGenerator(ontology: OWLOntology): ISimpleProofGenerator[OWLClass, OWLAxiom,OWLOntology] = {
    val forgetter = LetheBasedForgetter.ALC_ABox()
    val justifier = OWLApiBasedJustifier.UsingHermiT(ontology.getOWLOntologyManager)

    val generator = new ForgettingBasedProofGenerator(forgetter,ALCHTBoxFilter, justifier)
    generator.setOntology(ontology)
    return generator
  }
}

class GenerateAllProofs(proofGenerator: ISimpleProofGenerator[OWLClass,OWLAxiom,OWLOntology], onlyDirectSubsumption: Boolean = false) {

  var continueAt: Option[String] = None

  def trivialProof(proof: IProof[OWLAxiom]) =
    proof.getInferences().isEmpty ||
      proof.getInferences(proof.getFinalConclusion).stream().allMatch(_.getPremises.isEmpty())

  def checkIgnore(currentString: String) = continueAt match {

      case None =>
        val writer = new PrintWriter(GenerateAllProofs.lastFile)
        try {
          writer.println(currentString)
        } finally
          writer.close()
        false

      case Some(thing) if thing.equals(currentString) =>
        continueAt = None
        false

      case Some(_) => true
  }

  def generateProofs(ontology: OWLOntology, exporter: IProofWriter[OWLAxiom], filenamePrefix: String) = {
    val reasoner = new ReasonerFactory().createReasoner(ontology)

    val factory = ontology.getOWLOntologyManager.getOWLDataFactory

    var toProcess = List(reasoner.getTopClassNode)
    var processed = Set[Node[OWLClass]]()

    while(!toProcess.isEmpty){
      val next = toProcess.head
      toProcess = toProcess.tail

      processed += next

      val concept = next.getRepresentativeElement

      // first prove equivalences on node
      next.getEntitiesMinus(concept)
        .asScala
        .foreach{ concept2 =>

          val axiom = factory.getOWLEquivalentClassesAxiom(concept, concept2)

          val currentString = format(concept) + " equivalentTo " + format(concept2)


          if(!checkIgnore(currentString) && !ontology.containsAxiom(
            factory.getOWLEquivalentClassesAxiom(concept, concept2))) {

            val proof = proofGenerator.proveEquivalence(concept, concept2)
            if(!trivialProof(proof))
              exporter.writeToFile(proof, filenamePrefix + currentString)

          }
        }

      // then prove subsumptions
      reasoner
        .getSubClasses(concept, onlyDirectSubsumption)
        .getNodes.forEach{ succ =>

          val subConcept = succ.getRepresentativeElement

          val axiom = factory.getOWLSubClassOfAxiom(subConcept, concept)

          val currentString = format(subConcept) + " subclassOf " + format(concept)

          if(!checkIgnore(currentString) && !concept.isTopEntity && !ontology.containsAxiom(
            factory.getOWLSubClassOfAxiom(subConcept, concept))) {

            val proof = proofGenerator.proveSubsumption(subConcept, concept)

            if(!trivialProof(proof)) {
              exporter.writeToFile(proof, filenamePrefix + currentString)
            }
          }

          if (!processed(succ))
            toProcess ::= succ
        }

    }

    def format(concept: OWLClass) =
      concept.getIRI.getFragment
  }


}
