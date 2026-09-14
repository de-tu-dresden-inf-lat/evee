package de.tu_dresden.inf.lat.evee.protege.concreteDomainReasoner;

import de.tu_dresden.inf.lat.evee.concreteDomains.owlApi.OWLCDReasonerFactory;

import org.protege.editor.owl.model.inference.AbstractProtegeOWLReasonerInfo;
import org.semanticweb.owlapi.reasoner.BufferingMode;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

public class ProtegeCDReasonerFactory extends AbstractProtegeOWLReasonerInfo{

    private final OWLCDReasonerFactory reasonerFactory = new OWLCDReasonerFactory();

    @Override
    public BufferingMode getRecommendedBuffering() {
        return BufferingMode.BUFFERING;
    }

    @Override
    public OWLReasonerFactory getReasonerFactory() {
        return reasonerFactory;
    }
    
}
