package de.tu_dresden.inf.lat.evee.protege.nonEntailment.abduction;

import de.tu_dresden.inf.lat.evee.nonEntailment.interfaces.IOWLAbductionSolver;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.INonEntailmentExplanationService;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.ExplanationEvent;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.ExplanationEventType;
import de.tu_dresden.inf.lat.evee.general.interfaces.IExplanationGenerationListener;
import de.tu_dresden.inf.lat.evee.protege.tools.ui.OWLObjectListModel;
import de.tu_dresden.inf.lat.evee.protege.tools.ui.Util;
import org.protege.editor.core.ProtegeManager;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.renderer.OWLCellRenderer;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

abstract public class AbstractAbductionSolver<Result> implements Supplier<Set<OWLAxiom>>,
        INonEntailmentExplanationService<OWLAxiom>, IOWLAbductionSolver {

    protected Set<OWLAxiom> observation = null;
    protected Set<OWLAxiom> lastUsedObservation = null;
    protected Set<OWLEntity> abducibles = null;
    protected Set<OWLEntity> lastUsedAbducibles = null;
    protected OWLOntology ontology = null;
    protected OWLOntology lastUsedOntology = null;
    protected AbductionLoadingUI loadingUI;
    protected JPanel settingsHolderPanel;
    private JSpinner abductionNumberSpinner;
    private OWLEditorKit owlEditorKit;
    private JPanel resultHolderPanel;
    private JPanel resultScrollingPanel;
    protected int hypothesisIndex;
    private boolean computationSuccessful;
    private boolean cacheValidated;
    private final AbductionSolverOntologyChangeListener ontologyChangeListener;
    protected IExplanationGenerationListener<ExplanationEvent<INonEntailmentExplanationService<?>>> viewComponentListener;
//    protected boolean parametersChanged = true;
    protected final Map<OWLOntology, AbductionCache<Result>> cachedResults;
    protected static final String SETTINGS_LABEL = "Maximal number of hypotheses:";
    protected static final String SETTINGS_SPINNER_TOOLTIP = "Number of hypotheses to be generated in each computation step";
    protected static final String ADD_TO_ONTO_COMMAND = "ADD_TO_ONTO";
    protected static final String ADD_TO_ONTO_NAME = "Add to Ontology";
    protected static final String ADD_TO_ONTO_TOOLTIP = "Adds the axioms of this result to the current active ontology";

    private final Logger logger = LoggerFactory.getLogger(AbstractAbductionSolver.class);

    public AbstractAbductionSolver(){
        this.logger.debug("Creating AbstractAbductionSolver");
        this.hypothesisIndex = 1;
        this.cachedResults = new HashMap<>();
        this.ontologyChangeListener = new AbductionSolverOntologyChangeListener();
        this.createSettingsComponent();
        this.logger.debug("AbstractAbductionSolver created successfully.");
    }

    public void setComputationSuccessful(boolean successful){
        this.computationSuccessful = successful;
    }

    public boolean computationSuccessful(){
        return this.computationSuccessful;
    }

    @Override
    public void initialise(){
        this.logger.debug("Initialising AbductionSolver");
        this.owlEditorKit.getOWLModelManager().addOntologyChangeListener(this.ontologyChangeListener);
        this.resetCache();
        this.logger.debug("AbductionSolver initialised");
    }

    @Override
    public void dispose() {
        this.logger.debug("Disposing AbductionSolver");
        this.owlEditorKit.getOWLModelManager().removeOntologyChangeListener(this.ontologyChangeListener);
        this.logger.debug("AbductionSolver disposed");
    }

    @Override
    public void setObservation(Set<OWLAxiom> observation) {
        this.logger.debug("Setting observation");
        if (this.observation == null){
            this.observation = observation;
        }
        else if (! this.observation.equals(observation)){
            this.observation = observation;
        }
    }

    @Override
    public void setSignature(Collection<OWLEntity> abducibles) {
        this.logger.debug("Setting signature");
        HashSet<OWLEntity> abduciblesAsSet = new HashSet<>(abducibles);
        if (this.abducibles == null){
            this.abducibles = abduciblesAsSet;
        }
        else if (! this.abducibles.equals(abduciblesAsSet)){
            this.abducibles = abduciblesAsSet;
        }
    }

    @Override
    public void setOntology(OWLOntology ontology) {
        this.logger.debug("Setting ontology");
        if (this.ontology == null){
            this.ontology = ontology;
        }
        else if (! this.ontology.equals(ontology)){
            this.ontology = ontology;
        }
        if (this.cachedResults.get(ontology) == null){
            this.cachedResults.put(ontology, new AbductionCache<>());
            this.cacheValidated = false;
        }
    }

    @Override
    public Stream<Set<OWLAxiom>> generateExplanations() {
        return Stream.generate(this);
    }

    @Override
    public void setup(OWLEditorKit editorKit) {
        this.owlEditorKit = editorKit;
    }

    @Override
    public Component getResult() {
        return this.resultHolderPanel;
    }

    @Override
    public Component getSettingsComponent() {
        return this.settingsHolderPanel;
    }

    @Override
    public void registerListener(IExplanationGenerationListener<ExplanationEvent<INonEntailmentExplanationService<?>>> listener) {
        this.viewComponentListener = listener;
    }

    @Override
    public void computeExplanation() {
        this.logger.debug("Computing explanation");
        assert (this.ontology != null);
        assert (this.observation != null);
        assert (this.abducibles != null);
        if (this.parametersChanged()){
            this.lastUsedObservation = this.observation;
            this.lastUsedAbducibles = this.abducibles;
            this.lastUsedOntology = this.ontology;
            this.logger.debug("Parameters changed, creating new stream");
            if (this.cacheValidated &&
                    this.cachedResults.get(this.ontology).containsResultFor(
                            this.observation, this.abducibles)){
                this.logger.debug("Cached result found, no computation of hypotheses necessary");
                this.computationSuccessful = true;
                this.redisplayCachedExplanation();
            }
            else{
                this.logger.debug("No cached result found, computing new hypotheses");
                this.createNewExplanation();
            }
            this.hypothesisIndex = 1;
        }
        else{
            this.logger.debug("Parameters unchanged");
            if (this.computationSuccessful){
                this.logger.debug("Last computation was successful, continuing old stream");
                this.createResultComponent();
            }
            else{
                this.logger.debug("Last computation failed, re-displaying error message");
                this.viewComponentListener.handleEvent(new ExplanationEvent<>(this,
                        ExplanationEventType.ERROR));
            }
        }
    }

    abstract protected void redisplayCachedExplanation();

    abstract protected void createNewExplanation();

    abstract protected void prepareResultComponentCreation();

    private void createSettingsComponent(){
        SwingUtilities.invokeLater(() -> {
            this.settingsHolderPanel = new JPanel();
            this.settingsHolderPanel.setLayout(new BoxLayout(settingsHolderPanel, BoxLayout.PAGE_AXIS));
            JPanel spinnerHelperPanel = new JPanel();
            spinnerHelperPanel.setLayout(new BoxLayout(spinnerHelperPanel, BoxLayout.LINE_AXIS));
            JLabel label = Util.createLabel(SETTINGS_LABEL);
            spinnerHelperPanel.add(label);
            spinnerHelperPanel.add(Box.createRigidArea(new Dimension(5, 0)));
            SpinnerNumberModel spinnerModel = new SpinnerNumberModel(10, 1, null, 1);
            this.abductionNumberSpinner = new JSpinner(spinnerModel);
            this.abductionNumberSpinner.setToolTipText(SETTINGS_SPINNER_TOOLTIP);
            this.abductionNumberSpinner.setMaximumSize(new Dimension(500, this.abductionNumberSpinner.getPreferredSize().height));
            spinnerHelperPanel.add(this.abductionNumberSpinner);
            spinnerHelperPanel.add(Box.createGlue());
            this.settingsHolderPanel.add(spinnerHelperPanel);
//        this.settingsHolderPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            this.settingsHolderPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder(
                            BorderFactory.createEmptyBorder(), "Settings:"),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        });
    }

    protected void resetResultComponent(){
        SwingUtilities.invokeLater(() -> {
            this.resultHolderPanel = new JPanel(new BorderLayout());
            this.resultScrollingPanel = new JPanel();
            this.resultScrollingPanel.setLayout(new BoxLayout(this.resultScrollingPanel, BoxLayout.PAGE_AXIS));
            JScrollPane resultScrollPane = new JScrollPane();
            resultScrollPane.setViewportView(this.resultScrollingPanel);
            this.resultHolderPanel.add(resultScrollPane);
            this.resultHolderPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder(
                            BorderFactory.createEmptyBorder(5, 5, 5, 5),
                            "Hypotheses:"),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        });
    }

    /**
     * Compare two results based on the string representation. Shorter results should come first.
     */
    private Comparator<Set<OWLAxiom>> resultComparator = (result1, result2) -> {
        if(result1.size()!=result2.size())
            return result1.size()-result2.size();
        else {
            String r1 = result1.stream().map(Object::toString).reduce("", (a,b) -> a+b);
            String r2 = result2.stream().map(Object::toString).reduce("", (a,b) -> a+b);
            if(r1.length()!=r2.length())
                return r1.length()-r2.length();
            else
                return r1.compareTo(r2);
        }
    };

    protected void createResultComponent(){
        SwingUtilities.invokeLater(() -> {
            int resultNumber = (int) this.abductionNumberSpinner.getValue();
            this.logger.debug("Trying to show {} results of abduction generation process", resultNumber);
            List<Set<OWLAxiom>> hypotheses = new ArrayList<>();
            Stream<Set<OWLAxiom>> resultStream = this.generateExplanations();
            resultStream.limit(resultNumber).forEach(result -> {
                if (result != null){
                    hypotheses.add(result);}
            });
            this.logger.debug("Actually showing {} results of abduction generation process", hypotheses.size());
            hypotheses.stream().sorted(resultComparator).forEach(result -> {
                JPanel singleResultPanel = new JPanel();
                singleResultPanel.setLayout(new BorderLayout());
                singleResultPanel.setBorder(new CompoundBorder(
                        new EmptyBorder(5, 5, 5, 5),
                        new CompoundBorder(new LineBorder(Color.BLACK, 1),
                                new EmptyBorder(5, 5, 5, 5))));
                JPanel labelAndButtonPanel = new JPanel();
                labelAndButtonPanel.setLayout(new BoxLayout(labelAndButtonPanel, BoxLayout.LINE_AXIS));
                JLabel label = new JLabel("Hypothesis " + this.hypothesisIndex);
                OWLObjectListModel<OWLAxiom> resultListModel = new OWLObjectListModel<>();
                resultListModel.addElements(result);
                JList<OWLAxiom> resultList = new JList<>(resultListModel);
                labelAndButtonPanel.add(label);
                labelAndButtonPanel.add(Box.createHorizontalGlue());
                JButton addToOntologyButton = new JButton(ADD_TO_ONTO_NAME);
                addToOntologyButton.setToolTipText(ADD_TO_ONTO_TOOLTIP);
                addToOntologyButton.setActionCommand(ADD_TO_ONTO_COMMAND);
                addToOntologyButton.addActionListener(new AddToOntologyButtonListener(
                        this.ontology, resultList, this.hypothesisIndex));
                this.hypothesisIndex++;
                labelAndButtonPanel.add(addToOntologyButton);
                singleResultPanel.add(labelAndButtonPanel, BorderLayout.PAGE_START);
                OWLCellRenderer renderer = new OWLCellRenderer(this.owlEditorKit);
                renderer.setHighlightUnsatisfiableClasses(false);
                renderer.setHighlightUnsatisfiableProperties(false);
                renderer.setHighlightKeywords(true);
                renderer.setFeint(true);
                resultList.setCellRenderer(renderer);
                JScrollPane singleResultScrollPane = new JScrollPane(resultList);
                singleResultScrollPane.setPreferredSize(resultList.getPreferredSize());
                singleResultPanel.add(singleResultScrollPane, BorderLayout.CENTER);
                this.resultScrollingPanel.add(singleResultPanel);
            });
            this.resultHolderPanel.repaint();
            this.resultHolderPanel.revalidate();
//            event-handling needs to happen within invokeLater-Block
            this.viewComponentListener.handleEvent(new ExplanationEvent<>(this,
                    ExplanationEventType.COMPUTATION_COMPLETE));
        });
    }

//    todo: move loadingUI to Lethe- and Capi-Solver (not every abduction solver might need a loading screen)
    public void disposeLoadingScreen(){
        if (this.loadingUI != null) {
            this.loadingUI.disposeLoadingScreen();
        }
    }

    protected boolean parametersChanged(){
        return (! this.cacheValidated) ||
                (! this.abducibles.equals(this.lastUsedAbducibles)) ||
                (! this.observation.equals(this.lastUsedObservation)) ||
                (! this.ontology.equals(this.lastUsedOntology));
    }

    protected void validateCache(){
        this.cacheValidated = true;
    }

//    public void showError(){
//        SwingUtilities.invokeLater(() -> {
//            String message = this.getErrorMessage();
//            JOptionPane errorPane = new JOptionPane(message, JOptionPane.ERROR_MESSAGE);
//            JDialog errorDialog = errorPane.createDialog(ProtegeManager.getInstance().getFrame(
//                    this.owlEditorKit.getWorkspace()), "Error");
//            errorDialog.setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);
//            errorDialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(
//                    ProtegeManager.getInstance().getFrame(this.owlEditorKit.getWorkspace())));
//            errorDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
//            errorDialog.setVisible(true);
//        });
//    }

    private class AddToOntologyButtonListener implements ActionListener {

        private final OWLOntology ontology;
        private final JList<OWLAxiom> axiomList;
        private final int hypothesisIndex;
        private final Logger logger = LoggerFactory.getLogger(AddToOntologyButtonListener.class);

        private AddToOntologyButtonListener(OWLOntology ontology, JList<OWLAxiom> axiomList, int index){
            this.ontology = ontology;
            this.axiomList = axiomList;
            this.hypothesisIndex = index;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            this.logger.debug("Button AddToOntology clicked");
            if (e.getActionCommand().equals(ADD_TO_ONTO_COMMAND)){
                this.logger.debug("Adding new Axioms to Ontology:");
                SwingUtilities.invokeLater(() -> {
                    Set<OWLAxiom> selectedAxioms = new HashSet<>(axiomList.getSelectedValuesList());
                    String msgString = "";
                    if (selectedAxioms.size() == 0){
                        for (int index = 0; index < axiomList.getModel().getSize(); index++){
                            selectedAxioms.add(axiomList.getModel().getElementAt(index));
                        }
                        msgString += "All axioms ";
                    }
                    else {
                        if (selectedAxioms.size() == 1){
                            msgString += "Selected axiom ";
                        }
                        else{
                            msgString += "Selected " + selectedAxioms.size() + " axioms ";
                        }
                    }
                    selectedAxioms.forEach(axiom -> {
                        this.logger.debug(axiom.toString());
                        this.ontology.getOWLOntologyManager().addAxiom(this.ontology, axiom);
                    });
                    msgString += "of hypothesis " + hypothesisIndex + " added to ontology.";
                    JOptionPane msgPane = new JOptionPane(msgString, JOptionPane.INFORMATION_MESSAGE);
                    JDialog msgDialog = msgPane.createDialog(ProtegeManager.getInstance().getFrame(
                            owlEditorKit.getWorkspace()), "Added to ontology");
                    msgDialog.setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);
                    msgDialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(
                            ProtegeManager.getInstance().getFrame(owlEditorKit.getWorkspace())));
                    msgDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                    msgDialog.setVisible(true);
                });
            }
        }

    }

    protected void resetCache(){
        OWLOntology ontology = this.owlEditorKit.getOWLModelManager().getActiveOntology();
        this.logger.debug("Resetting AbductionCache for ontology " + ontology.getOntologyID().getOntologyIRI()
                .or(IRI.create("")));
        AbductionCache<Result> newCache = new AbductionCache<>();
        this.cachedResults.put(ontology, newCache);
        this.cacheValidated = false;
    }

    protected void resetCompleteCache(){
        this.logger.debug("Resetting complete cache");
        this.cachedResults.clear();
        this.resetCache();
    }

    private class AbductionSolverOntologyChangeListener implements OWLOntologyChangeListener {

        @Override
        public void ontologiesChanged(@Nonnull List<? extends OWLOntologyChange> list) {
            resetCache();
        }
    }

}
