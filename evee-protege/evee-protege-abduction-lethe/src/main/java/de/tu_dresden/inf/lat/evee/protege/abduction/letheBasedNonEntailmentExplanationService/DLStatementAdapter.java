package de.tu_dresden.inf.lat.evee.protege.abduction.letheBasedNonEntailmentExplanationService;

import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.collection.JavaConverters;
import uk.ac.man.cs.lethe.abduction.OWLAbducer;
import uk.ac.man.cs.lethe.internal.dl.datatypes.extended.GreatestFixpoint;
import uk.ac.man.cs.lethe.internal.dl.datatypes.extended.LeastFixpoint;
import uk.ac.man.cs.lethe.internal.dl.datatypes.*;
import uk.ac.man.cs.lethe.internal.dl.owlapi.OWLExporter;

import java.util.*;

public class DLStatementAdapter {

    private final DLStatement statement;
    private final Map<Concept, FixpointAdapter> fixpointAdapterMap;
//    maps to each fixpoint (identified via their variableName) the level to which this fixpoint
//    should be unraveled in the next result that is returned
    private final Map<Concept, Integer> fixpointCurrentLevelMap;
    private final Queue<List<Integer>> levelList;
    private int currentMaxLevel;
    private boolean singleResultReturned;

    private final OWLAbducer hypothesisSimplifier;

    private final Logger logger = LoggerFactory.getLogger(DLStatementAdapter.class);

    public DLStatementAdapter(DLStatement statement, OWLAbducer abducer){
        this.statement = statement;
        this.hypothesisSimplifier = abducer;
        this.fixpointAdapterMap = new LinkedHashMap<>();
        this.prepareFixpointMap();
        this.fixpointCurrentLevelMap = new LinkedHashMap<>();
        for (Concept fixpointIdentifier : this.fixpointAdapterMap.keySet()){
            this.fixpointCurrentLevelMap.put(fixpointIdentifier, 0);
        }
        this.levelList = new LinkedList<>();
        if (this.fixpointAdapterMap.keySet().size() > 0){
            this.createNextLevelList();
        }
        this.singleResultReturned = false;
    }

    public boolean singletonResult(){
        return this.fixpointAdapterMap.keySet().size() == 0;
    }

    public Set<OWLAxiom> getNextConversion(){
        this.logger.debug("getting next conversion");
        Set<OWLAxiom> result = new HashSet<>();
//        no fixpoints in statement -> only one result
        if (this.fixpointAdapterMap.keySet().size() == 0){
            this.logger.debug("no fixpoint in statement");
            if (! singleResultReturned){
                this.logger.debug("no result returned yet, generating result once");
                this.singleResultReturned = true;
                DLStatement simplifiedStatement = CheapSimplifier$.MODULE$.simplify(this.statement);
                this.logger.debug("Simplified statement:\n" + simplifiedStatement.toString());
                Set<OWLLogicalAxiom> axiomSet = JavaConverters.setAsJavaSet(new OWLExporter().toOwl(null, simplifiedStatement));
                this.logger.debug("Simplified statement converted to owl:");
                axiomSet.forEach(axiom -> this.logger.debug(axiom.toString()));
                axiomSet = JavaConverters.setAsJavaSet(
                        hypothesisSimplifier.simplify(
                                JavaConverters.asScalaSet(axiomSet)
                                        .toSet()));
                this.logger.debug("Very simplified statement:");
                axiomSet.forEach(axiom -> this.logger.debug(axiom.toString()));
                result.addAll(axiomSet);
                return result;
            }
            else {
                this.logger.debug("already returned result once, returning null");
                return null;
            }
        }
        else{
            List<Integer> nextLevelList = this.levelList.poll();
//        no more results for current maxLevel
            if (nextLevelList == null){
                this.logger.debug("no new results for this level, returning null");
                return null;
            }
            this.logger.debug("unraveling new result");
            int index = 0;
            for (Concept converter : this.fixpointCurrentLevelMap.keySet()){
                this.fixpointCurrentLevelMap.put(converter, nextLevelList.get(index));
                index+= 1;
            }
            DLStatement statement = this.statement;
//            this.logger.debug("statement in the beginning:\n" + statement);
            for (Concept fixpointName : this.fixpointCurrentLevelMap.keySet()){
                FixpointAdapter adapter = this.fixpointAdapterMap.get(fixpointName);
                statement = new Substitution(adapter.getFixpointVariable(), adapter.unravel(
                        this.fixpointCurrentLevelMap.get(fixpointName))).apply(statement);
//                this.logger.debug("statement at end of for-loop:\n" + statement);
            }
            for (Concept concept : JavaConverters.setAsJavaSet(statement.subConcepts().keySet())){
                if (concept instanceof LeastFixpoint){
                    LeastFixpoint lfp = (LeastFixpoint) concept;
                    statement = new Substitution(lfp, lfp.concept()).apply(statement);
                }
                else if (concept instanceof GreatestFixpoint){
                    GreatestFixpoint gfp = (GreatestFixpoint) concept;
                    statement = new Substitution(gfp, gfp.concept()).apply(statement);
                }
            }
//            this.logger.debug("finalised statement:\n" + statement);
            result.addAll(JavaConverters.setAsJavaSet(new OWLExporter().toOwl(null,
                    CheapSimplifier$.MODULE$.simplify(statement))));
            return result;
        }
    }

    protected void setMaxLevel(int newLevel){
        this.currentMaxLevel = newLevel;
    }


    protected void prepareFixpointMap(){
        for (Concept concept : JavaConverters.setAsJavaSet(this.statement.subConcepts().keySet())){
            if (concept instanceof LeastFixpoint){
                LeastFixpoint lfp = (LeastFixpoint) concept;
                FixpointAdapter adapter = new FixpointAdapter(lfp, lfp.concept(), lfp.variable(),
                        BottomConcept$.MODULE$);
                this.fixpointAdapterMap.put(lfp.variable(), adapter);
            }
            else if (concept instanceof  GreatestFixpoint){
                GreatestFixpoint gfp = (GreatestFixpoint) concept;
                FixpointAdapter adapter = new FixpointAdapter(gfp, gfp.concept(), gfp.variable(),
                        TopConcept$.MODULE$);
                this.fixpointAdapterMap.put(gfp.variable(), adapter);
            }
        }
    }

    /**
     * We save in this.levelList a list of lists of integers. Each integer represents the level of a fixpoint-statement.
     * A list of integers represents the level to which each fixpoint should be unraveled for a single solution.
     * We save in this.levelList only those lists which contain at least one element of the current maximum level up to
     * which we unravel a fixpoint.
     */
    protected void createNextLevelList(){
        this.levelList.clear();
        List<List<Integer>> results = this.recursivelyCreateLevelList(
                this.fixpointCurrentLevelMap.keySet().size());
        for (List<Integer> singleResult : results){
            if (singleResult.contains(this.currentMaxLevel)){
                this.levelList.add(singleResult);
            }
        }
    }

    private List<List<Integer>> recursivelyCreateLevelList(Integer position){
        ArrayList<List<Integer>> resultList = new ArrayList<>();
        if (position == 1){
            for (int level = 0; level <= currentMaxLevel; level++){
                ArrayList<Integer> singleResult = new ArrayList<>();
                singleResult.add(level);
                resultList.add(singleResult);
            }
        }
        else {
            List<List<Integer>> nextRecursionResult =
                    this.recursivelyCreateLevelList(position -1);
            for (List<Integer> singleResult : nextRecursionResult){
                for (int level = 0; level <= currentMaxLevel; level++){
                    List<Integer> newSingleResult = new ArrayList<>(singleResult);
                    newSingleResult.add(level);
                    resultList.add(newSingleResult);
                }
            }
        }
        return resultList;
    }

}
