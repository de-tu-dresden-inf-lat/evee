package de.tu_dresden.inf.lat.evee.concreteDomains

import de.tu_dresden.inf.lat.evee.concreteDomains.TestOntologies.{cls, owlClass}
import de.tu_dresden.inf.lat.evee.concreteDomains.owlApi.OWLCDReasonerFactory
import org.junit.Assert.{assertFalse, assertTrue}
import org.semanticweb.elk.owlapi.ElkReasonerFactory
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model.{OWLClassExpression, OWLOntology, OWLSubClassOfAxiom}
import org.semanticweb.owlapi.reasoner.OWLReasoner

/**
 * Helpers for running the concrete domain reasoner (PACER) in tests. The assertions take class names, which are
 * resolved in the default namespace of the given ontology.
 */
object ReasonerTestSupport {

  private val factory = OWLManager.getOWLDataFactory

  def withReasoner[T](ontology: OWLOntology)(test: OWLReasoner => T): T =
    run(new OWLCDReasonerFactory().createReasoner(ontology), test)

  /** plain ELK without the concrete domain, for comparison */
  def withElk[T](ontology: OWLOntology)(test: OWLReasoner => T): T =
    run(new ElkReasonerFactory().createReasoner(ontology), test)

  private def run[T](reasoner: OWLReasoner, test: OWLReasoner => T): T =
    try test(reasoner)
    finally reasoner.dispose()

  /** a subsumption between classes in the namespace of the small test ontologies */
  def subClassOf(sub: String, sup: String): OWLSubClassOfAxiom = factory.getOWLSubClassOfAxiom(cls(sub), cls(sup))

  /** a conjunction of classes in the namespace of the small test ontologies */
  def and(names: String*): OWLClassExpression = factory.getOWLObjectIntersectionOf(names.map(cls): _*)

  private def subClassOf(ontology: OWLOntology, sub: String, sup: String): OWLSubClassOfAxiom =
    factory.getOWLSubClassOfAxiom(owlClass(ontology, sub), owlClass(ontology, sup))

  def assertEntailed(ontology: OWLOntology, sub: String, sup: String): Unit = withReasoner(ontology) { reasoner =>
    assertTrue(s"$sub should be subsumed by $sup", reasoner.isEntailed(subClassOf(ontology, sub, sup)))
  }

  /** the subsumption is entailed, but not without the concrete domain */
  def assertEntailedOnlyWithConcreteDomain(ontology: OWLOntology, sub: String, sup: String): Unit = {
    withElk(ontology) { elk =>
      assertFalse(s"$sub should not be subsumed by $sup without the concrete domain",
        elk.isEntailed(subClassOf(ontology, sub, sup)))
    }
    assertEntailed(ontology, sub, sup)
  }

  def assertSatisfiable(ontology: OWLOntology, name: String): Unit = withReasoner(ontology) { reasoner =>
    assertTrue(s"$name should be satisfiable", reasoner.isSatisfiable(owlClass(ontology, name)))
  }

  def assertUnsatisfiable(ontology: OWLOntology, name: String): Unit = withReasoner(ontology) { reasoner =>
    assertFalse(s"$name should be unsatisfiable", reasoner.isSatisfiable(owlClass(ontology, name)))
  }

  /** the messages of the exception and all its causes */
  def messages(exception: Throwable): List[String] =
    if (exception == null) Nil else String.valueOf(exception.getMessage) :: messages(exception.getCause)
}
