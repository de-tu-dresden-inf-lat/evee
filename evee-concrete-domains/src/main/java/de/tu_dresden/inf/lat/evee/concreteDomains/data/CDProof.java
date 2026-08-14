package de.tu_dresden.inf.lat.evee.concreteDomains.data;

import de.tu_dresden.inf.lat.evee.concreteDomains.CDConstraint;
import de.tu_dresden.inf.lat.evee.concreteDomains.diffDomain.DiffConstraint;
import de.tu_dresden.inf.lat.evee.concreteDomains.linearDomain.LinearConstraint;
import de.tu_dresden.inf.lat.evee.concreteDomains.data.extendedDataStructure.ExtendedAxiom;

import de.tu_dresden.inf.lat.evee.proofs.data.Inference;
import de.tu_dresden.inf.lat.evee.proofs.data.Proof;
import de.tu_dresden.inf.lat.evee.proofs.data.exceptions.ProofException;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInference;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProof;
import de.tu_dresden.inf.lat.evee.concreteDomains.data.names.ConcreteDomainName;
import de.tu_dresden.inf.lat.evee.concreteDomains.tools.Tools;

import org.apache.commons.math3.fraction.BigFraction;
import org.semanticweb.owlapi.model.OWLClass;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Christian Alrabbaa
 *
 * A class to save the Manager output proof inorder to generate GML input for Evonne
 */

public class CDProof{
    private IProof<ExtendedAxiom<LinearConstraint<BigFraction>>> lcProof;
    private IProof<ExtendedAxiom<DiffConstraint>> qgProof;

    public IProof<ExtendedAxiom<LinearConstraint<BigFraction>>> getLCProof() {
        return this.lcProof;
    }

    public IProof<ExtendedAxiom<DiffConstraint>> getQGProof() {
        return this.qgProof;
    }

    public <T extends CDConstraint> void setProof(IProof<ExtendedAxiom<T>> proof, ConcreteDomainName cdn) {
        if(cdn == ConcreteDomainName.LinearConstraints)
            this.lcProof = new Proof<>(toLC(proof.getFinalConclusion()),
                    proof.getInferences().stream().map(this::toLCInf).collect(Collectors.toSet()));
        else
            this.qgProof =  new Proof<>(toQG(proof.getFinalConclusion()),
                    proof.getInferences().stream().map(this::toQGInf).collect(Collectors.toSet()));
    }


    private <T extends CDConstraint> IInference<ExtendedAxiom<LinearConstraint<BigFraction>>> toLCInf(IInference<ExtendedAxiom<T>> inf) {
        ExtendedAxiom<LinearConstraint<BigFraction>> newCon = toLC(inf.getConclusion());
        List<ExtendedAxiom<LinearConstraint<BigFraction>>> newPre=
                inf.getPremises().stream().map(this::toLC).collect(Collectors.toList());
        return new Inference<>(newCon,inf.getRuleName(),newPre);
    }

    private <T extends CDConstraint> ExtendedAxiom<LinearConstraint<BigFraction>> toLC(ExtendedAxiom<T> extAx) {
        Map<OWLClass, LinearConstraint<BigFraction>> map = new HashMap<>();
        extAx.map().keySet().toStream().foreach(k-> {
            try {
                return map.put(k, toLCBF(extAx.map().get(k).get()));
            } catch (ProofException e) {
                throw new RuntimeException(e);
            }
        });

        return new ExtendedAxiom<>(extAx.axiom(), Tools.toScalaImmutableMap(map));
    }

    private <T extends CDConstraint> LinearConstraint<BigFraction> toLCBF(T cdConstraint) throws ProofException {
        if(cdConstraint instanceof LinearConstraint)
            if(((LinearConstraint<?>)cdConstraint).getType().equals(BigFraction.class))
                return (LinearConstraint<BigFraction>) cdConstraint;

        throw new ProofException("Only Linear constraints with BigFractions are supported!");
    }

    private <T extends CDConstraint> IInference<ExtendedAxiom<DiffConstraint>> toQGInf(IInference<ExtendedAxiom<T>> inf) {
        ExtendedAxiom<DiffConstraint> newCon = toQG(inf.getConclusion());
        List<ExtendedAxiom<DiffConstraint>> newPre=
                inf.getPremises().stream().map(this::toQG).collect(Collectors.toList());
        return new Inference<>(newCon,inf.getRuleName(),newPre);
    }

    private <T extends CDConstraint> ExtendedAxiom<DiffConstraint> toQG(ExtendedAxiom<T> extAx) {
        Map<OWLClass, DiffConstraint> map = new HashMap<>();
        extAx.map().keySet().toStream().foreach(k->
            map.put(k, (DiffConstraint) extAx.map().get(k).get())
        );

        return new ExtendedAxiom<>(extAx.axiom(), Tools.toScalaImmutableMap(map));
    }
}
