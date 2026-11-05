package de.tu_dresden.inf.lat.evee.protege.concreteDomainReasoner.proofService;

import de.tu_dresden.inf.lat.evee.concreteDomains.CDConstraint;
import de.tu_dresden.inf.lat.evee.concreteDomains.elkCD.ELKCDReasoner;
import de.tu_dresden.inf.lat.evee.concreteDomains.owlApi.OWLCDProofGenerator;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProofGenerator;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeDynamicProofAdapter;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui.EveeDynamicProofLoadingUI;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

/**
 * Generates proofs with the given generator, which uses the given {@link OWLCDProofGenerator}. As the proof service
 * of ELK, it uses the running PACER reasoner of Protégé as it is, so the proofs are for the state of the ontology when
 * the reasoner was last synchronised, like the inferred class hierarchy. The limits for the alternatives of
 * constraint proofs come from the preferences.
 */
public class CDDynamicProofAdapter extends AbstractEveeDynamicProofAdapter {

    private final OWLCDProofGenerator generator;
    private final CDProofPreferencesManager preferencesManager;
    /** the limits last given to the generator; -1 before the first proof */
    private int maxVariablesForAllOrders = -1;
    private int maxVariablesForAllPaths = -1;
    private ELKCDReasoner<CDConstraint> reasonerInUse = null;
    private boolean reasonerChanged = false;

    public CDDynamicProofAdapter(OWLCDProofGenerator generator, IProofGenerator<OWLAxiom, OWLOntology> innerGenerator,
                                 CDProofPreferencesManager preferencesManager, EveeDynamicProofLoadingUI uiWindow) {
        super(preferencesManager, uiWindow);
        this.generator = generator;
        this.preferencesManager = preferencesManager;
        super.setInnerProofGenerator(innerGenerator);
        super.resetCachingProofGenerator();
    }

    @Override
    public void start(OWLAxiom entailment, OWLEditorKit editorKit) {
        ELKCDReasoner<CDConstraint> reasoner = activeReasoner(editorKit.getOWLModelManager());
        if (reasoner != this.reasonerInUse) {
            this.generator.setReasoner(reasoner);
            this.reasonerInUse = reasoner;
            this.reasonerChanged = true;
        }
        super.start(entailment, editorKit);
    }

    /** the running PACER reasoner of Protégé, if it is for the active ontology */
    @SuppressWarnings("unchecked")
    static ELKCDReasoner<CDConstraint> activeReasoner(OWLModelManager modelManager) {
        OWLReasoner reasoner = modelManager.getReasoner();
        if (reasoner instanceof ELKCDReasoner && reasoner.getRootOntology().equals(modelManager.getActiveOntology()))
            return (ELKCDReasoner<CDConstraint>) reasoner;
        return null;
    }

    /** the reasoner was synchronised, so the cached proofs may be out of date */
    public void reasonerSynchronised() {
        super.resetCachingProofGenerator();
    }

    @Override
    protected void setProofGeneratorParameters(boolean previousParametersChanged) {
        boolean parametersChanged = this.reasonerChanged;
        this.reasonerChanged = false;
        int allOrders = this.preferencesManager.loadMaxVariablesForAllOrders();
        int allPaths = this.preferencesManager.loadMaxVariablesForAllPaths();
        if (allOrders != this.maxVariablesForAllOrders || allPaths != this.maxVariablesForAllPaths) {
            this.generator.setMaxVariablesForAllOrders(allOrders);
            this.generator.setMaxVariablesForAllPaths(allPaths);
            this.maxVariablesForAllOrders = allOrders;
            this.maxVariablesForAllPaths = allPaths;
            parametersChanged = true;
        }
        // the base class resets the cache only for ontologies with an IRI
        if (parametersChanged)
            super.resetCachingProofGenerator();
        super.setProofGeneratorParameters(previousParametersChanged);
    }
}
