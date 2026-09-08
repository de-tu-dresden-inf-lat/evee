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
import org.semanticweb.owlapi.reasoner.Node


import com.clarkparsia.owlapi.explanation.DefaultExplanationGenerator
import com.clarkparsia.owlapi.explanation.util.SilentExplanationProgressMonitor

import java.io.{File, FileOutputStream}
import scala.collection.JavaConverters._
import scala.collection.mutable.MultiMap
import scala.collection.mutable
import org.semanticweb.owlapi.model.AddAxiom
import org.semanticweb.owlapi.model.OWLClassExpression
import org.semanticweb.owlapi.model.AxiomType
import org.semanticweb.owlapi.model.OWLAxiom
import org.semanticweb.owlapi.reasoner.NodeSet
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression
import org.semanticweb.owlapi.model.OWLNamedIndividual
import org.semanticweb.owlapi.reasoner.FreshEntityPolicy
import org.semanticweb.owlapi.reasoner.IndividualNodeSetPolicy
import org.semanticweb.owlapi.reasoner.OWLReasoner
import org.semanticweb.owlapi.model.OWLOntology
import org.semanticweb.owlapi.util.Version
import org.semanticweb.owlapi.model.{OWLDataProperty, OWLDataPropertyExpression}
import java.{util => ju}
import org.semanticweb.owlapi.model.{OWLDataProperty, OWLLiteral}
import org.semanticweb.owlapi.reasoner.BufferingMode
import org.semanticweb.owlapi.model.OWLDataProperty
import org.semanticweb.owlapi.model.OWLOntologyChange
import de.tu_dresden.inf.lat.evee.concreteDomains.diffDomain.DiffConstraint
import de.tu_dresden.inf.lat.evee.concreteDomains.data.extendedDataStructure.parser.ExtendedOntologyParser
import de.tu_dresden.inf.lat.evee.concreteDomains.data.names.ConcreteDomainName
import de.tu_dresden.inf.lat.evee.concreteDomains.diffDomain.DiffReasoner
import de.tu_dresden.inf.lat.evee.concreteDomains.multDomain.MultReasoner
import de.tu_dresden.inf.lat.evee.concreteDomains.linearDomain.LinearConstraintReasoner
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model.parameters.OntologyCopy
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration
import org.semanticweb.owlapi.model.RemoveAxiom

case class Token(){
  private var time: Long = 0
  private var calls: Int = 0

  def getTime(): Long = time
  def addTime(t: Long) = time = time + t

  def getReasonerCalls(): Int = calls

  def addOneReasonerCall(): Unit = calls = calls + 1
}

case class ClassificationState(
  computed: Boolean = false,
  consistent: Boolean = false,
)

case class AddCDAxiomsResult(
  ontologyChanged: Boolean,
  addedInformation: Set[Set[OWLClass]]
)
class ELKCDReasoner[CD_CONSTRAINT <: CDConstraint](rootOntology: OWLOntology,
                                                    domain: ConcreteDomainName,
                                                    cdReasoner: CDReasoner[CD_CONSTRAINT],
                                                    config: OWLReasonerConfiguration,
                                                    bufferingMode: Boolean)
                                                    extends ReasonerWithStatistics  
                                                    with OWLReasoner {

  private val REASONER_NAME = "ELK-CD"

  val manager =  OWLManager.createOWLOntologyManager();
  val factory = manager.getOWLDataFactory()
  val reasonerFactory = new ElkReasonerFactory()
  val pendingChangesBuffer = mutable.ListBuffer[OWLOntologyChange]()

  var mutableOntology = manager.copyOntology(rootOntology, OntologyCopy.DEEP);
  var extendedOntology = ExtendedOntologyParser.toExtendedOntology(domain, mutableOntology).asInstanceOf[ExtendedOntology[CD_CONSTRAINT]]
  var internalReasoner = reasonerFactory.createReasoner(extendedOntology.ontology, config)
  var state = ClassificationState()

  rootOntology.getOWLOntologyManager().addOntologyChangeListener(changes => {
    pendingChangesBuffer ++= changes.asScala.filter(change => change.getOntology() == rootOntology)
    if (!bufferingMode) {
      flush()
    }
  })

  
  override def classify() = {
    val token = Token()
    
    var alreadyAdded: Set[Set[OWLClass]] = Set.empty
    var changed = true

    while(changed) {
      val startTime = System.nanoTime()
      internalReasoner.flush()
      try {
        internalReasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY)
        internalReasoner.precomputeInferences(InferenceType.DISJOINT_CLASSES)
        internalReasoner.precomputeInferences(InferenceType.CLASS_ASSERTIONS)
      } catch {
        case exp: InconsistentOntologyException =>
          println()
          println("Ontology is inconsistent!")
          val explanation = explainInconsistency()
          explanation.foreach(println)
          System.exit(1) //TODO: not good 
      }
      token.addTime(System.nanoTime() - startTime)
      token.addOneReasonerCall()

      val result = addRequiredCDAxioms(alreadyAdded, token)
      alreadyAdded ++= result.addedInformation 
      changed ||= result.ontologyChanged
    }
    //Store the time in ms
    collectStatistics( token.getTime() / 1000000, token.getReasonerCalls())

    internalReasoner.flush()
    state = state.copy(true, internalReasoner.isConsistent)
  }

  private def explainInconsistency() = {
    val axiom = factory.getOWLSubClassOfAxiom(
      factory.getOWLThing(),
      factory.getOWLNothing())

    val proofGenerator = new ELKProofGenerator()
    proofGenerator.setReasoner(internalReasoner)
    println(proofGenerator.supportsProof(axiom))
    val proof = proofGenerator.getProof(axiom)
    val min = MinimalHypergraphProofExtractor.makeUnique(proof)
    min.getInferences().asScala.filter(_.getRuleName.equals("Asserted Conclusion")).map(_.getConclusion)
  }

  private def addRequiredCDAxioms(alreadyAdded: Set[Set[OWLClass]], token: Token): AddCDAxiomsResult = {
    
    var changed = false
    var addedInformation = Set.empty[Set[OWLClass]]

    extendedOntology.ontology.getNestedClassExpressions.forEach{ classExp =>
      val startTime = System.nanoTime()
      val superClasses = internalReasoner.getSuperClasses(classExp, false)
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
      if(!alreadyAdded.contains(relevant)){
        val result = addCDAxioms(relevant)
        changed ||= result.ontologyChanged
        addedInformation ++= result.addedInformation
      }
    }

    extendedOntology.ontology.getIndividualsInSignature().forEach{ individual =>
      val startTime = System.nanoTime()
      val classes = internalReasoner.getTypes(individual, false).getFlattened.asScala
      token.addTime(System.nanoTime() - startTime)

      val relevant = classes.filter(extendedOntology.constraintNames).toSet
      if(!alreadyAdded.contains(relevant)){
        val result = addCDAxioms(relevant)
        changed ||= result.ontologyChanged
        addedInformation ++= result.addedInformation
      }
    }

    return AddCDAxiomsResult(changed, addedInformation)
  }

  private def addCDAxioms(relevant: Set[OWLClass]): AddCDAxiomsResult = {
    //println("Adding axioms for: "+relevant)
    val predicates = relevant.map(extendedOntology.constraintFor)
    val lhs = relevant.size match {
      case 0 => factory.getOWLThing
      case 1 => relevant.head
      case _ => factory.getOWLObjectIntersectionOf(relevant.asJava)
    }

    if(!cdReasoner.consistent(predicates)){ {
      val newAxiom = factory.getOWLSubClassOfAxiom(lhs, factory.getOWLNothing)
      manager.addAxiom(extendedOntology.ontology, newAxiom)

      return AddCDAxiomsResult(ontologyChanged = true, addedInformation = Set(relevant))
      } 
    }
   

    val impliedNames: Set[OWLClass] = extendedOntology.constraintNames.filterNot(relevant).filter{owlClass =>
                            cdReasoner.implies(predicates, extendedOntology.constraintFor(owlClass))}

    impliedNames.foreach{ owlClass =>
        var newAxiom = factory.getOWLSubClassOfAxiom(lhs, owlClass)
        manager.addAxiom(extendedOntology.ontology, newAxiom)
      }
      
    
    // We already added all necessary information about the following extended set
    // since it does not imply any more names and is inconsistent iff `relevant` is consistent.
    return AddCDAxiomsResult(ontologyChanged = (impliedNames.size > 0), addedInformation = Set(relevant ++ impliedNames))
  }

///////////////////////////////////////////////////OWLReasoner methods ///////////////////////////////////////

//////////// config /////////////////////
  override def getReasonerName(): String = return REASONER_NAME

  override def getReasonerVersion(): Version =  return new Version(1, 0, 0, 0); //TODO

  override def getBufferingMode(): BufferingMode = if (bufferingMode) BufferingMode.BUFFERING else BufferingMode.NON_BUFFERING

  override def getRootOntology(): OWLOntology = return rootOntology

  override def getTimeOut(): Long = return 0 //no timeout

  override  def getFreshEntityPolicy(): FreshEntityPolicy = {
    return internalReasoner.getFreshEntityPolicy()
  }

  override def getIndividualNodeSetPolicy(): IndividualNodeSetPolicy = {
    return internalReasoner.getIndividualNodeSetPolicy()
  }

  /////////////// reasoning /////////////////////

  override def isConsistent(): Boolean = {
    if(!state.computed) classify()
    return state.consistent
  }

  override def isEntailed(axiom: OWLAxiom): Boolean = {
    if(!state.computed) classify()
    return internalReasoner.isEntailed(axiom)
  }

  override def isEntailed(axioms: ju.Set[_ <: OWLAxiom]): Boolean = {
    if(!state.computed) classify()
    return internalReasoner.isEntailed(axioms)
  }

  override def isEntailmentCheckingSupported(axiomType: AxiomType[_]): Boolean = {
    if(!state.computed) classify()
    return internalReasoner.isEntailmentCheckingSupported(axiomType)
  }

  override def getUnsatisfiableClasses():Node[OWLClass] = {
    if(!state.computed) classify()
    return internalReasoner.getUnsatisfiableClasses()
  }

  override def isSatisfiable(cls: OWLClassExpression): Boolean = {
    if(!state.computed) classify()
    return internalReasoner.isSatisfiable(cls)
  }

  override def getTopClassNode(): Node[OWLClass] = {
    if(!state.computed) classify()
    return internalReasoner.getTopClassNode()
  }

  override def getBottomClassNode(): Node[OWLClass] = {
    if(!state.computed) classify()
    return internalReasoner.getBottomClassNode()
  }

  override def getSubClasses(ce: OWLClassExpression, direct: Boolean): NodeSet[OWLClass] = {
    if(!state.computed) classify()
    return internalReasoner.getSubClasses(ce, direct)
  }

  override def getSuperClasses(ce: OWLClassExpression, direct: Boolean): NodeSet[OWLClass] = {
    if(!state.computed) classify()
    return internalReasoner.getSuperClasses(ce, direct)
  }

  override def getEquivalentClasses(ce: OWLClassExpression): Node[OWLClass] = {
    if(!state.computed) classify()
    return internalReasoner.getEquivalentClasses(ce)
  }
  
  override def getTopObjectPropertyNode(): Node[OWLObjectPropertyExpression] = {
    if(!state.computed) classify()
    return internalReasoner.getTopObjectPropertyNode()
  }

  override def getBottomObjectPropertyNode(): Node[OWLObjectPropertyExpression] = {
    if(!state.computed) classify()
    return internalReasoner.getBottomObjectPropertyNode()
  }

  override def getSubObjectProperties(pe: OWLObjectPropertyExpression, direct: Boolean): NodeSet[OWLObjectPropertyExpression] = {
    if(!state.computed) classify()
    return internalReasoner.getSubObjectProperties(pe, direct)
  }

  override def getSuperObjectProperties(pe: OWLObjectPropertyExpression, direct: Boolean): NodeSet[OWLObjectPropertyExpression] = {
    if(!state.computed) classify()
    return internalReasoner.getSuperObjectProperties(pe, direct)
  }

  override def getEquivalentObjectProperties(pe: OWLObjectPropertyExpression): Node[OWLObjectPropertyExpression] = {
    if(!state.computed) classify()
    return internalReasoner.getEquivalentObjectProperties(pe)
  }

  override def getTypes(ind: OWLNamedIndividual, direct: Boolean): NodeSet[OWLClass] = {
    if(!state.computed) classify()
    return internalReasoner.getTypes(ind, direct)
  }
  
  override def getInstances(ce: OWLClassExpression, direct: Boolean): NodeSet[OWLNamedIndividual] = {
    if(!state.computed) classify()
    return internalReasoner.getInstances(ce, direct)
  }

  override def getSameIndividuals(ind: OWLNamedIndividual): Node[OWLNamedIndividual] = {
    if(!state.computed) classify()
    return internalReasoner.getSameIndividuals(ind)
  }


  ///////////// unsupported reasoning methods //////////////////////

 override def getDisjointClasses(ce: OWLClassExpression): NodeSet[OWLClass] = throw new UnsupportedOperationException("unsupported method 'getDisjointClasses'")

  override def getDisjointObjectProperties(pe: OWLObjectPropertyExpression): NodeSet[OWLObjectPropertyExpression] = throw new UnsupportedOperationException("unsupported method 'getDisjointObjectProperties'")

  override def getInverseObjectProperties(pe: OWLObjectPropertyExpression): Node[OWLObjectPropertyExpression] = throw new UnsupportedOperationException("unsupported method 'getInverseObjectProperties'")

  override def getObjectPropertyDomains(pe: OWLObjectPropertyExpression, direct: Boolean): NodeSet[OWLClass] = throw new UnsupportedOperationException("unsupported method 'getObjectPropertyDomains'")

  override def getObjectPropertyRanges(pe: OWLObjectPropertyExpression, direct: Boolean): NodeSet[OWLClass] = throw new UnsupportedOperationException("unsupported method 'getObjectPropertyRanges'")

  override def getTopDataPropertyNode(): Node[OWLDataProperty] = throw new UnsupportedOperationException("unsupported method 'getTopDataPropertyNode'")

  override def getBottomDataPropertyNode(): Node[OWLDataProperty] = throw new UnsupportedOperationException("unsupported method 'getBottomDataPropertyNode'")

  override def getSubDataProperties(pe: OWLDataProperty, direct: Boolean): NodeSet[OWLDataProperty] = throw new UnsupportedOperationException("unsupported method 'getSubDataProperties'")

  override def getSuperDataProperties(pe: OWLDataProperty, direct: Boolean): NodeSet[OWLDataProperty] = throw new UnsupportedOperationException("unsupported method 'getSuperDataProperties'")

  override def getEquivalentDataProperties(pe: OWLDataProperty): Node[OWLDataProperty] = throw new UnsupportedOperationException("unsupported method 'getEquivalentDataProperties'")

  override def getDisjointDataProperties(pe: OWLDataPropertyExpression): NodeSet[OWLDataProperty] = throw new UnsupportedOperationException("unsupported method 'getDisjointDataProperties'")

  override def getDataPropertyDomains(pe: OWLDataProperty, direct: Boolean): NodeSet[OWLClass] = throw new UnsupportedOperationException("unsupported method 'getDataPropertyDomains'")

  override def getObjectPropertyValues(ind: OWLNamedIndividual, pe: OWLObjectPropertyExpression): NodeSet[OWLNamedIndividual] = throw new UnsupportedOperationException("unsupported method 'getObjectPropertyValues'")

  override def getDataPropertyValues(ind: OWLNamedIndividual, pe: OWLDataProperty): ju.Set[OWLLiteral] = throw new UnsupportedOperationException("unsupported method 'getDataPropertyValues'")

  override def getDifferentIndividuals(ind: OWLNamedIndividual): NodeSet[OWLNamedIndividual] = throw new UnsupportedOperationException("unsupported method 'getDifferentIndividuals'")


  ///// precomputation ////////

  override def precomputeInferences(inferenceTypes: InferenceType*): Unit = {
    if(!state.computed) classify()
    internalReasoner.precomputeInferences(inferenceTypes: _*)
  }

  override def isPrecomputed(inferenceType: InferenceType): Boolean = {
    if(!state.computed) classify()
    return internalReasoner.isPrecomputed(inferenceType)
  }

  override def getPrecomputableInferenceTypes(): java.util.Set[InferenceType] = {
    return internalReasoner.getPrecomputableInferenceTypes()
  }

  ////////// lifecycle ////////
  
  override def interrupt() = {
    internalReasoner.interrupt()
  }

  override def dispose() = {
    internalReasoner.dispose()
  }

  override def flush(): Unit = {
    mutableOntology = manager.copyOntology(rootOntology, OntologyCopy.DEEP);
    extendedOntology = ExtendedOntologyParser.toExtendedOntology(domain, mutableOntology).asInstanceOf[ExtendedOntology[CD_CONSTRAINT]]
    internalReasoner = reasonerFactory.createReasoner(extendedOntology.ontology, config)
    state = ClassificationState()
  }

  override def getPendingChanges(): ju.List[OWLOntologyChange] = {
    return new ju.ArrayList[OWLOntologyChange](pendingChangesBuffer.asJava) //return copy
  }

  override def getPendingAxiomAdditions(): ju.Set[OWLAxiom] = {
    pendingChangesBuffer.filter(change => change.isInstanceOf[AddAxiom])
                        .map(change => change.asInstanceOf[AddAxiom].getAxiom()).toSet.asJava
  }

  override def getPendingAxiomRemovals(): ju.Set[OWLAxiom] = {
    pendingChangesBuffer.filter(change => change.isInstanceOf[RemoveAxiom])
                        .map(change => change.asInstanceOf[RemoveAxiom].getAxiom()).toSet.asJava
  }
}