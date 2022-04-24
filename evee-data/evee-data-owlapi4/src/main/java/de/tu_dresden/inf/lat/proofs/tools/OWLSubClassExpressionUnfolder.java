/**
 * 
 */
package de.tu_dresden.inf.lat.proofs.tools;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.semanticweb.owlapi.model.AxiomType;
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
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

/**
 * @author stefborg
 *
 */
public class OWLSubClassExpressionUnfolder implements OWLClassExpressionVisitorEx<OWLClassExpression> {

	OWLOntology o;
	Set<OWLEntity> sig;
	OWLSuperClassExpressionUnfolder supUnfolder;

	public OWLSubClassExpressionUnfolder(OWLOntology o, Set<OWLEntity> sig) {
		this.o = o;
		this.sig = sig;
	}

	public void setSupUnfolder(OWLSuperClassExpressionUnfolder supUnfolder) {
		this.supUnfolder = supUnfolder;
	}

	public Stream<OWLClassExpression> visit(Collection<OWLClassExpression> exprs) {
		return exprs.stream().map(c -> c.accept(this));
	}

	@Override
	public OWLClassExpression visit(OWLClass ce) {
		if (OWLTools.isAuxiliary(ce, sig)) {
			// search for definition of ce, i.e. all axioms where ce occurs on the rhs
			Collection<OWLSubClassOfAxiom> axs = o.getAxioms(AxiomType.SUBCLASS_OF).stream()
					.filter(ax -> ax.containsEntityInSignature(ce)).map(ax -> (OWLSubClassOfAxiom) ax)
					.collect(Collectors.toList());
			if (axs.stream()
					.noneMatch(ax -> ax.getSuperClass().getClassesInSignature().stream().anyMatch(ce::equals))) {
				return ce;
			}
			Set<OWLClassExpression> def = axs.stream().filter(ax -> ax.getSuperClass().equals(ce))
					.map(ax -> ax.getSubClass()).map(s -> s.accept(this)).collect(Collectors.toSet());
			if (def.contains(null)) {
				return null;
			}
			switch (def.size()) {
			case 0:
				return null;
			case 1:
				return def.iterator().next();
			default:
				return OWLTools.odf.getOWLObjectUnionOf(def);
			}
		} else {
			return ce;
		}
	}

	@Override
	public OWLClassExpression visit(OWLObjectIntersectionOf ce) {
		Set<OWLClassExpression> op = visit(ce.getOperands()).collect(Collectors.toSet());
		if (op.contains(null)) {
			return null;
		}
		return OWLTools.odf.getOWLObjectIntersectionOf(op);
	}

	@Override
	public OWLClassExpression visit(OWLObjectUnionOf ce) {
		Set<OWLClassExpression> op = visit(ce.getOperands()).collect(Collectors.toSet());
		if (op.contains(null)) {
			return null;
		}
		return OWLTools.odf.getOWLObjectUnionOf(op);
	}

	@Override
	public OWLClassExpression visit(OWLObjectComplementOf ce) {
		OWLClassExpression op = ce.getOperand().accept(supUnfolder);
		if (op == null) {
			return null;
		}
		return OWLTools.odf.getOWLObjectComplementOf(op);
	}

	@Override
	public OWLClassExpression visit(OWLObjectSomeValuesFrom ce) {
		OWLClassExpression op = ce.getFiller().accept(this);
		if (op == null) {
			return null;
		}
		return OWLTools.odf.getOWLObjectSomeValuesFrom(ce.getProperty(), op);
	}

	@Override
	public OWLClassExpression visit(OWLObjectAllValuesFrom ce) {
		OWLClassExpression op = ce.getFiller().accept(this);
		if (op == null) {
			return null;
		}
		return OWLTools.odf.getOWLObjectAllValuesFrom(ce.getProperty(), op);
	}

	@Override
	public OWLClassExpression visit(OWLObjectOneOf ce) {
		return ce;
	}

	@Override
	public OWLClassExpression visit(OWLObjectHasValue ce) {
		return ce;
	}

	// TODO support more constructors

//	@Override
//	public <T> OWLClassExpression doDefault(T object) {
//		throw new UnsupportedOperationException("Unsupported class expression type: " + object);
//	}

	@Override
	public OWLClassExpression visit(OWLObjectMinCardinality ce) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLClassExpression visit(OWLObjectExactCardinality ce) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLClassExpression visit(OWLObjectMaxCardinality ce) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLClassExpression visit(OWLObjectHasSelf ce) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLClassExpression visit(OWLDataSomeValuesFrom ce) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLClassExpression visit(OWLDataAllValuesFrom ce) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLClassExpression visit(OWLDataHasValue ce) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLClassExpression visit(OWLDataMinCardinality ce) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLClassExpression visit(OWLDataExactCardinality ce) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLClassExpression visit(OWLDataMaxCardinality ce) {
		// TODO Auto-generated method stub
		return null;
	}

}
