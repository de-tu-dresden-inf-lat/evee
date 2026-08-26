package de.tu_dresden.inf.lat.evee.concreteDomains.elkCD

import de.tu_dresden.inf.lat.evee.concreteDomains.CDConstraint
import de.tu_dresden.inf.lat.evee.concreteDomains.CDReasoner
import de.tu_dresden.inf.lat.evee.concreteDomains.data.extendedDataStructure.ExtendedOntology

import de.tu_dresden.inf.lat.evee.proofGenerators.ELKProofGenerator
import de.tu_dresden.inf.lat.evee.proofs.tools.{MinimalHypergraphProofExtractor, MinimalProofExtractor}

import org.semanticweb.elk.owlapi.ElkReasonerFactory
import org.semanticweb.owlapi.model.parameters.Imports
import org.semanticweb.owlapi.model.{OWLClass, OWLEntity}
import org.semanticweb.owlapi.reasoner.{InconsistentOntologyException, InferenceType}

import com.clarkparsia.owlapi.explanation.DefaultExplanationGenerator
import com.clarkparsia.owlapi.explanation.util.SilentExplanationProgressMonitor

import java.io.{File, FileOutputStream}
import scala.collection.JavaConverters.{asScalaSetConverter, iterableAsScalaIterableConverter, setAsJavaSetConverter}
import scala.collection.mutable

case class Token(){
  private var time: Long = 0
  private var calls: Int = 0

  def getTime(): Long = time
  def addTime(t: Long) = time = time + t

  def getReasonerCalls(): Int = calls

  def addOneReasonerCall(): Unit = calls = calls + 1
}

trait Reasoner {
  def classify(): mutable.MultiMap[OWLEntity,OWLClass]
}

class ELKCDReasoner[+CD_CONSTRAINT <: CDConstraint](extendedOntology: ExtendedOntology[CD_CONSTRAINT],
                                                   cdReasoner: CDReasoner[CD_CONSTRAINT]) extends ReasonerWithStatistics {

  val factory = extendedOntology.ontology.getOWLOntologyManager.getOWLDataFactory()
  val manager = extendedOntology.ontology.getOWLOntologyManager

  val reasonerFactory = new ElkReasonerFactory()
  var owlReasoner = reasonerFactory.createReasoner(extendedOntology.ontology)

  var changed = true

  var addedInformation = Set[Set[OWLClass]]()


  override def classify(): mutable.MultiMap[OWLEntity, OWLClass] = {
    var startTime : Long = 0

    val t = Token()

    changed = true

    while(changed) {
      manager.saveOntology(extendedOntology.ontology, new FileOutputStream(new File("currentOntology.owl")))
      changed = false
      //println("NEW ROUND!")
      //println(SimpleOWLFormatter.format(extendedOntology.ontology))
      //println(extendedOntology.constraintNames)

      startTime = System.nanoTime()
      owlReasoner.flush()
      try {
        owlReasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY)
        owlReasoner.precomputeInferences(InferenceType.DISJOINT_CLASSES)
        owlReasoner.precomputeInferences(InferenceType.CLASS_ASSERTIONS)
      } catch {
        case exp: InconsistentOntologyException =>
          println()
          println("Ontology is inconsistent!")
          val explanation = explainInconsistency()
          explanation.foreach(println)
          System.exit(1)
      }
      t.addTime(System.nanoTime() - startTime)
      t.addOneReasonerCall()

      addRequiredCDAxioms(t)
    }
    //Store the time in ms
    collectStatistics( t.getTime() / 1000000, t.getReasonerCalls())
    createClassificationMap()
  }

  private def explainInconsistency() = {
    val axiom = factory.getOWLSubClassOfAxiom(
      factory.getOWLThing(),
      factory.getOWLNothing())

    val proofGenerator = new ELKProofGenerator()
    proofGenerator.setReasoner(owlReasoner)
    println(proofGenerator.supportsProof(axiom))
    val proof = proofGenerator.getProof(axiom)
    val min = MinimalHypergraphProofExtractor.makeUnique(proof)
    min.getInferences().asScala.filter(_.getRuleName.equals("Asserted Conclusion")).map(_.getConclusion)
  }

  def addRequiredCDAxioms(token: Token): Unit = {
    // extendedOntology.ontology.getClassesInSignature(Imports.INCLUDED).forEach{ owlClass =>
    extendedOntology.ontology.getNestedClassExpressions.forEach{ classExp =>
      //println("OWLClassExpression: " + classExp)

      val startTime = System.nanoTime()
      val superClasses = owlReasoner.getSuperClasses(classExp, false)
      token.addTime(System.nanoTime() - startTime)

      val subsumers = superClasses.getFlattened.asScala
      //println(subsumers)
      for (conjunct <- classExp.asConjunctSet().asScala) {
        conjunct match {
          case cls: OWLClass => subsumers.add(cls)
          case _ =>
        }
      }
      val relevant = subsumers.filter(extendedOntology.constraintNames).toSet
      //println("relevant names: " + relevant)
      if(!addedInformation.contains(relevant))
        addCDAxioms(relevant)
    }
    extendedOntology.ontology.getIndividualsInSignature().forEach{ individual =>
      val startTime = System.nanoTime()
      val classes = owlReasoner.getTypes(individual, false).getFlattened.asScala
      token.addTime(System.nanoTime() - startTime)

      val relevant = classes.filter(extendedOntology.constraintNames).toSet
      if(!addedInformation.contains(relevant))
        addCDAxioms(relevant)
    }
  }

  def addCDAxioms(relevant: Set[OWLClass]) = {
    //println("Adding axioms for: "+relevant)
    val predicates = relevant.map(extendedOntology.constraintFor)
    val lhs = relevant.size match {
      case 0 => factory.getOWLThing
      case 1 => relevant.head
      case _ => factory.getOWLObjectIntersectionOf(relevant.asJava)
    }
    if(!cdReasoner.consistent(predicates)) {
      val newAxiom =
        factory.getOWLSubClassOfAxiom(
          lhs,
          factory.getOWLNothing)
      //println(SimpleOWLFormatter.format(newAxiom))
      manager.addAxiom(
        extendedOntology.ontology,
        newAxiom
      )
      changed=true
    } else {
      var impliedNames = Set[OWLClass]()
      extendedOntology.constraintNames.filterNot(relevant).foreach{name2 =>
        if(cdReasoner.implies(predicates, extendedOntology.constraintFor(name2))) {
          impliedNames += name2
          val newAxiom =
            factory.getOWLSubClassOfAxiom(
              lhs,
              name2
            )
          //println(SimpleOWLFormatter.format(newAxiom))
          manager.addAxiom(
            extendedOntology.ontology,
            newAxiom
          )
          changed=true
        }
      }
      // We already added all necessary information about the following extended set since it does not imply any more names and is inconsistent iff `relevant` is consistent.
      addedInformation += (relevant ++ impliedNames)
    }

    addedInformation += (relevant)
  }

  private def createClassificationMap(): mutable.MultiMap[OWLEntity, OWLClass] = {
    val solutionMap = new mutable.HashMap[OWLEntity, mutable.Set[OWLClass]]() with mutable.MultiMap[OWLEntity,OWLClass]

    extendedOntology.ontology.getClassesInSignature(Imports.EXCLUDED).forEach{lhs =>
      owlReasoner.getSuperClasses(lhs, false).getFlattened.forEach(rhs =>
        solutionMap.addBinding(lhs,rhs)
      )
      owlReasoner.getEquivalentClasses(lhs).asScala/*.filterNot(_.equals(lhs))*/.foreach(solutionMap.addBinding(lhs,_))
      //if(!owlReasoner.isSatisfiable(lhs))
      //  solutionMap.addBinding(lhs, factory.getOWLNothing())
    }
    //println("empty: "+owlReasoner.getSubClasses(factory.getOWLNothing(),false))

    extendedOntology.ontology.getIndividualsInSignature().forEach{ individual =>
      owlReasoner.getTypes(individual, false).getFlattened.forEach(typ => solutionMap.addBinding(individual, typ))
    }

    return solutionMap
  }
}
