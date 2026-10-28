package de.tu_dresden.inf.lat.evee.concreteDomains.linearDomain;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

import de.tu_dresden.inf.lat.evee.concreteDomains.linearDomain.LinearConstraint;
import de.tu_dresden.inf.lat.evee.proofs.data.Inference;
import de.tu_dresden.inf.lat.evee.proofs.data.Proof;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInference;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProof;

import org.apache.commons.math3.fraction.BigFraction;
import org.apache.commons.math3.linear.FieldVector;
import org.semanticweb.owlapi.model.OWLDataProperty;

import scala.collection.immutable.Map;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Christian Alrabbaa

 * A class to track all inferences that lead to the implied constraint.
 */
public class ImplicationTracker {
    /** the prefix of the rule names, which list the factors of the premises */
    public static final String RULE_PREFIX = "Linear combination [";

    private final IProof<LinearConstraint<BigFraction>> derivationStructure;
    /** the factors of the premises of each elimination step: the conclusion is the sum of the premises times these factors */
    private final java.util.Map<IInference<LinearConstraint<BigFraction>>, BigFraction[]> factors = new HashMap<>();
    private final LinearConstraint<BigFraction> impliedLinearConstraint;
    private final Set<LinearConstraint<BigFraction>> theory;
    private Map<OWLDataProperty, Integer> varsMap;
    private boolean fromContradiction;
    private LinearConstraint<BigFraction> contradiction;

    public ImplicationTracker(Set<LinearConstraint<BigFraction>> linearConstraints, LinearConstraint<BigFraction> impliedLinearConstraint ){
        this.impliedLinearConstraint = impliedLinearConstraint;
        this.theory = linearConstraints;
        this.derivationStructure = new Proof<>(impliedLinearConstraint);
        this.fromContradiction = false;
    }

    public LinearConstraint<BigFraction> getImpliedLinearConstraint() {
        return impliedLinearConstraint;
    }

    public void setVarsMap(Map<OWLDataProperty, Integer> varsMap) {
        this.varsMap = varsMap;
    }

    public Map<OWLDataProperty, Integer> getVarsMap() {
        return varsMap;
    }

    public Optional<LinearConstraint<BigFraction>> getContradiction() {
        if(!fromContradiction)
            return Optional.empty();
        return Optional.of(contradiction);
    }

    public void addInference(FieldVector<BigFraction> conclusionVector, FieldVector<BigFraction> pivotVector, FieldVector<BigFraction> other, BigFraction factor1, BigFraction factor2){
        LinearConstraint<BigFraction> conclusion = MatrixConversions.toLinearConstraint(conclusionVector,this.varsMap);

        List<LinearConstraint<BigFraction>> premise = new LinkedList<>(Arrays.asList(MatrixConversions.toLinearConstraint(pivotVector, this.varsMap)));
        LinearConstraint<BigFraction> p2 = MatrixConversions.toLinearConstraint(other,this.varsMap);
        premise.add(p2);

        if(conclusion.isInconsistent()){
            System.out.println("the conclusion is a contradiction");
            this.fromContradiction = true;
            this.contradiction = conclusion;
        }
        IInference<LinearConstraint<BigFraction>> inference = new Inference<>(conclusion, ruleName(factor1, factor2), premise);
        this.factors.put(inference, new BigFraction[]{factor1, factor2});
        this.derivationStructure.addInference(inference);
    }

    public IProof<LinearConstraint<BigFraction>> getFlippedDerivationStructure() {
        IProof<LinearConstraint<BigFraction>> ds = new Proof<>(this.impliedLinearConstraint);

        Set<LinearConstraint<BigFraction>> toDo = new HashSet<>();
        Set<LinearConstraint<BigFraction>> tmp = new HashSet<>();
        toDo.add(this.impliedLinearConstraint);

        Set<LinearConstraint<BigFraction>> done =  Sets.newHashSet(this.theory);
        done.remove(this.impliedLinearConstraint);

        // TODO: implement this directly in the Proof class?
        SetMultimap<LinearConstraint<BigFraction>, IInference<LinearConstraint<BigFraction>>> premiseMap = HashMultimap.create();
        for (IInference<LinearConstraint<BigFraction>> inference : this.derivationStructure.getInferences())
            for (LinearConstraint<BigFraction> premise : inference.getPremises())
                premiseMap.put(premise, inference);

        while(!toDo.isEmpty() ){
            for(LinearConstraint<BigFraction> c : toDo){
                if(done.contains(c))
                    continue;
                done.add(c);

                //In case there is already an inference with the desired conclusion. To avoid proofs with premises that are not asserted nor tautology
                ds.addInferences(this.derivationStructure.getInferences(c));

                for (IInference<LinearConstraint<BigFraction>> x : premiseMap.get(c)) {

                    int cIndx = x.getPremises().indexOf(c);
                    List<LinearConstraint<BigFraction>> newPremise = Lists.newLinkedList(x.getPremises());
                    String newRuleName;

                    BigFraction[] factors = this.factors.get(x).clone();

                    BigFraction b = factors[cIndx];
                    factors[cIndx] = BigFraction.ONE.divide(factors[cIndx]);
                    factors[1 - cIndx] = (BigFraction.MINUS_ONE.multiply(factors[1 - cIndx])).divide(b);

                    if (x.getConclusion().rhs().equals(BigFraction.ZERO) && (x.getConclusion().lhs().keys().isEmpty())){
                        newPremise.remove(cIndx);
                        newRuleName = ruleName(factors[1 - cIndx]);
                    }
                    else {
                        newPremise.set(cIndx, x.getConclusion());
                        newRuleName = ruleName(factors[0], factors[1]);
                    }

                    ds.addInference(new Inference<>(c, newRuleName, newPremise));

                    tmp.addAll(newPremise);
                }
            }

            toDo = Sets.newHashSet(tmp);
            tmp.clear();
        }
        return ds;
    }

    public IProof<LinearConstraint<BigFraction>> getDerivationStructure() {
        return this.derivationStructure;
    }

    /** the name of a step whose conclusion is the sum of its premises times the factors, e.g. "Linear combination [1, -1/3]" */
    public static String ruleName(BigFraction... factors) {
        return Arrays.stream(factors).map(ImplicationTracker::format).collect(Collectors.joining(", ", RULE_PREFIX, "]"));
    }

    private static String format(BigFraction fraction) {
        if (fraction.getDenominator().equals(BigInteger.ONE))
            return fraction.getNumerator().toString();
        return fraction.getNumerator() + "/" + fraction.getDenominator();
    }
}
