package de.tu_dresden.inf.lat.evee.protege.abduction;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import uk.ac.man.cs.lethe.internal.dl.datatypes.Concept;

import java.util.HashMap;
import java.util.Map;

public class FixpointConverter {

    private final String fixpointIdentifier;
    private final Concept concept;
    private final OWLClass baseClass;
    private final Map<Integer, OWLClassExpression> levelToExpressionMap;
    private final DLStatementConverter dlStatementConverter;
    private Integer currentMaxLevel;

    public FixpointConverter(String fixpointIdentifier, Concept concept, OWLClass baseClass, DLStatementConverter dlStatementConverter){
        this.fixpointIdentifier = fixpointIdentifier;
        this.baseClass = baseClass;
        this.concept = concept;
        this.levelToExpressionMap = new HashMap<>();
        this.dlStatementConverter = dlStatementConverter;
        OWLClassExpression levelZero = this.dlStatementConverter.convert(this.concept, this.fixpointIdentifier, this.baseClass);
        this.currentMaxLevel = 0;
        this.levelToExpressionMap.put(this.currentMaxLevel, levelZero);
    }

//    public OWLClassExpression unravelFixpoint(int level){
//        if (this.levelToExpressionMap.keySet().size() == 0){
//
//        }
//    }

    protected void initialize(){

    }

    protected OWLClassExpression convert(int level) {
        Integer lvl = level;
        if (this.levelToExpressionMap.containsKey(lvl)) {
            return this.levelToExpressionMap.get(lvl);
        } else {
            OWLClassExpression maxUnraveledExpression = this.dlStatementConverter.convert(concept, this.fixpointIdentifier, this.levelToExpressionMap.get(this.currentMaxLevel));
            this.currentMaxLevel += 1;
            this.levelToExpressionMap.put(this.currentMaxLevel, maxUnraveledExpression);
            return maxUnraveledExpression;
        }
    }

}
