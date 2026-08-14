package de.tu_dresden.inf.lat.evee.concreteDomains.linearDomain;

import de.tu_dresden.inf.lat.evee.concreteDomains.CDDefaultContradictionGenerator;
import de.tu_dresden.inf.lat.evee.concreteDomains.CDProofGenerator;
import de.tu_dresden.inf.lat.evee.concreteDomains.CDReasoner;

import de.tu_dresden.inf.lat.evee.proofs.data.Inference;
import de.tu_dresden.inf.lat.evee.proofs.data.Proof;
import de.tu_dresden.inf.lat.evee.proofs.data.exceptions.ProofGenerationException;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInference;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProof;
import de.tu_dresden.inf.lat.evee.proofs.tools.ProofTools;

import org.apache.commons.math3.fraction.BigFraction;

import scala.Option;
import scala.collection.Iterable;
import scala.collection.JavaConverters;

import java.util.*;

/**
 * @author Christian Alrabbaa
 */
public class GaussianEliminationProver implements CDProofGenerator<LinearConstraint<BigFraction>> {
    private Set<LinearConstraint<BigFraction>> extendedOntology;
    private final GaussianEliminationReasoner cDReasoner;
    public GaussianEliminationProver(GaussianEliminationReasoner cDReasoner){
        this.cDReasoner = cDReasoner;
    }

    public Set<LinearConstraint<BigFraction>> getExtendedOntology() {
        return extendedOntology;
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
        this.extendedOntology = JavaConverters.setAsJavaSet(constraints.toSet());
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
        this.cDReasoner.considerAllPivotRows(true);

        Set<LinearConstraint<BigFraction>> premises = new HashSet<>(this.extendedOntology);

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

    @Override
    public boolean successful() {
        return false;
    } // TODO right default behavior?

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GaussianEliminationProver that = (GaussianEliminationProver) o;
        return Objects.equals(extendedOntology, that.extendedOntology);
    }

    @Override
    public int hashCode() {
        return Objects.hash(extendedOntology);
    }
}
