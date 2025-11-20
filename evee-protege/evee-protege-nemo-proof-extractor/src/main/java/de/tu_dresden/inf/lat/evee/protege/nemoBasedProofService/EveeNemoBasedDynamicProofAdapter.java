package de.tu_dresden.inf.lat.evee.protege.nemoBasedProofService;

import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeDynamicProofAdapter;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui.EveeDynamicProofLoadingUI;
import de.tu_dresden.inf.lat.evee.protege.tools.ui.UIUtilities;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import java.awt.Dialog;
import java.io.File;

import org.protege.editor.core.ProtegeManager;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.OWLAxiom;

import de.tu_dresden.inf.lat.evee.nemo.*;

public class EveeNemoBasedDynamicProofAdapter extends AbstractEveeDynamicProofAdapter{

    private static final String EMPTY_NEMO_PATH = "<html>No path to NEMO is set.<br>Please set a path to the NEMO executable and try again</html>";

    private NemoProofGenerator generator;
    private EveeNemoBasedProofPreferencesManager preferencesManager;
    private String nemoPath;

    public EveeNemoBasedDynamicProofAdapter(
            ECalculus calc, 
            EveeNemoBasedProofPreferencesManager proofPreferencesManager,
            EveeDynamicProofLoadingUI uiWindow) {

        super(proofPreferencesManager, uiWindow);
        this.preferencesManager = proofPreferencesManager;

        generator = new NemoProofGenerator();
        generator.setCalculus(calc);
        updateNemoPath();
                
        setInnerProofGenerator(generator);
        resetCachingProofGenerator();
    }

    @Override
    public void start(OWLAxiom entailment, OWLEditorKit editorKit){
        if (!nemoPath.equals(preferencesManager.loadNemoPath())){
            updateNemoPath();    
            resetCachingProofGenerator();
        }
        
        if(nemoPath.equals("")){
            showPathDialog(editorKit);
            return;
        }

        super.start(entailment, editorKit);
    }

    private void updateNemoPath(){
        String newPath = preferencesManager.loadNemoPath();
        nemoPath = newPath;
        generator.setNemoExecPath(newPath);
    }


    private void showPathDialog(OWLEditorKit editorKit){
         SwingUtilities.invokeLater(() -> {
            JOptionPane warningPane = new JOptionPane(EMPTY_NEMO_PATH, JOptionPane.WARNING_MESSAGE);
            JDialog warningDialog = warningPane.createDialog(ProtegeManager.getInstance().getFrame(
                    editorKit.getWorkspace()), "Warning");
            warningDialog.setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);
            warningDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            warningDialog.addWindowListener(new java.awt.event.WindowAdapter(){
                @Override
                public void windowDeactivated(java.awt.event.WindowEvent windowEvent) {
                    SwingUtilities.invokeLater(() -> {
                        windowEvent.getWindow().dispose();
                        JFileChooser fileChooser = new JFileChooser();
                        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                        int result = fileChooser.showOpenDialog(
                                ProtegeManager.getInstance().getFrame(editorKit.getWorkspace()));
                        if (result == JFileChooser.APPROVE_OPTION){
                            File file = fileChooser.getSelectedFile();
                            preferencesManager.saveNemoPath(file.getPath());
                        }
                    });
                }
            });
            UIUtilities.packAndSetWindow(warningDialog, editorKit, true);
        });
    }
}
