package de.tu_dresden.inf.lat.evee.concreteDomains.preprocess;

import de.tu_dresden.inf.lat.evee.general.tools.OWLTools;
import de.tu_dresden.inf.lat.evee.proofs.data.Inference;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInference;
import org.semanticweb.owlapi.model.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author stefborg
 *
 */
public class OWLAxiomReplacer implements OWLAxiomVisitorEx<OWLAxiom> {

	private OWLClassExpressionReplacer cer;

	public OWLAxiomReplacer(OWLObjectMap map) {
		this.cer = new OWLClassExpressionReplacer(map);
	}

	public List<OWLAxiom> visit(List<? extends OWLAxiom> axioms) {
		return axioms.stream().map(ax -> ax.accept(this)).collect(Collectors.toList());
	}

	public IInference<OWLAxiom> visit(IInference<OWLAxiom> inf) {
		return new Inference<>(inf.getConclusion().accept(this), "generic", visit(inf.getPremises()));
	}

	@Override
	public OWLAxiom visit(OWLSubClassOfAxiom axiom) {
		return OWLTools.odf.getOWLSubClassOfAxiom(axiom.getSubClass().accept(cer), axiom.getSuperClass().accept(cer));
	}

	@Override
	public OWLAxiom visit(OWLDisjointClassesAxiom axiom) {
		return OWLTools.odf.getOWLDisjointClassesAxiom(cer.visit(axiom.getClassExpressions()));
	}

	@Override
	public OWLAxiom visit(OWLObjectPropertyDomainAxiom axiom) {
		return OWLTools.odf.getOWLObjectPropertyDomainAxiom(cer.visit(axiom.getProperty()), axiom.getDomain().accept(cer));
	}

	@Override
	public OWLAxiom visit(OWLObjectPropertyRangeAxiom axiom) {
		return OWLTools.odf.getOWLObjectPropertyRangeAxiom(cer.visit(axiom.getProperty()), axiom.getRange().accept(cer));
	}

	@Override
	public OWLAxiom visit(OWLEquivalentClassesAxiom axiom) {
		return OWLTools.odf.getOWLEquivalentClassesAxiom(cer.visit(axiom.getClassExpressions()));
	}

	@Override
	public OWLAxiom visit(OWLSubObjectPropertyOfAxiom axiom) {
		return OWLTools.odf.getOWLSubObjectPropertyOfAxiom(cer.visit(axiom.getSubProperty()),
				cer.visit(axiom.getSuperProperty()));
	}

	@Override
	public OWLAxiom visit(OWLSubPropertyChainOfAxiom axiom) {
		return OWLTools.odf.getOWLSubPropertyChainOfAxiom(cer.visit(axiom.getPropertyChain()),
				cer.visit(axiom.getSuperProperty()));
	}

	// TODO support more axioms

//	@Override
//	public <T> OWLAxiom doDefault(T object) {
//		throw new UnsupportedOperationException("Unsupported axiom type: " + object);
//	}

	@Override
	public OWLAxiom visit(OWLAnnotationAssertionAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLAxiom visit(OWLSubAnnotationPropertyOfAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLAxiom visit(OWLAnnotationPropertyDomainAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLAxiom visit(OWLAnnotationPropertyRangeAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLAxiom visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLAxiom visit(OWLAsymmetricObjectPropertyAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLAxiom visit(OWLReflexiveObjectPropertyAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLAxiom visit(OWLDataPropertyDomainAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLAxiom visit(OWLEquivalentObjectPropertiesAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLAxiom visit(OWLNegativeDataPropertyAssertionAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLAxiom visit(OWLDifferentIndividualsAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLAxiom visit(OWLDisjointDataPropertiesAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLAxiom visit(OWLDisjointObjectPropertiesAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLAxiom visit(OWLObjectPropertyAssertionAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLAxiom visit(OWLFunctionalObjectPropertyAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLAxiom visit(OWLDisjointUnionAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLAxiom visit(OWLSymmetricObjectPropertyAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLAxiom visit(OWLDataPropertyRangeAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLAxiom visit(OWLFunctionalDataPropertyAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLAxiom visit(OWLEquivalentDataPropertiesAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLAxiom visit(OWLClassAssertionAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLAxiom visit(OWLDataPropertyAssertionAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLAxiom visit(OWLTransitiveObjectPropertyAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLAxiom visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLAxiom visit(OWLSubDataPropertyOfAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLAxiom visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLAxiom visit(OWLSameIndividualAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLAxiom visit(OWLInverseObjectPropertiesAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLAxiom visit(OWLHasKeyAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLAxiom visit(SWRLRule rule) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLAxiom visit(OWLDeclarationAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLAxiom visit(OWLDatatypeDefinitionAxiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

}
