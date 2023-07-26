package de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample;


import com.google.common.collect.Sets;
import de.tu_dresden.inf.lat.evee.general.data.exceptions.ModelGenerationException;

import de.tu_dresden.inf.lat.evee.nonEntailment.interfaces.IOWLCounterexampleGenerator;
import org.apache.log4j.Logger;
import org.graphstream.graph.Edge;
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

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;
//import org.graphstream.graph.*;
//import org.graphstream.graph.implementations.*;
//import org.graphstream.ui.swing_viewer.*;
//import org.graphstream.ui.view.Viewer;


public class ModelManager {
    private final ReasonerFactory rf;
    private final OWLOntologyManager man;
    private final OWLEditorKit owlEditorKit;
    private final OWLDataFactory df;
    private final IOWLCounterexampleGenerator counterExampleGenerator;
    private final OWLOntology ont;
    private OWLReasoner res;
    private Set<OWLIndividualAxiom> model;
    private Map<String, List<OWLClass>> classMap;
    private Map<String[],List<OWLObjectProperty>> roleMap;
    private int baseLabelDistance= -20;
    private Set<IRI> markedIndividuals;
    private Viewer viewer;
    private GraphicGraph graph;
    private OWLSubClassOfAxiom observation;
    private int maxLabelNumber;
//    private Set<Sprite> classLabels;
    private Set<OWLObjectPropertyAssertionAxiom> edges;
    private Set<OWLObjectPropertyAssertionAxiom> createdEdges;
    private String styleSheet;
    private final Logger logger = Logger.getLogger(ModelManager.class);

    public ModelManager(Set<OWLIndividualAxiom> model, OWLEditorKit owlEditorKit, IOWLCounterexampleGenerator counterExampleGenerator, OWLOntology ont,Set<OWLAxiom> observation) {
        this.maxLabelNumber = 2;
        this.styleSheet = GraphStyleSheets.PROTEGE;
        this.observation = (OWLSubClassOfAxiom) observation.iterator().next();
        this.ont = ont;
        this.man = OWLManager.createOWLOntologyManager();
        this.counterExampleGenerator = counterExampleGenerator;
        this.markedIndividuals = counterExampleGenerator.getMarkedIndividuals();
        this.model = model;
        this.owlEditorKit = owlEditorKit;
        this.rf = new ReasonerFactory();
        this.res = this.rf.createReasoner(ont);
        this.df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
        this.classMap = this.sortMap(this.createClassMap());
        this.roleMap = this.sortMap(this.createRoleMap());

    }

    public void recomputeModel(Set<OWLAxiom> additionalAxioms) throws Exception {
        this.man.addAxioms(this.ont, additionalAxioms);
        try {
            this.res = this.rf.createReasoner(this.ont);
            this.res.precomputeInferences();
            if(!isConsistent()) {
                throw new ModelGenerationException("Ontology is inconsistent!");
            }
            logger.info("Ontology with the additional axioms is consistent");
            this.model = this.counterExampleGenerator.generateModel();
            this.markedIndividuals = counterExampleGenerator.getMarkedIndividuals();
            this.classMap = this.sortMap(this.createClassMap());
            this.roleMap = this.sortMap(this.createRoleMap());
            createGraph();
            this.viewer = new SwingViewer(this.graph, Viewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
            this.viewer.enableAutoLayout();
            this.man.removeAxioms(this.ont, additionalAxioms);
            logger.info("new graph is created");
        } catch (Exception e) {
            this.man.removeAxioms(this.ont, additionalAxioms);
            throw e;
        }
    }

    public void refreshModel() {

        createGraph();

        this.viewer = new SwingViewer(this.graph, Viewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
        this.viewer.enableAutoLayout();
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

    public Component generateGraphModel() {
        System.setProperty("org.graphstream.ui", "swing");
        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
        createGraph();
        this.viewer = new SwingViewer(this.graph, Viewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
        this.viewer.enableAutoLayout();
        GraphModelComponent component = new GraphModelComponent( this, this.owlEditorKit);
        return component;
    }
    private Map<String[],Set<OWLObjectProperty>> createRoleMap() {
        Map<String[],Set<OWLObjectProperty>> roleMap = new HashMap<>();
        model.stream()
                .filter(ax -> ax.isOfType(AxiomType.OBJECT_PROPERTY_ASSERTION))
                .map(ax -> (OWLObjectPropertyAssertionAxiom) ax)
                .forEach(ax -> roleMap.merge(
                        new String[]{ax.getSubject().asOWLNamedIndividual().getIRI().getShortForm(),
                                ax.getObject().asOWLNamedIndividual().getIRI().getShortForm()},
                        Sets.newHashSet(ax.getProperty().asOWLObjectProperty()),
                        (a, b) -> {
                            a.addAll(b);
                            return a;
                        }));

        return roleMap;

    }
    private Map<String, Set<OWLClass>> createClassMap() {
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

    private <N,M extends OWLNamedObject> Map<N, List<M>> sortMap(Map<N, Set<M>> map) {
        Map<N, List<M>> listMap = new HashMap<>();
        map.entrySet().stream()
                .forEach(e -> listMap.put(e.getKey(),
                        sortList(e.getValue().stream()
                                .collect(Collectors.toList()))));
//        logger.info("class map is sorted: "+listMap.keySet());
        return listMap;
    }

    private <T extends OWLNamedObject> List<T> sortList(List<T> objectList) {
        List<T> finalList = new ArrayList<>();
        List<T> sorted = new ArrayList<>();
        List<T> sortedOut = objectList;
        int i = 0;

        while (!sortedOut.isEmpty() && i < 10) {
            if (sorted.isEmpty()) {
                sorted = sortedOut;
            }
            List<List<T>> sortedAndSortedOut = compareOWLObjects(sorted);
            sorted = sortedAndSortedOut.get(0);
            sortedOut = sortedAndSortedOut.get(1);
            if (sorted.isEmpty()) {
                finalList.addAll(sortedOut);
                break;
            }
            if (sortedOut.isEmpty()) {
                finalList.addAll(sorted);
                break;
            }
            finalList.addAll(sortedOut);
            i = i + 1;
        }
        Collections.reverse(finalList);
        return finalList;
    }

    private<M extends OWLNamedObject> OWLAxiom getInclusion(M a, M b) {
        if (!a.getObjectPropertiesInSignature().isEmpty()) {
            return df.getOWLSubObjectPropertyOfAxiom((OWLObjectProperty) a, (OWLObjectProperty) b);
        } else {
            return df.getOWLSubClassOfAxiom((OWLClass) a, (OWLClass) b);
        }
    }

    private <T extends OWLNamedObject> List<List<T>> compareOWLObjects(List<T> objectList) {
        Set<T> subsumed = new HashSet<>();
        Set<T> subsumers = new HashSet<>();
        List<List<T>> returnList = new ArrayList<>();

        objectList.stream().forEach(expr1 -> objectList.stream().filter(expr2 -> !expr1.equals(expr2))
                .filter(expr2 -> res.isEntailed(getInclusion(expr1, expr2))
                        && !res.isEntailed(getInclusion(expr2, expr1 )))
                .forEach(expr2 -> {
                    subsumed.add(expr1);
                    subsumers.add(expr2);
                }));

        List<T> moreExact = subsumed.stream().collect(Collectors.toList());
        objectList.removeAll(subsumers);
        objectList.removeAll(subsumed);
        moreExact.addAll(objectList);
        returnList.add(moreExact);
        subsumers.removeAll(subsumed);
        returnList.add(subsumers.stream().collect(Collectors.toList()));
            logger.debug(returnList.get(0));
        logger.debug(returnList.get(1));
        return returnList;
    }

    private void createEdges() {
        roleMap.entrySet().stream()
                .forEach(e -> {
                    String subj = e.getKey()[0];
                    String obj = e.getKey()[1];
                    boolean forward = true;
                    if(graph.edges()
                            .anyMatch(edge -> (edge.getNode0().getId().equals(obj))
                                    && edge.getNode1().getId().equals(subj))) {
                        forward = false;
                    }
                    for (int i =0;i<e.getValue().size();i++) {
                        String prop = e.getValue().get(i).getIRI().getShortForm();
                        createEdge(subj,prop,obj,i,forward);
                    }
                });
    }

    private void createEdge(String subj,
                            String prop,
                            String obj,
                            int edgeNum,
                            boolean forwardEdge) {
        int labelDistance = (edgeNum+1) * baseLabelDistance;
        String textColor= GraphStyleSheets.PROTEGE_BLUE_1;
        String fillColor= GraphStyleSheets.PROTEGE_BLUE_1;
        String alignment="along;";
        if (!forwardEdge) {
            textColor= GraphStyleSheets.PROTEGE_BLUE;
            fillColor= GraphStyleSheets.PROTEGE_BLUE;
            labelDistance = labelDistance * -1;
        }
        if (subj.equals(obj)) {
            alignment = "center;";
        }
        if (edgeNum>0) {
            fillColor= "rgba(0,0,0,0);";
        }

        Edge edge = graph.addEdge(subj + obj+ prop, subj, obj, true);
        edge.setAttribute("ui.label", prop);
        edge.setAttribute("ui.style","text-color: "+textColor+
                "text-offset: 0, "+labelDistance+";"+
                "fill-color: "+fillColor+
                "text-alignment: "+alignment);
    }

    private void createNodes() {
        logger.debug("root inds:"+this.markedIndividuals);
        classMap.keySet().stream().forEach(k -> {
            GraphicNode n = (GraphicNode) graph.addNode(k);
            for(IRI iri:this.markedIndividuals) {
                if(iri.getShortForm().equalsIgnoreCase(n.getId())) {
                    n.setAttribute("ui.class", "root");
                }
            }
        });
    }

    private void createSpriteLabel(String nodeID,OWLClass cls,int labelDistance,SpriteManager sMan) {
        String currOWLClass = "...";
        if (!Objects.equals(cls,null)) {

            for(OWLAnnotation a : EntitySearcher.getAnnotations(cls, ont, df.getRDFSLabel())) {
                OWLAnnotationValue val = a.getValue();
                if(val instanceof OWLLiteral) {
                    if(((OWLLiteral) val).hasLang("en")) {
                        currOWLClass = ((OWLLiteral) val).getLiteral();
                    }
                }
            }
            if (currOWLClass.equals("...")) {
                currOWLClass = cls.getIRI().getShortForm();
            }
        }
        Sprite sprite = sMan.addSprite(nodeID + currOWLClass);
        sprite.attachToNode(nodeID);
        sprite.setPosition(0, 0, 0);
        sprite.setAttribute("ui.label", currOWLClass);
        sprite.setAttribute("ui.style","text-offset: 0, "+labelDistance+";");
    }
    private void createNodeLabels() {
        SpriteManager sMan = new SpriteManager(graph);
        classMap.entrySet().stream().forEach(e ->
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
        graph.setAttribute("ui.quality");
        graph.setAttribute("ui.antialias");
        graph.setAttribute("ui.stylesheet",styleSheet);
    }

    private void createGraph() {
        graph = new GraphicGraph("model");
        createStyleSheet();
        createNodes();
        createEdges();
        createNodeLabels();
        logger.debug(graph.getAttribute("ui.stylesheet"));
    }
    public void setStyleSheet(String styleSheet) {
        this.styleSheet = styleSheet;
    }
    public void setMaxLabelNumber(int maxLabelNumber) {
        this.maxLabelNumber = maxLabelNumber;
    }
    protected OWLOntology getOnt() {
        return this.ont;
    }
    public GraphicGraph getGraph() {
        return this.graph;
    }
    public Map<String, List<OWLClass>> getClassMap() {
        return this.classMap;
    }
    public Viewer getViewer() {
        return this.viewer;
    }
}
