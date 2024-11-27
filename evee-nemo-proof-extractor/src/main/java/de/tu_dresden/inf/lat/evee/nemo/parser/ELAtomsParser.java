package de.tu_dresden.inf.lat.evee.nemo.parser;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import de.tu_dresden.inf.lat.evee.nemo.parser.exceptions.ConceptTranslationError;
import de.tu_dresden.inf.lat.evee.nemo.parser.tools.OWLHelper;
import org.semanticweb.owlapi.model.*;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * @author Christian Alrabbaa
 *
 */
public class ELAtomsParser {
	private final OWLHelper owlHelper = OWLHelper.getInstance();

	private static final String inferredSubClassOf = "<http://rulewerk.semantic-web.org/inferred/subClassOf>",
	normalFormSubClassOf = "<http://rulewerk.semantic-web.org/normalForm/subClassOf>",
	prepareSubClassOf = "prepareSco";
	private static final String xePredicate = "<http://rulewerk.semantic-web.org/inferred/xe>";
	private static final String aux = "aux",
			auxSubExists = "aux_subsubExt";
	private static final String conjunction = "<http://rulewerk.semantic-web.org/normalForm/conj>";
	private static final String exists = "<http://rulewerk.semantic-web.org/normalForm/exists>";
	private static final Set<String> subClassOf = Sets.newHashSet(inferredSubClassOf,
			normalFormSubClassOf,prepareSubClassOf);
	private static final Set<String> auxSubClassOf =Sets.newHashSet(aux, auxSubExists);

	// Pushing the EL calculus
	private static final String conjSubClassOf = "<http://rulewerk.semantic-web.org/normalForm/subClassConj>";
	private static final String subClassOfExists = "<http://rulewerk.semantic-web.org/normalForm/subClassEx>";
	private static final String supClassOfExists = "<http://rulewerk.semantic-web.org/normalForm/supClassEx>";
	private static final String subProperty = "<http://rulewerk.semantic-web.org/normalForm/subProp>";
	private static final String subPropertyChain = "<http://rulewerk.semantic-web.org/normalForm/subPropChain>";

	private final OWLSubClassOfAxiom defaultAxiom = owlHelper.getOWLSubClassOfAxiom(owlHelper.getOWLBot(),
			owlHelper.getOWLTop());

	public OWLSubClassOfAxiom getDefaultAxiom() {
		return this.defaultAxiom;
	}

	private ELAtomsParser() {}

	private static class LazyHolder {
		static ELAtomsParser instance = new ELAtomsParser();
	}

	public static ELAtomsParser getInstance() {
		return LazyHolder.instance;
	}

	public OWLAxiom parse(String axiomPredicateName, List<String> args) {

		if (auxSubClassOf.contains(axiomPredicateName))
			return parseAux(args);
		else if (axiomPredicateName.equals(xePredicate))
			return parseXE(args);
		else if (subClassOf.contains(axiomPredicateName))
			return parseSubclassOf(args);

		// Pushing the EL calculus
		else if (axiomPredicateName.equals(conjSubClassOf))
			return parseConjSubClassOf(args);
		else if (axiomPredicateName.equals(subClassOfExists))
			return parseSubClassOfExistential(args);
		else if (axiomPredicateName.equals(supClassOfExists))
			return parseSupClassOfExistential(args);
		else if (axiomPredicateName.equals(subProperty))
			return parseSubProperty(args);
		else if (axiomPredicateName.equals(subPropertyChain))
			return parseSubPropertyChain(args);

		return defaultAxiom;
	}

	private OWLAxiom parseSubPropertyChain(List<String> args) {
		return owlHelper.getOWLSubPropertyChainOfAxiom(Lists.newLinkedList(Arrays.asList(getProp(args.get(0)),
				getProp(args.get(1)))), getProp(args.get(2)));
	}

	private OWLAxiom parseSubProperty(List<String> args) {
		return owlHelper.getOWLSubObjectPropertyAxiom(getProp(args.get(0)), getProp(args.get(1)));
	}

	private OWLAxiom parseSubClassOfExistential(List<String> args) {
		return owlHelper.getOWLSubClassOfAxiom(
				owlHelper.getOWLExistentialRestriction(getProp(args.get(0)), getCls(args.get(1))), getCls(args.get(2)));
	}

	private OWLAxiom parseSupClassOfExistential(List<String> args) {
		return owlHelper.getOWLSubClassOfAxiom(getCls(args.get(0)),
				owlHelper.getOWLExistentialRestriction(getProp(args.get(1)), getCls(args.get(2))));
	}

	private OWLAxiom parseConjSubClassOf(List<String> args) {
		return owlHelper.getOWLSubClassOfAxiom(
				owlHelper.getOWLConjunction(Sets.newHashSet(getCls(args.get(0)), getCls(args.get(1)))),
				getCls(args.get(2)));
	}

	private OWLAxiom parseAux(List<String> args) {
		return owlHelper.getOWLSubClassOfAxiom(
				owlHelper.getOWLExistentialRestriction(getProp(args.get(1)), getCls(args.get(0))), getCls(args.get(2)));
	}

	private OWLSubClassOfAxiom parseSubclassOf(List<String> args) {
		return owlHelper.getOWLSubClassOfAxiom(getCls(args.get(0)), getCls(args.get(1)));
	}

	private OWLSubClassOfAxiom parseXE(List<String> args) {
		return owlHelper.getOWLSubClassOfAxiom(getCls(args.get(2)),
				owlHelper.getOWLExistentialRestriction(getProp(args.get(1)), getCls(args.get(0))));

	}

	public OWLClassExpression parseClassExpression(String clsPredicateName, List<Object> args)
			throws ConceptTranslationError {
		if (clsPredicateName.equals(exists)) {
			return owlHelper.getOWLExistentialRestriction(getProp(args.get(1)), getCls(args.get(2)));
		}

		if (clsPredicateName.equals(conjunction)) {
			return owlHelper.getOWLConjunction(Sets.newHashSet(getCls(args.get(1)), getCls(args.get(2))));
		}

		throw new ConceptTranslationError("The following concept type \"" + clsPredicateName + "\" is not supported");
	}

	private OWLObjectPropertyExpression getProp(Object o) {
		if (o instanceof OWLObjectPropertyExpression)
			return (OWLObjectProperty) o;
		return owlHelper.getPropertyName((String) o);
	}

	private OWLClassExpression getCls(Object o) {
		if (o instanceof OWLClassExpression)
			return (OWLClassExpression) o;
		return owlHelper.getOWLConceptName((String) o);
	}

}
