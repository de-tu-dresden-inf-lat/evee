/**
 * 
 */
package de.tu_dresden.inf.lat.evee.proofs.tools.evaluators.inferences;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLPropertyAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInference;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInferenceEvaluator;
import de.tu_dresden.inf.lat.evee.proofs.tools.ConceptNameGenerator;
import de.tu_dresden.inf.lat.evee.proofs.tools.OWLAxiomSplitter;
import de.tu_dresden.inf.lat.evee.proofs.tools.OWLSubClassExpressionUnfolder;
import de.tu_dresden.inf.lat.evee.proofs.tools.OWLSuperClassExpressionUnfolder;
import de.tu_dresden.inf.lat.evee.proofs.tools.OWLTools;
import de.tu_dresden.inf.lat.evee.proofs.tools.ProofTools;

/**
 * Count the (average) number of OWLSubClassOfAxioms with a complex left-hand
 * side in a laconic representation of the premises.
 * 
 * @author stefborg
 *
 */
public class C11LaconicGCICountEvaluator implements IInferenceEvaluator<OWLAxiom> {

	@Override
	public Double evaluate(IInference<OWLAxiom> inf) {
		if (inf.getConclusion() instanceof OWLPropertyAxiom) {
			// role axioms cannot be explained by ELK nor the black-box justification
			// algorithm -> but we know that there are no complex left-hand sides involved
			return 0d;
		}
		Set<OWLEntity> sig = ProofTools.getSignature(inf);
		Collection<OWLAxiom> delta = computeDelta(new LinkedList<>(inf.getPremises()));
		Collection<OWLAxiom> deltaPlus = computeDeltaPlus(delta);
		// TODO: replace by Justifier interface to be able to switch between HermiT and ELK?
		Set<? extends Set<? extends OWLAxiom>> just = OWLTools.computeJustifications(deltaPlus, inf.getConclusion());
		return just.stream().map(j -> unfoldFreshNames(j, sig)).filter(j -> isLaconic(j, inf.getConclusion()))
				.mapToDouble(j -> j.stream().filter(ax -> ax instanceof OWLSubClassOfAxiom)
						.filter(ax -> ((OWLSubClassOfAxiom) ax).getSubClass().isAnonymous()).count())
				.summaryStatistics().getAverage();
	}

	@Override
	public String getDescription() {
		return "C11: Number of laconic GCIs";
	}

	private static Collection<OWLAxiom> computeDelta(Collection<OWLAxiom> j) {
		Set<OWLEntity> sig = OWLTools.getSignature(j);
		OWLAxiomSplitter splitter = new OWLAxiomSplitter(sig, new ConceptNameGenerator("delta", false, sig));
		boolean changed = true;
		while (changed) {
			changed = false;
			Collection<OWLAxiom> replaced = new LinkedList<>();
			for (OWLAxiom ax : j) {
				Collection<? extends OWLAxiom> newAxioms = ax.accept(splitter);
				if (newAxioms == null) {
					replaced.add(ax);
				} else {
					changed = true;
					replaced.addAll(newAxioms);
				}
			}
			j = replaced;
		}
		return j;
	}

	private static Collection<OWLAxiom> computeDeltaPlus(Collection<OWLAxiom> delta) {
		Collection<OWLAxiom> newAxioms = new LinkedList<>();
		for (OWLAxiom ax : delta) {
			if (ax instanceof OWLSubClassOfAxiom) {
				OWLSubClassOfAxiom sco = (OWLSubClassOfAxiom) ax;
				if (sco.getSuperClass() instanceof OWLObjectSomeValuesFrom) {
					OWLObjectPropertyExpression r = ((OWLObjectSomeValuesFrom) sco.getSuperClass()).getProperty();
					OWLClassExpression c = ((OWLObjectSomeValuesFrom) sco.getSuperClass()).getFiller();
					if (!c.isOWLThing()) {
						newAxioms.add(OWLTools.odf.getOWLSubClassOfAxiom(sco.getSubClass(),
								OWLTools.odf.getOWLObjectSomeValuesFrom(r, OWLTools.odf.getOWLThing())));
					}
				}
				if (sco.getSubClass() instanceof OWLObjectAllValuesFrom) {
					OWLObjectPropertyExpression r = ((OWLObjectAllValuesFrom) sco.getSubClass()).getProperty();
					OWLClassExpression c = ((OWLObjectAllValuesFrom) sco.getSubClass()).getFiller();
					if (!c.isOWLNothing()) {
						newAxioms.add(OWLTools.odf.getOWLSubClassOfAxiom(
								OWLTools.odf.getOWLObjectAllValuesFrom(r, OWLTools.odf.getOWLNothing()),
								sco.getSuperClass()));
					}
				}
			}
		}
		delta.addAll(newAxioms);
		return delta;
	}

	private static Set<OWLAxiom> unfoldFreshNames(Set<? extends OWLAxiom> j, Set<OWLEntity> sig) {
		OWLOntology o = OWLTools.createOntology(j.stream());
		OWLSubClassExpressionUnfolder subUnfolder = new OWLSubClassExpressionUnfolder(o, sig);
		OWLSuperClassExpressionUnfolder supUnfolder = new OWLSuperClassExpressionUnfolder(o, sig);
		subUnfolder.setSupUnfolder(supUnfolder);
		supUnfolder.setSubUnfolder(subUnfolder);
		Set<OWLAxiom> res = j.stream().map(ax -> unfold(ax, subUnfolder, supUnfolder)).filter(ax -> ax != null)
				.collect(Collectors.toSet());
		OWLTools.manager.removeOntology(o);
		return res;
	}

	private static OWLAxiom unfold(OWLAxiom ax, OWLSubClassExpressionUnfolder subUnfolder,
			OWLSuperClassExpressionUnfolder supUnfolder) {
		if (!(ax instanceof OWLSubClassOfAxiom)) {
			return ax;
		}
		OWLClassExpression sub = ((OWLSubClassOfAxiom) ax).getSubClass().accept(subUnfolder);
		OWLClassExpression sup = ((OWLSubClassOfAxiom) ax).getSuperClass().accept(supUnfolder);
		if ((sub == null) || (sup == null)) {
			return null;
		}
		return OWLTools.odf.getOWLSubClassOfAxiom(sub, sup);

	}

	private static boolean isLaconic(Set<OWLAxiom> j, OWLAxiom c) {
		Collection<OWLAxiom> delta = computeDelta(j);
		for (OWLAxiom ax : delta) {
			if (OWLTools.entails(delta.stream().filter(a -> !a.equals(ax)), c)) {
				return false;
			}
		}
		return true;
	}

}
