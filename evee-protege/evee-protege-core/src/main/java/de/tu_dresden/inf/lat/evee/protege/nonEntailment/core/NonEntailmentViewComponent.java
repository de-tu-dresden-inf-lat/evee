package de.tu_dresden.inf.lat.evee.protege.nonEntailment.core;

import de.tu_dresden.inf.lat.evee.general.interfaces.ExplanationGenerationListener;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.core.service.NonEntailmentExplanationPlugin;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.core.service.NonEntailmentExplanationPluginLoader;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.core.interfaces.NonEntailmentExplanationService;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.ExplanationEvent;
import org.apache.commons.io.FilenameUtils;
import org.protege.editor.core.ProtegeManager;
import org.protege.editor.core.ui.util.ComponentFactory;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.classexpression.OWLExpressionParserException;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.model.parser.ParserUtil;
import org.protege.editor.owl.model.parser.ProtegeOWLEntityChecker;
import org.protege.editor.owl.ui.clsdescriptioneditor.OWLExpressionChecker;
import org.protege.editor.owl.ui.clsdescriptioneditor.ExpressionEditor;
import org.protege.editor.owl.ui.renderer.OWLCellRenderer;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.manchestersyntax.parser.ManchesterOWLSyntaxParserImpl;
import org.semanticweb.owlapi.manchestersyntax.renderer.ParserException;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.util.mansyntax.ManchesterOWLSyntaxParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import de.tu_dresden.inf.lat.evee.protege.tools.ui.OWLObjectListModel;

public class NonEntailmentViewComponent extends AbstractOWLViewComponent implements ActionListener, ExplanationGenerationListener<ExplanationEvent<NonEntailmentExplanationService>> {

//    static { System.load("E:\\Programs\\Protege\\Protege-5.5.0\\plugins\\evee-protege-core-0.2-SNAPSHOT.jar\\HelloWorld.dll"); }
//    static {
//        File file = new File(String.valueOf(NonEntailmentViewComponent.class.getResource("E:\\Programs\\Protege\\Protege-5.5.0\\plugins\\evee-protege-core-0.2-SNAPSHOT.jar\\HelloWorld.dll")));
//        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
//        System.load();
//    }
//    working, but referencing file outside the jar:
//    static {System.load("E:\\ProgrammingProjects\\SHK-TUD\\evee\\evee-protege\\evee-protege-core\\src\\main\\java\\de\\tu_dresden\\inf\\lat\\evee\\protege\\nonEntailment\\core\\HelloWorld.dll"); }

//    public static native String getHelloWorld();

    private final NonEntailmentExplainerManager nonEntailmentExplainerManager;
    private final ViewComponentOntologyChangeListener changeListener;

    private NonEntailmentSignatureSelectionUI signatureSelectionUI;
    private final Insets STANDARD_INSETS = new Insets(5, 5, 5, 5);
    private ExpressionEditor<OWLAxiom> observationTextEditor;
    private OWLObjectListModel<OWLAxiom> selectedObservationListModel;
    private JList<OWLAxiom> selectedObservationList;
    private JButton computeButton;
    private JPanel resultHolderPanel;
    private JPanel holderPanel;
    private JPanel serviceSelectionPanel;
    private JPanel signatureAndObservationPanel;
    private JPanel signatureManagementPanel;
    private JPanel observationManagementPanel;
    private JPanel nonEntailmentExplanationServicePanel;
    private JSplitPane outerSplitPane;
    private JPanel splitPaneHolderPanel;
    private JComboBox<String> serviceNamesComboBox;
    private static final String COMPUTE_COMMAND = "COMPUTE_NON_ENTAILMENT";
    private static final String COMPUTE_NAME = "Compute";
    private static final String COMPUTE_TOOLTIP = "Compute non-entailment explanation using Selected Signature and Observation";
    private static final String ADD_OBSERVATION_COMMAND = "ADD_OBSERVATION";
    private static final String ADD_OBSERVATION_NAME = "Add";
    private static final String ADD_OBSERVATION_TOOLTIP = "Add axioms to observation";
    private static final String DELETE_OBSERVATION_COMMAND = "DELETE_OBSERVATION";
    private static final String DELETE_OBSERVATION_NAME = "Delete";
    private static final String DELETE_OBSERVATION_TOOLTIP = "Delete selected axioms from observation";
    private static final String RESET_OBSERVATION_COMMAND = "RESET_OBSERVATION";
    private static final String RESET_OBSERVATION_NAME = "Reset";
    private static final String RESET_OBSERVATION_TOOLTIP = "Delete all axioms from observation";
    private static final String LOAD_SIGNATURE_COMMAND = "LOAD_SIGNATURE";
    private static final String LOAD_SIGNATURE_BUTTON_NAME = "Load from file";
    private static final String LOAD_SIGNATURE_BUTTON_TOOLTIP = "Load a signature from a file";
    private static final String SAVE_SIGNATURE_COMMAND = "SAVE_SIGNATURE";
    private static final String SAVE_SIGNATURE_BUTTON_NAME = "Save to file";
    private static final String SAVE_SIGNATURE_BUTTON_TOOLTIP = "Save a signature to a file";
    private static final String USE_SIGNATURE_DELIMITER = "##### Use Signature: #####";
    private static final String TRUE = "TRUE";
    private static final String FALSE = "FALSE";
    private static final String CLASSES_DELIMITER = "##### Classes: #####";
    private static final String OBJECT_PROPERTIES_DELIMITER = "##### Object Properties: #####";
    private static final String INDIVIDUAL_DELIMITER = "##### Individuals: #####";
    private static final String LOAD_OBSERVATION_COMMAND = "LOAD_OBSERVATION";
    private static final String LOAD_OBSERVATION_BUTTON_NAME = "Load from file";
    private static final String LOAD_OBSERVATION_TOOLTIP = "Load an observation from a file";
    private static final String SAVE_OBSERVATION_COMMAND = "SAVE_OBSERVATION";
    private static final String SAVE_OBSERVATION_BUTTON_NAME = "Save to file";
    private static final String SAVE_OBSERVATION_TOOLTIP = "Save an observation to a file";

    private final Logger logger = LoggerFactory.getLogger(NonEntailmentViewComponent.class);

    public NonEntailmentViewComponent(){
        this.nonEntailmentExplainerManager = new NonEntailmentExplainerManager();
        this.changeListener = new ViewComponentOntologyChangeListener();
        this.logger.debug("Object NonEntailmentViewComponent created");


//        this.logger.debug("Lets try if this c-stuff works:");
//        get root-directory of protege:
//        this.logger.debug("current directory:" + System.getProperty("user.dir"));

//        not working? (file object is null) might try again with FileCreatorTest but result should be the same
//        File file = new File(String.valueOf(NonEntailmentViewComponent.class.getResource("E:\\Programs\\Protege\\Protege-5.5.0\\plugins\\evee-protege-core-0.2-SNAPSHOT.jar\\HelloWorld.dll")));
//        this.logger.debug("created the following file-object via getResource: " + file.getName());

//        not working: "Das System kann die angegebene Datei nicht finden"
//        try {
//            String filePath = NonEntailmentViewComponent.class.getProtectionDomain().getCodeSource().getLocation().getPath();
//            String command = filePath + "/FileCreatorTest.dll";
//            Process process = new ProcessBuilder(command).start();
//            int exitCode = process.waitFor();
//        } catch (IOException | InterruptedException e) {
//            this.logger.error("Error occurred when using ProcessBuilder: " + e);
//        }

//        this.logger.debug(getHelloWorld());

//        working: unpacking files inside the jar to temporary directory, then executing binary unpacked this way
//        Path filePath = Paths.get(System.getProperty("user.dir"), "plugins", "evee-protege-core-0.2-SNAPSHOT.jar");
//        File fileObject = new File(String.valueOf(filePath));
//        try {
//            JarFile jarFile = new JarFile(fileObject);
//            Enumeration enumEntries = jarFile.entries();
//            while (enumEntries.hasMoreElements()) {
//                JarEntry fileToUnpack = (JarEntry) enumEntries.nextElement();
//                Path destinationDirectory = Paths.get(System.getProperty("user.dir"));
//                if (fileToUnpack.getName().equals("FileCreatorTest.dll")){
//                    File cFile = new File(destinationDirectory + java.io.File.separator + fileToUnpack.getName());
//                    InputStream is = jarFile.getInputStream(fileToUnpack); // get the input stream
//                    FileOutputStream fos = new FileOutputStream(cFile);
//                    while (is.available() > 0) {  // write contents of 'is' to 'fos'
//                        fos.write(is.read());
//                    }
//                    fos.close();
//                    is.close();
//                }
//            }
//            jarFile.close();
//            String command = Paths.get(System.getProperty("user.dir"),"FileCreatorTest.dll").toString();
//            Process process = new ProcessBuilder(command).start();
//            int exitCode = process.waitFor();
//        } catch (IOException | InterruptedException e) {
//            this.logger.error("Error when doing the C-Stuff: " + e);
//        }





    }

    protected ArrayList<OWLObject> getObservations(){
        return new ArrayList<>(this.selectedObservationListModel.getOwlObjects());
    }

    @Override
    protected void initialiseOWLView() {
        this.logger.debug("initialisation started");
        this.signatureSelectionUI = new NonEntailmentSignatureSelectionUI(
                this, this.getOWLEditorKit(),
                this.getOWLModelManager());
        NonEntailmentExplanationPluginLoader loader = new NonEntailmentExplanationPluginLoader(this.getOWLEditorKit());
        for (NonEntailmentExplanationPlugin plugin : loader.getPlugins()){
            try{
                NonEntailmentExplanationService service = plugin.newInstance();
                service.setup(this.getOWLEditorKit());
                service.initialise();
                service.registerListener(this);
                this.nonEntailmentExplainerManager.registerNonEntailmentExplanationService(service, plugin.getName());
            }
            catch (Exception e){
                this.logger.error("Error while loading non-entailment explanation plugin:\n" + e);
            }
        }
        SwingUtilities.invokeLater(() -> {
            this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
            this.createGeneralSettingsComponent();
            this.nonEntailmentExplainerManager.setExplanationService((String) this.serviceNamesComboBox.getSelectedItem());
            this.createSignatureManagementComponent();
            this.createObservationComponent();
            this.resetView();
        });
        this.getOWLEditorKit().getOWLModelManager().addListener(this.changeListener);
        this.getOWLEditorKit().getOWLModelManager().addOntologyChangeListener(this.changeListener);
        this.logger.debug("initialisation completed");
    }

    private void resetView(){
        this.holderPanel = new JPanel(new GridBagLayout());
        this.signatureAndObservationPanel = new JPanel();
        this.signatureAndObservationPanel.setLayout(new BoxLayout(this.signatureAndObservationPanel, BoxLayout.PAGE_AXIS));
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Signature", this.signatureManagementPanel);
        tabbedPane.addTab("Observation", this.observationManagementPanel);
        this.signatureAndObservationPanel.add(tabbedPane);
        this.nonEntailmentExplanationServicePanel = new JPanel();
        this.nonEntailmentExplanationServicePanel.setLayout(new BoxLayout(this.nonEntailmentExplanationServicePanel, BoxLayout.PAGE_AXIS));
        this.resultHolderPanel = new JPanel();
        this.resultHolderPanel.setLayout(new BoxLayout(this.resultHolderPanel, BoxLayout.PAGE_AXIS));
        NonEntailmentExplanationService explainer = this.nonEntailmentExplainerManager.getCurrentExplainer();
        if (explainer != null){
            if (explainer.getSettingsComponent() != null){
                JSplitPane settingsAndResultSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                        explainer.getSettingsComponent(), this.resultHolderPanel);
                settingsAndResultSplitPane.setDividerLocation(0.3);
                this.nonEntailmentExplanationServicePanel.add(settingsAndResultSplitPane);
            }
            else {
                this.nonEntailmentExplanationServicePanel.add(this.resultHolderPanel);
            }
        }
        else {
            this.nonEntailmentExplanationServicePanel.add(this.resultHolderPanel);
        }
        this.outerSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                this.signatureAndObservationPanel,
                this.nonEntailmentExplanationServicePanel);
        this.outerSplitPane.setDividerLocation(0.3);
        this.splitPaneHolderPanel = new JPanel();
        this.splitPaneHolderPanel.setLayout(new BoxLayout(this.splitPaneHolderPanel, BoxLayout.PAGE_AXIS));
        this.splitPaneHolderPanel.add(this.outerSplitPane);
        GridBagConstraints constraints = new GridBagConstraints();
//        general constraints:
        constraints.insets = this.STANDARD_INSETS;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.gridx = 0;
//        upper panel constraints:
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridy = 0;
        constraints.weightx = 0.1;
        constraints.weighty = 0.0;
        this.holderPanel.add(this.serviceSelectionPanel, constraints);
//        lower panel constraints:
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridy = 1;
        constraints.weightx = 0.5;
        constraints.weighty = 0.9;
        this.holderPanel.add(this.splitPaneHolderPanel, constraints);
        this.removeAll();
        this.add(holderPanel);
        this.repaint();
        this.revalidate();
    }

    @Override
    protected void disposeOWLView() {
        this.signatureSelectionUI.dispose(this.getOWLModelManager());
        this.getOWLEditorKit().getOWLModelManager().removeListener(this.changeListener);
        this.getOWLEditorKit().getOWLModelManager().removeOntologyChangeListener(this.changeListener);
        this.nonEntailmentExplainerManager.dispose();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof JComboBox){
            JComboBox comboBox = (JComboBox) e.getSource();
            String serviceName = (String) comboBox.getSelectedItem();
            this.nonEntailmentExplainerManager.setExplanationService(serviceName);
            this.changeComputeButtonStatus();
        }
        else{
            switch (e.getActionCommand()){
                case COMPUTE_COMMAND:
                    this.computeExplanation();
                    break;
                case ADD_OBSERVATION_COMMAND:
                    this.addObservation();
                    break;
                case DELETE_OBSERVATION_COMMAND:
                    this.deleteObservation();
                    break;
                case RESET_OBSERVATION_COMMAND:
                    this.resetObservation();
                    break;
                case LOAD_SIGNATURE_COMMAND:
                    this.loadSignature();
                    break;
                case SAVE_SIGNATURE_COMMAND:
                    this.saveSignature();
                    break;
                case LOAD_OBSERVATION_COMMAND:
                    this.loadObservation();
                    break;
                case SAVE_OBSERVATION_COMMAND:
                    this.saveObservation();
                    break;
            }
        }
    }

    @Override
    public void handleEvent(ExplanationEvent<NonEntailmentExplanationService> event){
        switch (event.getType()){
            case COMPUTATION_COMPLETE :
                this.showResult(event.getSource().getResult());
                break;
            case ERROR :
                this.showError(event.getSource().getErrorMessage());
                break;
        }
    }


    private void createSignatureManagementComponent(){
        this.signatureSelectionUI.createSignatureSelectionComponents(this.getOWLEditorKit());
        JPanel ontologySignaturePanel = this.signatureSelectionUI.getOntologySignatureTabbedPanel();
        this.signatureManagementPanel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
//        general:
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = this.STANDARD_INSETS;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.gridx = 0;
//        specific for given signature tabbed pane:
        constraints.gridy= 0;
        constraints.weightx = 0.3;
        constraints.weighty = 0.6;
        this.signatureManagementPanel.add(ontologySignaturePanel, constraints);
        JButton loadSignatureButton = this.createButton(LOAD_SIGNATURE_COMMAND, LOAD_SIGNATURE_BUTTON_NAME, LOAD_SIGNATURE_BUTTON_TOOLTIP);
        JToolBar toolbar = new JToolBar();
        toolbar.setOrientation(JToolBar.HORIZONTAL);
        toolbar.setFloatable(false);
        toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.LINE_AXIS));
        toolbar.add(loadSignatureButton);
        toolbar.add(Box.createRigidArea(new Dimension(5, 0)));
        JButton saveSignatureButton = this.createButton(SAVE_SIGNATURE_COMMAND, SAVE_SIGNATURE_BUTTON_NAME, SAVE_SIGNATURE_BUTTON_TOOLTIP);
        toolbar.add(saveSignatureButton);
        JPanel signatureSelectionToolPanel = this.signatureSelectionUI.getSignatureSelectionButtons();
        signatureSelectionToolPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        signatureSelectionToolPanel.add(toolbar);
//        specific for signature selected buttons:
        constraints.gridy = 1;
        constraints.weightx = 0;
        constraints.weighty = 0;
        this.signatureManagementPanel.add(signatureSelectionToolPanel, constraints);
        JPanel listPanel = this.signatureSelectionUI.getSelectedSignatureListPanel();
//        specific for selected signature pane:
        constraints.gridy = 2;
        constraints.weightx = 0.3;
        constraints.weighty = 0.6;
        this.signatureManagementPanel.add(listPanel, constraints);
    }

    private JButton createButton(String actionCommand, String name, String toolTip){
        JButton newButton = new JButton(name);
        newButton.setActionCommand(actionCommand);
        newButton.setToolTipText(toolTip);
        newButton.addActionListener(this);
        return newButton;
    }

    private void createObservationComponent(){
        this.observationManagementPanel = new JPanel();
        this.observationManagementPanel.setLayout(new GridBagLayout());
        JPanel observationTextPanel = this.createObservationTextPanel();
        GridBagConstraints constraints = new GridBagConstraints();
//        general constraints:
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = this.STANDARD_INSETS;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.gridx = 0;
//        specific for editor panel:
        constraints.gridy= 0;
        constraints.weightx = 0.3;
        constraints.weighty = 0.6;
        this.observationManagementPanel.add(observationTextPanel, constraints);
        JPanel buttonPanel = this.createObservationButtonPanel();
//        specific for button panel:
        constraints.gridy = 1;
        constraints.weightx = 0;
        constraints.weighty = 0;
        this.observationManagementPanel.add(buttonPanel, constraints);
        JPanel selectedObservationPanel = this.createSelectedObservationPanel();
//        specific for selected observations:
        constraints.gridy = 2;
        constraints.weightx = 0.3;
        constraints.weighty = 0.6;
        this.observationManagementPanel.add(selectedObservationPanel, constraints);
    }

    private JPanel createObservationTextPanel(){
        JPanel observationEditorPanel = new JPanel();
        observationEditorPanel.setLayout(new BoxLayout(observationEditorPanel, BoxLayout.PAGE_AXIS));
        OWLExpressionChecker<OWLAxiom> logicalAxiomChecker =
                new OWLLogicalAxiomChecker(this.getOWLModelManager());
        this.observationTextEditor = new ExpressionEditor<>(this.getOWLEditorKit(), logicalAxiomChecker);
        JScrollPane editorScrollPane = ComponentFactory.createScrollPane(this.observationTextEditor);
        editorScrollPane.setPreferredSize(new Dimension(400, 400));
        observationEditorPanel.add(editorScrollPane);
        observationEditorPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createEmptyBorder(5, 5, 5, 5),
                        "Enter observation:"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        return observationEditorPanel;
    }

    private JPanel createObservationButtonPanel(){
        JPanel buttonHolderPanel = new JPanel();
        buttonHolderPanel.setLayout(new BoxLayout(buttonHolderPanel, BoxLayout.PAGE_AXIS));
        JToolBar firstRowToolbar = new JToolBar();
        firstRowToolbar.setOrientation(JToolBar.HORIZONTAL);
        firstRowToolbar.setFloatable(false);
        firstRowToolbar.setLayout(new BoxLayout(firstRowToolbar, BoxLayout.LINE_AXIS));
        JButton addObservationButton = this.createButton(ADD_OBSERVATION_COMMAND,
                ADD_OBSERVATION_NAME, ADD_OBSERVATION_TOOLTIP);
        firstRowToolbar.add(addObservationButton);
        firstRowToolbar.add(Box.createRigidArea(new Dimension(5, 0)));
        JButton deleteObservationButton = this.createButton(DELETE_OBSERVATION_COMMAND,
                DELETE_OBSERVATION_NAME, DELETE_OBSERVATION_TOOLTIP);
        firstRowToolbar.add(deleteObservationButton);
        firstRowToolbar.add(Box.createRigidArea(new Dimension(5, 0)));
        JButton resetObservationButton = this.createButton(RESET_OBSERVATION_COMMAND,
                RESET_OBSERVATION_NAME, RESET_OBSERVATION_TOOLTIP);
        firstRowToolbar.add(resetObservationButton);
        buttonHolderPanel.add(firstRowToolbar);
        buttonHolderPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        JToolBar secondRowToolBar = new JToolBar();
        secondRowToolBar.setOrientation(JToolBar.HORIZONTAL);
        secondRowToolBar.setFloatable(false);
        secondRowToolBar.setLayout(new BoxLayout(secondRowToolBar, BoxLayout.LINE_AXIS));
        JButton loadObservationButton = this.createButton(LOAD_OBSERVATION_COMMAND, LOAD_OBSERVATION_BUTTON_NAME, LOAD_OBSERVATION_TOOLTIP);
        secondRowToolBar.add(loadObservationButton);
        secondRowToolBar.add(Box.createRigidArea(new Dimension(5, 0)));
        JButton saveObservationButton = this.createButton(SAVE_OBSERVATION_COMMAND, SAVE_OBSERVATION_BUTTON_NAME, SAVE_OBSERVATION_TOOLTIP);
        secondRowToolBar.add(saveObservationButton);
        buttonHolderPanel.add(secondRowToolBar);
        buttonHolderPanel.setAlignmentX(Box.CENTER_ALIGNMENT);
        return buttonHolderPanel;
    }

    private JPanel createSelectedObservationPanel(){
        JPanel observationPanel = new JPanel();
        observationPanel.setLayout(new BoxLayout(observationPanel, BoxLayout.PAGE_AXIS));
        this.selectedObservationListModel = new OWLObjectListModel<>();
        this.selectedObservationList = new JList<>(this.selectedObservationListModel);
        this.selectedObservationList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2){
                    JList list = (JList) e.getSource();
                    Object selectedValue = list.getSelectedValue();
                    if (selectedValue instanceof OWLObject){
                        observationTextEditor.setText(reverseParseOWLObject((OWLObject) selectedValue));
                    }
                }
            }
        });
        OWLCellRenderer renderer = new OWLCellRenderer(this.getOWLEditorKit());
        renderer.setHighlightKeywords(true);
        renderer.setHighlightUnsatisfiableClasses(false);
        renderer.setHighlightUnsatisfiableProperties(false);
        this.selectedObservationList.setCellRenderer(renderer);
        JScrollPane scrollPane = new JScrollPane(this.selectedObservationList);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setPreferredSize(new Dimension(400, 400));
        observationPanel.add(scrollPane);
        observationPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createEmptyBorder(),
                        "Selected observation:"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        return observationPanel;
    }

    private void createGeneralSettingsComponent(){
        this.serviceSelectionPanel = new JPanel();
        this.serviceSelectionPanel.setLayout(new BoxLayout(this.serviceSelectionPanel, BoxLayout.PAGE_AXIS));
        Vector<String> serviceNames = this.nonEntailmentExplainerManager.getExplanationServiceNames();
        this.serviceNamesComboBox = new JComboBox<>(serviceNames);
        this.serviceNamesComboBox.addActionListener(this);
        this.serviceSelectionPanel.add(this.serviceNamesComboBox);
        this.serviceSelectionPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        this.computeButton = this.createButton(COMPUTE_COMMAND, COMPUTE_NAME, COMPUTE_TOOLTIP);
        this.computeButton.setEnabled(false);
        JPanel buttonHelperPanel = new JPanel();
        buttonHelperPanel.setLayout(new BoxLayout(buttonHelperPanel, BoxLayout.LINE_AXIS));
        buttonHelperPanel.add(this.computeButton);
        buttonHelperPanel.add(Box.createGlue());
        this.serviceSelectionPanel.add(buttonHelperPanel);
        this.serviceSelectionPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        this.serviceSelectionPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createEmptyBorder(5, 5, 5, 5),
                        "Non-Entailment Explanation Service:"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
    }

    private void computeExplanation(){
        this.logger.debug("Request to compute non entailment explanation");
        SwingUtilities.invokeLater(() -> {
            this.logger.debug("Setting parameters and computing explanation");
            NonEntailmentExplanationService explainer = this.nonEntailmentExplainerManager.getCurrentExplainer();
            explainer.setOntology(this.getOWLModelManager().getActiveOntology());
            explainer.setSignature(this.signatureSelectionUI.getSelectedSignature());
            explainer.setObservation(new HashSet<>(this.selectedObservationListModel.getOwlObjects()));
            this.logger.debug("Resetting viewComponent");
            this.resetView();
            this.logger.debug("Computing explanation");
            explainer.computeExplanation();
        });
    }

    private void showResult(Component resultComponent){
        SwingUtilities.invokeLater(() -> {
            this.resultHolderPanel.removeAll();
            this.resultHolderPanel.add(resultComponent);
            this.repaint();
            this.revalidate();
        });
    }

    public void showError(String message){
        SwingUtilities.invokeLater(() -> {
            JOptionPane errorPane = new JOptionPane(message, JOptionPane.ERROR_MESSAGE);
            JDialog errorDialog = errorPane.createDialog(ProtegeManager.getInstance().getFrame(
                    this.getOWLEditorKit().getWorkspace()), "Error");
            errorDialog.setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);
            errorDialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(
                    ProtegeManager.getInstance().getFrame(this.getOWLEditorKit().getWorkspace())));
            errorDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            errorDialog.setVisible(true);
        });
    }

    private void addObservation(){
        SwingUtilities.invokeLater(() -> {
            try{
                OWLAxiom axiomToAdd = this.observationTextEditor.createObject();
                this.selectedObservationListModel.checkAndAddElement(axiomToAdd);
            }
            catch (OWLException e) {
                this.logger.debug("Exception caught when trying to add observation: " + e);
            }
            finally {
                this.selectedObservationList.clearSelection();
                this.observationTextEditor.setText("");
                this.changeComputeButtonStatus();
            }
        });
    }

    private void deleteObservation(){
        SwingUtilities.invokeLater(() -> {
            List<OWLAxiom> toDelete = this.selectedObservationList.getSelectedValuesList();
            this.selectedObservationListModel.removeElements(toDelete);
            this.selectedObservationList.clearSelection();
            this.observationTextEditor.setText("");
            this.changeComputeButtonStatus();
        });
    }

    private void resetObservation(){
        SwingUtilities.invokeLater(() -> {
            this.selectedObservationListModel.removeAll();
            this.selectedObservationList.clearSelection();
            this.observationTextEditor.setText("");
            this.changeComputeButtonStatus();
        });
    }

    private void loadSignature(){
        this.logger.debug("Loading signature form file");
        JFileChooser fileChooser = this.createFileChooser();
        int result = fileChooser.showOpenDialog(this);
        List<IRI> classes = new ArrayList<>();
        List<IRI> objectProperties = new ArrayList<>();
        List<IRI> individuals = new ArrayList<>();
        List<IRI> currentList = classes;
        if (result == JFileChooser.APPROVE_OPTION){
            File file = fileChooser.getSelectedFile();
            try (FileReader fileReader = new FileReader(file);
                 BufferedReader bufferedReader = new BufferedReader(fileReader)){
                String line;
                while ((line = bufferedReader.readLine()) != null){
                    this.logger.debug("line:" + line);
                    switch (line) {
                        case USE_SIGNATURE_DELIMITER:
                            break;
                        case TRUE:
                            this.logger.debug("Reading UseSignature==TRUE, ignored for nonEntailmentViewComponent");
                            break;
                        case FALSE:
                            this.logger.debug("Reading UseSignature==FALSE, ignored for nonEntailmentViewComponent");
                            break;
                        case CLASSES_DELIMITER:
                            this.logger.debug("loading classes");
                            currentList = classes;
                            break;
                        case OBJECT_PROPERTIES_DELIMITER:
                            this.logger.debug("loading object properties");
                            currentList = objectProperties;
                            break;
                        case INDIVIDUAL_DELIMITER:
                            this.logger.debug("loading individuals");
                            currentList = individuals;
                            break;
                        default:
                            currentList.add(IRI.create(line));
                            break;
                    }
                }
            }
            catch (IOException e){
                this.logger.error("Error when loading signature from file: ", e);
                this.showError("Error: " + e);
            }
        }
        if (classes.size() == 0 && objectProperties.size() == 0 && individuals.size() == 0){
            return;
        }
        Set<OWLEntity> knownEntitySet = new HashSet<>();
        OWLOntology activeOntology = this.getOWLEditorKit().getOWLModelManager().getActiveOntology();
        activeOntology.getClassesInSignature(Imports.INCLUDED).forEach(owlClass -> {
            if (classes.contains(owlClass.getIRI())){
                knownEntitySet.add(owlClass);
            }});
        activeOntology.getObjectPropertiesInSignature(Imports.INCLUDED).forEach(objectProperty -> {
            if (objectProperties.contains(objectProperty.getIRI())){
                knownEntitySet.add(objectProperty);
            }});
        activeOntology.getIndividualsInSignature(Imports.INCLUDED).forEach(individual -> {
            if (individuals.contains(individual.getIRI())){
                knownEntitySet.add(individual);
            }});
//        classes.forEach(iri ->
//                knownEntitySet.addAll(
//                        this.activeOntology.getEntitiesInSignature(
//                                iri)));
//        ontologyEntitySet.removeAll(knownEntitySet);
        this.signatureSelectionUI.setSelectedSignature(knownEntitySet);
        this.signatureSelectionUI.clearSelectedSignatureUISelection();
    }

    private void saveSignature(){
        ArrayList<OWLEntity> classes = new ArrayList<>();
        ArrayList<OWLEntity> objectProperties = new ArrayList<>();
        ArrayList<OWLEntity> individuals = new ArrayList<>();
        this.signatureSelectionUI.getSelectedSignature().forEach(owlEntity -> {
            if (owlEntity.isOWLClass()){
                classes.add(owlEntity);
            }
            else if (owlEntity.isOWLObjectProperty()){
                objectProperties.add(owlEntity);
            }
            else if (owlEntity.isOWLNamedIndividual()){
                individuals.add(owlEntity);
            }
        });
        JFileChooser fileChooser = this.createFileChooser();
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION){
            File file = fileChooser.getSelectedFile();
            if (! FilenameUtils.getExtension(file.getName()).equalsIgnoreCase("txt")) {
                file = new File(file.getParentFile(), FilenameUtils.getBaseName(file.getName()) + ".txt");
            }
            try (FileWriter fileWriter = new FileWriter(file);
                 BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)){
                bufferedWriter.write(USE_SIGNATURE_DELIMITER + "\n");
                bufferedWriter.write(TRUE + "\n"); // written to keep compatibility between proofSignature-File and nonEntailmentSignature-File
                bufferedWriter.write(CLASSES_DELIMITER + "\n");
                for (OWLEntity entity : classes){
                    bufferedWriter.write(entity.getIRI() + "\n");
                }
                bufferedWriter.write(OBJECT_PROPERTIES_DELIMITER + "\n");
                for (OWLEntity entity : objectProperties){
                    bufferedWriter.write(entity.getIRI() + "\n");
                }
                bufferedWriter.write(INDIVIDUAL_DELIMITER + "\n");
                for (OWLEntity entity : individuals){
                    bufferedWriter.write(entity.getIRI() + "\n");
                }
            }
            catch (IOException e){
                this.logger.error("Error when saving signature to file: ", e);
                this.signatureSelectionUI.dispose();
                this.showError("Error: " + e);
            }
        }
        this.signatureSelectionUI.clearSelectedSignatureUISelection();
    }

    private JFileChooser createFileChooser(){
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        FileNameExtensionFilter fileFilter = new FileNameExtensionFilter(
                "txt files (*.txt)", "txt");
        fileChooser.setFileFilter(fileFilter);
        return fileChooser;
    }

    private void loadObservation() {
        this.logger.debug("Loading observation from file");
        JFileChooser fileChooser = this.createFileChooser();
        int result = fileChooser.showOpenDialog(this);
        Set<OWLLogicalAxiom> observationAxioms = new HashSet<>();
        if (result == JFileChooser.APPROVE_OPTION){
            File file = fileChooser.getSelectedFile();
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            try {
                OWLOntology observationOntology = manager.loadOntologyFromOntologyDocument(file);
                OWLOntology activeOntology = this.getOWLEditorKit().getOWLModelManager().getActiveOntology();
                Set<OWLLogicalAxiom> loadedAxioms = observationOntology.getLogicalAxioms();
                observationAxioms = loadedAxioms.stream().filter(
                                axiom -> activeOntology.getSignature().containsAll(
                                        axiom.getSignature())).collect(Collectors.toSet());
            } catch (OWLOntologyCreationException e) {
                this.logger.error("Error when loading observation from file: " + e);
                this.showError(e.getMessage());
            }
        }
        this.selectedObservationListModel.removeAll();
        this.selectedObservationListModel.addElements(observationAxioms);
        this.selectedObservationList.clearSelection();
    }

    private void saveObservation(){
        this.logger.debug("Saving observation to file");
        JFileChooser fileChooser = this.createFileChooser();
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION){
            File file = fileChooser.getSelectedFile();
            if (! FilenameUtils.getExtension(file.getName()).equalsIgnoreCase("txt")) {
                file = new File(file.getParentFile(), FilenameUtils.getBaseName(file.getName()) + ".txt");
            }
            OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
            try {
                String observationOntologyName = this.getOWLEditorKit().getOWLModelManager().getActiveOntology().getOntologyID().getOntologyIRI().toString() + "observationOntology";
                OWLOntology observationOntology = ontologyManager.createOntology(IRI.create(observationOntologyName));
                ontologyManager.addAxioms(observationOntology, new HashSet<>(this.selectedObservationListModel.getOwlObjects()));
                ontologyManager.saveOntology(observationOntology, new RDFXMLDocumentFormat(), new FileOutputStream(file));
            } catch (OWLOntologyCreationException | OWLOntologyStorageException | FileNotFoundException exception) {
                this.logger.error("Error when saving observation ontology to file: " + exception);
                this.showError(exception.getMessage());
            }
        }
        this.selectedObservationList.clearSelection();
    }

    private String reverseParseOWLObject(OWLObject owlObject){
        if (owlObject instanceof OWLClassAssertionAxiom){
            OWLClassAssertionAxiom assertion = ((OWLClassAssertionAxiom) owlObject);
            return assertion.getIndividual() + " Type: " + assertion.getClassExpression();
        }
        else{
            return owlObject.toString();
        }
    }

    protected void changeComputeButtonStatus(){
        boolean enabled = true;
        if (this.selectedObservationListModel.getSize() <= 0 ||
                this.signatureSelectionUI.listModelIsEmpty()){
            enabled = false;
        }
        if (this.nonEntailmentExplainerManager.getCurrentExplainer() == null){
            enabled = false;
        }
        else if (!(this.nonEntailmentExplainerManager.getCurrentExplainer().supportsMultiObservation()) &&
                this.selectedObservationListModel.getSize() != 1){
            enabled = false;
        }
        boolean finalEnabled = enabled;
        SwingUtilities.invokeLater(() -> this.computeButton.setEnabled(finalEnabled));
    }

    private class ViewComponentOntologyChangeListener implements OWLModelManagerListener, OWLOntologyChangeListener {

        @Override
        public void handleChange(OWLModelManagerChangeEvent changeEvent) {
            SwingUtilities.invokeLater(() -> {
                if (changeEvent.isType(EventType.ACTIVE_ONTOLOGY_CHANGED) ||
                        changeEvent.isType(EventType.ONTOLOGY_RELOADED)) {
                    selectedObservationListModel.removeAll();
                    change();
                }
            });
        }

        @Override
        public void ontologiesChanged(@Nonnull List<? extends OWLOntologyChange> list) {
            change();
        }

        private void change(){
            nonEntailmentExplainerManager.getCurrentExplainer().setOntology(getOWLModelManager().getActiveOntology());
            resetView();
            changeComputeButtonStatus();
        }
    }

    private static class OWLLogicalAxiomChecker implements OWLExpressionChecker<OWLAxiom>{

        private final OWLModelManager manager;

        public OWLLogicalAxiomChecker(OWLModelManager manager){
            this.manager = manager;
        }

        @Override
        public void check(String input) throws OWLExpressionParserException {
            if (input.length() != 0){
                this.createObject(input);
            }
        }

        @Override
        public OWLAxiom createObject(String input) throws OWLExpressionParserException {
            ManchesterOWLSyntaxParser parser = new ManchesterOWLSyntaxParserImpl(
                    OWLOntologyLoaderConfiguration::new,
                    this.manager.getOWLDataFactory());
            parser.setOWLEntityChecker(
                    new ProtegeOWLEntityChecker(
                            this.manager.getOWLEntityFinder()));
            parser.setStringToParse(input);
            try {
                OWLAxiom axiom = parser.parseAxiom();
                if(axiom.isLogicalAxiom()) {
                    return axiom;
                }
                else {
                    throw new OWLExpressionParserException(
                            "Expected a logical axiom"
                            , 0, 0, true, true, true, true, true, false, Collections.emptySet());
                }
            }
            catch (ParserException e) {
                throw ParserUtil.convertException(e);
            }
        }
    }

}
