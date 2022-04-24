package de.tu_dresden.inf.lat.proofs.data;

import de.tu_dresden.inf.lat.proofs.data.exceptions.ReasonerNotSupportedException;
import de.tu_dresden.inf.lat.proofs.data.exceptions.ProofGenerationCancelledException;
import de.tu_dresden.inf.lat.proofs.interfaces.IProof;
import de.tu_dresden.inf.lat.proofs.interfaces.IProofGenerator;
import de.tu_dresden.inf.lat.proofs.interfaces.ISimpleProofGenerator;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import java.util.List;

public abstract class AbstractSimpleOWLProofGenerator
        implements IProofGenerator<OWLAxiom, OWLOntology>, ISimpleProofGenerator<OWLClass, OWLAxiom, OWLOntology> {

    @Override
    public abstract void setOntology(OWLOntology ontology);

    public abstract void setReasoner(OWLReasoner reasoner) throws ReasonerNotSupportedException;

    @Override
    public boolean supportsProof(OWLAxiom axiom) {
        System.out.println("Checking whether we support "+axiom);
        if (axiom.isOfType(AxiomType.SUBCLASS_OF)){
            return ((OWLSubClassOfAxiom) axiom).getSubClass() instanceof OWLClass &&
                    ((OWLSubClassOfAxiom) axiom).getSuperClass() instanceof OWLClass ;
        }
        else if (axiom.isOfType(AxiomType.EQUIVALENT_CLASSES)){
            List<OWLClassExpression> classes = ((OWLEquivalentClassesAxiom) axiom).getClassExpressionsAsList();
                if (!(classes.size() == 2)){
                    return false;
                }
                else return (classes.get(0) instanceof OWLClass && classes.get(1) instanceof OWLClass );
        }
        else {
            return false;
        }
    }

    @Override
    public IProof<OWLAxiom> getProof(OWLAxiom axiom) throws ProofGenerationCancelledException {
        // Patrick: I made this non-final as I wanted to be able to use a simple owl proof generator
        // with my own getProof method, working the other way around as intended here
        if (! supportsProof(axiom)){
            throw new UnsupportedOperationException("not implemented yet");
        }
        if (axiom.isOfType(AxiomType.SUBCLASS_OF)){
            return proveSubsumption(((OWLSubClassOfAxiom) axiom).getSubClass().asOWLClass(),
                    ((OWLSubClassOfAxiom) axiom).getSuperClass().asOWLClass());
        }
        else {
            List<OWLClassExpression> classes = ((OWLEquivalentClassesAxiom) axiom).getClassExpressionsAsList();
            return proveEquivalence(classes.get(0).asOWLClass(), classes.get(1).asOWLClass());
        }
    }

}
