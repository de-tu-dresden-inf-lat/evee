/**
 * 
 */
package de.tu_dresden.inf.lat.proofs.tools;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx;
import org.semanticweb.owlapi.model.OWLDataAllValuesFrom;
import org.semanticweb.owlapi.model.OWLDataExactCardinality;
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLDataMaxCardinality;
import org.semanticweb.owlapi.model.OWLDataMinCardinality;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectExactCardinality;
import org.semanticweb.owlapi.model.OWLObjectHasSelf;
import org.semanticweb.owlapi.model.OWLObjectHasValue;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

/**
 * @author stefborg
 *
 */
public class OWLSubClassExpressionSplitter implements OWLClassExpressionVisitorEx<Collection<OWLSubClassOfAxiom>> {

	OWLClassExpression sup;
	Set<OWLEntity> sig;
	ConceptNameGenerator A;
	Collection<OWLSubClassOfAxiom> newAxioms;

	public OWLSubClassExpressionSplitter(OWLClassExpression sup, Set<OWLEntity> sig, ConceptNameGenerator A) {
		this.sup = sup;
		this.sig = sig;
		this.A = A;
		this.newAxioms = new LinkedList<>();
	}

	private Collection<OWLSubClassOfAxiom> nullOrNewAxioms() {
		if (newAxioms.isEmpty()) {
			return null;
		} else {
			return newAxioms;
		}
	}

	@Override
	public Collection<OWLSubClassOfAxiom> visit(OWLClass ce) {
		return null;
	}

	@Override
	public Collection<OWLSubClassOfAxiom> visit(OWLObjectIntersectionOf ce) {
		if (!ce.getOperands().stream().anyMatch(e -> OWLTools.isAuxiliary(e, sig))) {
			newAxioms.add(OWLTools.odf
					.getOWLSubClassOfAxiom(OWLTools.odf.getOWLObjectIntersectionOf(ce.getOperands().stream().map(e -> {
						OWLClass ar = A.next();
						newAxioms.add(OWLTools.odf.getOWLSubClassOfAxiom(e, ar));
						return ar;
					}).collect(Collectors.toSet())), sup));
		}
		return nullOrNewAxioms();
	}

	@Override
	public Collection<OWLSubClassOfAxiom> visit(OWLObjectUnionOf ce) {
		ce.getOperands().stream().filter(e -> !e.isOWLNothing()).map(e -> OWLTools.odf.getOWLSubClassOfAxiom(e, sup))
				.forEach(newAxioms::add);
		return nullOrNewAxioms();
	}

	@Override
	public Collection<OWLSubClassOfAxiom> visit(OWLObjectComplementOf ce) {
		if (!ce.getOperand().isOWLThing() && !OWLTools.isAuxiliary(ce.getOperand(), sig)) {
			OWLClass ac = A.next();
			newAxioms.add(OWLTools.odf.getOWLSubClassOfAxiom(OWLTools.odf.getOWLObjectComplementOf(ac), sup));
			newAxioms.add(OWLTools.odf.getOWLSubClassOfAxiom(ac, ce.getOperand()));
		}
		return nullOrNewAxioms();
	}

	@Override
	public Collection<OWLSubClassOfAxiom> visit(OWLObjectSomeValuesFrom ce) {
		if (!ce.getFiller().isOWLNothing() && !OWLTools.isAuxiliary(ce.getFiller(), sig)) {
			OWLClass ac = A.next();
			newAxioms.add(
					OWLTools.odf.getOWLSubClassOfAxiom(OWLTools.odf.getOWLObjectSomeValuesFrom(ce.getProperty(), ac), sup));
			newAxioms.add(OWLTools.odf.getOWLSubClassOfAxiom(ce.getFiller(), ac));
		}
		return nullOrNewAxioms();
	}

	@Override
	public Collection<OWLSubClassOfAxiom> visit(OWLObjectAllValuesFrom ce) {
		if (!ce.getFiller().isOWLNothing() && !OWLTools.isAuxiliary(ce.getFiller(), sig)) {
			OWLClass ac = A.next();
			newAxioms.add(
					OWLTools.odf.getOWLSubClassOfAxiom(OWLTools.odf.getOWLObjectAllValuesFrom(ce.getProperty(), ac), sup));
			newAxioms.add(OWLTools.odf.getOWLSubClassOfAxiom(ce.getFiller(), ac));
		}
		return nullOrNewAxioms();
	}

	@Override
	public Collection<OWLSubClassOfAxiom> visit(OWLObjectOneOf ce) {
		return null;
	}

	@Override
	public Collection<OWLSubClassOfAxiom> visit(OWLObjectHasValue ce) {
		OWLClass ac = A.next();
		newAxioms.add(OWLTools.odf.getOWLSubClassOfAxiom(OWLTools.odf.getOWLObjectSomeValuesFrom(ce.getProperty(), ac), sup));
		newAxioms.add(OWLTools.odf.getOWLSubClassOfAxiom(OWLTools.odf.getOWLObjectOneOf(ce.getFiller()), ac));
		return nullOrNewAxioms();
	}

	// TODO support more constructors

//	@Override
//	public <T> Collection<OWLSubClassOfAxiom> doDefault(T object) {
//		throw new UnsupportedOperationException("Unsupported class expression type: " + object);
//	}

	@Override
	public Collection<OWLSubClassOfAxiom> visit(OWLObjectMinCardinality ce) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<OWLSubClassOfAxiom> visit(OWLObjectExactCardinality ce) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<OWLSubClassOfAxiom> visit(OWLObjectMaxCardinality ce) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<OWLSubClassOfAxiom> visit(OWLObjectHasSelf ce) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<OWLSubClassOfAxiom> visit(OWLDataSomeValuesFrom ce) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<OWLSubClassOfAxiom> visit(OWLDataAllValuesFrom ce) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<OWLSubClassOfAxiom> visit(OWLDataHasValue ce) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<OWLSubClassOfAxiom> visit(OWLDataMinCardinality ce) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<OWLSubClassOfAxiom> visit(OWLDataExactCardinality ce) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<OWLSubClassOfAxiom> visit(OWLDataMaxCardinality ce) {
		// TODO Auto-generated method stub
		return null;
	}

}
