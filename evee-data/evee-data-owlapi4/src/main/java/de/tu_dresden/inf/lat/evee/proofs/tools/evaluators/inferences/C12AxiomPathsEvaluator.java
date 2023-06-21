/**
 * 
 */
package de.tu_dresden.inf.lat.evee.proofs.tools.evaluators.inferences;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.model.parameters.Navigation;

import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInference;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInferenceEvaluator;
import de.tu_dresden.inf.lat.evee.general.tools.OWLTools;

/**
 * Count the number of maximal length expression paths among the premises.
 * 
 * @author stefborg
 *
 */
public class C12AxiomPathsEvaluator implements IInferenceEvaluator<OWLAxiom> {

	@Override
	public Double evaluate(IInference<OWLAxiom> inf) {
		return (double) getExpressionPaths(inf).size();
	}

	@Override
	public String getDescription() {
		return "C12: Number of expression paths";
	}

	public static Set<List<OWLAxiom>> getExpressionPaths(IInference<OWLAxiom> inf) {
		OWLOntology o = OWLTools.createOntology(inf.getPremises().stream());
		Set<List<OWLAxiom>> paths = inf.getPremises().stream().map(ax -> Collections.singletonList((OWLAxiom) ax))
				.collect(Collectors.toSet());
		boolean changed = true;
		while (changed) {
			changed = false;
			Set<List<OWLAxiom>> replaced = new HashSet<>();
			for (List<OWLAxiom> p : paths) {
				Set<List<OWLAxiom>> newPaths = extend(p, o);
				if (newPaths == null) {
					replaced.add(p);
				} else {
					changed = true;
					replaced.addAll(newPaths);
				}
			}
			paths = replaced;
		}
		OWLTools.manager.removeOntology(o);
		removeDuplicates(paths);
		return paths;
	}

	private static void removeDuplicates(Set<List<OWLAxiom>> paths) {
		boolean changed = true;
		while (changed) {
			changed = false;
			List<OWLAxiom> duplicate = null;
			outer: for (List<OWLAxiom> p1 : paths) {
				for (List<OWLAxiom> p2 : paths) {
					if (!p1.equals(p2)) {
						if (p2.stream().allMatch(p1::contains)) {
							duplicate = p2;
							break outer;
						}
					}
				}
			}
			if (duplicate != null) {
				paths.remove(duplicate);
				changed = true;
			}
		}
	}

	private static Set<List<OWLAxiom>> extend(List<OWLAxiom> path, OWLOntology o) {
		boolean changed = false;
		Set<List<OWLAxiom>> newPaths = extendDown(path, o);
		if (newPaths == null) {
			newPaths = Collections.singleton(path);
		} else {
			changed = true;
		}
		Set<List<OWLAxiom>> extendedPaths = new HashSet<>();
		for (List<OWLAxiom> path2 : newPaths) {
			Set<List<OWLAxiom>> newPaths2 = extendUp(path2, o);
			if (newPaths2 == null) {
				extendedPaths.add(path2);
			} else {
				changed = true;
				extendedPaths.addAll(newPaths2);
			}
		}
		if (changed) {
			return extendedPaths;
		} else {
			return null;
		}
	}

	private static Set<List<OWLAxiom>> extendDown(List<OWLAxiom> path, OWLOntology o) {
		OWLAxiom bottom = path.get(0);
		Set<List<OWLAxiom>> newPaths = null;

		if (bottom instanceof OWLSubClassOfAxiom) {
			if (!((OWLSubClassOfAxiom) bottom).getSuperClass().isAnonymous()) {
				OWLClassExpression sub = ((OWLSubClassOfAxiom) bottom).getSubClass();
				newPaths = o.getAxioms(OWLSubClassOfAxiom.class, sub, Imports.EXCLUDED, Navigation.IN_SUPER_POSITION)
						.stream().filter(ax -> ax.getSuperClass().equals(sub)).filter(ax -> !path.contains(ax))
						.filter(ax -> !ax.getSubClass().isAnonymous()).map(ax -> extendList(ax, path))
						.collect(Collectors.toSet());
			}
		}
		if (bottom instanceof OWLSubObjectPropertyOfAxiom) {
			if (!((OWLSubObjectPropertyOfAxiom) bottom).getSuperProperty().isAnonymous()) {
				OWLObjectPropertyExpression sub = ((OWLSubObjectPropertyOfAxiom) bottom).getSubProperty();
				newPaths = o
						.getAxioms(OWLSubObjectPropertyOfAxiom.class, sub, Imports.EXCLUDED,
								Navigation.IN_SUPER_POSITION)
						.stream().filter(ax -> ax.getSuperProperty().equals(sub)).filter(ax -> !path.contains(ax))
						.filter(ax -> !ax.getSubProperty().isAnonymous()).map(ax -> extendList(ax, path))
						.collect(Collectors.toSet());
			}
		}

		if ((newPaths != null) && (newPaths.size() > 0)) {
			return newPaths;
		} else {
			return null;
		}
	}

	private static Set<List<OWLAxiom>> extendUp(List<OWLAxiom> path, OWLOntology o) {
		OWLAxiom top = path.get(path.size() - 1);
		Set<List<OWLAxiom>> newPaths = null;

		if (top instanceof OWLSubClassOfAxiom) {
			if (!((OWLSubClassOfAxiom) top).getSubClass().isAnonymous()) {
				OWLClassExpression sup = ((OWLSubClassOfAxiom) top).getSuperClass();
				newPaths = o.getAxioms(OWLSubClassOfAxiom.class, sup, Imports.EXCLUDED, Navigation.IN_SUB_POSITION)
						.stream().filter(ax -> ax.getSubClass().equals(sup)).filter(ax -> !path.contains(ax))
						.filter(ax -> !ax.getSuperClass().isAnonymous()).map(ax -> extendList(path, ax))
						.collect(Collectors.toSet());
			}
		}
		if (top instanceof OWLSubObjectPropertyOfAxiom) {
			if (!((OWLSubObjectPropertyOfAxiom) top).getSubProperty().isAnonymous()) {
				OWLObjectPropertyExpression sup = ((OWLSubObjectPropertyOfAxiom) top).getSuperProperty();
				newPaths = o
						.getAxioms(OWLSubObjectPropertyOfAxiom.class, sup, Imports.EXCLUDED, Navigation.IN_SUB_POSITION)
						.stream().filter(ax -> ax.getSubProperty().equals(sup)).filter(ax -> !path.contains(ax))
						.filter(ax -> !ax.getSuperProperty().isAnonymous()).map(ax -> extendList(path, ax))
						.collect(Collectors.toSet());
			}
		}

		if ((newPaths != null) && (newPaths.size() > 0)) {
			return newPaths;
		} else {
			return null;
		}
	}

	private static <T> List<T> extendList(T el, List<T> list) {
		List<T> newList = new LinkedList<>();
		newList.add(el);
		newList.addAll(list);
		return newList;
	}

	private static <T> List<T> extendList(List<T> list, T el) {
		List<T> newList = new LinkedList<>(list);
		newList.add(el);
		return newList;
	}

}
