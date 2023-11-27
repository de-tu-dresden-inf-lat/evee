package de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample;

import de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample.listener.GraphViewMouseListener;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample.ui.GraphModelView;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample.util.GraphStyleSheets;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample.util.EdgeLabelPositioner;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample.util.MappingUtils;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample.util.OWLObjectCollectionSorter;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.counterexample.IGraphViewService;
import org.apache.log4j.Logger;
import org.graphstream.ui.graphicGraph.GraphicEdge;
import org.graphstream.ui.graphicGraph.GraphicGraph;
import org.graphstream.ui.graphicGraph.GraphicNode;
import org.graphstream.ui.spriteManager.Sprite;
import org.graphstream.ui.spriteManager.SpriteManager;
import org.graphstream.ui.swing_viewer.SwingViewer;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.search.EntitySearcher;

import java.util.*;

public class GraphViewGenerator implements IGraphViewService {
    private final int BASE_LABEL_DISTANCE = -20;
    private final int autoLayoutTimeMs;
    private int maxLabelNum;

    private final String COLUMN = "column";
    private final String LINE = "line";
    private final String styleSheet;
    private final OWLDataFactory df;
    private Map<String, List<OWLClass>> individualsToClassesMap;
    private Map<String[],List<OWLObjectProperty>> pairsToObjectPropertiesMap;
    private GraphicGraph graphModel;
    private OWLOntology ont;
    private final Logger logger = Logger.getLogger(GraphViewGenerator.class);
    private Set<IRI> markedIndividuals;
    private SwingViewer graphModelViewer;
    private String edgeLabelGrouping = LINE;

    public GraphViewGenerator(String styleSheet,int autoLayoutTimeMs) {
        this.autoLayoutTimeMs = autoLayoutTimeMs;
        this.df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
        this.styleSheet = styleSheet;

    }

    public GraphicGraph generateGraphModel(Map<String, List<OWLClass>> individualsToClassesMap, Map<String[],
            List<OWLObjectProperty>> pairsToObjectPropertiesMap,
                                           OWLOntology ont,
                                           Set<IRI> markedIndividuals,
                                           int labelsNum) {
        this.individualsToClassesMap = individualsToClassesMap;
        this.pairsToObjectPropertiesMap = pairsToObjectPropertiesMap;
        this.ont = ont;
        this.markedIndividuals = markedIndividuals;
        this.maxLabelNum = labelsNum;

        System.setProperty("org.graphstream.ui", "swing");
        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
        graphModel = new GraphicGraph("model");
        createStyleSheet();
        createNodes();
        createEdges();
        createNodeLabels();

        return graphModel;
    }


    private void createEdges() {

        pairsToObjectPropertiesMap.entrySet().stream()
                .forEach(e -> {
                    String subj = e.getKey()[0];
                    String obj = e.getKey()[1];
                    boolean isForwardEdge = true;
                    if(graphModel.edges()
                            .anyMatch(edge -> (edge.getNode0().getId().equals(obj))
                                    && edge.getNode1().getId().equals(subj))) {
                        isForwardEdge = false;
                    }

                    if(edgeLabelGrouping == COLUMN) {
                        for (int i =0;i<e.getValue().size();i++) {
                            String prop = owlEntityToLabel(e.getValue().get(i));
                            createEdge(subj,prop,obj,i,isForwardEdge);
                        }
                    } else if (edgeLabelGrouping == LINE) {
                        String prop = owlEntityToLabel(e.getValue().get(0));
                        if (e.getValue().size()>1) {
                            for (int i =1;i<e.getValue().size();i++) {
                                prop = prop + ", " + owlEntityToLabel(e.getValue().get(i));
                            }
                        }
                        createEdge(subj,prop,obj,0,isForwardEdge);
                    }
                });
    }

    private void createEdge(String subjId,
                            String label,
                            String objId,
                            int edgeNum,
                            boolean isForwardEdge) {
        int labelDistance = (edgeNum+1) * BASE_LABEL_DISTANCE;
        String textColor= GraphStyleSheets.PROTEGE_BLUE;
        String fillColor= GraphStyleSheets.PROTEGE_BLUE_1;
        String alignment="along;";

        if (subjId.equals(objId)) {
            alignment = "center;";
        }
        if (edgeNum>0) {
            fillColor= "rgba(0,0,0,0);";
        }
        if (!isForwardEdge) {
            labelDistance = labelDistance * -1;
        }

        GraphicEdge edge = (GraphicEdge) graphModel.addEdge("edge"+subjId + objId+edgeNum, subjId, objId, true);
        edge.setAttribute("ui.label", label);
//        edge.setAttribute("properties",prop);
        edge.setAttribute("ui.style","text-color: "+textColor+
                "text-offset: 0, "+labelDistance+";"+
                "fill-color: "+fillColor+
                "text-alignment: "+alignment);
//        logger.info("prop of node"+edge.getAttribute("properties").toString());
    }

    private void createNodes() {
        logger.debug("root inds:"+ markedIndividuals);
        individualsToClassesMap.keySet().stream().forEach(k -> {
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
        individualsToClassesMap.entrySet().stream().forEach(e ->
                {
                    int classNummer = e.getValue().size();
                    if (classNummer > maxLabelNum) {
                        classNummer = maxLabelNum;
                    }
                    String nodeID = e.getKey();
                    Iterator<OWLClass> owlclassIterator = e.getValue().iterator();
                    for (int i = 0; i < classNummer; i++) {
                        int labelDistance = -(i+1) * BASE_LABEL_DISTANCE;
                        OWLClass cls = owlclassIterator.next();
                        createSpriteLabel(nodeID,cls,labelDistance,sMan);
                    }
                    if (owlclassIterator.hasNext()) {
                        int labelDistance = -(classNummer+1) * BASE_LABEL_DISTANCE;
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

    @Override
    public GraphModelView computeView(Set<OWLIndividualAxiom> model,
                                      OWLOntology ontology,
                                      Set<IRI> markedIndividuals,
                                      int labelsNum) {
        OWLObjectCollectionSorter sorter = new OWLObjectCollectionSorter(ontology);
        individualsToClassesMap = sorter.sortOWLObjectMap(MappingUtils.toIndividualsToClassesMap(model));
        pairsToObjectPropertiesMap = sorter.sortOWLObjectMap(MappingUtils.toPairsToObjectPropertiesMap(model));
        logger.debug(individualsToClassesMap);

        generateGraphModel(individualsToClassesMap,
                pairsToObjectPropertiesMap,
                ontology,
                markedIndividuals,
                labelsNum);
        GraphViewMouseListener graphViewMouseListener = new GraphViewMouseListener(individualsToClassesMap,
                pairsToObjectPropertiesMap);
        graphModelViewer = new SwingViewer(graphModel,
                Viewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
        graphModelViewer.enableAutoLayout();
        View view = graphModelViewer.addDefaultView(false);

        GraphModelView graphView = new GraphModelView(view,graphViewMouseListener);
        return graphView;
    }

    @Override
    public void doPostProcessing() {
        logger.info("postprocessing is started");
        try {
            Thread.sleep(autoLayoutTimeMs);
            graphModelViewer.disableAutoLayout();
            EdgeLabelPositioner.initializeLabelsPositions(graphModel);
        } catch (InterruptedException e) {
            logger.error("postprocessing failed",e);
        }
    }
}
