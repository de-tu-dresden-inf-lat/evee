package de.tu_dresden.inf.lat.evee.proofs.tools.measures;

import java.util.List;

import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLAxiomVisitorEx;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDatatypeDefinitionAxiom;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLHasKeyAxiom;
import org.semanticweb.owlapi.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.model.OWLSubAnnotationPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.SWRLRule;

import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInference;

public class OWLAxiomSizeMeasurer implements OWLAxiomVisitorEx<Double> {

	private OWLClassExpressionSizeMeasurer cem = new OWLClassExpressionSizeMeasurer();

	public double visit(List<? extends OWLAxiom> premises) {
		return premises.stream().mapToDouble(ax -> ax.accept(this)).sum();
	}

	public Double visit(IInference<OWLAxiom> inf) {
		return visit(inf.getPremises()) + 1d + inf.getConclusion().accept(this);
	}

	@Override
	public Double visit(OWLSubClassOfAxiom axiom) {
		return axiom.getSubClass().accept(cem) + 1d + axiom.getSuperClass().accept(cem);
	}

	@Override
	public Double visit(OWLDisjointClassesAxiom axiom) {
		return 1d + cem.visit(axiom.getClassExpressions());
	}

	@Override
	public Double visit(OWLObjectPropertyDomainAxiom axiom) {
		return 1d + cem.visit(axiom.getProperty()) + 1d + axiom.getDomain().accept(cem);
	}

	@Override
	public Double visit(OWLObjectPropertyRangeAxiom axiom) {
		return 1d + cem.visit(axiom.getProperty()) + 1d + axiom.getRange().accept(cem);
	}

	@Override
	public Double visit(OWLEquivalentClassesAxiom axiom) {
		return 1d + cem.visit(axiom.getClassExpressions());
	}

	@Override
	public Double visit(OWLSubObjectPropertyOfAxiom axiom) {
		return cem.visit(axiom.getSubProperty()) + 1d + cem.visit(axiom.getSuperProperty());
	}

	@Override
	public Double visit(OWLSubPropertyChainOfAxiom axiom) {
		return cem.visit(axiom.getPropertyChain()) + ((double) axiom.getPropertyChain().size())
				+ cem.visit(axiom.getSuperProperty());
	}

	@Override
	public Double visit(OWLObjectPropertyAssertionAxiom axiom) {
		return cem.visit(axiom.getSubject()) + cem.visit(axiom.getObject()) + 1d + cem.visit(axiom.getProperty());
	}

	@Override
	public Double visit(OWLClassAssertionAxiom axiom) {
		return cem.visit(axiom.getIndividual()) + 1d + axiom.getClassExpression().accept(cem);
	}

	// TODO support more axioms

	@Override
	public Double visit(OWLAnnotationAssertionAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double visit(OWLSubAnnotationPropertyOfAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double visit(OWLAnnotationPropertyDomainAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double visit(OWLAnnotationPropertyRangeAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double visit(OWLAsymmetricObjectPropertyAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double visit(OWLReflexiveObjectPropertyAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double visit(OWLDataPropertyDomainAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double visit(OWLEquivalentObjectPropertiesAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double visit(OWLNegativeDataPropertyAssertionAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double visit(OWLDifferentIndividualsAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double visit(OWLDisjointDataPropertiesAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double visit(OWLDisjointObjectPropertiesAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double visit(OWLFunctionalObjectPropertyAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double visit(OWLDisjointUnionAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double visit(OWLSymmetricObjectPropertyAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double visit(OWLDataPropertyRangeAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double visit(OWLFunctionalDataPropertyAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double visit(OWLEquivalentDataPropertiesAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double visit(OWLDataPropertyAssertionAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double visit(OWLTransitiveObjectPropertyAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double visit(OWLSubDataPropertyOfAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double visit(OWLSameIndividualAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double visit(OWLInverseObjectPropertiesAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double visit(OWLHasKeyAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double visit(SWRLRule rule) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double visit(OWLDeclarationAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double visit(OWLDatatypeDefinitionAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

}
