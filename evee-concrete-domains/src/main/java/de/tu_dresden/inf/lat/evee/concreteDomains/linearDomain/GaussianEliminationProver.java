package de.tu_dresden.inf.lat.evee.concreteDomains.linearDomain;

import de.tu_dresden.inf.lat.evee.concreteDomains.CDDefaultContradictionGenerator;
import de.tu_dresden.inf.lat.evee.concreteDomains.CDProofGenerator;
import de.tu_dresden.inf.lat.evee.concreteDomains.CDReasoner;
import de.tu_dresden.inf.lat.evee.concreteDomains.CDRules;
import de.tu_dresden.inf.lat.evee.proofs.data.Inference;
import de.tu_dresden.inf.lat.evee.proofs.data.Proof;
import de.tu_dresden.inf.lat.evee.proofs.data.exceptions.ProofGenerationException;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInference;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProof;
import de.tu_dresden.inf.lat.evee.proofs.tools.ProofTools;
import org.apache.commons.math3.fraction.BigFraction;
import org.semanticweb.owlapi.model.OWLDataProperty;

import scala.Option;
import scala.collection.Iterable;
import scala.collection.JavaConverters;

import java.util.*;

/**
 * Proofs for linear constraints, built from the steps of Gaussian elimination.
 *
 * @author Christian Alrabbaa
 */
public class GaussianEliminationProver implements CDProofGenerator<LinearConstraint<BigFraction>> {
    private Set<LinearConstraint<BigFraction>> constraints;
    private final GaussianEliminationReasoner cDReasoner;

    public GaussianEliminationProver() {
        this(new GaussianEliminationReasoner());
    }

    /** the reasoner is changed to track all inferences, so it should not be used for anything else */
    public GaussianEliminationProver(GaussianEliminationReasoner cDReasoner) {
        this.cDReasoner = cDReasoner;
    }

    @Override
    public CDReasoner<LinearConstraint<BigFraction>> getCDReasoner() {
        return cDReasoner;
    }

    @Override
    public IProof<LinearConstraint<BigFraction>> proveInconsistency() throws ProofGenerationException {
        return getProof(CDDefaultContradictionGenerator.defaultLCBF());
    }

    @Override
    public void setOntology(Iterable<LinearConstraint<BigFraction>> constraints) {
        this.constraints = JavaConverters.setAsJavaSet(constraints.toSet());
    }

    @Override
    public boolean supportsProof(LinearConstraint<BigFraction> linearConstraint) {
        return true;
    }

    /**
     * Return a derivation structure with all inferences relevant to proving {@code conclusion}</br>
     * @implNote !!This currently returns the full derivation structure with one root!!
     * @param conclusion The constraint to prove
     * @throws ProofGenerationException
     */
    @Override
    public IProof<LinearConstraint<BigFraction>> getProof(LinearConstraint<BigFraction> conclusion) throws ProofGenerationException {
        if (conclusion.isDef() && !this.constraints.contains(conclusion))
            return definedProof(conclusion);

        this.cDReasoner.considerAllPivotRows(true);

        Set<LinearConstraint<BigFraction>> premises = new HashSet<>(this.constraints);

        ImplicationTracker tracker = new ImplicationTracker(premises, conclusion);
        boolean implied = this.cDReasoner.implies(JavaConverters.collectionAsScalaIterable(premises), conclusion,
                Option.apply(tracker));

        if (!implied)
            throw new ProofGenerationException("Linear Constraint is not implied -> " + conclusion + "\nTheory\n"+premises);

        IProof<LinearConstraint<BigFraction>> ds = conclusion.isInconsistent()? tracker.getDerivationStructure():
                tracker.getFlippedDerivationStructure();

        List<IInference<LinearConstraint<BigFraction>>> inferences = new ArrayList<>();
        for (IInference<LinearConstraint<BigFraction>> inf : ds.getInferences()) {
            // Filter inferences where premise = conclusion
            if (inf.getPremises().size() != 1 || !inf.getConclusion().equals(inf.getPremises().get(0)))
                inferences.add(inf);
        }

        for (LinearConstraint<BigFraction> premise : premises) {
            inferences.add(new Inference<>(premise, ProofTools.ASSERTED_2, Collections.emptyList()));
        }

        if(conclusion.isInconsistent()){
            if(!tracker.getContradiction().isPresent())
                throw new ProofGenerationException("Inconsistent conclusion without a contradiction in the tracker!\n" +
                        "conclusion -> " + conclusion);
            inferences.add(new Inference<>(conclusion, "Contradiction",
                    Collections.singletonList(tracker.getContradiction().get())));
        }

        ds = new Proof<>(conclusion, inferences);

        if(ds.getInferences().isEmpty())
            throw new ProofGenerationException("Failed to prove LinearConstraint -> " + conclusion);
        else
            return ds;
    }

    /** def(x) does not follow by elimination: x has a value because one of the constraints mentions x */
    private IProof<LinearConstraint<BigFraction>> definedProof(LinearConstraint<BigFraction> conclusion) throws ProofGenerationException {
        OWLDataProperty x = conclusion.lhs().keys().head();
        Optional<LinearConstraint<BigFraction>> premise = this.constraints.stream()
                .filter(c -> c.getOWLDataProperties().contains(x))
                .min(Comparator.comparing(Object::toString));
        if (!premise.isPresent())
            throw new ProofGenerationException("Linear Constraint is not implied -> " + conclusion + "\nTheory\n" + this.constraints);
        return new Proof<>(conclusion, Arrays.asList(
                new Inference<>(conclusion, CDRules.DEFINED(), Collections.singletonList(premise.get())),
                new Inference<>(premise.get(), ProofTools.ASSERTED_2, Collections.emptyList())));
    }

    /** getProof throws an exception if it fails */
    @Override
    public boolean successful() {
        return true;
    }
}
