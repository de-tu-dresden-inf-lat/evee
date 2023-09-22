package de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample;

import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.ui.graphicGraph.GraphicEdge;
import org.graphstream.ui.graphicGraph.GraphicGraph;
import org.graphstream.ui.graphicGraph.GraphicNode;
import org.semanticweb.owlapi.model.OWLObjectProperty;

import java.util.HashMap;
import java.util.Set;
import java.util.*;
import java.util.stream.Collectors;

public class EdgeLabelPositioner {
    private final Logger logger = Logger.getLogger(EdgeLabelPositioner.class);


    public void positionLabelsOnFirstClick(Map<String[],List<OWLObjectProperty>> objectPropertyMap,
                                           GraphicGraph graphModel) {

        graphModel.edges().map(e -> (GraphicEdge) e)
                .filter(e -> e.getGroup()!=null)
                .filter(e -> e.getNode0()!= e.getNode1())
                .forEach(e ->{
                    boolean positiveOffset = true;
                    if(e.getStyle().getTextOffset().get(1)<0) {
                        positiveOffset = false;
                    }

                    if(ifChangeLebelsPositions( (GraphicNode) e.getNode0(),
                            (GraphicNode) e.getNode1(),
                            positiveOffset)) {
                        logger.debug("change position for "+e.getNode0().getId()+e.getNode1().getId());
                        positionLabele(e);
                    }
                });
    }


    public void positionLabelsOnNodeMove(GraphicNode node,
                                         double oldPositionX,
                                         double newPositionX,
                                         GraphicGraph graphModel) {



        Set<GraphicEdge> curvedEdges = node.edges()
                .map(e -> (GraphicEdge) e)
                .filter(e -> e.getGroup()!=null)
                .filter(e -> e.getNode0()!= e.getNode1())
                .collect(Collectors.toSet());


        Map<GraphicNode, Set<GraphicEdge>> curvedEdgesMap = initializeCurvedEdgesMap(curvedEdges,node);
        curvedEdgesMap.entrySet().stream()
                .filter(e -> ifChangeLebelsPositionsOnMove(oldPositionX,
                        newPositionX,
                        e.getKey()))
                .forEach(e -> e.getValue().forEach(ed ->positionLabele(ed)));


    }
    private Map<GraphicNode, Set<GraphicEdge>> initializeCurvedEdgesMap(Set<GraphicEdge> curvedEdges,GraphicNode node) {
        Map<GraphicNode, Set<GraphicEdge>> curvedEdgesMap = new HashMap<>();
        curvedEdges.stream().forEach(e -> {
            curvedEdgesMap.merge(
                    (GraphicNode) e.getOpposite(node),
                    Sets.newHashSet(e),
                    (a, b) -> {
                        a.addAll(b);
                        return a;
                    });
        });
        return curvedEdgesMap;
    }
    private boolean ifChangeLebelsPositionsOnMove(double oldPositionX,
                                                  double newPositionX,
                                                  GraphicNode targetNode) {

        double targetNodePositionX = targetNode.getX();

        if(oldPositionX >= targetNodePositionX) {
            if(newPositionX <=targetNodePositionX ) {
                return true;
            }
        } else {
            if(newPositionX >=targetNodePositionX ) {
                return true;
            }
        }
        return false;
    }

    private boolean ifChangeLebelsPositions(GraphicNode n0,
                                            GraphicNode n1,
                                            boolean positiveOffset) {
        if(n0.getX()>= n1.getX()) {
            positiveOffset = !positiveOffset;
        }
        if(positiveOffset) {
            return false;
        }
        return true;
    }
    private void positionLabele(GraphicEdge edge) {

        double CurrentOffset = edge.getStyle().getTextOffset().get(1);
        edge.getStyle().getTextOffset().setValue(1, -CurrentOffset);

    }
}
