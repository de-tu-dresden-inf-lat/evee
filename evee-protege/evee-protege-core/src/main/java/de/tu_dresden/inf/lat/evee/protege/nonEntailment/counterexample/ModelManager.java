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
import org.protege.editor.owl.ui.renderer.OWLCellRenderer;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    private Object[][] roleData;
    private Object[][] conceptData;
    private Set<IRI> markedIndividuals;
    private Viewer viewer;
    private GraphicGraph graph;
    private OWLSubClassOfAxiom observation;
    private int maxLabelNumber;
    private final double labelSpace = -0.085;
    private Set<Sprite> classLabels;
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
        this.roleData = this.createRoleData();
        this.conceptData = this.createConceptData();
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
            this.roleData = this.createRoleData();
            this.conceptData = this.createConceptData();
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

    public Component getTableModel() {
        JPanel component = new JPanel();
        component.setLayout(new BoxLayout(component, BoxLayout.PAGE_AXIS));
        String[] columnC = {"Indivdual", "Concept Names"};
        String[] columnR = {"Subject Individual", "Object Property", "Object Individual"};
        DefaultTableModel resultModelC = new DefaultTableModel();
        DefaultTableModel resultModelR = new DefaultTableModel();
        resultModelC.setDataVector(conceptData, columnC);
        resultModelR.setDataVector(roleData, columnR);
        component.setBorder(new EmptyBorder(new Insets(20, 20, 20, 20)));
        component.add(Box.createRigidArea(new Dimension(20, 20)));
        OWLCellRenderer renderer = new OWLCellRenderer(owlEditorKit);
        renderer.setWrap(false);
        JTable resultsC = new JTable(resultModelC);
        JTable resultsR = new JTable(resultModelR);
        resultsC.setDefaultRenderer(Object.class, renderer);
        resultsR.setDefaultRenderer(Object.class, renderer);
        component.add(new JScrollPane(resultsC));
        component.add(Box.createRigidArea(new Dimension(20, 20)));
        component.add(new JScrollPane(resultsR));

        return component;
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

    private Object[][] createRoleData() {
        List<List<Object>> roleList = new ArrayList<>();
        model.stream()
                .filter(ax -> ax.isOfType(AxiomType.OBJECT_PROPERTY_ASSERTION))
                .map(ax -> (OWLObjectPropertyAssertionAxiom) ax)
                .forEach(a ->
                        roleList.add(Arrays.asList(a.getSubject().asOWLNamedIndividual().getIRI().getShortForm(), a.getProperty(), a.getObject().asOWLNamedIndividual().getIRI().getShortForm())));
        logger.info("role list is created: "+roleList);
        return roleList.stream()
                .map(l -> l.stream().toArray(Object[]::new))
                .toArray(Object[][]::new);
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
//            logger.debug("sorting:iteration "+i+", sorted: "+ sorted+", sorted out: "+sortedOut);
            if (sorted.isEmpty()) {
//                Collections.reverse(sortHasSubclass(sortedOut));
                finalList.addAll(sortedOut);
                break;
            }
            if (sortedOut.isEmpty()) {
//                Collections.reverse(sortHasSubclass(sorted));
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
//                    n.setAttribute("ui.style", "fill-color: #000000;");
                    n.setAttribute("ui.class", "root");
                }
            }
        });
        logger.debug("nodes are created: "+graph.nodes().collect(Collectors.toList()));
    }
    private void createSprites() {
        SpriteManager sMan = new SpriteManager(graph);
        classLabels = new HashSet<>();
        classMap.entrySet().stream().forEach(e ->
                {
                    int classNummer = e.getValue().size();
                    if (classNummer > maxLabelNumber) {
                        classNummer = maxLabelNumber;
                    }
                    Iterator<OWLClass> owlclassIterator = e.getValue().iterator();
                    for (int i = 0; i < classNummer; i++) {
                        String currOWLClass = owlclassIterator.next().getIRI().getShortForm();
                        Sprite sprite = sMan.addSprite(e.getKey() + currOWLClass);
                        sprite.attachToNode(e.getKey());
//                        sprite.setPosition(StyleConstants.Units.PERCENTS,0, -0.075 * i, 0);
                        sprite.setPosition(0, labelSpace * i, 0);
                        sprite.setAttribute("ui.label", currOWLClass);
                        classLabels.add(sprite);
                    }
                    if (owlclassIterator.hasNext()) {
                        Sprite sprite = sMan.addSprite(e.getKey() + "expand");
                        sprite.attachToNode(e.getKey());
                        sprite.setPosition(0, labelSpace * classNummer, 0);
                        sprite.setAttribute("ui.label", "...");
                        classLabels.add(sprite);
                    }
                }
        );
    }
    private void createEdges() {
        SpriteManager sMan = new SpriteManager(graph);
        Arrays.stream(roleData).forEach(e -> {
            String desc = (String) e[0];
            String succ = (String) e[2];
            OWLObjectProperty prop = (OWLObjectProperty) e[1];
            Edge edge = graph.addEdge(desc + succ+ prop.getIRI().getShortForm(), desc, succ, true);

//            Sprite label = sMan.addSprite(edge.getId() + "label");
//            graph.addSprite(label.getId());
//            label.setAttribute("ui.class", "edge");
//
//            label.setAttribute("ui.label",prop.getIRI().getShortForm() );
//            label.attachToEdge(edge.getId());
//            label.setPosition(0.5, 0, 0);
            edge.setAttribute("ui.label", prop.getIRI().getShortForm());
        });
    }
    private void handleLoop(OWLNamedIndividual a, OWLObjectProperty r, OWLNamedIndividual b) {
        if(!a.equals(b)) {
            List<OWLObjectProperty> direct = new ArrayList<>();
            List<OWLObjectProperty> inverse = new ArrayList<>();
            model.stream()
                    .filter(ax -> ax.isOfType(AxiomType.OBJECT_PROPERTY_ASSERTION))
                    .map( ax -> (OWLObjectPropertyAssertionAxiom) ax)
                    .filter(ax -> ax.getProperty().equals(r))
                    .forEach(ax -> {
                        if (ax.getObject().equals(a)) {
                            direct.add(ax.getProperty().asOWLObjectProperty());
                        } else {
                            inverse.add(ax.getProperty().asOWLObjectProperty());
                        }
                    });

        }
    }

    private void createStyleSheet() {
        graph.setAttribute("ui.quality");
        graph.setAttribute("ui.antialias");
        graph.setAttribute("ui.stylesheet",styleSheet);
//        graph.setAttribute("ui.stylesheet",
//                " edge {text-alignment: along;text-offset: -25, -25;text-background-mode: plain; text-background-color: white;text-size:13;}" +
//                        "node {text-offset: -25, -25;text-background-mode: plain; text-background-color: white;text-size:15;fill-color: #FFFFFF; size: 20px; stroke-mode: plain; stroke-color: #000000; }" +
//                        "sprite {text-size:13;" +
//                        "text-background-mode: plain;" +
//                        "text-mode:normal;" +
//                        "text-offset: 0, 25;" +
//                        "fill-mode: none;}");
    }

    private void createGraph() {
        graph = new GraphicGraph("model");
        createNodes();
        createEdges();
        createSprites();
        createStyleSheet();
    }
    public void setStyleSheet(String styleSheet) {
        this.styleSheet = styleSheet;
    }
    public void setMaxLabelNumber(int maxLabelNumber) {
        this.maxLabelNumber = maxLabelNumber;
    }
    public Object[][] getConceptData() {
        return this.conceptData;
    }
    public Object[][] getRoleData() {
        return this.roleData;
    }
    protected OWLOntology getOnt() {
        return this.ont;
    }
    public GraphicGraph getGraph() {
        return this.graph;
    }
    public Set<Sprite> getClassLabels() {return this.classLabels;}
    public Map<String, List<OWLClass>> getClassMap() {
        return this.classMap;
    }
    public Viewer getViewer() {
        return this.viewer;
    }
}
