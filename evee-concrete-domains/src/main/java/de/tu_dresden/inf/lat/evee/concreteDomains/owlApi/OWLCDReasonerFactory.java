package de.tu_dresden.inf.lat.evee.concreteDomains.owlApi;

import de.tu_dresden.inf.lat.evee.concreteDomains.CDReasoner;
import de.tu_dresden.inf.lat.evee.concreteDomains.data.exceptions.ParsingException;
import de.tu_dresden.inf.lat.evee.concreteDomains.data.extendedDataStructure.ExtendedOntology;
import de.tu_dresden.inf.lat.evee.concreteDomains.data.extendedDataStructure.parser.ExtendedOntologyParser;
import de.tu_dresden.inf.lat.evee.concreteDomains.data.names.ConcreteDomainName;
import de.tu_dresden.inf.lat.evee.concreteDomains.diffDomain.DiffReasoner;
import de.tu_dresden.inf.lat.evee.concreteDomains.linearDomain.GaussianEliminationReasoner;
import de.tu_dresden.inf.lat.evee.concreteDomains.multDomain.MultReasoner;
import de.tu_dresden.inf.lat.evee.concreteDomains.elkCD.ELKCDReasoner;
import de.tu_dresden.inf.lat.evee.concreteDomains.elkCD.ELKCDReasonerEmptyDomain;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OWLCDReasonerFactory implements OWLReasonerFactory {

    private final Logger logger = LoggerFactory.getLogger(OWLCDReasonerFactory.class);

    @Override
    public String getReasonerName() {
        return "PACER";
    }

    @Override
    public OWLReasoner createNonBufferingReasoner(OWLOntology ontology) {
        return createReasoner(ontology, null, false);
    }

    @Override
    public OWLReasoner createReasoner(OWLOntology ontology) {
        return createReasoner(ontology, null, true);
    }

    @Override
    public OWLReasoner createNonBufferingReasoner(OWLOntology ontology, OWLReasonerConfiguration config) {
        return createReasoner(ontology, config, false);
    }

    @Override
    public OWLReasoner createReasoner(OWLOntology ontology, OWLReasonerConfiguration config) {
        return createReasoner(ontology, config, true);
    }

    /**
     * Without an annotation cd:concreteDomain (in the ontology or, if it has none, in its imports), the reasoner
     * reasons like ELK. If the concrete domain or a constraint
     * cannot be read, it throws a {@link ParsingException} whose message says what is wrong and where.
     */
    private OWLReasoner createReasoner(OWLOntology ontology, OWLReasonerConfiguration config, boolean buffering) {
        if (!ExtendedOntologyParser.hasConcreteDomain(ontology)) {
            logger.info("Neither the ontology nor its imports have an annotation cd:concreteDomain, so PACER reasons like ELK");
            return new ELKCDReasonerEmptyDomain(ontology, config, buffering);
        }
        ConcreteDomainName domain = ExtendedOntologyParser.getConcreteDomainName(ontology);

        CDReasoner cdReasoner;
        switch(domain){
            case QDiff:
                cdReasoner = new DiffReasoner();
                break;
            case QLinear:
                cdReasoner = new GaussianEliminationReasoner();
                break;
            case QMult:
                cdReasoner = new MultReasoner();
                break;
            default:
                throw new IllegalArgumentException("Unsupported concrete domain: " + domain);
        }
        return new ELKCDReasoner(ontology, domain, cdReasoner, config, buffering);
    }
}
