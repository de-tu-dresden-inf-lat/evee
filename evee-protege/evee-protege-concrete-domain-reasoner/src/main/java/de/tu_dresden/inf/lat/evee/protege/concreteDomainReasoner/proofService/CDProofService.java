package de.tu_dresden.inf.lat.evee.protege.concreteDomainReasoner.proofService;

import de.tu_dresden.inf.lat.evee.concreteDomains.owlApi.OWLCDProofGenerator;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProofGenerator;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeProofService;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui.EveeDynamicProofLoadingUI;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

/** A proof service that explains the entailments of the running PACER reasoner. */
public abstract class CDProofService extends AbstractEveeProofService {

    /**
     * @param generator      the generator whose reasoner is set to the running PACER reasoner
     * @param innerGenerator the generator of the proofs, which uses generator, e.g. to minimise its proofs
     */
    protected CDProofService(OWLCDProofGenerator generator, IProofGenerator<OWLAxiom, OWLOntology> innerGenerator,
                             String proofServiceID) {
        super(new CDDynamicProofAdapter(generator, innerGenerator, new CDProofPreferencesManager(proofServiceID),
                new EveeDynamicProofLoadingUI(new CDProofPreferencesManager(proofServiceID).getProofServiceName())));
    }

    /** after the reasoner was synchronised, the proofs are generated again */
    @Override
    public void handleChange(OWLModelManagerChangeEvent event) {
        super.handleChange(event);
        if (event.isType(EventType.ONTOLOGY_CLASSIFIED))
            ((CDDynamicProofAdapter) this.proofAdapter).reasonerSynchronised();
    }

    /** only while PACER is the reasoner of Protégé for the active ontology, as for the proof service of ELK */
    @Override
    public boolean hasProof(OWLAxiom axiom) {
        return super.hasProof(axiom)
                && CDDynamicProofAdapter.activeReasoner(this.getEditorKit().getOWLModelManager()) != null;
    }
}
