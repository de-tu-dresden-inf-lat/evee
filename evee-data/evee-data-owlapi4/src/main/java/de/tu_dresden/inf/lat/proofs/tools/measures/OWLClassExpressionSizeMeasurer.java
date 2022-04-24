package de.tu_dresden.inf.lat.proofs.tools.measures;

import java.util.Collection;
import java.util.List;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx;
import org.semanticweb.owlapi.model.OWLDataAllValuesFrom;
import org.semanticweb.owlapi.model.OWLDataExactCardinality;
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLDataMaxCardinality;
import org.semanticweb.owlapi.model.OWLDataMinCardinality;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectExactCardinality;
import org.semanticweb.owlapi.model.OWLObjectHasSelf;
import org.semanticweb.owlapi.model.OWLObjectHasValue;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectInverseOf;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;

public class OWLClassExpressionSizeMeasurer implements OWLClassExpressionVisitorEx<Double> {

	public Double visit(Collection<OWLClassExpression> exprs) {
		return exprs.stream().mapToDouble(c -> c.accept(this)).sum();
	}

	public Double visit(OWLObjectPropertyExpression p) {
		if (p instanceof OWLObjectProperty) {
			return 1d;
		}
		if (p instanceof OWLObjectInverseOf) {
			return 1d + visit(p.getInverseProperty());
		}
		return 0d;
	}

	public Double visit(List<OWLObjectPropertyExpression> l) {
		return l.stream().mapToDouble(this::visit).sum();
	}

	public Double visitIndividuals(Collection<OWLIndividual> s) {
		return (double) s.size();
	}

	private Double visit(OWLIndividual ind) {
		return 1d;
	}

	@Override
	public Double visit(OWLClass ce) {
		return 1d;
	}

	@Override
	public Double visit(OWLObjectIntersectionOf ce) {
		return visit(ce.getOperands()) + ((double) ce.getOperands().size()) - 1d;
	}

	@Override
	public Double visit(OWLObjectUnionOf ce) {
		return visit(ce.getOperands()) + ((double) ce.getOperands().size()) - 1d;
	}

	@Override
	public Double visit(OWLObjectComplementOf ce) {
		return 1d + ce.getOperand().accept(this);
	}

	@Override
	public Double visit(OWLObjectSomeValuesFrom ce) {
		return 1d + visit(ce.getProperty()) + ce.getFiller().accept(this);
	}

	@Override
	public Double visit(OWLObjectAllValuesFrom ce) {
		return 1d + visit(ce.getProperty()) + ce.getFiller().accept(this);
	}

	@Override
	public Double visit(OWLObjectHasValue ce) {
		return 1d + visit(ce.getProperty()) + visit(ce.getFiller());
	}

	@Override
	public Double visit(OWLObjectOneOf ce) {
		return 1d + visitIndividuals(ce.getIndividuals());
	}

	// TODO support more constructors

	@Override
	public Double visit(OWLObjectMinCardinality ce) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double visit(OWLObjectExactCardinality ce) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double visit(OWLObjectMaxCardinality ce) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double visit(OWLObjectHasSelf ce) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double visit(OWLDataSomeValuesFrom ce) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double visit(OWLDataAllValuesFrom ce) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double visit(OWLDataHasValue ce) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double visit(OWLDataMinCardinality ce) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double visit(OWLDataExactCardinality ce) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double visit(OWLDataMaxCardinality ce) {
		// TODO Auto-generated method stub
		return null;
	}

}
