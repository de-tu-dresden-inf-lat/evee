package de.tu_dresden.inf.lat.evee.protege.modelgeneration;

import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.NonEntailmentExplanationService;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.NonEntailmentModelGenerationExplanationService;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.ExplanationEvent;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.ExplanationEventType;
import de.tu_dresden.inf.lat.evee.general.interfaces.ExplanationGenerationListener;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.renderer.OWLCellRenderer;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.stream.Stream;

public class ELModelGenerator implements NonEntailmentModelGenerationExplanationService {


    private ELReasoner elReasoner;


    private OWLOntology activeOntology;
    private Set<OWLAxiom> observation;
    private boolean incorrectNumAx = false;
    private boolean incorrectAxType = false;
    private DefaultTableModel resultModelC = new DefaultTableModel();
    private DefaultTableModel resultModelR = new DefaultTableModel();
    private String columnC[]={"Indivdual","Concept Names"};
    private String columnR[] = { "Subject Individual","Object Property","Object Individual" };
    private int removedAxioms;
    boolean subsumed;
    boolean consistent;
    private OWLEditorKit owlEditorKit;
    private ExplanationGenerationListener<ExplanationEvent<NonEntailmentExplanationService>> viewComponentListener;

    private final Logger logger = LoggerFactory.getLogger(ELModelGenerator.class);




    public ELModelGenerator() {
        this.logger.debug("Creating de.tu_dresden.inf.lat.evee.protege.modelgeneration.ELModelGenerator");
        this.observation = new HashSet<>();
        this.logger.debug("de.tu_dresden.inf.lat.evee.protege.modelgeneration.ELModelGenerator created successfully.");
    }
    public void setup(OWLEditorKit editorKit) {
        this.owlEditorKit = editorKit;
    }

    public void computeExplanation() {
        if(observation.size()!= 1) {
            incorrectNumAx = true;
            return;
        }
        OWLAxiom axiom = observation.iterator().next();
        try {
            OWLClassExpression cl1 = ((OWLSubClassOfAxiom) axiom).getSubClass();
            OWLClassExpression cl2 = ((OWLSubClassOfAxiom) axiom).getSuperClass();
            elReasoner = new ELReasoner();
            elReasoner.setOntology(activeOntology);
            elReasoner.checkSubsumption(cl1, cl2,false);
            resultModelC.setDataVector(elReasoner.getConceptData(),columnC);
            resultModelR.setDataVector(elReasoner.getRoleData(),columnR);
            consistent = elReasoner.getConsistent();
            subsumed = elReasoner.getSubsumed();
            removedAxioms = elReasoner.getNumRemoved();
        } catch (Exception e) {
            incorrectAxType = true;
//            return;
        }
        this.viewComponentListener.handleEvent(new ExplanationEvent<>(this,
                ExplanationEventType.COMPUTATION_COMPLETE));
    }


    public Component getResult() {
        JPanel component = new JPanel();

        if (incorrectAxType) {
            component.add(new JLabel("Incorrect Axiom Type. Please provide an OWLSubClassOfAxiom."));
            return component;
        }
        if (incorrectNumAx) {
            component.add(new JLabel("Incorrect Axiom Number. Please provide a single OWLSubClassOfAxiom."));
            return component;
        }
        JPanel textPanel = new JPanel();
        if(!consistent) {
            textPanel.add(new JLabel("The input ontology is inconsistent.\n"),"North");
        }
        if(subsumed) {
            textPanel.add(new JLabel("Class Expression A is subsumed by Class Expression B. A model is generated. \n"),"Center");
        } else {
            textPanel.add(new JLabel("Class Expression A is not subsumed by Class Expression B. A counterexample is generated. \n"),"Center");
        }
        textPanel.add(new JLabel(removedAxioms +" axioms are not supported. Reasoning results may be incorrect.\n"),"South");
        component.add(textPanel,"North");
        OWLCellRenderer renderer = new OWLCellRenderer(owlEditorKit);
        renderer.setWrap(false);
        JTable resultsC = new JTable(resultModelC) ;
        JTable resultsR = new JTable(resultModelR) ;
        resultsC.setDefaultRenderer(Object.class, renderer);
        resultsR.setDefaultRenderer(Object.class, renderer);
        component.add(new JScrollPane(resultsC),"Center");
        component.add(new JScrollPane(resultsR),"South");
        return component;
    }

    @Override
    public Component getSettingsComponent() {
        return null;
    }

    @Override
    public void registerListener(ExplanationGenerationListener<ExplanationEvent<NonEntailmentExplanationService>> listener) {
        this.viewComponentListener = listener;
    }

    @Override
    public String getErrorMessage() {
        return "";
    }


    public void setObservation(Set<OWLAxiom> owlAxioms) {

        this.observation = owlAxioms;
    }

    @Override
    public void setSignature(Collection<OWLEntity> owlEntities) {

    }


    public void setAbducibles(Collection<OWLEntity> owlEntities) {

    }


    public void setOntology(OWLOntology ontology)  {
        this.activeOntology = ontology;
    }




    public Stream<Set<OWLAxiom>> generateHypotheses() {
        return null;
    }

    @Override
    public boolean supportsMultiObservation() {
        return false;
    }


    public void initialise() throws Exception {

    }


    public void dispose() throws Exception {

    }
}
