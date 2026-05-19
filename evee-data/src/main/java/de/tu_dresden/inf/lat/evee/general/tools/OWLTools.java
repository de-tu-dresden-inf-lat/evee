package de.tu_dresden.inf.lat.evee.general.tools;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.List;

import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;


import com.clarkparsia.owlapi.explanation.DefaultExplanationGenerator;
import com.clarkparsia.owlapi.explanation.util.SilentExplanationProgressMonitor;

import com.google.common.collect.Sets;

public class OWLTools {

	public static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	public static OWLDataFactory odf = OWLManager.getOWLDataFactory();

	public static OWLOntologyManager getManager() {
			//return manager;
			return OWLManager.createOWLOntologyManager();
	}

	public static OWLOntology createOntology(Stream<? extends OWLAxiom> axioms) {
		try {
			OWLOntology ontology = manager.createOntology();
			axioms.forEach(ax -> manager.addAxiom(ontology, ax));
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
			axioms.forEach(ax -> manager.addAxiom(ontology, ax));
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
		OWLOntology ontology = OWLTools.createOntology(axioms);
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

	public static Set<OWLSubClassOfAxiom> equivToSubOf(OWLEquivalentClassesAxiom axiom){
		Set<OWLSubClassOfAxiom> result = new HashSet<>();
		List<OWLClassExpression> expressions = axiom.getClassExpressions().stream().collect(Collectors.toList());

		for (int i = 0; i < expressions.size()-1; i++) {
			OWLClassExpression ex1 = expressions.get(i);
			OWLClassExpression ex2 = expressions.get(i+1);
			result.add(odf.getOWLSubClassOfAxiom(ex1, ex2));
			result.add(odf.getOWLSubClassOfAxiom(ex2, ex1));
		}

		return result;
	}

	public static Set<OWLSubClassOfAxiom> disjToSubOf(OWLDisjointClassesAxiom axiom){
		Set<OWLSubClassOfAxiom> result = new HashSet<>();
		List<OWLClassExpression> expressions = axiom.getClassExpressions().stream().collect(Collectors.toList());

		for(int i=0; i<expressions.size()-1; i++){
			OWLClassExpression cls1 = expressions.get(i);
			for (int j=i+1; j<expressions.size(); j++){
				OWLClassExpression cls2 = expressions.get(j);
				result.add(odf.getOWLSubClassOfAxiom(odf.getOWLObjectIntersectionOf(cls1, cls2), odf.getOWLNothing()));
			}
		}

		return result;
	}

	public static Set<OWLSubClassOfAxiom> domainToSubOf(OWLObjectPropertyDomainAxiom axiom){
		OWLObjectPropertyExpression property = axiom.getProperty();
		OWLClassExpression domain = axiom.getDomain();

		OWLSubClassOfAxiom sub = 
			odf.getOWLSubClassOfAxiom(odf.getOWLObjectSomeValuesFrom(property, odf.getOWLThing()), domain);

		return Sets.newHashSet(sub);
	}

	public static Set<OWLSubClassOfAxiom> rangeToSubOf(OWLObjectPropertyRangeAxiom axiom){
		OWLObjectPropertyExpression property = axiom.getProperty();
		OWLClassExpression range = axiom.getRange();

		OWLSubClassOfAxiom sub = 
			odf.getOWLSubClassOfAxiom(odf.getOWLThing(), odf.getOWLObjectAllValuesFrom(property, range));

		return Sets.newHashSet(sub);
	}

}
