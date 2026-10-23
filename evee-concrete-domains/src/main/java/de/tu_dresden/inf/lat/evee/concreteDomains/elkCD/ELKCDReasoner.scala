package de.tu_dresden.inf.lat.evee.concreteDomains.elkCD

import de.tu_dresden.inf.lat.evee.concreteDomains.CDConstraint
import de.tu_dresden.inf.lat.evee.concreteDomains.CDReasoner
import de.tu_dresden.inf.lat.evee.concreteDomains.data.extendedDataStructure.ExtendedOntology

import org.semanticweb.elk.owlapi.ElkReasonerFactory
import org.semanticweb.owlapi.model.parameters.Imports
import org.semanticweb.owlapi.model.{OWLClass, OWLEntity}
import org.semanticweb.owlapi.reasoner.InferenceType
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
import org.semanticweb.owlapi.reasoner.impl.OWLClassNodeSet
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression
import org.semanticweb.owlapi.model.OWLNamedIndividual
import org.semanticweb.owlapi.reasoner.FreshEntityPolicy
import org.semanticweb.owlapi.reasoner.IndividualNodeSetPolicy
import org.semanticweb.owlapi.reasoner.OWLReasoner
import org.semanticweb.owlapi.model.{OWLObject, OWLOntology}
import org.semanticweb.owlapi.util.Version
import org.semanticweb.owlapi.model.{OWLDataProperty, OWLDataPropertyExpression}
import java.{util => ju}
import org.semanticweb.owlapi.model.{OWLDataProperty, OWLLiteral}
import org.semanticweb.owlapi.reasoner.BufferingMode
import org.semanticweb.owlapi.model.OWLDataProperty
import org.semanticweb.owlapi.model.{OWLOntologyChange, OWLOntologyChangeListener}
import de.tu_dresden.inf.lat.evee.concreteDomains.diffDomain.DiffConstraint
import de.tu_dresden.inf.lat.evee.concreteDomains.data.extendedDataStructure.parser.ExtendedOntologyParser
import de.tu_dresden.inf.lat.evee.concreteDomains.data.names.ConcreteDomainName

import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration
import org.semanticweb.owlapi.model.RemoveAxiom
import org.slf4j.LoggerFactory

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

class ELKCDReasonerEmptyDomain(
    rootOntology: OWLOntology,
    config: OWLReasonerConfiguration,
    bufferingMode: Boolean
) extends ELKCDReasoner[Nothing](
      rootOntology,
      null,
      null,
      config,
      bufferingMode
) 
class ELKCDReasoner[CD_CONSTRAINT <: CDConstraint](rootOntology: OWLOntology,
                                                    domain: ConcreteDomainName,
                                                    cdReasoner: CDReasoner[CD_CONSTRAINT],
                                                    config: OWLReasonerConfiguration,
                                                    bufferingMode: Boolean)
                                                    extends ReasonerWithStatistics  
                                                    with OWLReasoner {

  private val REASONER_NAME = "ELK-CD"
  
  private val logger = LoggerFactory.getLogger(classOf[ELKCDReasoner[CD_CONSTRAINT]])

  val factory = OWLManager.getOWLDataFactory()
  val reasonerFactory = new ElkReasonerFactory()

  /** the changes of the root ontology and its imports that the reasoner has not taken into account yet */
  private val pendingChangesBuffer = mutable.ListBuffer[OWLOntologyChange]()

  /**
   * An internal copy of the axioms of the root ontology and its imports, with the constraints of its classes. The
   * reasoner adds the axioms that follow from the concrete domain to this copy, never to the root ontology.
   */
  var extendedOntology: ExtendedOntology[CD_CONSTRAINT] = _
  var internalReasoner: OWLReasoner = _
  var state = ClassificationState()

  /** the sets of constraint classes whose consequences have been added to the internal ontology */
  private var handledConstraintSets: Set[Set[OWLClass]] = Set.empty

  /** the class expressions for which all consequences of the concrete domain are in the internal ontology */
  private val checkedExpressions = mutable.Set[OWLClassExpression]()

  /** the axioms that were added to the internal ontology because of the concrete domain */
  private val addedAxioms = mutable.Set[OWLAxiom]()

  load()

  private val changeListener = new OWLOntologyChangeListener {
    override def ontologiesChanged(changes: ju.List[_ <: OWLOntologyChange]): Unit = {
      val ontologies = rootOntology.getImportsClosure
      pendingChangesBuffer ++= changes.asScala.filter(change => ontologies.contains(change.getOntology))
    }
  }
  rootOntology.getOWLOntologyManager.addOntologyChangeListener(changeListener)

  /**
   * Copies the current axioms of the root ontology and its imports into a new internal ontology, in a new manager, and
   * reads the constraints of their classes.
   */
  private def load(): Unit = {
    val ontology = OWLManager.createOWLOntologyManager().createOntology()
    rootOntology.getImportsClosure.forEach(imported =>
      imported.getAxioms().forEach(axiom => ontology.getOWLOntologyManager.addAxiom(ontology, axiom)))
    val constraints = ExtendedOntologyParser.toConstraintMap(domain, rootOntology)
    extendedOntology = ExtendedOntology(ontology, domain, constraints).asInstanceOf[ExtendedOntology[CD_CONSTRAINT]]
    internalReasoner = reasonerFactory.createReasoner(ontology, config)
    state = ClassificationState()
    handledConstraintSets = Set.empty
    checkedExpressions.clear()
    addedAxioms.clear()
  }

  /**
   * Takes pending changes into account in non-buffering mode and classifies if necessary. Then adds the consequences
   * of the concrete domain for the class expressions in the query that have not been checked yet, e.g., because they
   * do not occur in the ontology.
   */
  private def prepare(query: OWLObject*): Unit = synchronized {
    if (!bufferingMode)
      flush()
    if (!state.computed)
      classify()
    if (state.consistent) {
      val unchecked = query.flatMap(_.getNestedClassExpressions.asScala).filterNot(checkedExpressions).toSet
      if (unchecked.nonEmpty)
        addConsequences(unchecked, Token())
    }
  }
  
  /**
   * Adds the axioms that follow from the concrete domain for the class expressions of the ontology, its constraint
   * classes and its individuals to the internal ontology. If the ontology is inconsistent, the internal reasoner
   * answers queries like ELK does for inconsistent ontologies, i.e., most of them throw an
   * InconsistentOntologyException.
   */
  override def classify() = {
    val token = Token()

    logger.info("Starting classification with ELK-CD reasoner...")

    val expressions = extendedOntology.ontology.getNestedClassExpressions.asScala.toSet ++ extendedOntology.constraintNames
    val consistent = addConsequences(expressions, token, includingIndividuals = true)

    //Store the time in ms
    collectStatistics( token.getTime() / 1000000, token.getReasonerCalls())

    logger.warn("end of classify(). added: "+ handledConstraintSets) //debug log

    state = ClassificationState(computed = true, consistent = consistent)
  }

  /**
   * Adds the axioms that follow from the concrete domain for the given class expressions (and the individuals) to the
   * internal ontology until nothing changes. Stops as soon as the internal ontology is inconsistent.
   *
   * @return whether the internal ontology is consistent
   */
  private def addConsequences(expressions: Set[OWLClassExpression], token: Token,
                              includingIndividuals: Boolean = false): Boolean = {
    var changed = true
    var consistent = true

    while(changed && consistent) {
      val startTime = System.nanoTime()
      internalReasoner.flush()
      consistent = internalReasoner.isConsistent
      token.addTime(System.nanoTime() - startTime)
      token.addOneReasonerCall()

      if (consistent)
        changed = addRequiredCDAxioms(expressions, includingIndividuals, token)
    }
    if (consistent)
      checkedExpressions ++= expressions
    consistent
  }

  /** @return whether axioms were added */
  private def addRequiredCDAxioms(expressions: Set[OWLClassExpression], includingIndividuals: Boolean,
                                  token: Token): Boolean = {

    if (cdReasoner == null) { //using only ELK to reason, no concrete domain
        return false
    }

    var changed = false

    def addCDAxiomsFor(relevant: Set[OWLClass]): Unit =
      if (!handledConstraintSets.contains(relevant)) {
        val result = addCDAxioms(relevant)
        changed = changed || result.ontologyChanged
        handledConstraintSets ++= result.addedInformation
      }

    // an unsatisfiable class expression has no further consequences, but ELK reports all classes as its subsumers
    expressions.filter(internalReasoner.isSatisfiable).foreach{ classExp =>
      val startTime = System.nanoTime()
      val superClasses = allSuperClasses(classExp)
      token.addTime(System.nanoTime() - startTime)

      val subsumers = superClasses.getFlattened.asScala
      //println(subsumers)
      for (conjunct <- classExp.asConjunctSet().asScala) {
        conjunct match {
          case cls: OWLClass => subsumers.add(cls)
          case _ =>
        }
      }
      addCDAxiomsFor(subsumers.filter(extendedOntology.constraintNames).toSet)
    }

    if (includingIndividuals)
      extendedOntology.ontology.getIndividualsInSignature().forEach{ individual =>
        val startTime = System.nanoTime()
        val classes = internalReasoner.getTypes(individual, false).getFlattened.asScala
        token.addTime(System.nanoTime() - startTime)

        addCDAxiomsFor(classes.filter(extendedOntology.constraintNames).toSet)
      }

    changed
  }

  /**
   * All superclasses of the class expression. ELK 0.6.0 omits the direct superclasses of a complex class expression
   * from all its superclasses, so they are added here.
   */
  private def allSuperClasses(ce: OWLClassExpression): NodeSet[OWLClass] =
    if (!ce.isAnonymous)
      internalReasoner.getSuperClasses(ce, false)
    else {
      val result = new OWLClassNodeSet()
      result.addAllNodes(internalReasoner.getSuperClasses(ce, true).getNodes)
      result.addAllNodes(internalReasoner.getSuperClasses(ce, false).getNodes)
      result
    }

  /** adds the axiom to the internal ontology and records it, unless it is already there, e.g., asserted by the user */
  private def addConcreteDomainAxiom(axiom: OWLAxiom): Unit = {
    val ontology = extendedOntology.ontology
    if (!ontology.containsAxiom(axiom)) {
      ontology.getOWLOntologyManager.addAxiom(ontology, axiom)
      addedAxioms += axiom
    }
  }

  /**
   * The axioms that the reasoner added to its internal ontology because of the concrete domain. All other axioms of
   * the internal ontology are axioms of the root ontology or its imports, as of the last flush.
   */
  def concreteDomainAxioms: Set[OWLAxiom] = synchronized { addedAxioms.toSet }

  private def addCDAxioms(relevant: Set[OWLClass]): AddCDAxiomsResult = {
    //println("Adding axioms for: "+relevant)
    val predicates = relevant.map(extendedOntology.constraintFor)
    val lhs = relevant.size match {
      case 0 => factory.getOWLThing
      case 1 => relevant.head
      case _ => factory.getOWLObjectIntersectionOf(relevant.toArray[OWLClassExpression]: _*)
    }

    if(!cdReasoner.consistent(predicates)){ 

     // logger.warn("not consistent: " + predicates) //debug log
     // logger.warn("adding axiom: " + lhs + " ⊑ ⊥") //debug log

      addConcreteDomainAxiom(factory.getOWLSubClassOfAxiom(lhs, factory.getOWLNothing))

      return AddCDAxiomsResult(ontologyChanged = true, addedInformation = Set(relevant))
    }

    val impliedNames: Set[OWLClass] = extendedOntology.constraintNames.filterNot(relevant).filter{owlClass =>
                            cdReasoner.implies(predicates, extendedOntology.constraintFor(owlClass))}

    impliedNames.foreach{ owlClass =>
        val newAxiom = factory.getOWLSubClassOfAxiom(lhs, owlClass)
        addConcreteDomainAxiom(newAxiom)

        logger.warn("adding "+ newAxiom) //debug log
      }
    
    // We already added all necessary information about the following extended set
    // since it does not imply any more names and is inconsistent iff `relevant` is consistent.
    return AddCDAxiomsResult(ontologyChanged = (impliedNames.size > 0), addedInformation = Set(relevant ++ impliedNames, relevant))
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
    prepare()
    return state.consistent
  }

  override def isEntailed(axiom: OWLAxiom): Boolean = {
    prepare(axiom)
    return internalReasoner.isEntailed(axiom)
  }

  override def isEntailed(axioms: ju.Set[_ <: OWLAxiom]): Boolean = {
    prepare(axioms.asScala.toSeq: _*)
    return internalReasoner.isEntailed(axioms)
  }

  override def isEntailmentCheckingSupported(axiomType: AxiomType[_]): Boolean = {
    prepare()
    return internalReasoner.isEntailmentCheckingSupported(axiomType)
  }

  override def getUnsatisfiableClasses():Node[OWLClass] = {
    prepare()
    return internalReasoner.getUnsatisfiableClasses()
  }

  override def isSatisfiable(cls: OWLClassExpression): Boolean = {
    prepare(cls)
    return internalReasoner.isSatisfiable(cls)
  }

  override def getTopClassNode(): Node[OWLClass] = {
    prepare()
    return internalReasoner.getTopClassNode()
  }

  override def getBottomClassNode(): Node[OWLClass] = {
    prepare()
    return internalReasoner.getBottomClassNode()
  }

  override def getSubClasses(ce: OWLClassExpression, direct: Boolean): NodeSet[OWLClass] = {
    prepare(ce)
    return internalReasoner.getSubClasses(ce, direct)
  }

  override def getSuperClasses(ce: OWLClassExpression, direct: Boolean): NodeSet[OWLClass] = {
    prepare(ce)
    return if (direct) internalReasoner.getSuperClasses(ce, true) else allSuperClasses(ce)
  }

  override def getEquivalentClasses(ce: OWLClassExpression): Node[OWLClass] = {
    prepare(ce)
    return internalReasoner.getEquivalentClasses(ce)
  }
  
  override def getTopObjectPropertyNode(): Node[OWLObjectPropertyExpression] = {
    prepare()
    return internalReasoner.getTopObjectPropertyNode()
  }

  override def getBottomObjectPropertyNode(): Node[OWLObjectPropertyExpression] = {
    prepare()
    return internalReasoner.getBottomObjectPropertyNode()
  }

  override def getSubObjectProperties(pe: OWLObjectPropertyExpression, direct: Boolean): NodeSet[OWLObjectPropertyExpression] = {
    prepare()
    return internalReasoner.getSubObjectProperties(pe, direct)
  }

  override def getSuperObjectProperties(pe: OWLObjectPropertyExpression, direct: Boolean): NodeSet[OWLObjectPropertyExpression] = {
    prepare()
    return internalReasoner.getSuperObjectProperties(pe, direct)
  }

  override def getEquivalentObjectProperties(pe: OWLObjectPropertyExpression): Node[OWLObjectPropertyExpression] = {
    prepare()
    return internalReasoner.getEquivalentObjectProperties(pe)
  }

  override def getTypes(ind: OWLNamedIndividual, direct: Boolean): NodeSet[OWLClass] = {
    prepare()
    return internalReasoner.getTypes(ind, direct)
  }
  
  override def getInstances(ce: OWLClassExpression, direct: Boolean): NodeSet[OWLNamedIndividual] = {
    prepare(ce)
    return internalReasoner.getInstances(ce, direct)
  }

  override def getSameIndividuals(ind: OWLNamedIndividual): Node[OWLNamedIndividual] = {
    prepare()
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
    prepare()
    internalReasoner.precomputeInferences(inferenceTypes: _*)
  }

  override def isPrecomputed(inferenceType: InferenceType): Boolean = {
    prepare()
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
    rootOntology.getOWLOntologyManager.removeOntologyChangeListener(changeListener)
    pendingChangesBuffer.clear()
    internalReasoner.dispose()
  }

  /**
   * Takes the pending changes into account. The new internal ontology is built before the old one is discarded, so
   * that the reasoner keeps working if the changed ontology cannot be read, e.g., because of a wrong constraint.
   */
  override def flush(): Unit = synchronized {
    if (pendingChangesBuffer.nonEmpty) {
      val oldReasoner = internalReasoner
      load()
      oldReasoner.dispose()
      pendingChangesBuffer.clear()
    }
  }

  // In non-buffering mode, changes are taken into account before the next query, so none of them is pending.

  override def getPendingChanges(): ju.List[OWLOntologyChange] =
    if (bufferingMode) new ju.ArrayList[OWLOntologyChange](pendingChangesBuffer.asJava) else new ju.ArrayList()

  override def getPendingAxiomAdditions(): ju.Set[OWLAxiom] = pendingAxioms {
    case change: AddAxiom => change.getAxiom
  }

  override def getPendingAxiomRemovals(): ju.Set[OWLAxiom] = pendingAxioms {
    case change: RemoveAxiom => change.getAxiom
  }

  private def pendingAxioms(axiomOf: PartialFunction[OWLOntologyChange, OWLAxiom]): ju.Set[OWLAxiom] =
    if (bufferingMode) pendingChangesBuffer.collect(axiomOf).toSet.asJava else new ju.HashSet()
}