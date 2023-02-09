package de.tu_dresden.inf.lat.evee.protege.abduction.letheBasedNonEntailmentExplanationService;

import uk.ac.man.cs.lethe.internal.dl.datatypes.Concept;
import uk.ac.man.cs.lethe.internal.dl.datatypes.Substitution;

import java.util.HashMap;
import java.util.Map;

public class FixpointAdapter {

    private final Concept fixpoint; // LFP.X1(A u X1)
    private final Concept fixpointConcept; // A u X1
    private final Concept fixpointVariable; // X1
    private final Map<Integer, Concept> levelToExpressionMap;
    private Integer currentMaxLevel;

    public FixpointAdapter(Concept fixpoint, Concept fixpointConcept, Concept fixpointVariable, Concept baseConcept){
        this.fixpoint = fixpoint;
        this.fixpointConcept = fixpointConcept;
        this.fixpointVariable = fixpointVariable;
        this.levelToExpressionMap = new HashMap<>();
        this.currentMaxLevel = 0;
        this.levelToExpressionMap.put(this.currentMaxLevel, baseConcept);
    }

    public Concept getFixpointVariable(){
        return this.fixpointVariable;
    }

    public Concept getFixpoint(){
        return this.fixpoint;
    }

    protected Concept unravel(int level) {
        Integer lvl = level;
        if (this.levelToExpressionMap.containsKey(lvl)) {
            return this.levelToExpressionMap.get(lvl);
        } else {
            Concept maxUnraveledExpression = new Substitution(this.fixpointVariable,
                    this.levelToExpressionMap.get(this.currentMaxLevel)).apply(this.fixpointConcept);
            this.currentMaxLevel += 1;
            this.levelToExpressionMap.put(this.currentMaxLevel, maxUnraveledExpression);
            return maxUnraveledExpression;
        }
    }

}
