package de.tu_dresden.inf.lat.evee.concreteDomains

import de.tu_dresden.inf.lat.evee.concreteDomains.ReasonerTestSupport.{subClassOf, withReasoner}
import de.tu_dresden.inf.lat.evee.concreteDomains.TestOntologies.ontology
import de.tu_dresden.inf.lat.evee.concreteDomains.data.extendedDataStructure.parser.ExtendedOntologyParser
import de.tu_dresden.inf.lat.evee.concreteDomains.data.names.{CDAnnotationNames, ConcreteDomainName}
import de.tu_dresden.inf.lat.evee.concreteDomains.owlApi.CDSchema
import org.junit.Assert.{assertEquals, assertFalse, assertSame, assertTrue}
import org.junit.Test
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model.{IRI, OWLOntologyManager}
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary

import scala.collection.JavaConverters._

/** The schema ontology declares and describes the names of PACER; importing it is optional and changes no answer. */
class CDSchemaTest {

  private val factory = OWLManager.getOWLDataFactory

  /** a manager that finds the schema ontology in the library */
  private def manager(): OWLOntologyManager = {
    val manager = OWLManager.createOWLOntologyManager()
    manager.getIRIMappers.add(CDSchema.IRI_MAPPER)
    manager
  }

  private val importSchema = "Import(<" + CDAnnotationNames.SCHEMA_ONTOLOGY + ">)"

  /** the copy in the library has the IRI of the schema ontology and declares the annotation properties */
  @Test def copyInTheLibrary(): Unit = {
    val schema = manager().loadOntology(CDSchema.ONTOLOGY_IRI)
    assertEquals(CDSchema.ONTOLOGY_IRI, schema.getOntologyID.getOntologyIRI.get)
    for (property <- Seq(CDAnnotationNames.CONCRETE_DOMAIN_ANNOTATION_PROP, CDAnnotationNames.CONSTRAINT_ANNOTATION_PROP))
      assertTrue(property, schema.containsAnnotationPropertyInSignature(IRI.create(property)))
  }

  /** the copy can also be loaded without the IRI mapper; it is loaded only once, and imports of it are resolved */
  @Test def loadInto(): Unit = {
    val m = OWLManager.createOWLOntologyManager()
    val schema = CDSchema.loadInto(m)
    assertEquals(CDSchema.ONTOLOGY_IRI, schema.getOntologyID.getOntologyIRI.get)
    assertSame(schema, CDSchema.loadInto(m))
    val o = ontology(m, "http://ex.org/o", "QDiff", Seq(), importSchema)
    assertTrue(o.getImportsClosure.contains(schema))
  }

  /** the annotation properties and the concrete domains have a label and a comment */
  @Test def namesAreDescribed(): Unit = {
    val schema = manager().loadOntology(CDSchema.ONTOLOGY_IRI)
    val names = Seq(CDAnnotationNames.CONCRETE_DOMAIN_ANNOTATION_PROP, CDAnnotationNames.CONSTRAINT_ANNOTATION_PROP) ++
      ConcreteDomainName.values.map(_.getIRI)
    for (name <- names; property <- Seq(OWLRDFVocabulary.RDFS_LABEL, OWLRDFVocabulary.RDFS_COMMENT))
      assertTrue(s"$name should have an $property", schema.getAnnotationAssertionAxioms(IRI.create(name)).asScala
        .exists(_.getProperty.getIRI == property.getIRI))
  }

  /** the schema ontology names no concrete domain and has no constraints, so importing it changes nothing */
  @Test def importChangesNothing(): Unit = {
    val m = manager()
    val withDomain = ontology(m, "http://ex.org/o", "QDiff", Seq("C0" -> ":x = 5", "C1" -> ":x > 1"), importSchema,
      "SubClassOf(:A :C0)")
    assertEquals(2, ExtendedOntologyParser.toExtendedOntology(withDomain).constraintMap.size)
    withReasoner(withDomain) { reasoner => assertTrue(reasoner.isEntailed(subClassOf("A", "C1"))) }
    val withoutDomain = ontology(m, "http://ex.org/plain", null, Seq(), importSchema)
    assertFalse(ExtendedOntologyParser.hasConcreteDomain(withoutDomain))
  }
}
