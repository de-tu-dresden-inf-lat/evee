package de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample;


import com.google.common.collect.Sets;
import de.tu_dresden.inf.lat.evee.general.data.exceptions.ModelGenerationException;

import de.tu_dresden.inf.lat.evee.nonEntailment.interfaces.IOWLCounterexampleGenerator;
import org.apache.log4j.Logger;
import org.graphstream.graph.Edge;
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
        this.classMap = this.sortClassMap(this.createClassMap());
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
            this.classMap = this.sortClassMap(this.createClassMap());
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
        logger.info("class map is created: "+classMap.keySet());
        return classMap;
    }

    private Map<String, List<OWLClass>> sortClassMap(Map<String, Set<OWLClass>> map) {
        Map<String, List<OWLClass>> listMap = new HashMap<>();
        map.entrySet().stream()
                .forEach(e -> listMap.put(e.getKey(),
                        sortList(e.getValue().stream()
                                .collect(Collectors.toList()))));
        logger.info("class map is sorted: "+listMap.keySet());
        return listMap;
    }

    private Object[][] createConceptData() {
        List<List<Object>> conceptList = new ArrayList<>();
        classMap.entrySet().stream().forEach(e -> e.getValue()
                .forEach(c -> conceptList.add(Arrays.asList(e.getKey(), c)))
        );
        return conceptList.stream()
                .map(l -> l.stream().toArray(Object[]::new))
                .toArray(Object[][]::new);
    }

    private List<OWLClass> sortList(List<OWLClass> classList) {
        List<OWLClass> finalList = new ArrayList<>();
        List<OWLClass> sorted = new ArrayList<>();
        List<OWLClass> sortedOut = classList;
        int i = 0;

        while (!sortedOut.isEmpty() && i < 10) {
            if (sorted.isEmpty()) {
                sorted = sortedOut;
            }
            List<List<OWLClass>> sortedAndSortedOut = compareClassExpressions(sorted);
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
    private List<OWLClass> sortHasSubclass(List<OWLClass> classList) {
        List<OWLClass> sortedClassList = new ArrayList<>();
        for(OWLClass cl:classList) {
            if(res.getSubClasses(cl,false).isEmpty()) {
                sortedClassList.add(cl);
            }
        }
        classList.removeAll(sortedClassList);
        sortedClassList.addAll(classList);
        return sortedClassList;
    }

    private List<List<OWLClass>> compareClassExpressions(List<OWLClass> classList) {
        Set<OWLClass> subsumed = new HashSet<>();
        Set<OWLClass> subsumers = new HashSet<>();
        List<List<OWLClass>> returnList = new ArrayList<>();

        classList.stream().forEach(expr1 -> classList.stream().filter(expr2 -> !expr1.equals(expr2))
                .filter(expr2 -> res.isEntailed(df.getOWLSubClassOfAxiom(expr1, expr2))
                        && !res.isEntailed(df.getOWLSubClassOfAxiom(expr2, expr1 )))
                .forEach(expr2 -> {
                    subsumed.add(expr1);
                    subsumers.add(expr2);
                }));

        List<OWLClass> moreExact = subsumed.stream().collect(Collectors.toList());
        classList.removeAll(subsumers);
        classList.removeAll(subsumed);
        moreExact.addAll(classList);
        returnList.add(moreExact);
        subsumers.removeAll(subsumed);
        returnList.add(subsumers.stream().collect(Collectors.toList()));
        return returnList;
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
        logger.debug("nodes are created: "+graph.nodes().collect(Collectors.toList()));
    }
    private void createNodeLabels() {
        SpriteManager sMan = new SpriteManager(graph);
        classMap.entrySet().stream().forEach(e ->
                {
                    int classNummer = e.getValue().size();
                    if (classNummer > maxLabelNumber) {
                        classNummer = maxLabelNumber;
                    }
                    Iterator<OWLClass> owlclassIterator = e.getValue().iterator();
                    for (int i = 0; i < classNummer; i++) {
                        int labelDistance = -(i+1) * baseLabelDistance;
                        String currOWLClass = owlclassIterator.next().getIRI().getShortForm();
                        Sprite sprite = sMan.addSprite(e.getKey() + currOWLClass);
                        sprite.attachToNode(e.getKey());
                        sprite.setPosition(0, 0, 0);
                        sprite.setAttribute("ui.label", currOWLClass);
                        sprite.setAttribute("ui.style","text-offset: 0, "+labelDistance+";");
                    }
                    if (owlclassIterator.hasNext()) {
                        int labelDistance = -(classNummer+1) * baseLabelDistance;
                        Sprite sprite = sMan.addSprite(e.getKey() + "expand");
                        sprite.attachToNode(e.getKey());
                        sprite.setPosition(0, 0, 0);
                        sprite.setAttribute("ui.label", "...");
                        sprite.setAttribute("ui.style","text-offset: 0, "+labelDistance+";");
                    }
                }
        );
    }
    private void createEdges() {
        edges = model.stream()
                .filter(ax -> ax.isOfType(AxiomType.OBJECT_PROPERTY_ASSERTION))
                .map(ax -> (OWLObjectPropertyAssertionAxiom) ax)
                .collect(Collectors.toSet());
        createdEdges = new HashSet<>();

        for (OWLObjectPropertyAssertionAxiom edge1:edges) {
            logger.debug("processing axiom "+edge1.toString());
            if (!createdEdges.contains(edge1)) {
                for (OWLObjectPropertyAssertionAxiom edge2:edges) {
                    if(edge1.getIndividualsInSignature().equals(edge2.getIndividualsInSignature())
                            && !edge1.getProperty().equals(edge2.getProperty())) {
                        handleLoop(edge1.getSubject().asOWLNamedIndividual(),edge1.getObject().asOWLNamedIndividual());
                        break;
                    }
                }
            }
            if(!createdEdges.contains(edge1)) {
                createEdge(edge1.getSubject().asOWLNamedIndividual(),
                        edge1.getProperty().asOWLObjectProperty(),
                        edge1.getObject().asOWLNamedIndividual(),
                        0,
                        1);
            }
        }
    }
    private void createEdge(OWLNamedIndividual a,
                            OWLObjectProperty r,
                            OWLNamedIndividual b,
                            int edgeNum,
                            int directNum) {

        String desc = a.getIRI().getShortForm().toString();
        String succ = b.getIRI().getShortForm().toString();
        String property = r.getIRI().getShortForm().toString();
        String fillColor= "rgba(0,0,0,0);";
        String textColor= GraphStyleSheets.PROTEGE_BLUE_1;
        if(edgeNum == 0) {
            fillColor= GraphStyleSheets.PROTEGE_BLUE_1;
        } else if (edgeNum == directNum) {
            fillColor= GraphStyleSheets.PROTEGE_BLUE;
            textColor= GraphStyleSheets.PROTEGE_BLUE;
        } else if (edgeNum > directNum) {
            textColor= GraphStyleSheets.PROTEGE_BLUE;
        }
        String alignment = "along;";
        if (a.equals(b)) {
            alignment = "center;";
        }
        int labelDistance = (edgeNum+1) * baseLabelDistance;
        Edge edge = graph.addEdge(desc + succ+ property, desc, succ, true);
        edge.setAttribute("ui.label", property);
        edge.setAttribute("ui.style","text-color: "+textColor+
                "text-offset: 0, "+labelDistance+";"+
                "fill-color: "+fillColor+
                "text-alignment: "+alignment);
    }
    private void handleLoop(OWLNamedIndividual a, OWLNamedIndividual b) {
        logger.debug("handling loop for "+a+b);
        Set<OWLNamedIndividual> inds = Sets.newHashSet(a,b);
        List<OWLObjectProperty> direct = new ArrayList<>();
        List<OWLObjectProperty> inverse = new ArrayList<>();

        logger.debug(edges);
        for (OWLObjectPropertyAssertionAxiom edge:edges) {
            if(edge.getIndividualsInSignature().equals(inds)) {
                createdEdges.add(edge);
                if (edge.getSubject().equals(a)) {
                    direct.add(edge.getProperty().asOWLObjectProperty());
                } else {
                    inverse.add(edge.getProperty().asOWLObjectProperty());
                }
            }
        }
        logger.debug("direct:"+direct);
        logger.debug("inverse:"+inverse);
        for (int i = 0; i<direct.size();i++) {
            createEdge(a,direct.get(i),b,i,direct.size());
        }
        if (!a.equals(b)) {
            for (int i = 0; i<inverse.size();i++) {
                createEdge(b,inverse.get(i),a,i+direct.size(),direct.size());
            }
        }
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
