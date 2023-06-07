package de.tu_dresden.inf.lat.counterExample;

import org.semanticweb.elk.owlapi.ElkReasoner;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import com.github.jsonldjava.shaded.com.google.common.collect.Sets;

import de.tu_dresden.inf.lat.model.tools.ToOWLTools;

public class TautologyChecker {
	private static final IRI ontologyIRI = IRI.create("http://empty.owl");

	private final OWLOntologyManager manager;
	private final OWLClassExpression lHS;
	private final OWLClassExpression rHS;
	private final ToOWLTools owlTools;

	private OWLOntology emptyOntology;

	public TautologyChecker(OWLAxiom axiom) {
		this.manager = OWLManager.createOWLOntologyManager();
		this.owlTools = ToOWLTools.getInstance();
		this.lHS = this.owlTools.getLHS(axiom);
		this.rHS = this.owlTools.getRHS(axiom);
		try {
			this.emptyOntology = createOntology();
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
	}

	public boolean isTautology() {
		ElkReasoner reasoner = (ElkReasoner) new ElkReasonerFactory().createReasoner(this.emptyOntology);

		return reasoner.isEntailed(owlTools.getOWLSubClassOfAxiom(this.lHS, this.rHS));
	}

	private OWLOntology createOntology() throws OWLOntologyCreationException {
		OWLAxiom axiom1 = owlTools.getOWLSubClassOfAxiom(this.lHS, owlTools.getOWLTop());
		OWLAxiom axiom2 = owlTools.getOWLSubClassOfAxiom(this.rHS, owlTools.getOWLTop());

		return this.manager.createOntology(Sets.newHashSet(axiom1, axiom2), ontologyIRI);
	}

}
