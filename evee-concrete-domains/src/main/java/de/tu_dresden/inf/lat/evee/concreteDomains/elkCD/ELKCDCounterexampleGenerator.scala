// package de.tu_dresden.inf.lat.evee.concreteDomains.elkCD

// import de.tu_dresden.inf.lat.evee.concreteDomains.CDConstraint 

// import de.tu_dresden.inf.lat.evee.concreteDomains.data.extendedDataStructure.{ExtendedOntology, ExtendedAxiom}
// import de.tu_dresden.inf.lat.evee.concreteDomains.data.cdGeneral.CDValueConverter

// import de.tu_dresden.lat.csv.general.CSVEntry
// import de.tu_dresden.lat.CombinedModelGenerator
// import de.tu_dresden.inf.lat.evee.concreteDomains.concreteDomains.CDCounterexampleGenerator

// import de.tu_dresden.inf.lat.evee.nonEntailment.interfaces.IOWLCounterexampleGenerator

// import org.semanticweb.owlapi.model.{AxiomType, OWLClassAssertionAxiom, OWLDataProperty, OWLDataPropertyAssertionAxiom, OWLEntity, OWLIndividualAxiom, OWLLiteral, OWLNamedIndividual}

// import java.util
// import java.util.{Optional, stream}
// import scala.collection.JavaConverters.{asJavaCollectionConverter, setAsJavaSetConverter}
// import scala.collection.convert.ImplicitConversions.`collection AsScalaIterable`


// class   ELKCDCounterexampleGenerator[CD_CONSTRAINT <: CDConstraint, CD_VALUE]
//   (elkCDReasoner: ELKCDReasoner[CD_CONSTRAINT],
//    cdCounterexampleGenerator: CDCounterexampleGenerator[CD_CONSTRAINT, CD_VALUE],
//    owlCounterexampleGenerator: IOWLCounterexampleGenerator)
//   extends CombinedModelGenerator[Set[ExtendedAxiom[CD_CONSTRAINT]], OWLEntity, ExtendedOntology[CD_CONSTRAINT], Set[OWLIndividualAxiom]] {

//   private var observation: Set[ExtendedAxiom[CD_CONSTRAINT]] = _
//   private var signature: Iterable[OWLEntity] = _
//   private var relevantDataProperties: Set[OWLDataProperty] = _
//   private var extendedOntology: ExtendedOntology[CD_CONSTRAINT] = _

//   override def setObservation(observation: Set[ExtendedAxiom[CD_CONSTRAINT]]): Unit = {
//     this.observation = observation
//     owlCounterexampleGenerator.setObservation(this.observation.map(ea => ea.axiom).asJava)
//   }

//   override def setSignature(signature: util.Collection[OWLEntity]): Unit = {
//     this.signature = signature
//     owlCounterexampleGenerator.setSignature(signature)
//     this.relevantDataProperties = signature.filter(_.isOWLDataProperty).map(_.asOWLDataProperty).toSet
//     cdCounterexampleGenerator.setSignature(relevantDataProperties.asJavaCollection)
//   }

//   override def setOntology(ontology: ExtendedOntology[CD_CONSTRAINT]): Unit = {
//     this.extendedOntology = ontology
//     owlCounterexampleGenerator.setOntology(extendedOntology.ontology)
//   }

//   private def extendModel(model: Set[OWLIndividualAxiom]): Set[OWLIndividualAxiom] = {
//     println
//     println
//     println("Original model:")
//     for (axiom <- model)
//       println(axiom)
//     val individuals = model.flatMap(_.getIndividualsInSignature)
//     // TODO: combine all possible counterexamples for all elements to get multiple models?
//     (individuals.flatMap(getCounterexample(_, model)) ++ model)
//   }

//   private def getCounterexample(individual: OWLNamedIndividual, model: Set[OWLIndividualAxiom]): Set[OWLIndividualAxiom] = {
//     val positiveConstraints = model
//       .filter(_.isOfType(AxiomType.CLASS_ASSERTION))
//       .map(_.asInstanceOf[OWLClassAssertionAxiom])
//       .filter(_.getIndividual.equals(individual))
//       .map(_.getClassExpression)
//       .filter(_.isNamed)
//       .map(_.asOWLClass)
//       .intersect(extendedOntology.constraintNames)
//       .map(extendedOntology.constraintFor)
//     if (positiveConstraints.isEmpty)
//       return model

//     val positiveDataProperties = positiveConstraints.flatMap(_.getOWLDataProperties)
//     val negativeConstraints = extendedOntology.constraintMap.values
//       .filter(c => !positiveConstraints.contains(c))
//       .filter(_.getOWLDataProperties.subsetOf(relevantDataProperties.intersect(positiveDataProperties)))
//     cdCounterexampleGenerator.setOntology(positiveConstraints)
//     cdCounterexampleGenerator.setObservation(negativeConstraints)
//     // TODO: return a stream of counterexamples?
//     val counterexample = cdCounterexampleGenerator.generateExplanations().findFirst().get()
//     counterexample.map{ case (prop, value) => getAssertion(individual, prop, CDValueConverter.toOWLLiteral(value)) }
//       .toSet

//     // TODO: implement a caching decorator for the cdCounterExampleGenerator?
//   }

//   private def getAssertion(individual: OWLNamedIndividual, prop: OWLDataProperty, value: OWLLiteral): OWLDataPropertyAssertionAxiom = {
//     extendedOntology.ontology.getOWLOntologyManager.getOWLDataFactory.getOWLDataPropertyAssertionAxiom(prop, individual, value)
//   }

//   override def generateExplanations(): stream.Stream[Set[OWLIndividualAxiom]] = {
//     generateExplanations(Optional.empty())
//   }
//   override def generateExplanations(cSVEntryOptional: Optional[CSVEntry]): stream.Stream[Set[OWLIndividualAxiom]] = {
//     elkCDReasoner.classify()

//     if (cSVEntryOptional.isPresent) {
//       cSVEntryOptional.get.setOWLReasonerNumberOfCalls(Integer.toString(elkCDReasoner.getMaxNumberOfReasonerCalls()))
//       cSVEntryOptional.get.setOWLClassificationDuration(elkCDReasoner.getMaxClassificationTime().toString)
//     }

//     println
//     println
//     println("Saturated ontology:")
//     for (axiom <- extendedOntology.ontology.getAxioms())
//       println(ExtendedAxiom(axiom,extendedOntology.constraintMap).toString)
//     owlCounterexampleGenerator.generateExplanations().map(model => extendModel(model.toSet))
//   }

//   override def supportsExplanation(): Boolean = owlCounterexampleGenerator.supportsExplanation()

//   override def successful(): Boolean =
//     true // TODO fix this to a more reasonable behaviour

//   override def ignoresPartsOfOntology(): Boolean = ???
// }
