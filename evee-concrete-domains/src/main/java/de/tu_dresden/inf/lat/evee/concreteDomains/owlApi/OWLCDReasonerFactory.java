package de.tu_dresden.inf.lat.evee.concreteDomains.owlApi;

import de.tu_dresden.inf.lat.evee.concreteDomains.CDReasoner;
import de.tu_dresden.inf.lat.evee.concreteDomains.data.extendedDataStructure.ExtendedOntology;
import de.tu_dresden.inf.lat.evee.concreteDomains.data.extendedDataStructure.parser.ExtendedOntologyParser;
import de.tu_dresden.inf.lat.evee.concreteDomains.data.names.ConcreteDomainName;
import de.tu_dresden.inf.lat.evee.concreteDomains.diffDomain.DiffReasoner;
import de.tu_dresden.inf.lat.evee.concreteDomains.linearDomain.LinearConstraintReasoner;
import de.tu_dresden.inf.lat.evee.concreteDomains.multDomain.MultReasoner;
import de.tu_dresden.inf.lat.evee.concreteDomains.elkCD.ELKCDReasoner;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;




public class OWLCDReasonerFactory implements OWLReasonerFactory {

    @Override
    public String getReasonerName() {
        return OWLCDReasonerFactory.class.getPackage().getImplementationTitle();

    }

    @Override
    public OWLReasoner createNonBufferingReasoner(OWLOntology ontology) {
          try{
            return createReasoner(ontology, null, false);
        } catch (Exception e){
            throw new RuntimeException(e); //TODO: handle exception properly
        }
    }

    @Override
    public OWLReasoner createReasoner(OWLOntology ontology) {
        try{
            return createReasoner(ontology, null, true);
        } catch (Exception e){
            throw new RuntimeException(e); //TODO: handle exception properly
        }
    }

    @Override
    public OWLReasoner createNonBufferingReasoner(OWLOntology ontology, OWLReasonerConfiguration config) {
        try{
            return createReasoner(ontology, config, false);
        } catch (Exception e){
            throw new RuntimeException(e); //TODO: handle exception properly
        }
    }

    @Override
    public OWLReasoner createReasoner(OWLOntology ontology, OWLReasonerConfiguration config) {
        try{
            return createReasoner(ontology, config, true);
        } catch (Exception e){
            throw new RuntimeException(e); //TODO: handle exception properly
        }
    }
    

    private OWLReasoner createReasoner(OWLOntology ontology, OWLReasonerConfiguration config, boolean buffering) {

        ConcreteDomainName domain = ExtendedOntologyParser.getConcreteDomainName(ontology);

        CDReasoner cdReasoner;
        switch(domain){
            case QDiff:
                cdReasoner = new DiffReasoner();
                break;
            case QLinear:
                cdReasoner = new LinearConstraintReasoner();
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
