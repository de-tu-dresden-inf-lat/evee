package de.tu_dresden.inf.lat.evee.forgettingBasedProofs

import java.io.File

import de.tu_dresden.inf.lat.prettyPrinting.formatting.SimpleOWLFormatter
import de.tu_dresden.inf.lat.evee.proofs.interfaces.{IInference, IProof}
import org.semanticweb.owlapi.model.OWLAxiom
import guru.nidi.graphviz.attribute.Color
import guru.nidi.graphviz.attribute.Label
import guru.nidi.graphviz.attribute.Shape
import guru.nidi.graphviz.attribute.Style
import guru.nidi.graphviz.engine.Format
import guru.nidi.graphviz.engine.Graphviz
import guru.nidi.graphviz.model.MutableGraph
import guru.nidi.graphviz.model.MutableNode
import guru.nidi.graphviz.model.Factory

import scala.collection.immutable.HashSet
import scala.collection.JavaConverters._
import scala.collection.mutable

trait ProofDisplay[T] {
  def format(proof: IProof[T]): Unit
}

//class ProofDotVisualizer extends ProofDisplay {
//
//  private var fileName: String = _
//
//  def setFileName(fileName: String): Unit ={
//    this.fileName = fileName
//  }
//
//  override def format(proof: Proof): Unit = {
//
//    val expToolProof = ProofConverter.convertForExplicationTool(proof)
//
//    DotGraphGenerator.DrawProof(expToolProof, fileName)
//  }
//}

// TODO ideally extend generic using formatter Formatter[T]
object ProofFormatter extends ProofDisplay[OWLAxiom] {
  def format(proof: IProof[OWLAxiom]): Unit ={
    format(proof.getFinalConclusion, proof)
  }

  private def format(conclusion: OWLAxiom, proof: IProof[OWLAxiom]): Unit ={

    println(SimpleOWLFormatter.format(conclusion))

    proof.getInferences(conclusion).asScala.foreach{inference =>
      if(inference.getPremises.isEmpty){
        println(" was in the ontology.")
        println("\n")
      } else {
        println(" was shown using: ")
        println(inference.getPremises().asScala.map(SimpleOWLFormatter.format))
        println("\n")
        inference.getPremises().asScala.foreach(format(_, proof))
      }

    }
  }
}

object ProofTreeVisualiser {

  import guru.nidi.graphviz.model.Factory.to
  import scala.collection.JavaConversions._

  var edgeId = 0

  var nodeId = 0

  def drawProof(proof: IProof[OWLAxiom], fileName: String): Unit = {

    val graph = Factory.mutGraph(fileName).setDirected(true)

    fillGraph(graph, proof.getFinalConclusion, proof)

    Graphviz.fromGraph(graph).render(Format.PNG).toFile(new File(fileName))
  }

  private def fillGraph(g: MutableGraph, conclusion: OWLAxiom, proof: IProof[OWLAxiom])
  : MutableNode = {


    val hyperConnection = Factory.mutNode("edge"+edgeId).add(Label.of(""),
      Style.FILLED, Color.rgb(0, 191, 255), Shape.RECTANGLE)
    edgeId += 1

    val conclusionNode = Factory.mutNode("node"+nodeId.toString).add(Label.of(SimpleOWLFormatter.format(conclusion))).add(Shape.RECTANGLE)
    nodeId+=1

    g.add(hyperConnection.addLink(to(conclusionNode)))

    // g.add(hyperConnection.addLink(to(Factory.mutNode(conclusion.toString).add(Label.of(SimpleOWLFormatter.format(conclusion))))))

    for (inf <- proof.getInferences(conclusion))
      for (prem <-  inf.getPremises) {

        val premiseNode = fillGraph(g,prem,proof)

        g.add(premiseNode.addLink(hyperConnection));
        //g.add(Factory.mutNode(prem.toString).add(Label.of(SimpleOWLFormatter.format(prem))).addLink(hyperConnection))
      }

    conclusionNode
  }


}

object ProofGraphVisualiser {

  import guru.nidi.graphviz.model.Factory.to
  import scala.collection.JavaConversions._

  var edgeId = 0


  var processedNodes: mutable.Set[OWLAxiom] = _

  var nodeMap: mutable.Map[OWLAxiom, MutableNode] = _

  def drawProof(proof: IProof[OWLAxiom], fileName: String): Unit = {

    val graph = Factory.mutGraph(fileName).setDirected(true)

    processedNodes = new mutable.HashSet[OWLAxiom]()
    nodeMap = new mutable.HashMap[OWLAxiom, MutableNode]()

    fillGraph(graph, proof.getFinalConclusion, proof)

    Graphviz.fromGraph(graph).render(Format.PNG).toFile(new File(fileName))
  }

  private def fillGraph(g: MutableGraph, conclusion: OWLAxiom, proof: IProof[OWLAxiom]): MutableNode = {

    if(processedNodes(conclusion))
      return nodeMap(conclusion)

    processedNodes.add(conclusion)

    val conclusionNode = Factory.mutNode(SimpleOWLFormatter.format(conclusion))

    nodeMap.put(conclusion, conclusionNode)


    // g.add(hyperConnection.addLink(to(Factory.mutNode(conclusion.toString).add(Label.of(SimpleOWLFormatter.format(conclusion))))))
    for (inf <- proof.getInferences(conclusion)) {
      val hyperConnection = Factory.mutNode(String.valueOf(edgeId)).add(Label.of(inf.getRuleName),
        Style.FILLED, Color.rgb(0, 191, 255), Shape.RECTANGLE)
      edgeId += 1
      g.add(hyperConnection.addLink(to(conclusionNode)))


      for (prem <- inf.getPremises) {

        val premiseNode = fillGraph(g, prem, proof)

        g.add(premiseNode.addLink(hyperConnection));
        //g.add(Factory.mutNode(prem.toString).add(Label.of(SimpleOWLFormatter.format(prem))).addLink(hyperConnection))
      }
    }
    conclusionNode
  }


}
