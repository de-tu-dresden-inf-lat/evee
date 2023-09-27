package de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample;


import com.google.common.collect.Sets;
import de.tu_dresden.inf.lat.evee.general.data.exceptions.ModelGenerationException;

import de.tu_dresden.inf.lat.evee.general.interfaces.IIsCancellable;
import de.tu_dresden.inf.lat.evee.nonEntailment.interfaces.IOWLCounterexampleGenerator;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.core.NonEntailmentExplanationProgressTracker;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample.ui.GraphModelComponent;
import org.apache.log4j.Logger;
import org.graphstream.ui.graphicGraph.GraphicEdge;
import org.graphstream.ui.graphicGraph.GraphicGraph;
import org.graphstream.ui.graphicGraph.GraphicNode;
import org.graphstream.ui.spriteManager.Sprite;
import org.graphstream.ui.spriteManager.SpriteManager;
import org.graphstream.ui.swing_viewer.SwingViewer;
import org.graphstream.ui.view.Viewer;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.search.EntitySearcher;

import javax.swing.*;
import java.util.List;
import java.util.*;
//import org.graphstream.graph.*;
//import org.graphstream.graph.implementations.*;
//import org.graphstream.ui.swing_viewer.*;
//import org.graphstream.ui.view.Viewer;


public class ModelManager implements IIsCancellable {

    private final String EDGE_LABEL_GROUPING = "line";
    private final ReasonerFactory rf = new ReasonerFactory();
    private final OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();;
    private final OWLOntologyManager man = OWLManager.createOWLOntologyManager();
    private final OWLEditorKit owlEditorKit;
    private final IOWLCounterexampleGenerator counterExampleGenerator;
    private final OWLOntology ont;
    private OWLReasoner res;
    private Set<OWLIndividualAxiom> model;



    private Map<String, List<OWLClass>> individualClassMap;
    private Map<String[],List<OWLObjectProperty>> objectPropertyMap;
    private int baseLabelDistance= -20;
    private int maxLabelNumber;
    private Set<IRI> markedIndividuals;
    private Viewer graphModelViewer;
    private GraphicGraph graphModel;
    private OWLSubClassOfAxiom observation;
    private String styleSheet;
    private final Logger logger = Logger.getLogger(ModelManager.class);
    private GraphModelComponent graphModelComponent;


    public ModelManager(Set<OWLIndividualAxiom> model,
                        OWLEditorKit owlEditorKit,
                        IOWLCounterexampleGenerator counterExampleGenerator,
                        OWLOntology ont,Set<OWLAxiom> observation) {
        this.maxLabelNumber = 2;
        this.styleSheet = GraphStyleSheets.PROTEGE;
        this.observation = (OWLSubClassOfAxiom) observation.iterator().next();
        this.ont = ont;
        this.counterExampleGenerator = counterExampleGenerator;
        this.model = model;
        this.owlEditorKit = owlEditorKit;
        this.res = this.rf.createReasoner(ont);
        this.markedIndividuals = counterExampleGenerator.getMarkedIndividuals();
        System.setProperty("org.graphstream.ui", "swing");
        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
    }



    public void refreshModelViewer(Set<OWLAxiom> additionalAxioms, NonEntailmentExplanationProgressTracker progressTracker) throws Exception {
        man.addAxioms(ont, additionalAxioms);
        try {
            progressTracker.setMax(4);
            res = rf.createReasoner(ont);
            res.precomputeInferences();
            if(!isConsistent()) {
                throw new ModelGenerationException("Ontology is inconsistent!");
            }
            logger.info("Ontology with the additional axioms is consistent");
            counterExampleGenerator.addProgressTracker(progressTracker);
            model = counterExampleGenerator.generateModel();
            markedIndividuals = counterExampleGenerator.getMarkedIndividuals();
            man.removeAxioms(ont, additionalAxioms);
            refreshGraphModelViewer();
            logger.info("new graph is created");
        } catch (Exception e) {
            man.removeAxioms(ont, additionalAxioms);
            throw  e;
        }
    }

    public void refreshGraphModelViewer() {
        refreshGraphModel();
        this.graphModelViewer = new SwingViewer(this.graphModel,
                Viewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
        this.graphModelViewer.enableAutoLayout();
        logger.info("graph model viewer is refreshed");
    }

    public void refreshGraphModelComponent() {
        refreshGraphModelViewer();
        graphModelComponent = new GraphModelComponent( this, this.owlEditorKit);
        logger.info("graph model component is refreshed");
    }


    private void refreshModelMaps() {
        OWLObjectSorter objectSorter = new OWLObjectSorter(res,df);
        this.individualClassMap = objectSorter.sortOWLObjectMap(this.createIndividualClassMap());
        this.objectPropertyMap = objectSorter.sortOWLObjectMap(this.createObjectPropertyMap());
        logger.info("model maps are refreshed");
    }
    private void refreshGraphModel() {
        refreshModelMaps();
        graphModel = new GraphicGraph("model");
        createStyleSheet();
        createNodes();
        createEdges();
        createNodeLabels();
        logger.info("graph model is refreshed");
    }

    private Map<String[],Set<OWLObjectProperty>> createObjectPropertyMap() {
        Map<String[],Set<OWLObjectProperty>> objectPropertyMap = new HashMap<>();
        model.stream()
                .filter(ax -> ax.isOfType(AxiomType.OBJECT_PROPERTY_ASSERTION))
                .map(ax -> (OWLObjectPropertyAssertionAxiom) ax)
                .forEach(ax -> objectPropertyMap.merge(
                        new String[]{ax.getSubject().asOWLNamedIndividual().getIRI().getShortForm(),
                                ax.getObject().asOWLNamedIndividual().getIRI().getShortForm()},
                        Sets.newHashSet(ax.getProperty().asOWLObjectProperty()),
                        (a, b) -> {
                            a.addAll(b);
                            return a;
                        }));
        return objectPropertyMap;

    }
    private Map<String, Set<OWLClass>> createIndividualClassMap() {
        Map<String, Set<OWLClass>> classMap = new HashMap<>();
        model.stream().forEach(a ->
                a.getIndividualsInSignature().forEach(i ->
                        classMap.put(i.asOWLNamedIndividual().getIRI().getShortForm(),Sets.newHashSet())));
        model.stream()
                .filter(ax -> ax.isOfType(AxiomType.CLASS_ASSERTION))
                .map(ax -> (OWLClassAssertionAxiom) ax)
                .forEach(ax -> classMap.merge(
                        ax.getIndividual().asOWLNamedIndividual().getIRI().getShortForm(),
                        Sets.newHashSet(ax.getClassExpression().asOWLClass()),
                        (a, b) -> {
                            a.addAll(b);
                            return a;
                        }));



        return classMap;
    }



    private void createEdges() {

        objectPropertyMap.entrySet().stream()
                .forEach(e -> {
                    String subj = e.getKey()[0];
                    String obj = e.getKey()[1];
                    boolean forward = true;
                    if(graphModel.edges()
                            .anyMatch(edge -> (edge.getNode0().getId().equals(obj))
                                    && edge.getNode1().getId().equals(subj))) {
                        forward = false;
                    }
                    if(EDGE_LABEL_GROUPING == "column") {
                        for (int i =0;i<e.getValue().size();i++) {
                            String prop = owlEntityToLabel(e.getValue().get(i));
                            createEdge(subj,prop,obj,i);
                        }
                    } else {
                        String prop = owlEntityToLabel(e.getValue().get(0));
                        if (e.getValue().size()>1) {
                            for (int i =1;i<e.getValue().size();i++) {
                                prop = prop + ", " + owlEntityToLabel(e.getValue().get(i));
                            }
                        }
                        createEdge(subj,prop,obj,0);
                    }
                });
    }

    private void createEdge(String subj,
                            String prop,
                            String obj,
                            int edgeNum) {
        int labelDistance = (edgeNum+1) * baseLabelDistance;
        String textColor= GraphStyleSheets.PROTEGE_BLUE;
        String fillColor= GraphStyleSheets.PROTEGE_BLUE_1;
        String alignment="along;";

        if (subj.equals(obj)) {
            alignment = "center;";
        }
        if (edgeNum>0) {
            fillColor= "rgba(0,0,0,0);";
        }

        GraphicEdge edge = (GraphicEdge) graphModel.addEdge("edge"+subj + obj+edgeNum, subj, obj, true);
        edge.setAttribute("ui.label", prop);
//        edge.setAttribute("properties",prop);
        edge.setAttribute("ui.style","text-color: "+textColor+
                "text-offset: 0, "+labelDistance+";"+
                "fill-color: "+fillColor+
                "text-alignment: "+alignment);
//        logger.info("prop of node"+edge.getAttribute("properties").toString());
    }

    private void createNodes() {
        logger.debug("root inds:"+this.markedIndividuals);
        individualClassMap.keySet().stream().forEach(k -> {
            GraphicNode n = (GraphicNode) graphModel.addNode(k);

            for(IRI iri:this.markedIndividuals) {
                if(iri.getShortForm().equalsIgnoreCase(n.getId())) {
                    n.setAttribute("ui.class", "root");
                }
            }
        });
    }

    private String owlEntityToLabel(OWLEntity obj) {

        for(OWLAnnotation a : EntitySearcher.getAnnotations(obj, ont, df.getRDFSLabel())) {
            OWLAnnotationValue val = a.getValue();
            if(val instanceof OWLLiteral) {
                if(((OWLLiteral) val).hasLang("en")) {
                    return ((OWLLiteral) val).getLiteral();
                }
            }
        }
        return obj.getIRI().getShortForm();
    }

    private void createSpriteLabel(String nodeID,
                                   OWLClass cls,
                                   int labelDistance,
                                   SpriteManager sMan) {
        String label = "...";
        String identifier = "unroll";
        if (!Objects.equals(cls,null)) {
            label = owlEntityToLabel(cls);
            identifier = label;
        }

        Sprite sprite = sMan.addSprite(nodeID + identifier);
        sprite.attachToNode(nodeID);
        sprite.setPosition(0, 0, 0);
        sprite.setAttribute("ui.label", label);
        sprite.setAttribute("ui.style","text-offset: 0, "+labelDistance+";");
    }
    private void createNodeLabels() {
        SpriteManager sMan = new SpriteManager(graphModel);
        individualClassMap.entrySet().stream().forEach(e ->
                {
                    int classNummer = e.getValue().size();
                    if (classNummer > maxLabelNumber) {
                        classNummer = maxLabelNumber;
                    }
                    String nodeID = e.getKey();
                    Iterator<OWLClass> owlclassIterator = e.getValue().iterator();
                    for (int i = 0; i < classNummer; i++) {
                        int labelDistance = -(i+1) * baseLabelDistance;
                        OWLClass cls = owlclassIterator.next();
                        createSpriteLabel(nodeID,cls,labelDistance,sMan);
                    }
                    if (owlclassIterator.hasNext()) {
                        int labelDistance = -(classNummer+1) * baseLabelDistance;
                        createSpriteLabel(nodeID,null,labelDistance,sMan);
                    }
                }
        );
    }

    private void createStyleSheet() {
        graphModel.setAttribute("ui.quality");
        graphModel.setAttribute("ui.antialias");
        graphModel.setAttribute("ui.stylesheet",styleSheet);
    }

    private boolean isConsistent() {
        IRI newIndIRI = IRI.create("newInd");
        OWLNamedIndividual newInd = this.df.getOWLNamedIndividual(newIndIRI);
        OWLClassAssertionAxiom newAx = df.getOWLClassAssertionAxiom(observation.getSubClass(),newInd);
        this.man.addAxiom(this.ont,newAx);
        OWLReasoner reasoner = rf.createReasoner(this.ont);
        boolean isConsistent = reasoner.isConsistent();
        this.man.removeAxiom(this.ont,newAx);
        return isConsistent;
    }
    public GraphModelComponent getGraphModelComponent() {
        return graphModelComponent;
    }
    public void setStyleSheet(String styleSheet) {
        this.styleSheet = styleSheet;
    }
    public void setMaxLabelNumber(int maxLabelNumber) {
        this.maxLabelNumber = maxLabelNumber;
    }
    public OWLOntology getOnt() {
        return this.ont;
    }
    public GraphicGraph getGraphModel() {
        return this.graphModel;
    }
    public Map<String, List<OWLClass>> getIndividualClassMap() {
        return this.individualClassMap;
    }
    public Viewer getGraphModelViewer() {
        return this.graphModelViewer;
    }
    public Map<String[], List<OWLObjectProperty>> getObjectPropertyMap() {
        return objectPropertyMap;
    }

    @Override
    public boolean successful() {
        return false;
    }



}
