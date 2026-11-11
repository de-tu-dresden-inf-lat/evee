package de.tu_dresden.inf.lat.evee.concreteDomains.linearDomain;

import de.tu_dresden.inf.lat.evee.concreteDomains.CDProofGenerator;
import de.tu_dresden.inf.lat.evee.concreteDomains.CDReasoner;
import de.tu_dresden.inf.lat.evee.concreteDomains.CDRules;
import de.tu_dresden.inf.lat.evee.proofs.data.Inference;
import de.tu_dresden.inf.lat.evee.proofs.data.Proof;
import de.tu_dresden.inf.lat.evee.proofs.data.exceptions.ProofGenerationException;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInference;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProof;
import de.tu_dresden.inf.lat.evee.proofs.tools.ProofTools;
import org.semanticweb.owlapi.model.OWLDataProperty;

import scala.Option;
import scala.collection.Iterable;
import scala.collection.JavaConverters;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Proofs for linear constraints, built from the steps of Gaussian elimination.
 *
 * @author Christian Alrabbaa
 */
public class GaussianEliminationProver implements CDProofGenerator<LinearConstraint> {
    /** the default of {@link #setMaxVariablesForAllOrders(int)} */
    public static final int DEFAULT_MAX_VARIABLES_FOR_ALL_ORDERS = 4;

    private Set<LinearConstraint> constraints;
    private final GaussianEliminationReasoner cDReasoner;
    private int maxVariablesForAllOrders = DEFAULT_MAX_VARIABLES_FOR_ALL_ORDERS;

    public GaussianEliminationProver() {
        this(new GaussianEliminationReasoner());
    }

    /** the reasoner is changed to track all inferences, so it should not be used for anything else */
    public GaussianEliminationProver(GaussianEliminationReasoner cDReasoner) {
        this.cDReasoner = cDReasoner;
    }

    /**
     * The proofs depend on the order in which the variables are eliminated. If the premises and the conclusion have at
     * most this many variables, the derivation structure contains the eliminations in all orders (n! for n variables),
     * and minimisation chooses; otherwise only the elimination with the variables sorted by IRI.
     */
    public void setMaxVariablesForAllOrders(int maxVariablesForAllOrders) {
        this.maxVariablesForAllOrders = maxVariablesForAllOrders;
    }

    @Override
    public CDReasoner<LinearConstraint> getCDReasoner() {
        return cDReasoner;
    }

    @Override
    public IProof<LinearConstraint> proveInconsistency() throws ProofGenerationException {
        return getProof(LinearConstraint.contradiction());
    }

    @Override
    public void setOntology(Iterable<LinearConstraint> constraints) {
        this.constraints = JavaConverters.setAsJavaSet(constraints.toSet());
    }

    @Override
    public boolean supportsProof(LinearConstraint linearConstraint) {
        return true;
    }

    /**
     * Returns a derivation structure with the inferences of Gaussian elimination that prove {@code conclusion}, for the
     * variable orders described in {@link #setMaxVariablesForAllOrders(int)}.
     * @param conclusion The constraint to prove
     * @throws ProofGenerationException
     */
    @Override
    public IProof<LinearConstraint> getProof(LinearConstraint conclusion) throws ProofGenerationException {
        if (conclusion.isDef() && !this.constraints.contains(conclusion))
            return definedProof(conclusion);

        // a fixed order of the premises, so that the proofs do not depend on hash codes
        List<LinearConstraint> premises = this.constraints.stream()
                .sorted(Comparator.comparing(Object::toString)).collect(Collectors.toList());
        List<OWLDataProperty> variables = new ArrayList<>();
        for (LinearConstraint constraint : premises)
            variables.addAll(JavaConverters.setAsJavaSet(constraint.getOWLDataProperties()));
        variables.addAll(JavaConverters.setAsJavaSet(conclusion.getOWLDataProperties()));
        variables = variables.stream().distinct().sorted(Comparator.comparing(v -> v.getIRI().toString()))
                .collect(Collectors.toList());
        List<List<OWLDataProperty>> orders = variables.size() <= this.maxVariablesForAllOrders ?
                permutations(variables) : Collections.singletonList(variables);

        Set<IInference<LinearConstraint>> inferences = new LinkedHashSet<>();
        this.cDReasoner.considerAllPivotRows(true);
        for (List<OWLDataProperty> order : orders) {
            this.cDReasoner.variableOrder(JavaConverters.asScalaBuffer(order).toList());
            inferences.addAll(eliminationSteps(premises, conclusion));
        }

        for (LinearConstraint premise : premises) {
            inferences.add(new Inference<>(premise, ProofTools.ASSERTED_2, Collections.emptyList()));
        }
        if (inferences.isEmpty())
            throw new ProofGenerationException("Failed to prove LinearConstraint -> " + conclusion);
        return new Proof<>(conclusion, new ArrayList<>(inferences));
    }

    /** the inferences of one elimination, with the variable order set in the reasoner */
    private List<IInference<LinearConstraint>> eliminationSteps(List<LinearConstraint> premises,
            LinearConstraint conclusion) throws ProofGenerationException {
        ImplicationTracker tracker = new ImplicationTracker(new HashSet<>(premises), conclusion);
        boolean implied = this.cDReasoner.implies(JavaConverters.collectionAsScalaIterable(premises), conclusion,
                Option.apply(tracker));

        if (!implied)
            throw new ProofGenerationException("Linear Constraint is not implied -> " + conclusion + "\nTheory\n"+premises);

        IProof<LinearConstraint> ds = conclusion.isInconsistent()? tracker.getDerivationStructure():
                tracker.getFlippedDerivationStructure();

        List<IInference<LinearConstraint>> inferences = new ArrayList<>();
        for (IInference<LinearConstraint> inf : ds.getInferences()) {
            // Filter inferences where premise = conclusion
            if (inf.getPremises().size() != 1 || !inf.getConclusion().equals(inf.getPremises().get(0)))
                inferences.add(inf);
        }

        if(conclusion.isInconsistent()){
            if(!tracker.getContradiction().isPresent())
                throw new ProofGenerationException("Inconsistent conclusion without a contradiction in the tracker!\n" +
                        "conclusion -> " + conclusion);
            inferences.add(new Inference<>(conclusion, "Contradiction",
                    Collections.singletonList(tracker.getContradiction().get())));
        }

        return inferences;
    }

    /** all orders of the elements */
    private static <T> List<List<T>> permutations(List<T> elements) {
        if (elements.isEmpty())
            return Collections.singletonList(Collections.emptyList());
        List<List<T>> result = new ArrayList<>();
        for (T first : elements) {
            List<T> rest = new ArrayList<>(elements);
            rest.remove(first);
            for (List<T> order : permutations(rest)) {
                List<T> permutation = new ArrayList<>();
                permutation.add(first);
                permutation.addAll(order);
                result.add(permutation);
            }
        }
        return result;
    }

    /** def(x) does not follow by elimination: x has a value because one of the constraints mentions x */
    private IProof<LinearConstraint> definedProof(LinearConstraint conclusion) throws ProofGenerationException {
        OWLDataProperty x = conclusion.lhs().keys().head();
        Optional<LinearConstraint> premise = this.constraints.stream()
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
