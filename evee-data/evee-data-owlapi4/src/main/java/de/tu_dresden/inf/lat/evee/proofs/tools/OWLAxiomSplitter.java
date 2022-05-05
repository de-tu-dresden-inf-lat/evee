/**
 * 
 */
package de.tu_dresden.inf.lat.evee.proofs.tools;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLAxiomVisitorEx;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
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
import org.semanticweb.owlapi.model.OWLEntity;
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

/**
 * @author stefborg
 *
 */
public class OWLAxiomSplitter implements OWLAxiomVisitorEx<Collection<? extends OWLAxiom>> {

	Set<OWLEntity> sig;
	ConceptNameGenerator A;

	public OWLAxiomSplitter(Set<OWLEntity> sig, ConceptNameGenerator A) {
		this.sig = sig;
		this.A = A;
	}

	@Override
	public Collection<? extends OWLAxiom> visit(OWLSubClassOfAxiom axiom) {
		OWLClassExpression sub = axiom.getSubClass();
		OWLClassExpression sup = axiom.getSuperClass();
		if (!OWLTools.isAuxiliary(sub, sig) && !OWLTools.isAuxiliary(sup, sig)) {
			// introduce new auxiliary names
			OWLClass asub = A.next();
			OWLClass asuper = A.next();
			Collection<OWLSubClassOfAxiom> newAxioms = new LinkedList<>();
			newAxioms.add(OWLTools.odf.getOWLSubClassOfAxiom(sub, asub));
			newAxioms.add(OWLTools.odf.getOWLSubClassOfAxiom(asub, asuper));
			newAxioms.add(OWLTools.odf.getOWLSubClassOfAxiom(asuper, sup));
			return newAxioms;
		}
		if (!OWLTools.isAuxiliary(sup, sig)) {
			// split rhs
			return sup.accept(new OWLSuperClassExpressionSplitter(sub, sig, A));
		}
		if (!OWLTools.isAuxiliary(sub, sig)) {
			// split lhs
			return sub.accept(new OWLSubClassExpressionSplitter(sup, sig, A));
		}
		return null;
	}

	@Override
	public Collection<? extends OWLAxiom> visit(OWLDisjointClassesAxiom axiom) {
		return axiom.asOWLSubClassOfAxioms();
	}

	@Override
	public Collection<? extends OWLAxiom> visit(OWLObjectPropertyDomainAxiom axiom) {
		return Collections.singleton(axiom.asOWLSubClassOfAxiom());
	}

	@Override
	public Collection<? extends OWLAxiom> visit(OWLObjectPropertyRangeAxiom axiom) {
		return Collections.singleton(axiom.asOWLSubClassOfAxiom());
	}

	@Override
	public Collection<? extends OWLAxiom> visit(OWLEquivalentClassesAxiom axiom) {
		return axiom.asOWLSubClassOfAxioms();
	}

	@Override
	public Collection<? extends OWLAxiom> visit(OWLSubObjectPropertyOfAxiom axiom) {
		return null;
	}

	@Override
	public Collection<? extends OWLAxiom> visit(OWLSubPropertyChainOfAxiom axiom) {
		return null;
	}
	
	// TODO support more axioms

//	@Override
//	public <T> Collection<? extends OWLAxiom> doDefault(T object) {
//		throw new UnsupportedOperationException("Unsupported axiom type: " + object);
//	}

	@Override
	public Collection<? extends OWLAxiom> visit(OWLAnnotationAssertionAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<? extends OWLAxiom> visit(OWLSubAnnotationPropertyOfAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<? extends OWLAxiom> visit(OWLAnnotationPropertyDomainAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<? extends OWLAxiom> visit(OWLAnnotationPropertyRangeAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<? extends OWLAxiom> visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<? extends OWLAxiom> visit(OWLAsymmetricObjectPropertyAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<? extends OWLAxiom> visit(OWLReflexiveObjectPropertyAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<? extends OWLAxiom> visit(OWLDataPropertyDomainAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<? extends OWLAxiom> visit(OWLEquivalentObjectPropertiesAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<? extends OWLAxiom> visit(OWLNegativeDataPropertyAssertionAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<? extends OWLAxiom> visit(OWLDifferentIndividualsAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<? extends OWLAxiom> visit(OWLDisjointDataPropertiesAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<? extends OWLAxiom> visit(OWLDisjointObjectPropertiesAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<? extends OWLAxiom> visit(OWLObjectPropertyAssertionAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<? extends OWLAxiom> visit(OWLFunctionalObjectPropertyAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<? extends OWLAxiom> visit(OWLDisjointUnionAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<? extends OWLAxiom> visit(OWLSymmetricObjectPropertyAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<? extends OWLAxiom> visit(OWLDataPropertyRangeAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<? extends OWLAxiom> visit(OWLFunctionalDataPropertyAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<? extends OWLAxiom> visit(OWLEquivalentDataPropertiesAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<? extends OWLAxiom> visit(OWLClassAssertionAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<? extends OWLAxiom> visit(OWLDataPropertyAssertionAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<? extends OWLAxiom> visit(OWLTransitiveObjectPropertyAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<? extends OWLAxiom> visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<? extends OWLAxiom> visit(OWLSubDataPropertyOfAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<? extends OWLAxiom> visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<? extends OWLAxiom> visit(OWLSameIndividualAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<? extends OWLAxiom> visit(OWLInverseObjectPropertiesAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<? extends OWLAxiom> visit(OWLHasKeyAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<? extends OWLAxiom> visit(SWRLRule rule) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<? extends OWLAxiom> visit(OWLDeclarationAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<? extends OWLAxiom> visit(OWLDatatypeDefinitionAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

}
