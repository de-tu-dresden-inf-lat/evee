package de.tu_dresden.inf.lat.evee.protege.concreteDomainReasoner;

import de.tu_dresden.inf.lat.evee.concreteDomains.data.exceptions.ParsingException;
import de.tu_dresden.inf.lat.evee.concreteDomains.owlApi.OWLCDReasonerFactory;

import org.protege.editor.owl.model.inference.AbstractProtegeOWLReasonerInfo;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.BufferingMode;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import javax.swing.*;
import java.util.function.Supplier;

public class ProtegeCDReasonerFactory extends AbstractProtegeOWLReasonerInfo{

    private final OWLReasonerFactory reasonerFactory = new ReportingReasonerFactory(new OWLCDReasonerFactory());

    @Override
    public BufferingMode getRecommendedBuffering() {
        return BufferingMode.BUFFERING;
    }

    @Override
    public OWLReasonerFactory getReasonerFactory() {
        return reasonerFactory;
    }

    /**
     * Shows the errors of creating a reasoner, e.g. a constraint that cannot be read, in a dialog. Protégé only logs
     * them, as no reasoner is running yet; errors of a running reasoner it shows itself.
     */
    private static class ReportingReasonerFactory implements OWLReasonerFactory {

        private final OWLReasonerFactory factory;

        ReportingReasonerFactory(OWLReasonerFactory factory) {
            this.factory = factory;
        }

        @Override
        public String getReasonerName() {
            return factory.getReasonerName();
        }

        @Override
        public OWLReasoner createNonBufferingReasoner(OWLOntology ontology) {
            return reported(() -> factory.createNonBufferingReasoner(ontology));
        }

        @Override
        public OWLReasoner createReasoner(OWLOntology ontology) {
            return reported(() -> factory.createReasoner(ontology));
        }

        @Override
        public OWLReasoner createNonBufferingReasoner(OWLOntology ontology, OWLReasonerConfiguration configuration) {
            return reported(() -> factory.createNonBufferingReasoner(ontology, configuration));
        }

        @Override
        public OWLReasoner createReasoner(OWLOntology ontology, OWLReasonerConfiguration configuration) {
            return reported(() -> factory.createReasoner(ontology, configuration));
        }

        private static OWLReasoner reported(Supplier<OWLReasoner> creation) {
            try {
                return creation.get();
            } catch (RuntimeException e) {
                String message = e instanceof ParsingException ? e.getMessage()
                        : e.getClass().getSimpleName() + ": " + e.getMessage() + "\n(see the log for details)";
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null,
                        "PACER cannot reason with this ontology:\n" + message, "PACER", JOptionPane.ERROR_MESSAGE));
                throw e;
            }
        }
    }
}
