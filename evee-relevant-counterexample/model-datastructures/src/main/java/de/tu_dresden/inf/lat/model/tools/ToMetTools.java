package de.tu_dresden.inf.lat.model.tools;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectInverseOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

/**
 * @author Christian Alrabbaa
 *
 */
public class ToMetTools {

	/*
	 * Things we have so far: A Er.Top Er.Bot Er.B Er-.Top Er-.Bot Er-.B ~A ~Er.Top
	 * ~Er.Bot ~Er.B ~Er-.Top ~Er-.Bot ~Er-.B Ar-.Bot Ar-.Top Ar-.~B Ar.Bot Ar.Top
	 * Ar.~B
	 * 
	 * Range, Domain, and SubClassOf(X, Y), Disjoint Classes, Equivalent Classes .
	 **/

	/*
	 * TODO Missing and needed -> Sub Object Property, Class Assertions.
	 **/

	/*
	 * Missing but not currently needed -> Annotation Assertions, Inverse Object
	 * Property, Functional Object Property, Inverse Functional Object Property,
	 * Object Union Of, Object has value (Er.{a}??), Declaration, Transitive Object
	 * Property
	 **/

	// TODO still need to handle conjunction

	private static final Logger logger = Logger.getLogger(ToMetTools.class);

	private ToMetTools() {

	}

	private static class LazyHolder {
		static ToMetTools instance = new ToMetTools();
	}

	public static ToMetTools getInstance() {
		return LazyHolder.instance;
	}

//	/**
//	 * @param className
//	 * @return
//	 */
//	public String forceInequal(OWLClass className) {
//
//		if (className.isOWLThing() || className.isOWLNothing())
//			return "";
//		return getInequalityRule(getShortName(className.getIRI())) + "\n";
//	}
//
//	/**
//	 * @param metImplication
//	 * @return
//	 */
//	private String getInequalityRule(String classNameStr) {
//
//		String rulePrefix = "@l TRUE /";
//
//		String ruleSuffix = " priority " + 7 + " $;";
//
//		String ruleBody = " @l " + surround("~ " + classNameStr) + " $| @l " + surround(classNameStr);
//
//		return rulePrefix + ruleBody + ruleSuffix;
//	}

	/**
	 * @param axiom
	 * @return
	 */
	public String getMetCalculusRule(OWLAxiom axiom) {

		String implication = getMetImplication(axiom);
		if (!implication.isEmpty())
			return getRule(implication) + "\n";
		return "";
	}

	public String getMetInstance(OWLObject object) {
//		if (object instanceof OWLAxiom && !(object instanceof OWLClassAssertionAxiom))
//			return instance(getMetImplication((OWLAxiom) object));
//		if (object instanceof OWLClassAssertionAxiom)
//			return toMetString((OWLClassAssertionAxiom) object);
		if (object instanceof OWLClassExpression)
			return instance(getMetClassExpression((OWLClassExpression) object));

		logger.info("OWL Object is not supported yet!");
		return "";
	}

	public String getMetClassExpression(OWLClassExpression clsExp) {

		if (clsExp.getClassExpressionType() == ClassExpressionType.OWL_CLASS) {
			return toMetString((OWLClass) clsExp);
		}

		if (clsExp.getClassExpressionType() == ClassExpressionType.OBJECT_COMPLEMENT_OF) {
			return toMetString((OWLObjectComplementOf) clsExp);
		}

		if (clsExp.getClassExpressionType() == ClassExpressionType.OBJECT_SOME_VALUES_FROM) {
			return toMetString((OWLObjectSomeValuesFrom) clsExp);
		}

		if (clsExp.getClassExpressionType() == ClassExpressionType.OBJECT_ALL_VALUES_FROM) {
			return toMetString((OWLObjectAllValuesFrom) clsExp);
		}

		if (clsExp.getClassExpressionType() == ClassExpressionType.OBJECT_INTERSECTION_OF) {
			return toMetString((OWLObjectIntersectionOf) clsExp);
		}
		// TODO at least add disjunction and nominals

		logger.info("Class Expressions of type " + clsExp.getClassExpressionType() + " are not supported yet!");
		return "";
	}

	public String getMetImplication(OWLAxiom generalAxiom) {

		if (generalAxiom.getAxiomType() == AxiomType.SUBCLASS_OF)
			return toMetString((OWLSubClassOfAxiom) generalAxiom);

//		if (generalAxiom.getAxiomType() == AxiomType.CLASS_ASSERTION)
//			return toMetString((OWLClassAssertionAxiom) generalAxiom);

//		if (generalAxiom.getAxiomType() == AxiomType.SUB_OBJECT_PROPERTY)
//			return toMetString(((OWLSubObjectPropertyOfAxiom) generalAxiom).);
		logger.info("Axioms of type " + generalAxiom.getAxiomType() + " are not supported yet!");
		return "";
	}

//	private String toMetString(OWLSubObjectPropertyOfAxiom generalAxiom) {
//		// TODO Auto-generated method stub
//		return null;
//	}

	private String toMetString(OWLClassAssertionAxiom clsAssertAxiom) {

		String lhs = toMetString(clsAssertAxiom.getIndividual());
		String rhs = getMetClassExpression(clsAssertAxiom.getClassExpression());
		return implication(lhs, rhs);

	}

	/**
	 * @param metImplication
	 * @return
	 */
	private String getRule(String metImplication) {

		String rulePrefix = "@l TRUE / @l ";

		// TODO parametrise the priority
		String ruleSuffix = " priority " + 5 + " $;";

		return rulePrefix + surround(metImplication) + ruleSuffix;
	}

	private String instance(String expression) {

		String element = "@e";
		if (!expression.isEmpty())
			return element + " " + expression + "\n";
		return "";
	}

//	private String instance(String expression, OWLNamedIndividual individual) {
//
//		String element = "@" + getShortName(individual.getIRI());
//		if (!expression.isEmpty())
//			return element + " " + expression + "\n";
//		return "";
//	}

	private String toMetString(OWLSubClassOfAxiom subClassOf) {

		return implication(getMetClassExpression(subClassOf.getSubClass()),
				getMetClassExpression(subClassOf.getSuperClass()));
	}

	private String toMetString(OWLObjectAllValuesFrom oWLAllValFrom) {

		OWLObjectPropertyExpression pExp = oWLAllValFrom.getProperty();
		if (pExp instanceof OWLObjectProperty)
			return forall(toMetString((OWLObjectProperty) pExp), getMetClassExpression(oWLAllValFrom.getFiller()));
		else
			return forall(toMetString((OWLObjectInverseOf) pExp), getMetClassExpression(oWLAllValFrom.getFiller()));
	}

	private String toMetString(OWLObjectComplementOf clsExp) {

		return surround("~ " + getMetClassExpression(clsExp.getOperand()));

	}

	private String toMetString(OWLObjectIntersectionOf clsExp) {

		StringBuffer res = new StringBuffer();
		clsExp.asConjunctSet().forEach(conjunct -> {
			res.append(surround(getMetClassExpression(conjunct)) + " & ");
		});

		return res.toString().substring(0, res.lastIndexOf(" & "));

	}

	private String toMetString(OWLObjectSomeValuesFrom oWLSomeValFrom) {

		OWLObjectPropertyExpression pExp = oWLSomeValFrom.getProperty();
		if (pExp instanceof OWLObjectProperty)
			return exists(toMetString((OWLObjectProperty) pExp), getMetClassExpression(oWLSomeValFrom.getFiller()));
		else
			return exists(toMetString((OWLObjectInverseOf) pExp), getMetClassExpression(oWLSomeValFrom.getFiller()));

	}

	private String toMetString(OWLObjectInverseOf pExp) {

		assert pExp.getNamedProperty() instanceof OWLObjectProperty : "Nested inverse Expression";
		return getShortName(pExp.getNamedProperty().getIRI()) + "-";
	}

	private String toMetString(OWLObjectProperty pExp) {

		return getShortName(pExp.getIRI());
	}

	private String toMetString(OWLClass oWLClass) {

		if (oWLClass.isOWLThing())
			return "true";

		if (oWLClass.isOWLNothing())
			return "false";

		return getShortName(oWLClass.getIRI());
	}

	private String toMetString(OWLIndividual Individual) {

		if (Individual instanceof OWLNamedIndividual) {
			return surroundC(getShortName(((OWLNamedIndividual) Individual).getIRI()));
		}
		logger.info("Individuals of type OWLAnonymousIndividual are not supported yet!");
		return "";
	}

	private String exists(String role, String clsExp) {

		if (!role.isEmpty() && !clsExp.isEmpty())
			return surround("exists " + role + "." + clsExp);
		return "";
	}

	private String forall(String role, String clsExp) {

		if (!role.isEmpty() && !clsExp.isEmpty())
			return surround("forall " + role + "." + clsExp);
		return "";
	}

	private String implication(String lhs, String rhs) {

		if (!lhs.isEmpty() && !rhs.isEmpty())
			return surround(lhs + " -> " + rhs);
		return "";
	}

	private String getShortName(IRI name) {
		// TODO it is better to have a map
		// TODO "http:" is problematic, create a map to new names and use those instead
		return name.getShortForm();
	}

	private String surround(String str) {

		return "( " + str + " )";
	}

	private String surroundC(String str) {

		return "{" + str + "}";
	}
}
