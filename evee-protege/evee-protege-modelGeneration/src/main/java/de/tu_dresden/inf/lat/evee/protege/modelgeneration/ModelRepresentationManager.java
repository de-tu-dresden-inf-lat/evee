package de.tu_dresden.inf.lat.evee.protege.modelgeneration;


import com.google.common.collect.Sets;
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
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;
//import org.graphstream.graph.*;
//import org.graphstream.graph.implementations.*;
//import org.graphstream.ui.swing_viewer.*;
//import org.graphstream.ui.view.Viewer;


public class ModelRepresentationManager {
    private final OWLReasoner res;
    private OWLEditorKit owlEditorKit;
    private Set<OWLAxiom> model;
    private OWLDataFactory df;
    private Map<String,List<OWLClass>> classMap;
    private Object[][] roleData;
    private Object[][] conceptData;

    public ModelRepresentationManager(Set<OWLAxiom> model, OWLEditorKit owlEditorKit, OWLOntology ont) {
        this.model = model;
        this.owlEditorKit = owlEditorKit;
        ReasonerFactory rf = new ReasonerFactory();
        this.res = rf.createReasoner(ont);
        this.df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
        this.classMap = sortClassMap(getClassMap());
        this.roleData = getRoleData();
        this.conceptData = getConceptData();
    }

    public Component getTableModel() {


        JPanel component = new JPanel();
        component.setLayout(new BoxLayout(component,BoxLayout.PAGE_AXIS));

        String columnC[]={"Indivdual","Concept Names"};
        String columnR[] = { "Subject Individual","Object Property","Object Individual" };
        DefaultTableModel resultModelC = new DefaultTableModel();
        DefaultTableModel resultModelR = new DefaultTableModel();

        resultModelC.setDataVector(conceptData,columnC);
        resultModelR.setDataVector(roleData,columnR);
        component.setBorder(new EmptyBorder(new Insets(20,20,20,20)));
        component.add(Box.createRigidArea(new Dimension(20,20)));
        OWLCellRenderer renderer = new OWLCellRenderer(owlEditorKit);
        renderer.setWrap(false);
        JTable resultsC = new JTable(resultModelC) ;
        JTable resultsR = new JTable(resultModelR) ;
        resultsC.setDefaultRenderer(Object.class, renderer);
        resultsR.setDefaultRenderer(Object.class, renderer);
        component.add(new JScrollPane(resultsC));
        component.add(Box.createRigidArea(new Dimension(20,20)));
        component.add(new JScrollPane(resultsR));

        return component;
    }

    public Component getGraphModel() {
//        Map<String, List<String>> classMap = createClassMap(conceptData,true);
        System.setProperty("org.graphstream.ui", "swing");
        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
        GraphicGraph graph = getGraph(classMap,roleData);
        Viewer viewer = new SwingViewer(graph, Viewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
        viewer.enableAutoLayout();
        GraphModelTabComponent component = new GraphModelTabComponent(viewer,graph,classMap,owlEditorKit);
        return component;
    }

    private Map<String,Set<OWLClass>> getClassMap () {
        Map<String,Set<OWLClass>> classMap = new HashMap<>();
        model.stream()
                .filter(ax -> ax.isOfType(AxiomType.CLASS_ASSERTION))
                .map(ax -> (OWLClassAssertionAxiom) ax)
                .forEach(ax ->classMap.merge(
                        ax.getIndividual().asOWLNamedIndividual().getIRI().getShortForm().toString(),
                        Sets.newHashSet(ax.getClassExpression().asOWLClass()),
                        (a, b) -> {
                    a.addAll(b);
                    return a;
                }));
        return  classMap;
    }
    private Map<String, List<OWLClass>> sortClassMap(Map<String,Set<OWLClass>> map) {
        Map<String,List<OWLClass>> listMap = new HashMap<>();
        map.entrySet().stream()
                .forEach(e ->listMap.put(e.getKey(),
                        sortList(e.getValue().stream()
                                .collect(Collectors.toList()))));
        return listMap;
    }
    private Object [][] getRoleData() {
        List<List<Object>> roleList = new ArrayList<>();
        model.stream()
                .filter(ax -> ax.isOfType(AxiomType.OBJECT_PROPERTY_ASSERTION))
                .map(ax -> (OWLObjectPropertyAssertionAxiom) ax)
                .forEach(a ->
                roleList.add(Arrays.asList(a.getSubject().toString().substring(1, a.getSubject().toString().length() - 1), a.getProperty(), a.getObject().toString().substring(1, a.getObject().toString().length() - 1))));

        return roleList.stream()
                .map(l -> l.stream().toArray(Object[]::new))
                .toArray(Object[][]::new);
    }
    private Object [][] getConceptData() {
        List<List<Object>> conceptList = new ArrayList<>();
        classMap.entrySet().stream().forEach(e -> e.getValue()
                .forEach(c -> conceptList.add(Arrays.asList(e.getKey(), c)))
        );
        return conceptList.stream()
                .map(l -> l.stream().toArray(Object[]::new))
                .toArray(Object[][]::new);
    }
    private List<OWLClass> sortList (List<OWLClass> classList) {
        List<OWLClass> finalList = new ArrayList<>();
        List<OWLClass> sorted = new ArrayList<>();
        List<OWLClass> sortedOut = classList;
        int i = 0;

        while (!sortedOut.isEmpty() && i < 10  ) {
            if(sorted.isEmpty()) {
                sorted = sortedOut;
            }
            List<List<OWLClass>> sortedAndSortedOut  = compareClassExpressions(sorted);
            sorted = sortedAndSortedOut.get(0);
            sortedOut= sortedAndSortedOut.get(1);
            finalList.addAll(sortedOut);

            if(sorted.isEmpty()) {
                break;
            }
            if (sortedOut.isEmpty()) {
                finalList.addAll(sorted);
                break;
            }
            i = i + 1;
        }
        Collections.reverse(finalList);
        return finalList;
    }
    private List<List<OWLClass>> compareClassExpressions(List <OWLClass> classList) {
        Set<OWLClass> subsumed = new HashSet<>();
        Set<OWLClass> subsumers = new HashSet<>();
        List<List<OWLClass>> returnList = new ArrayList<>();

        classList.stream().forEach(expr1 -> classList.stream().filter(expr2 -> !expr1.equals(expr2))
                .filter(expr2 -> res.isEntailed(df.getOWLSubClassOfAxiom(expr1, expr2)))
                .forEach(expr2 -> {subsumed.add(expr1);subsumers.add(expr2);}));
        List<OWLClass> moreExact = subsumed.stream().collect(Collectors.toList());
        classList.removeAll(subsumers);
        classList.removeAll(subsumed);
        moreExact.addAll(classList);
        returnList.add(moreExact);
        subsumers.removeAll(subsumed);
        returnList.add(subsumers.stream().collect(Collectors.toList()));
        return returnList;
    }
    private GraphicGraph createNodes(GraphicGraph graph, Map<String, List<OWLClass>> classMap) {

        classMap.keySet().stream().forEach(k -> {
            GraphicNode n = (GraphicNode) graph.addNode(k);
            if (n.getId()=="root-Ind") {
                n.setAttribute("ui.style","fill-color: #000000;");
            }
        });
        return graph;
    }
    private GraphicGraph getGraph(Map<String, List<OWLClass>> classMap,
                                         Object[][] roleData) {
        GraphicGraph graph = new GraphicGraph("model");
        createNodes(graph,classMap);
//        Map<String, List<String>> map = new HashMap<>();

//        Arrays.stream(conceptData).forEach(e -> {
//            String ind = (String)e[0];
//            OWLClass owlclass = (OWLClass) e[1];
//            String stringCl = owlclass.getIRI().getShortForm().toString();
//            GraphicNode n = (GraphicNode) graph.addNode(ind);
//            if (n.getId()=="root-Ind") {
//                n.setAttribute("ui.style","fill-color: #000000;");
//            }
//            map.merge(ind, Lists.newArrayList(stringCl), (a, b) -> {
//                a.addAll(b);
//                return a;
//            });
//        });
      Arrays.stream(roleData).forEach(e -> {
          String desc = (String)e[0];
          String succ = (String)e[2];
          OWLObjectProperty prop = (OWLObjectProperty)e[1];
          Edge edge = graph.addEdge(desc+succ,desc,succ,true);
          edge.setAttribute("ui.label",prop.getIRI().getShortForm().toString());
      });
        SpriteManager sMan = new SpriteManager(graph);
        classMap.entrySet().stream().forEach(e->
                {
                    int maxExpNummer = e.getValue().size();
                    if (maxExpNummer> 2) {
                        maxExpNummer = 2;
                    }
                    Iterator<OWLClass> owlclassIterator = e.getValue().iterator();
                    for (int i = 0; i<maxExpNummer; i++) {
                        String currOWLClass = owlclassIterator.next().getIRI().getShortForm().toString();
                        Sprite sprite = sMan.addSprite(e.getKey()+currOWLClass);
                        sprite.attachToNode(e.getKey());
                        sprite.setPosition(0,-0.075*i,0);
                        sprite.setAttribute("ui.label",currOWLClass);
                    }
                    if (owlclassIterator.hasNext()) {
                        Sprite sprite = sMan.addSprite(e.getKey()+"expand");
                        sprite.attachToNode(e.getKey());
                        sprite.setPosition(0,-0.075*maxExpNummer,0);
                        sprite.setAttribute("ui.label","...");
                    }
                }
        );
        graph.setAttribute("ui.quality");
        graph.setAttribute("ui.antialias");
        graph.setAttribute("ui.stylesheet",
                " edge {text-alignment: along;text-offset: -25, -25;text-background-mode: plain; text-background-color: white;text-size:13;}" +
                        "node {text-offset: -25, -25;text-background-mode: plain; text-background-color: white;text-size:15;fill-color: #FFFFFF; size: 20px; stroke-mode: plain; stroke-color: #000000; }" +
                        "sprite {text-size:13;" +
                        "text-background-mode: plain;"+
                        "text-mode:normal;" +
                        "text-offset: 0, 25;" +
                        "fill-mode: none;}");
        return graph;
    }
}
