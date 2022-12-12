package de.tu_dresden.inf.lat.evee.protege.abduction;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import uk.ac.man.cs.lethe.internal.dl.datatypes.DLStatement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class DLStatementCache {

    private final List<Set<OWLAxiom>> observationList;
    private final List<Collection<OWLEntity>> abducibleList;
    private final List<DLStatement> statementList;

    public DLStatementCache(){
        this.observationList = new ArrayList<>();
        this.abducibleList = new ArrayList<>();
        this.statementList = new ArrayList<>();
    }

    protected boolean containsStatement(Set<OWLAxiom> givenObservation, Collection<OWLEntity> givenAbducibles){
        for (int idx = 0; idx < this.observationList.size(); idx ++){
            if (this.observationList.get(idx).equals(givenObservation) &&
                    this.abducibleList.get(idx).equals(givenAbducibles)){
                return true;
            }
        }
        return false;
    }

    protected DLStatement getStatement(Set<OWLAxiom> givenObservation, Collection<OWLEntity> givenAbducibles){
        DLStatement statement = null;
        for (int idx = 0; idx < this.observationList.size(); idx ++){
            if (this.observationList.get(idx).equals(givenObservation) &&
                    this.abducibleList.get(idx).equals(givenAbducibles)){
                statement = this.statementList.get(idx);
            }
        }
        return statement;
    }

    protected void putStatement(Set<OWLAxiom> observation, Collection<OWLEntity> abducibles, DLStatement statement){
        this.observationList.add(observation);
        this.abducibleList.add(abducibles);
        this.statementList.add(statement);
    }

}
