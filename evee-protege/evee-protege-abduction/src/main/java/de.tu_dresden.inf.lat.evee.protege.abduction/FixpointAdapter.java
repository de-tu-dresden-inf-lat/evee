package de.tu_dresden.inf.lat.evee.protege.abduction;

import uk.ac.man.cs.lethe.internal.dl.datatypes.Concept;
import uk.ac.man.cs.lethe.internal.dl.datatypes.Substitution;

import java.util.HashMap;
import java.util.Map;

public class FixpointAdapter {

    private final Concept fixpointConcept;
    private final Concept fixpointVariable;
    private final Map<Integer, Concept> levelToExpressionMap;
    private Integer currentMaxLevel;

    public FixpointAdapter(Concept fixpointConcept, Concept fixpointVariable, Concept baseConcept){
        this.fixpointConcept = fixpointConcept;
        this.fixpointVariable = fixpointVariable;
        this.levelToExpressionMap = new HashMap<>();
        Concept levelZero = new Substitution(this.fixpointVariable, baseConcept).apply(
                this.fixpointConcept);
        this.currentMaxLevel = 0;
        this.levelToExpressionMap.put(this.currentMaxLevel, levelZero);
    }

    public Concept getFixpointConcept(){
        return this.fixpointConcept;
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
