package de.tu_dresden.inf.lat.evee.protege.abstractProofService.signature;

import de.tu_dresden.inf.lat.evee.protege.abstractProofService.preferences.EveeKnownSignaturePreferencesManager;
import org.apache.commons.io.FilenameUtils;
import org.protege.editor.core.ProtegeManager;
import org.protege.editor.owl.ui.action.ProtegeOWLAction;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.*;
import java.util.List;

public class KnownSignatureSelectionUI extends ProtegeOWLAction implements ActionListener {

    private static final String INIT = "Manage signature";
    private static final String RIGHT = "right";
    private static final String LEFT = "left";
    private static final String DELETE = "delete";
    private static final String LOAD = "load";
    private static final String SAVE = "save";
    private static final String CANCEL = "cancel";
    private static final String APPLY = "apply";
    private final Insets insets = new Insets(5, 5, 5, 5);
    private JDialog dialog;
    private JPanel holderPanel;
    private EveeKnownSignaturePreferencesManager preferencesManager;
    private OWLEntityListModel ontologyListModel;
    private JList<OWLEntity> ontologyList;
    private OWLEntityListModel knownSignatureListModel;
    private JList<OWLEntity> knownSignatureList;
    private final Logger logger = LoggerFactory.getLogger(KnownSignatureSelectionUI.class);

    public KnownSignatureSelectionUI(){

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case INIT :
                this.createUI();
                break;
            case RIGHT :
                this.moveRight();
                break;
            case LEFT :
                this.moveLeft();
                break;
            case DELETE :
                this.delete();
                break;
            case LOAD :
                this.load();
                break;
            case SAVE :
                this.save();
                break;
            case CANCEL :
                this.cancel();
                break;
            case APPLY :
                this.apply();
                break;
        }
    }

    @Override
    public void initialise() throws Exception {

    }

    @Override
    public void dispose() throws Exception {

    }

    private void createUI(){
        if (! this.getOWLModelManager().getActiveOntology().getOntologyID().getOntologyIRI().isPresent()){
            SwingUtilities.invokeLater(() -> {
                JOptionPane errorPane = new JOptionPane("Error: Anonymous ontology detected.", JOptionPane.ERROR_MESSAGE);
                JDialog errorDialog = errorPane.createDialog(ProtegeManager.getInstance().getFrame(this.getEditorKit().getWorkspace()), "Error");
                errorDialog.setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);
                errorDialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(
                        ProtegeManager.getInstance().getFrame(this.getEditorKit().getWorkspace())));
                errorDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                errorDialog.setVisible(true);
            });
            return;
        }
        String ontoName = this.getOWLModelManager().getActiveOntology().getOntologyID().getOntologyIRI().get().toString();
        SwingUtilities.invokeLater(() -> {
            this.dialog = new JDialog(ProtegeManager.getInstance().getFrame(this.getEditorKit().getWorkspace()));
//            this.dialog.setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);
            this.dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            this.dialog.setTitle("Manage signature for " + ontoName);
            this.holderPanel = new JPanel();
            this.dialog.getContentPane().add(holderPanel);
            this.holderPanel.setLayout(new GridBagLayout());
        });
        this.createListModels();
        this.addTopLabels();
        this.addScrollPanes();
        this.addMiddleButtons();
        this.addBottomButtons();
        SwingUtilities.invokeLater(() -> {
            this.dialog.pack();
            this.dialog.setLocationRelativeTo(
                    ProtegeManager.getInstance().getFrame(this.getWorkspace()));
            this.dialog.setVisible(true);
        });
    }

    private void createListModels(){
        this.preferencesManager = new EveeKnownSignaturePreferencesManager(
                this.getOWLModelManager().getActiveOntology().getOntologyID().getOntologyIRI().get().toString()); // getOntologyIRI().isPresent() was checked beforehand
        Set<OWLEntity> ontologyEntitySet = new HashSet<>(
                this.getOWLModelManager().getActiveOntology().getClassesInSignature(Imports.INCLUDED));
        ontologyEntitySet.addAll(
                this.getOWLModelManager().getActiveOntology().getDataPropertiesInSignature(Imports.INCLUDED)
        );
        ontologyEntitySet.addAll(
                this.getOWLModelManager().getActiveOntology().getDatatypesInSignature(Imports.INCLUDED)
        );
        ontologyEntitySet.addAll(
                this.getOWLModelManager().getActiveOntology().getIndividualsInSignature(Imports.INCLUDED)
        );
        ontologyEntitySet.addAll(
                this.getOWLModelManager().getActiveOntology().getObjectPropertiesInSignature(Imports.INCLUDED)
        );
        List<String> knownEntityIRIList = this.preferencesManager.loadKnownSignature();
        Set<OWLEntity> knownEntitySet = new HashSet<>();
        for (String iri : knownEntityIRIList){
            this.getOWLModelManager().getActiveOntology().getSignature(Imports.INCLUDED).stream().filter(
                    owlEntity -> owlEntity.getIRI().equals(IRI.create(iri))).forEach(
                    knownEntitySet::add);
        }
        ontologyEntitySet.removeAll(knownEntitySet);
        SwingUtilities.invokeLater(() -> {
            this.ontologyListModel = new OWLEntityListModel(ontologyEntitySet);
            this.knownSignatureListModel = new OWLEntityListModel(knownEntitySet);
        });
    }

    private void addTopLabels(){
        SwingUtilities.invokeLater(() -> {
            GridBagConstraints labelGBC = new GridBagConstraints();
            labelGBC.gridx = 0;
            labelGBC.gridy = 0;
            labelGBC.gridwidth = 3;
            labelGBC.fill = GridBagConstraints.HORIZONTAL;
            labelGBC.insets = this.insets;
            labelGBC.anchor = GridBagConstraints.CENTER;
            labelGBC.weightx = 0.1;
            labelGBC.weighty = 0.1;
            String firstString = "<html>Any OWL Entity in the right list will <b>not</b> be shown in any Evee proof.<br />";
            JLabel firstLabel = this.createLabel(firstString);
            String secondString = "This will also be considered when optimizing the Evee proofs (e.g. in the case of minimal size optimization).";
            JLabel secondLabel = this.createLabel(secondString);
            JPanel labelPanel = new JPanel();
            labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.PAGE_AXIS));
            labelPanel.add(firstLabel);
            labelPanel.add(secondLabel);
            this.holderPanel.add(labelPanel, labelGBC);
            labelGBC.gridy = 1;
            labelGBC.gridwidth = 1;
            JLabel ontoSignatureLabel = this.createLabel("Ontology Signature");
            this.holderPanel.add(ontoSignatureLabel, labelGBC);
            labelGBC.gridx = 2;
            JLabel knownSignatureLabel = this.createLabel("Known Signature");
            this.holderPanel.add(knownSignatureLabel, labelGBC);
        });
    }

    private JLabel createLabel(String labelText){
        JLabel label = new JLabel(labelText, JLabel.CENTER);
        label.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        label.setVerticalTextPosition(JLabel.CENTER);
        label.setHorizontalTextPosition(JLabel.CENTER);
        return label;
    }

    private void addScrollPanes(){
        SwingUtilities.invokeLater(() -> {
            GridBagConstraints scrollPaneGBC = new GridBagConstraints();
            scrollPaneGBC.gridx = 0;
            scrollPaneGBC.gridy = 2;
            scrollPaneGBC.fill = GridBagConstraints.BOTH;
            scrollPaneGBC.insets = this.insets;
            scrollPaneGBC.anchor = GridBagConstraints.CENTER;
            scrollPaneGBC.weightx = 0.9;
            scrollPaneGBC.weighty = 0.9;
            this.ontologyList = new JList<>(this.ontologyListModel);
            JScrollPane ontologySignatureScrollPane = this.createScrollPane(this.ontologyList);
            this.holderPanel.add(ontologySignatureScrollPane, scrollPaneGBC);
            this.knownSignatureList = new JList<>(this.knownSignatureListModel);
            JScrollPane knownSignatureScrollPane = this.createScrollPane(this.knownSignatureList);
            scrollPaneGBC.gridx = 2;
            this.holderPanel.add(knownSignatureScrollPane, scrollPaneGBC);
        });
    }

    private JScrollPane createScrollPane(JList<OWLEntity> entityList){
            entityList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            entityList.setLayoutOrientation(JList.VERTICAL);
            JScrollPane scrollPane = new JScrollPane(entityList);
            scrollPane.setPreferredSize(new Dimension(400, 600));
            scrollPane.setAlignmentX(JScrollPane.LEFT_ALIGNMENT);
            return scrollPane;
    }

    private void addMiddleButtons(){
        SwingUtilities.invokeLater(() -> {
            GridBagConstraints middleToolBarConstraints = new GridBagConstraints();
            middleToolBarConstraints.gridx = 1;
            middleToolBarConstraints.gridy = 2;
            middleToolBarConstraints.insets = this.insets;
            middleToolBarConstraints.anchor = GridBagConstraints.CENTER;
            middleToolBarConstraints.weightx = 0.1;
            middleToolBarConstraints.weighty = 0.1;
            JToolBar middleToolBar = new JToolBar();
            middleToolBar.setOrientation(JToolBar.VERTICAL);
            middleToolBar.setFloatable(false);
            middleToolBar.setLayout(new BoxLayout(middleToolBar, BoxLayout.PAGE_AXIS));
            JButton toRight = this.createButton(RIGHT, ">", "Move right");
            JButton toLeft = this.createButton(LEFT, "<", "Move left");
            middleToolBar.add(toRight);
            middleToolBar.add(Box.createRigidArea(new Dimension(0, 5)));
            middleToolBar.add(toLeft);
            this.holderPanel.add(middleToolBar, middleToolBarConstraints);
        });
    }

    private void addBottomButtons(){
        SwingUtilities.invokeLater(() -> {
            GridBagConstraints buttonGBC = new GridBagConstraints();
            buttonGBC.gridx = 0;
            buttonGBC.gridy = 3;
            buttonGBC.insets = this.insets;
            buttonGBC.anchor = GridBagConstraints.CENTER;
            buttonGBC.weightx = 0.1;
            buttonGBC.weighty = 0.1;
            JButton deleteButton = this.createButton(
                    DELETE, "Delete Signature",
                    "Remove all entries from known signature");
            this.holderPanel.add(deleteButton, buttonGBC);
            buttonGBC.gridx = 2;
            JButton loadButton = this.createButton(
                    LOAD, "Load from file",
                    "Load a signature from a file");
            this.holderPanel.add(loadButton, buttonGBC);
            buttonGBC.gridy = 4;
            JButton saveButton = this.createButton(
                    SAVE, "Save to file",
                    "Save a signature to a file");
            this.holderPanel.add(saveButton, buttonGBC);
            buttonGBC.gridx = 0;
            buttonGBC.gridy = 5;
            JButton cancelButton = this.createButton(
                    CANCEL, "Cancel",
                    "Cancel without saving any changes to the known signature");
            this.holderPanel.add(cancelButton, buttonGBC);
            buttonGBC.gridx = 2;
            JButton applyButton = this.createButton(
                    APPLY, "Apply",
                    "Apply current known signature to all Evee proofs");
            this.holderPanel.add(applyButton, buttonGBC);
        });
    }

    private JButton createButton(String actionCommand, String name, String toolTip){
        JButton newButton = new JButton(name);
        newButton.setToolTipText(toolTip);
        newButton.setActionCommand(actionCommand);
        newButton.addActionListener(this);
        return newButton;
    }

    private void moveRight(){
        SwingUtilities.invokeLater(() -> {
            Collection<OWLEntity> entities = this.ontologyList.getSelectedValuesList();
            this.knownSignatureListModel.addElements(entities);
            this.ontologyListModel.removeElements(entities);
            this.ontologyList.clearSelection();
            this.knownSignatureList.clearSelection();
        });
    }

    private void moveLeft(){
        SwingUtilities.invokeLater(() -> {
            Collection<OWLEntity> entities = this.knownSignatureList.getSelectedValuesList();
            this.ontologyListModel.addElements(entities);
            this.knownSignatureListModel.removeElements(entities);
            this.ontologyList.clearSelection();
            this.knownSignatureList.clearSelection();
        });
    }

    private void delete(){
        SwingUtilities.invokeLater(() -> {
            this.ontologyListModel.addElements(this.knownSignatureListModel.getOwlEntityList());
            this.knownSignatureListModel.deleteList();
            this.ontologyList.clearSelection();
            this.knownSignatureList.clearSelection();
        });
    }

    private void load(){
        JFileChooser fileChooser = this.createFileChooser();
        int result = fileChooser.showOpenDialog(this.dialog);
        List<IRI> iriList = new ArrayList<>();
        if (result == JFileChooser.APPROVE_OPTION){
            File file = fileChooser.getSelectedFile();
            try (FileReader fileReader = new FileReader(file);
                 BufferedReader bufferedReader = new BufferedReader(fileReader)){
                String line;
                while ((line = bufferedReader.readLine()) != null){
                    iriList.add(IRI.create(line));
                }
            }
            catch (IOException e){
                this.logger.error("Error when loading from file: ", e);
            }
        }
        if (iriList.size() == 0){
            return;
        }
        List<OWLEntity> entityList = new ArrayList<>();
        Set<OWLEntity> entitySet = this.getOWLModelManager().getActiveOntology().getSignature(Imports.INCLUDED);
        for (OWLEntity entity : entitySet){
            if (iriList.contains(entity.getIRI())){
                entityList.add(entity);
            }
        }
        this.knownSignatureListModel.deleteList();
        this.knownSignatureListModel.addElements(entityList);
        this.ontologyList.clearSelection();
        this.knownSignatureList.clearSelection();
    }

    private void save(){
        JFileChooser fileChooser = this.createFileChooser();
        int result = fileChooser.showSaveDialog(this.dialog);
        if (result == JFileChooser.APPROVE_OPTION){
            File file = fileChooser.getSelectedFile();
            if (! FilenameUtils.getExtension(file.getName()).equalsIgnoreCase("txt")) {
                file = new File(file.getParentFile(), FilenameUtils.getBaseName(file.getName()) + ".txt");
            }
            try (FileWriter fileWriter = new FileWriter(file);
                 BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)){
                for (OWLEntity entity : this.knownSignatureListModel.getOwlEntityList()){
                    bufferedWriter.write(entity.getIRI() + "\n");
                }
            }
            catch (IOException e){
                this.logger.error("Error when saving to file: ", e);
            }
        }
        this.ontologyList.clearSelection();
        this.knownSignatureList.clearSelection();
    }

    private JFileChooser createFileChooser(){
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        FileNameExtensionFilter fileFilter = new FileNameExtensionFilter(
                "txt files (*.txt)", "txt");
        fileChooser.setFileFilter(fileFilter);
        return fileChooser;
    }

    private void cancel(){
        SwingUtilities.invokeLater(() -> this.dialog.dispose());
    }

    private void apply(){
        SwingUtilities.invokeLater(() -> {
            List<String> iriList = new ArrayList<>();
            this.knownSignatureListModel.getOwlEntityList().forEach(entity -> iriList.add(entity.getIRI().toString()));
            this.preferencesManager.saveKnownSignature(iriList);
            this.dialog.dispose();
        });
    }


    private class OWLEntityListModel extends AbstractListModel<OWLEntity>{

        private List<OWLEntity> owlEntityList;

        public OWLEntityListModel(Collection<OWLEntity> entities){
            this.owlEntityList = new ArrayList<>(entities);
            this.sort();
        }

        @Override
        public int getSize() {
            return this.owlEntityList.size();
        }

        @Override
        public OWLEntity getElementAt(int index) {
            return this.owlEntityList.get(index);
        }

        public void sort(){
            Collections.sort(this.owlEntityList);
            this.fireContentsChanged(this, 0, this.owlEntityList.size() -1);
        }

        public void addElements(Collection<OWLEntity> elements){
            this.owlEntityList.addAll(elements);
            this.sort();
        }

        public void removeElements(Collection<OWLEntity> elements){
            this.owlEntityList.removeAll(elements);
            this.fireContentsChanged(this, 0, this.owlEntityList.size() -1);
        }

        public List<OWLEntity> getOwlEntityList(){
            return new ArrayList<>(this.owlEntityList);
        }

        public void deleteList(){
            this.owlEntityList = new ArrayList<>();
            this.fireContentsChanged(this, 0, 0);
        }

    }

}