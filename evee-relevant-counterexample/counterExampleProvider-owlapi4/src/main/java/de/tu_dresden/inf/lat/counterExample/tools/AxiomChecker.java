package de.tu_dresden.inf.lat.counterExample.tools;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectHasValue;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

/**
 * @author Christian Alrabbaa
 *
 */
public class AxiomChecker {
	public static final String axiomTypes = "SubClassOf, Equivalence";
	public static final String elConstructs = "Concept Names, Existential Restrictions, Conjunctions";
	public static final String alcConstructs = "Concept Names, Existential Restrictions, Conjunctions, Nominals, Universal Restrictions, Disjunctions, Negation";

	/*
	 * EL
	 **/

	/**
	 * Checks if the provided axiom is an EL axiom.<br>
	 * Currently supported axioms: ({@value #axiomTypes}).<br>
	 * Currently supported constructors: ({@value #elConstructs})
	 * 
	 * @param axiom
	 * @return
	 */
	public static boolean isInEL(OWLAxiom axiom) {
		Collection<OWLClassExpression> expressions = parseAxiom(axiom);

		return !expressions.isEmpty() && isInEL(expressions);
	}

	/**
	 * Checks if each of the provided ClassExpressions is in EL.<br>
	 * Currently supported constructors: ({@value #elConstructs})
	 * 
	 * @param expressions
	 * @return
	 */
	public static boolean isInEL(Collection<OWLClassExpression> expressions) {

		for (OWLClassExpression exp : expressions)
			if (!isInEL(exp))
				return false;

		return true;
	}

	/**
	 * Checks if the provided ClassExpression is in EL.<br>
	 * Currently supported constructors: ({@value #elConstructs})
	 * 
	 * @param expression
	 * @return
	 */
	public static boolean isInEL(OWLClassExpression expression) {
		if (expression.asConjunctSet().isEmpty())
			return false;

		Set<Boolean> result = new HashSet<>();

		for (OWLClassExpression conjunct : expression.asConjunctSet()) {

			if (conjunct.getClassExpressionType() == ClassExpressionType.OWL_CLASS)
				result.add(true);

			else if (conjunct.getClassExpressionType() == ClassExpressionType.OBJECT_SOME_VALUES_FROM)
				result.add(isInEL(((OWLObjectSomeValuesFrom) conjunct).getFiller()));

			else
				result.add(false);
		}

		return result.size() <= 1 && result.iterator().next();
	}

	/*
	 * ALC
	 **/

	/**
	 * Checks if the provided axiom is an ALC axiom.<br>
	 * Currently supported axioms: ({@value #axiomTypes}).<br>
	 * Currently supported constructors: ({@value #alcConstructs})
	 * 
	 * @param axiom
	 * @return
	 */
	public static boolean isInALC(OWLAxiom axiom) {
		Collection<OWLClassExpression> expressions = parseAxiom(axiom);

		return !expressions.isEmpty() && isInALC(expressions);
	}

	/**
	 * Checks if each of the provided ClassExpressions is in ALC.<br>
	 * Currently supported constructors: ({@value #alcConstructs})
	 * 
	 * @param expressions
	 * @return
	 */
	public static boolean isInALC(Collection<OWLClassExpression> expressions) {

		for (OWLClassExpression exp : expressions)
			if (!isInALC(exp))
				return false;

		return true;
	}

	/**
	 * Checks if the provided ClassExpression is an ALC axiom.<br>
	 * Currently supported constructors: ({@value #alcConstructs})
	 * 
	 * @param expression
	 * @return
	 */
	public static boolean isInALC(OWLClassExpression expression) {

		if (expression.getClassExpressionType() == ClassExpressionType.OBJECT_INTERSECTION_OF)
			return isInALC((OWLObjectIntersectionOf) expression);

		if (expression.getClassExpressionType() == ClassExpressionType.OBJECT_UNION_OF)
			return isInALC((OWLObjectUnionOf) expression);

		if (expression.getClassExpressionType() == ClassExpressionType.OWL_CLASS)
			return true;

		if (expression.getClassExpressionType() == ClassExpressionType.OBJECT_ONE_OF)
			return true;

		if (expression.getClassExpressionType() == ClassExpressionType.OBJECT_SOME_VALUES_FROM)
			return isInALC(((OWLObjectSomeValuesFrom) expression).getFiller());

		if (expression.getClassExpressionType() == ClassExpressionType.OBJECT_COMPLEMENT_OF)
			return isInALC(((OWLObjectComplementOf) expression).getOperand());

		if (expression.getClassExpressionType() == ClassExpressionType.OBJECT_ALL_VALUES_FROM)
			return isInALC(((OWLObjectAllValuesFrom) expression).getFiller());

		if (expression.getClassExpressionType() == ClassExpressionType.OBJECT_HAS_VALUE)
			return isInALC(((OWLObjectHasValue) expression).getFiller());

		return false;
	}

	public static boolean isInALC(OWLObjectIntersectionOf conjunction) {
		Set<Boolean> result = new HashSet<>();

		for (OWLClassExpression expression : conjunction.asConjunctSet())
			result.add(isInALC(expression));

		return result.size() <= 1 && result.iterator().next();
	}

	public static boolean isInALC(OWLObjectUnionOf conjunction) {
		Set<Boolean> result = new HashSet<>();

		for (OWLClassExpression expression : conjunction.asDisjunctSet())
			result.add(isInALC(expression));

		return result.size() <= 1 && result.iterator().next();
	}

	public static boolean isInALC(OWLIndividual individual) {
		return true;
	}

	/*
	 * General
	 **/

	private static Collection<OWLClassExpression> parseAxiom(OWLAxiom axiom) {
		Set<OWLClassExpression> expressions = new HashSet<>();

		if (axiom.getAxiomType() == AxiomType.SUBCLASS_OF) {
			expressions.add(((OWLSubClassOfAxiom) axiom).getSubClass());
			expressions.add(((OWLSubClassOfAxiom) axiom).getSuperClass());

		} else if (axiom.getAxiomType() == AxiomType.EQUIVALENT_CLASSES)
			expressions.addAll(((OWLEquivalentClassesAxiom) axiom).getClassExpressions());

		else
			assert false : "Axiom is not supported!";

		return expressions;
	}

}
