package de.tu_dresden.inf.lat.evee.protege.abduction;

import de.tu_dresden.inf.lat.evee.proofs.interfaces.OWLAbductionSolver;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.man.cs.lethe.internal.dl.abduction.OWLAbducer;
import uk.ac.man.cs.lethe.internal.dl.abduction.forgetting.ConjunctiveDLStatement;
import uk.ac.man.cs.lethe.internal.dl.abduction.forgetting.DisjunctiveDLStatement;
import uk.ac.man.cs.lethe.internal.dl.datatypes.*;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class LetheAbductionSolver implements OWLAbductionSolver, Supplier<Set<OWLAxiom>> {

    private Set<OWLAxiom> observation = null;
    private Collection<OWLEntity> abducibles = null;
    private OWLOntology activeOntology = null;
    private OWLDataFactory dataFactory = null;
    private final OWLAbducer abducer = new OWLAbducer();
    private final List<DLStatementConverter> resultConverterList;
    private final Map<OWLOntology, DLStatementCache> ontoStatementMap;
    private int maxLevel;
    private int currentConverterIndex;
    boolean continueStream = false;

    private final Logger logger = LoggerFactory.getLogger(LetheAbductionSolver.class);

    public LetheAbductionSolver(){
        this.observation = new HashSet<>();
        this.resultConverterList = new ArrayList<>();
        this.ontoStatementMap = new HashMap<>();
        this.maxLevel = 0;
        this.currentConverterIndex = 0;
    }

    @Override
    public void setObservation(Set<OWLAxiom> observation) {
        this.observation = observation;
        this.continueStream = false;
    }

    @Override
    public void setAbducibles(Collection<OWLEntity> abducibles) {
        this.abducibles = abducibles;
        this.continueStream = false;
    }

    @Override
    public void setOntology(OWLOntology ontology) {
        this.activeOntology = ontology;
        this.dataFactory = this.activeOntology.getOWLOntologyManager().getOWLDataFactory();
        this.logger.debug("Resetting DLStatementCache for ontology " + this.activeOntology.getOntologyID().getOntologyIRI());
        DLStatementCache newCache = new DLStatementCache();
        this.ontoStatementMap.put(activeOntology, newCache);
        this.continueStream = false;
    }

    @Override
    public Stream<Set<OWLAxiom>> generateHypotheses() {
        this.logger.debug("Method generateHypotheses of LetheAbductionSolver called");
        assert (this.activeOntology != null);
        assert (this.dataFactory != null);
        assert (this.observation != null);
        assert (this.abducibles != null);
        if (this.continueStream) {
            this.logger.debug("Continuing old stream of hypotheses");
        }
        else{
            this.logger.debug("Creating new stream of hypotheses");
            this.continueStream = true;
            DLStatement hypotheses;
            DLStatementCache cache = this.ontoStatementMap.get(this.activeOntology);
            if (cache.getStatement(this.observation, this.abducibles) == null){
                this.logger.debug("No result for this observation/abducibles found, computing new DLStatement");
                this.abducer.setBackgroundOntology(this.activeOntology);
                this.abducer.setAbducibles(new HashSet<>(this.abducibles));
                hypotheses = this.abducer.abduce(this.observation);
                cache.putStatement(this.observation, this.abducibles, hypotheses);
            }
            else {
                this.logger.debug("Result for this observation and abducilbes found, retrieving cached DLStatement");
                hypotheses = cache.getStatement(this.observation, this.abducibles);
            }
            this.maxLevel = 0;
            this.currentConverterIndex = 0;
            this.resultConverterList.clear();
            this.logger.debug("Hypotheses found:\n" + hypotheses);
            ((DisjunctiveDLStatement) hypotheses).statements().foreach(
                    statement -> {
                        this.resultConverterList.add(new DLStatementConverter(
                                (ConjunctiveDLStatement) statement, this.activeOntology));
                        return null;
                    });
        }
        this.logger.debug("Returning stream of hypotheses");
        return Stream.generate(this);
    }

    @Override
    public Set<OWLAxiom> get() {
        int startIndex = this.currentConverterIndex;
        boolean checkedStartIndex = false;
        while (true){
            if (this.currentConverterIndex == this.resultConverterList.size()){
                this.maxLevel += 1;
                this.currentConverterIndex = 0;
                for (DLStatementConverter converter : this.resultConverterList){
                    if (! converter.singletonResult()){
                        converter.setMaxLevel(this.maxLevel);
                        converter.createNextLevelList();
                    }
                }
            }
            Set<OWLAxiom> result = this.resultConverterList.get(this.currentConverterIndex).getNextConversion();
            this.currentConverterIndex += 1;
            if (result == null){
//                check if we tried all statements
                if (startIndex == this.currentConverterIndex)
                {
                    if (checkedStartIndex){
                        break;
                    }
                    else {
                        checkedStartIndex = true;
                    }
                }
            }
            else{
                return result;
            }
        }
        return null;
    }

}
