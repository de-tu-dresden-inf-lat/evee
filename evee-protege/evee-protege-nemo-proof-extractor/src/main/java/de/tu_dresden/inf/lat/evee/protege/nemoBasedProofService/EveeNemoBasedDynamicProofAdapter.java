package de.tu_dresden.inf.lat.evee.protege.nemoBasedProofService;

import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeDynamicProofAdapter;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui.EveeDynamicProofLoadingUI;

import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.OWLAxiom;

import de.tu_dresden.inf.lat.evee.nemo.*;

public class EveeNemoBasedDynamicProofAdapter extends AbstractEveeDynamicProofAdapter{

    private NemoProofGenerator generator;
    private EveeNemoBasedProofPreferencesManager preferencesManager;

    public EveeNemoBasedDynamicProofAdapter(
            ECalculus calc, 
            EveeNemoBasedProofPreferencesManager proofPreferencesManager,
            EveeDynamicProofLoadingUI uiWindow) {

        super(proofPreferencesManager, uiWindow);
        this.preferencesManager = proofPreferencesManager;
  
        initGenerator(calc);
        setInnerProofGenerator(generator);
        resetCachingProofGenerator();
    }

    @Override
    public void start(OWLAxiom entailment, OWLEditorKit editorKit){
        updateNemoPath();
        super.start(entailment, editorKit);
    }

    private void initGenerator(ECalculus calc){
        generator = new NemoProofGenerator();
        generator.setCalculus(calc);
        updateNemoPath();
    }

    private void updateNemoPath(){
        String nemoPath = preferencesManager.loadNemoPath();
        generator.setNemoExecPath(nemoPath);
    }
}
