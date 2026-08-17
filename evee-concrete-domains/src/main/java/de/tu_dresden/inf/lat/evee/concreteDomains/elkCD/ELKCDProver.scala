package de.tu_dresden.inf.lat.evee.concreteDomains.elkCD

import de.tu_dresden.inf.lat.evee.concreteDomains.{CDConstraint, CDDefaultContradictionGenerator}
import de.tu_dresden.inf.lat.evee.concreteDomains.data.extendedDataStructure.{ExtendedAxiom, ExtendedOntology}
import de.tu_dresden.inf.lat.evee.concreteDomains.tools.{OWLClass2CDPDecoder, Tools}
import de.tu_dresden.inf.lat.evee.concreteDomains.CDProofGenerator
import de.tu_dresden.inf.lat.evee.concreteDomains.CombinedProofGenerator

import de.tu_dresden.inf.lat.evee.proofs.data.exceptions.ProofGenerationException
import de.tu_dresden.inf.lat.evee.proofs.data.{Inference, Proof}
import de.tu_dresden.inf.lat.evee.proofs.interfaces.{IInference, IProof, IProofGenerator}

import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model.{AxiomType, IRI, OWLAxiom, OWLClass, OWLClassExpression, OWLDataFactory, OWLOntology, OWLSubClassOfAxiom}

import java.util
import java.util.Optional
import scala.collection.JavaConverters.{asScalaBufferConverter, asScalaSetConverter, seqAsJavaListConverter}
import scala.collection.mutable.ListBuffer

/**
 * @author Christian Alrabbaa
 */

class ELKCDProver[CD_CONSTRAINT <: CDConstraint](elkCDReasoner: ELKCDReasoner[CD_CONSTRAINT], cdProver: CDProofGenerator[CD_CONSTRAINT], owlProver: IProofGenerator[OWLAxiom, OWLOntology])
  extends CombinedProofGenerator[ExtendedAxiom[CD_CONSTRAINT],ExtendedOntology[CD_CONSTRAINT]]{

  private val conjunctionDecomposition = "Conjunction Decomposition"
  private val conjunctionTautology = "Conjunction Tautology"
  private val conceptInclusionTautology = "Concept Inclusion Tautology"
  private val identical = "1"


  private val factory: OWLDataFactory =  OWLManager.getOWLDataFactory

  private var extendedOntology: ExtendedOntology[CD_CONSTRAINT] = _
  private var originalExtendedOntology: ExtendedOntology[CD_CONSTRAINT] = _

  private var defaultCDContradictionPredicate: CD_CONSTRAINT = _

  private val prefix: String = "Aux"
  private var counter: Integer = 0
  private var updatedConstraintsMap: scala.collection.mutable.Map[CD_CONSTRAINT, OWLClass] = _

  override def setOntology(ontology: ExtendedOntology[CD_CONSTRAINT]): Unit = {
    extendedOntology = ontology
  }

  override def setOriginalOntology(ontology: ExtendedOntology[CD_CONSTRAINT]): Unit = {
    originalExtendedOntology = ontology
  }

  override def supportsProof(axiom: ExtendedAxiom[CD_CONSTRAINT]): Boolean = ???

  /**
   * Return an equivalent IProof with ExtendedAxioms instead of OWLAxioms
   * @param originalDS
   * @return
   */
  private def getExtendedAxioms(originalDS:IProof[OWLAxiom]):IProof[ExtendedAxiom[CD_CONSTRAINT]] = {
    val ds = new Proof[ExtendedAxiom[CD_CONSTRAINT]](getExtendedAxiom(originalDS.getFinalConclusion))

    originalDS.getInferences().asScala.foreach(inf => {
      val fc = getExtendedAxiom(inf.getConclusion)
      val p = inf.getPremises.asScala.map(getExtendedAxiom).toList

      val newInf = getInference(fc, inf.getRuleName, p)
      ds.addInference(newInf)
    })
    ds
  }

  /**
   * Return an ExtendedAxiom instance of the provided axiom</br>
   * @note Currently this only supports SubClassOfAxioms
   * @param axiom
   * @return
   */
  private def getExtendedAxiom(axiom:OWLAxiom):ExtendedAxiom[CD_CONSTRAINT]={
    if (!axiom.isOfType(AxiomType.SUBCLASS_OF))
      return new ExtendedAxiom[CD_CONSTRAINT](axiom, Map.empty)

    val subClsAxiom = axiom.asInstanceOf[OWLSubClassOfAxiom]
    val relevantConstraintsMap = getRelevantConstraintsMap(subClsAxiom)

    new ExtendedAxiom[CD_CONSTRAINT](subClsAxiom,relevantConstraintsMap)
  }

  /**
   * Return new Inference object
   * @param conclusion
   * @param ruleName
   * @param premise
   * @return
   */
  private def getInference(conclusion: ExtendedAxiom[CD_CONSTRAINT], ruleName: String,
                           premise: List[ExtendedAxiom[CD_CONSTRAINT]]): Inference[ExtendedAxiom[CD_CONSTRAINT]] = {
    new Inference[ExtendedAxiom[CD_CONSTRAINT]](conclusion, ruleName, premise.asJava)
  }

  /**
   * Return a map for concept names that represent linear constraints
   * @param subClsAxiom
   * @return
   */
  private def getRelevantConstraintsMap(subClsAxiom: OWLSubClassOfAxiom): Map[OWLClass, CD_CONSTRAINT] = {
    val subConcepts = subClsAxiom.getClassesInSignature
    val relevantConstraintsMap = scala.collection.mutable.Map[OWLClass, CD_CONSTRAINT]()

    subConcepts.asScala.intersect(this.extendedOntology.constraintNames)
      .foreach(x => relevantConstraintsMap += (x -> this.extendedOntology.constraintFor(x)))

    //Set default contradiction predicate at this level
    if(defaultCDContradictionPredicate == null)
      if(relevantConstraintsMap.nonEmpty)
        defaultCDContradictionPredicate = CDDefaultContradictionGenerator
          .getDefaultContradiction(relevantConstraintsMap.values.head)

    relevantConstraintsMap.toMap
  }

  private def isCDAxiom(extendedAxiom: ExtendedAxiom[CD_CONSTRAINT]): Boolean = {
    val bot = factory.getOWLNothing
    if (extendedAxiom.axiom.isOfType(AxiomType.SUBCLASS_OF)) {
      val subClsAxiom = extendedAxiom.axiom.asInstanceOf[OWLSubClassOfAxiom]
      if(!subClsAxiom.getSuperClass.isInstanceOf[OWLClass])
        return false

      val cNames = Tools.getConceptNamesAtLevelZero(subClsAxiom)

      if(cNames.isEmpty)
        return false

      if (cNames.size() == 1 && cNames.contains(bot))
        return false

      cNames.asScala.filter(!_.equals(bot)).foreach(x=>{
        if (!extendedAxiom.constraintNames.contains(x))
          return false
      })
      return true
    }
    false
  }

  private def getRHSConstraint(extendedAxiom: ExtendedAxiom[CD_CONSTRAINT]): CD_CONSTRAINT = {
    assert(extendedAxiom.axiom.isOfType(AxiomType.SUBCLASS_OF))
    val subClsOf =  extendedAxiom.axiom.asInstanceOf[OWLSubClassOfAxiom]

    if(!subClsOf.getSuperClass.isInstanceOf[OWLClass])
      throw new ProofGenerationException("Right hand side should be a concept name")

    val cls = subClsOf.getSuperClass.asInstanceOf[OWLClass]

    if(cls.isBottomEntity) {
      return defaultCDContradictionPredicate
    }

    if(!extendedAxiom.map.contains(cls))
      throw new ProofGenerationException("Failed to recognise the right hand side concept -> " + cls)

    extendedAxiom.constraintFor(cls)
  }

  private def getLHSConstraints(extendedAxiom: ExtendedAxiom[CD_CONSTRAINT]): Set[CD_CONSTRAINT] = {
    assert(extendedAxiom.axiom.isOfType(AxiomType.SUBCLASS_OF))
    val subClsOf = extendedAxiom.axiom.asInstanceOf[OWLSubClassOfAxiom]

   Tools.getConceptNamesAtLevelZero(subClsOf.getSubClass).asScala.map(extendedAxiom.constraintFor).toSet
  }

  private def isCDOnlyRHS(extAx:ExtendedAxiom[CD_CONSTRAINT]): Boolean = {
    if (!extAx.axiom.isOfType(AxiomType.SUBCLASS_OF))
      return false

    val subClsOf = extAx.axiom.asInstanceOf[OWLSubClassOfAxiom]

    if (!Tools.getConceptNamesAtLevelZero(subClsOf.getSuperClass).isEmpty) {
      val allCDConcepts = Tools.getConceptNamesAtLevelZero(subClsOf.getSuperClass).asScala.map(extAx.map.contains)
        .reduce(_ && _)
      if (allCDConcepts) {
        return true
      }
    }
    false
  }

  private def isCDOnly(extAx:ExtendedAxiom[CD_CONSTRAINT]): Boolean = {
    if(originalExtendedOntology.ontology.containsAxiom(extAx.axiom))
      return false

    if(isCDAxiom(extAx))
      return true

    false
  }

  private def addConjunctionDecompositionInferences(axiom: OWLSubClassOfAxiom, map: Map[OWLClass,CD_CONSTRAINT],
                                                    ds: IProof[ExtendedAxiom[CD_CONSTRAINT]])  {
    val d = new OWLClass2CDPDecoder[CD_CONSTRAINT]

    if (axiom.getSuperClass.asConjunctSet().size() == 1)
      return

    axiom.getSuperClass.asConjunctSet().asScala.foreach(x=>{
      val a = factory.getOWLSubClassOfAxiom(axiom.getSubClass,x)
      val c = new ExtendedAxiom[CD_CONSTRAINT](a, d.filterMap(map,a))
      val newInf = getInference(c, conjunctionDecomposition, List(new ExtendedAxiom[CD_CONSTRAINT](axiom, map)))
      ds.addInference(newInf)
    })
  }

  def formatRuleName(ruleName: String): String = {
    if(ruleName.startsWith("[") && ruleName.endsWith("]"))
      return ruleName
    "[" + ruleName + "]"
  }

  private def addCDInference(lhs: OWLClassExpression, rHSOnlyCDPredicatesAxiom: ExtendedAxiom[CD_CONSTRAINT],
                             onlyCDPredicatesAxiom: ExtendedAxiom[CD_CONSTRAINT],
                               ds: IProof[ExtendedAxiom[CD_CONSTRAINT]]) = {
    val premiseConstraints = getLHSConstraints(onlyCDPredicatesAxiom)
    cdProver.setOntology(premiseConstraints)

    val conclusionConstraint =
      if (!extendedOntology.ontology.getAxioms().contains(factory.getOWLSubClassOfAxiom(onlyCDPredicatesAxiom.axiom
        .asInstanceOf[OWLSubClassOfAxiom].getSubClass, factory.getOWLNothing)))
        getRHSConstraint(onlyCDPredicatesAxiom)
      else
        defaultCDContradictionPredicate


    val cdProof = cdProver.getProof(conclusionConstraint)

    val d = new OWLClass2CDPDecoder[CD_CONSTRAINT]

    if(premiseConstraints.size == 1 && premiseConstraints.contains(conclusionConstraint)){
      val c = {
        val a = factory.getOWLSubClassOfAxiom(lhs, onlyCDPredicatesAxiom.axiom.asInstanceOf[OWLSubClassOfAxiom].getSuperClass)
        new ExtendedAxiom[CD_CONSTRAINT](a, d.filterMap(this.extendedOntology.constraintMap, a))
      }

      var newInfOpt = Option.empty[Inference[ExtendedAxiom[CD_CONSTRAINT]]]
        if(!c.equals(rHSOnlyCDPredicatesAxiom)) {
          newInfOpt = Some(getInference(c, formatRuleName(identical), List(rHSOnlyCDPredicatesAxiom)))
        }else if (lhs.isInstanceOf[OWLClass] && this.extendedOntology.constraintMap.contains(lhs.asOWLClass())){
          val p = {
            val a = factory.getOWLSubClassOfAxiom(lhs, lhs)
            new ExtendedAxiom[CD_CONSTRAINT](a, d.filterMap(this.extendedOntology.constraintMap, a))
          }
          ds.addInference(getInference(p, formatRuleName(conceptInclusionTautology), List.empty))

          newInfOpt = Some(getInference(c, formatRuleName(identical), List(p)))
        }

      if(newInfOpt.nonEmpty)
        ds.addInference(newInfOpt.get)
      }

    cdProof.getInferences().asScala.foreach(inf => {
      if (!inf.getPremises.isEmpty) {
        val p = inf.getPremises.asScala.map(x => {
          val a = factory.getOWLSubClassOfAxiom(lhs, getKeyOf(x))
          new ExtendedAxiom[CD_CONSTRAINT](a, d.filterMap(this.extendedOntology.constraintMap, a))
        }).toList
        val c = if (inf.getConclusion.isInconsistent) {
          val a = factory.getOWLSubClassOfAxiom(lhs, factory.getOWLNothing)
          new ExtendedAxiom[CD_CONSTRAINT](a, d.filterMap(this.extendedOntology.constraintMap, a))
        } else {
          val a = factory.getOWLSubClassOfAxiom(lhs, getKeyOf(inf.getConclusion))
          new ExtendedAxiom[CD_CONSTRAINT](a, d.filterMap(this.extendedOntology.constraintMap, a))
        }
        val newInf = getInference(c, formatRuleName(inf.getRuleName), p)
        ds.addInference(newInf)
      } else {
        if (lhs.asConjunctSet().contains(getKeyOf(inf.getConclusion))) {
          val a = factory.getOWLSubClassOfAxiom(lhs, getKeyOf(inf.getConclusion))
          val c = new ExtendedAxiom[CD_CONSTRAINT](a, d.filterMap(this.extendedOntology.constraintMap, a))
          val ruleName = if (lhs.asConjunctSet().size() == 1) conceptInclusionTautology else conjunctionTautology

          val newInf = getInference(c, ruleName, List())
          ds.addInference(newInf)
        }
      }
    })
  }

  private def getKeyOf(key: CD_CONSTRAINT):OWLClass = {
    if(this.updatedConstraintsMap.contains(key))
      return this.updatedConstraintsMap(key)

    val nextName = getNextName()
    this.updatedConstraintsMap+= key -> nextName

    val newMap = scala.collection.mutable.Map[OWLClass, CD_CONSTRAINT]()
    newMap ++= this.extendedOntology.constraintMap
    newMap +=  nextName -> key
    this.extendedOntology = new ExtendedOntology[CD_CONSTRAINT](this.extendedOntology.ontology, newMap.toMap)
    nextName
  }

  private def getNextName(): OWLClass={
    counter = counter + 1
    factory.getOWLClass(IRI.create(prefix+counter))
  }

  private def getAsOWLSubClassOf(axiom: OWLAxiom): OWLSubClassOfAxiom = {
    axiom match {
      case axiom: OWLSubClassOfAxiom => axiom
      case _ => throw new ProofGenerationException("Only OWLSubClassOf axioms are supported")
    }
  }

  private def addCDInference2(inf:IInference[ExtendedAxiom[CD_CONSTRAINT]],
                              ds: IProof[ExtendedAxiom[CD_CONSTRAINT]]) = {
    if (isCDOnly(inf.getConclusion)) {
      val lHS = getAsOWLSubClassOf(inf.getConclusion.axiom).getSubClass
      addCDInference(lHS, inf.getConclusion, inf.getConclusion, ds)
    }else
      ds.addInference(inf)
  }

  private def getAllPairs(premises: util.List[_ <: ExtendedAxiom[CD_CONSTRAINT]]): List[(ExtendedAxiom[CD_CONSTRAINT],
    ExtendedAxiom[CD_CONSTRAINT])] = {
    val res = ListBuffer[(ExtendedAxiom[CD_CONSTRAINT], ExtendedAxiom[CD_CONSTRAINT])]()

    for(i <- premises.asScala){
      for(j <- premises.asScala){
        if(!i.equals(j))
          res += ((i,j))
      }
    }

    res.toList
  }

  private def provePremisesIfCD(inf: IInference[ExtendedAxiom[CD_CONSTRAINT]],
                                turnedToCDProofs: ListBuffer[ExtendedAxiom[CD_CONSTRAINT]],
                                ds: IProof[ExtendedAxiom[CD_CONSTRAINT]]) = {
      inf.getPremises.asScala.filterNot(turnedToCDProofs.contains).filter(x => isCDAxiom(x)).foreach(x=>{

        addConjunctionDecompositionInferences(x.axiom.asInstanceOf[OWLSubClassOfAxiom], x.map, ds)

        addCDInference(x.axiom.asInstanceOf[OWLSubClassOfAxiom].getSubClass, x, x, ds)
      })

    }

  private def updateDerivationStructure(ELKProof: IProof[ExtendedAxiom[CD_CONSTRAINT]]): IProof[ExtendedAxiom[CD_CONSTRAINT]] = {
    val res = new Proof[ExtendedAxiom[CD_CONSTRAINT]](ELKProof.getFinalConclusion)
    val turnedToCDProofs = ListBuffer[ExtendedAxiom[CD_CONSTRAINT]]()

    ELKProof.getInferences().asScala.foreach(inf => {
      var gotReplaced = false
      val allPairs:List[(ExtendedAxiom[CD_CONSTRAINT], ExtendedAxiom[CD_CONSTRAINT])] = getAllPairs(inf.getPremises)

      for((a,b)<-allPairs) {

        if (isCDOnlyRHS(a) && isCDOnly(b) && !a.equals(b)) {
          val subCLsAxiom1 = getAsOWLSubClassOf(a.axiom)

          //Add inferences that split the top level conjuncts
          addConjunctionDecompositionInferences(subCLsAxiom1, a.map, res)

          //Add an inference that represent the CD reasoning
          addCDInference(subCLsAxiom1.getSubClass, a, b, res)

          gotReplaced = true
          turnedToCDProofs += b
        }
      }

      if(!gotReplaced && !inf.getPremises.isEmpty) {
        provePremisesIfCD(inf, turnedToCDProofs, res)
        res.addInference(inf)
      }

      //Handle inferences with empty premises and CD only conclusion
      if(inf.getPremises.isEmpty){
        addCDInference2(inf, res)
      }else
        res.addInference(inf)
    })
    res
  }

  /**
   * Returns a derivation structure Containing inference using OWLAxioms and concrete domain predicates
   *
   * @param axiom Final conclusion
   * @return
   */
  // override def getProof(axiom: ExtendedAxiom[CD_CONSTRAINT]): IProof[ExtendedAxiom[CD_CONSTRAINT]] = {
  //   getProof(axiom, Optional.empty())
  // }

  def getProofFromOWLDS(owlDS: IProof[OWLAxiom]): IProof[ExtendedAxiom[CD_CONSTRAINT]] = {

    val ds = getExtendedAxioms(owlDS)

    this.updatedConstraintsMap = scala.collection.mutable.Map[CD_CONSTRAINT, OWLClass]()
    this.extendedOntology.constraintMap.foreach(e => this.updatedConstraintsMap += e._2 -> e._1)

    val updatedDS = updateDerivationStructure(ds)

    updatedDS
  }

  override def getProof(axiom: ExtendedAxiom[CD_CONSTRAINT])
  : IProof[ExtendedAxiom[CD_CONSTRAINT]] = {

    //Extended EL reasoning with LinearEquations
    elkCDReasoner.classify()


    //Derivation Structure with EL axioms extended with CD constraints
    owlProver.setOntology(extendedOntology.ontology)
    val ds = getExtendedAxioms(owlProver.getProof(axiom.axiom))

    //This map is needed to keep using auxiliary names in the proof instead of the actual constraints.
    //This map gets updated by "updateDerivationStructure" which updates the extended ontology of this prover
    this.updatedConstraintsMap = scala.collection.mutable.Map[CD_CONSTRAINT, OWLClass]()
    this.extendedOntology.constraintMap.foreach(e => this.updatedConstraintsMap += e._2 -> e._1)

    //Replace inferences that correspond to CD reasoning with equivalent CD inferences
    val updatedDS = updateDerivationStructure(ds)

    updatedDS
  }

  override def successful(): Boolean = ???

}