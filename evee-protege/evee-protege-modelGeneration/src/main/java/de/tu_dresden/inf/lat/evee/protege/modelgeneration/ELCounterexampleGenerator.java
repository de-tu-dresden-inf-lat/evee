package de.tu_dresden.inf.lat.evee.protege.modelgeneration;


import de.tu_dresden.inf.lat.evee.general.data.exceptions.ModelGenerationException;
import de.tu_dresden.inf.lat.evee.nonEntailment.interfaces.IOWLModelGenerator;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.ExplanationEvent;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.ExplanationEventType;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class ELCounterexampleGenerator extends AbstractCounterexampleGenerator {

    private final Logger logger = LoggerFactory.getLogger(ELCounterexampleGenerator.class);
    boolean subsumed;
    private OWLClassExpression subClass;
    private OWLClassExpression superClass;
    private int removedAxioms = 0;

    private final OWLDataFactory df;
    private final OWLOntologyManager man;

    public ELCounterexampleGenerator() {
        this.logger.debug("Creating de.tu_dresden.inf.lat.evee.protege.modelgeneration.ELCounterExampleGenerator");
        this.observation = new HashSet<>();
        this.errorMessage = "";
        this.man = OWLManager.createOWLOntologyManager();
        this.df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
        this.logger.debug("de.tu_dresden.inf.lat.evee.protege.modelgeneration.ELCounterExampleGenerator created successfully.");
    }

    public void computeExplanation() {
        try {
            checkObservation(observation.iterator().next());
            workingCopy = getWorkingCopy();
            model = generateModel();

            subsumed = checkSubsumption(model);
            this.viewComponentListener.handleEvent(new ExplanationEvent<>(this,
                    ExplanationEventType.COMPUTATION_COMPLETE));
        } catch (Exception e) {
            this.errorMessage = e.getMessage();
            this.viewComponentListener.handleEvent(new ExplanationEvent<>(this,
                    ExplanationEventType.ERROR));
        }
    }

    private OWLOntology getWorkingCopy() throws Exception {
        OWLNamedIndividual rootInd = df.getOWLNamedIndividual(IRI.create("root-Ind"));
        OWLClass freshClass = df.getOWLClass(IRI.create("FreshClass"));
        OWLClassAssertionAxiom axiom1 = df.getOWLClassAssertionAxiom(subClass, rootInd);
        OWLSubClassOfAxiom axiom2 = df.getOWLSubClassOfAxiom(superClass, freshClass);
        Set<OWLAxiom> TBoxAxioms = activeOntology.getTBoxAxioms(Imports.INCLUDED).stream().collect(Collectors.toSet());
        OWLOntology workingCopy = man.createOntology(TBoxAxioms);
        man.addAxiom(workingCopy, axiom2);
        ELNormaliser normaliser = new ELNormaliser();
        normaliser.setOntology(workingCopy);
        workingCopy = normaliser.normalise();
        man.addAxiom(workingCopy, axiom1);
        this.removedAxioms = normaliser.getNumRemoved();
        return workingCopy;
    }

//    private void checkConsistency(Set<OWLAxiom> model) throws Exception {
//        if (model.isEmpty()) {
//            this.errorMessage = "The Ontology is inconsistent";
//            throw new Exception();
//        }
//    }

    private boolean checkSubsumption(Set<OWLIndividualAxiom> model) {
        return model.contains(df.getOWLClassAssertionAxiom(df.getOWLClass(IRI.create("FreshClass")), df.getOWLNamedIndividual(IRI.create("root-Ind"))));
    }

    void checkObservation(OWLAxiom axiom) throws Exception {
        OWLClassExpression subClass = ((OWLSubClassOfAxiom) axiom).getSubClass();
        OWLClassExpression superClass = ((OWLSubClassOfAxiom) axiom).getSuperClass();
        if (this.isELExpressions(subClass) && this.isELExpressions(superClass)) {
            this.subClass = subClass;
            this.superClass = superClass;
        } else {
//            this.errorMessage = ;
            throw new Exception("ClassExpressions is not in EL");
        }
    }

    private boolean isELExpressions(OWLClassExpression expression) {

        Set<OWLClassExpression> nestedClassExpressions = expression.getNestedClassExpressions();
        for (OWLClassExpression exp : nestedClassExpressions) {
            if (!(exp instanceof OWLObjectSomeValuesFrom
                    || exp instanceof OWLObjectIntersectionOf
                    || exp instanceof OWLClass)) {
                return false;
            }
        }
        return true;
    }

//    public Component getResult() {
//
////        JPanel component = new JPanel();
////        component.setLayout(new BoxLayout(component, BoxLayout.PAGE_AXIS));
////        JTabbedPane tabbedPane = getTabbedPane();
////        JPanel textPanel = getTextPanel();
////        component.add(textPanel);
////        component.add(Box.createRigidArea(new Dimension(20, 20)));
////        component.add(tabbedPane);
//        return component;
//    }

//    private JPanel getTextPanel() {
//        JPanel textPanel = new JPanel();
//        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.PAGE_AXIS));
//
//        if (subsumed) {
//            textPanel.add(new JLabel("Subsumption relation holds. A model is shown below."));
//        } else {
//            textPanel.add(new JLabel("Subsumption relation does not hold. A counterexample is shown below. "));
//        }
//        textPanel.add(new JLabel(removedAxioms + " axioms are not supported. Reasoning results may be incorrect."));
//
//        return textPanel;
//    }

    private Set<OWLIndividualAxiom> filterAxioms(Set<OWLIndividualAxiom> axioms) {
        Set<OWLIndividualAxiom> filtered = axioms.stream()
                .filter(ax -> ax.isOfType(AxiomType.CLASS_ASSERTION))
                .map(ax -> (OWLClassAssertionAxiom) ax)
                .filter(ax -> !(ax.getClassExpression().toString().startsWith("<X")
                        || ax.getClassExpression().toString().equals("<FreshClass>")))
                .collect(Collectors.toSet());
        filtered.addAll(axioms.stream().filter(ax -> ax.isOfType(AxiomType.OBJECT_PROPERTY_ASSERTION)).collect(Collectors.toSet()));
        return filtered;
    }

    @Override
    public Set<OWLIndividualAxiom> generateModel() throws ModelGenerationException {
        IOWLModelGenerator modelGenerator = new ELSmallModelGenerator();
        modelGenerator.setOntology(workingCopy);
        Set<OWLIndividualAxiom> model = modelGenerator.generateModel();
        model = this.filterAxioms(model);
        return model;
    }

    public int getRemovedAxioms() {
        return removedAxioms;
    }
    public boolean getSubsumed() {
        return subsumed;
    }


    @Override
    public Set<IRI> getMarkedIndividuals() {
        return null;
    }
}
