package de.tu_dresden.inf.lat.evee.concreteDomains.linearDomain;

import de.tu_dresden.inf.lat.evee.proofs.data.Inference;
import de.tu_dresden.inf.lat.evee.proofs.data.Proof;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInference;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProof;

import org.apache.commons.math3.fraction.BigFraction;
import org.apache.commons.math3.fraction.BigFractionFormat;
import org.apache.commons.math3.linear.FieldVector;
import org.semanticweb.owlapi.model.OWLDataProperty;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

import scala.collection.immutable.Map;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Christian Alrabbaa

 * A class to track all inferences that lead to the implied constraint.
 */
public class ImplicationTracker {
    private final IProof<LinearConstraint<BigFraction>> derivationStructure;
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
        String ruleName = "["+factor1+","+factor2+"]";

        LinearConstraint<BigFraction> conclusion = MatrixConversions.toLinearConstraint(conclusionVector,this.varsMap);

        List<LinearConstraint<BigFraction>> premise = new LinkedList<>(Arrays.asList(MatrixConversions.toLinearConstraint(pivotVector, this.varsMap)));
        LinearConstraint<BigFraction> p2 = MatrixConversions.toLinearConstraint(other,this.varsMap);
        premise.add(p2);

        if(conclusion.isInconsistent()){
            System.out.println("the conclusion is a contradiction");
            this.fromContradiction = true;
            this.contradiction = conclusion;
        }
        this.derivationStructure.addInference(new Inference<>(conclusion, format(ruleName), premise));
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
                    String newRuleName ="";

                    BigFraction[] factors = getFractionsFromStr(Arrays.stream(x.getRuleName().substring(1, x.getRuleName().length() - 1)
                            .split(",")).map(String::trim).collect(Collectors.toList()));

                    BigFraction b = factors[cIndx];
                    factors[cIndx] = BigFraction.ONE.divide(factors[cIndx]);
                    factors[1 - cIndx] = (BigFraction.MINUS_ONE.multiply(factors[1 - cIndx])).divide(b);

                    if (x.getConclusion().rhs().equals(BigFraction.ZERO) && (x.getConclusion().lhs().keys().isEmpty())){
                        newPremise.remove(cIndx);
                        newRuleName = "["+factors[1-cIndx]+"]";
                    }
                    else {
                        newPremise.set(cIndx, x.getConclusion());
                        newRuleName = "[" + factors[0] + "," + factors[1] + "]";
                    }

                    ds.addInference(new Inference<>(c,format(newRuleName),newPremise));

                    tmp.addAll(newPremise);
                }
            }

            toDo = Sets.newHashSet(tmp);
            tmp.clear();
        }
        return ds;
    }

    private BigFraction[] getFractionsFromStr(List<String> fractions) {
        BigFraction[] res = new BigFraction[fractions.size()];
        int i=0;
        for(String y : fractions){
            res[i] = BigFractionFormat.getProperInstance().parse(y);
            i++;
        }
        return res;
    }

    public IProof<LinearConstraint<BigFraction>> getDerivationStructure() {
        return this.derivationStructure;
    }

    private String format(String ruleName){
        if(ruleName.contains("/"))
            return ruleName.replaceAll("\\s+","").replace("/"," / ");
        return ruleName;
    }
}
