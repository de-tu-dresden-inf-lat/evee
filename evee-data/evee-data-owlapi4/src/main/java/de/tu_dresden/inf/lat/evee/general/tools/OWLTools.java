package de.tu_dresden.inf.lat.evee.general.tools;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import com.clarkparsia.owlapi.explanation.DefaultExplanationGenerator;
import com.clarkparsia.owlapi.explanation.util.SilentExplanationProgressMonitor;

public class OWLTools {

	public static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	public static OWLDataFactory odf = OWLManager.getOWLDataFactory();

	public static OWLOntology createOntology(Stream<? extends OWLAxiom> axioms) {
		try {
			OWLOntology ontology = manager.createOntology();
			manager.addAxioms(ontology, axioms.collect(Collectors.toSet()));
			return ontology;
		} catch (OWLOntologyCreationException e) {
			System.err.println("Could not create ontology.");
			e.printStackTrace();
			return null;
		}
	}


	public static OWLOntology createOntology(Set<? extends OWLAxiom> axioms) {
		try {
			OWLOntology ontology = manager.createOntology();
			manager.addAxioms(ontology, axioms);
			return ontology;
		} catch (OWLOntologyCreationException e) {
			System.err.println("Could not create ontology.");
			e.printStackTrace();
			return null;
		}
	}


	public static OWLReasoner createReasoner(OWLOntology ontology) {
		return new ReasonerFactory().createReasoner(ontology);
	}

	public static boolean entails(Stream<? extends OWLAxiom> axioms, OWLAxiom conclusion) {
		OWLOntology ontology = createOntology(axioms);
		OWLReasoner reasoner = createReasoner(ontology);
		boolean result;
		result = reasoner.isEntailed(conclusion);
		reasoner.dispose();
		manager.removeOntology(ontology);
		return result;
	}

	public static Set<OWLEntity> removeTopBottom(Set<OWLEntity> sig1) {
		sig1.remove(odf.getOWLThing());
		sig1.remove(odf.getOWLNothing());
		sig1.remove(odf.getOWLTopObjectProperty());
		sig1.remove(odf.getOWLBottomObjectProperty());
		return sig1;
	}

	public static Set<OWLEntity> getSignature(OWLAxiom axiom) {
		Set<OWLEntity> sig = axiom.getSignature();
		return removeTopBottom(sig);
	}

	public static Set<OWLEntity> getSignature(Collection<? extends OWLAxiom> axioms) {
		Set<OWLEntity> sig = axioms.stream().flatMap(ax -> ax.getSignature().stream()).collect(Collectors.toSet());
		return sig;
	}

	public static boolean isAuxiliary(OWLClassExpression c, Set<OWLEntity> sig) {
		return !c.isAnonymous() && !sig.contains(c) && !c.isOWLThing() && !c.isOWLNothing();
	}

	public static Set<ClassExpressionType> getClassExpressionTypes(Collection<? extends OWLAxiom> axioms) {
		Set<ClassExpressionType> res = new HashSet<>();
		for (OWLAxiom ax : axioms) {
			for (OWLClassExpression expr : ax.getNestedClassExpressions()) {
				res.add(expr.getClassExpressionType());
			}
		}
		res.remove(ClassExpressionType.OWL_CLASS);
		return res;
	}

	public static Set<ClassExpressionType> getClassExpressionTypes(OWLAxiom axiom) {
		return getClassExpressionTypes(Collections.singleton(axiom));
	}


	public static Set<? extends OWLAxiom> computeJustification(Collection<? extends OWLAxiom> axioms,
																			   OWLAxiom conclusion) {
		OWLOntology ontology = OWLTools.createOntology(axioms.stream());
		OWLReasoner reasoner = createReasoner(ontology);
		Set<? extends OWLAxiom> res = computeJustification(reasoner, conclusion);
		reasoner.dispose();
		OWLTools.manager.removeOntology(ontology);
		return res;
	}


	protected static Set<OWLAxiom> computeJustification(OWLReasoner reasoner, OWLAxiom conclusion) {
		DefaultExplanationGenerator explainer = new DefaultExplanationGenerator(OWLTools.manager, new ReasonerFactory(),
				reasoner.getRootOntology(), reasoner, new SilentExplanationProgressMonitor());
		return explainer.getExplanation(conclusion);
	}


	public static Set<? extends Set<? extends OWLAxiom>> computeJustifications(Collection<? extends OWLAxiom> axioms,
			OWLAxiom conclusion) {
		OWLOntology ontology = OWLTools.createOntology(axioms.stream());
		OWLReasoner reasoner = createReasoner(ontology);
		Set<? extends Set<? extends OWLAxiom>> res = computeJustifications(reasoner, conclusion);
		reasoner.dispose();
		OWLTools.manager.removeOntology(ontology);
		return res;
	}

	protected static Set<Set<OWLAxiom>> computeJustifications(OWLReasoner reasoner, OWLAxiom conclusion) {
		DefaultExplanationGenerator explainer = new DefaultExplanationGenerator(OWLTools.manager, new ReasonerFactory(),
				reasoner.getRootOntology(), reasoner, new SilentExplanationProgressMonitor());
		return explainer.getExplanations(conclusion);
	}

}
