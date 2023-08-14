package de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample;

import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.ui.geom.Point2;
import org.graphstream.ui.graphicGraph.GraphicEdge;
import org.graphstream.ui.graphicGraph.GraphicGraph;
import org.graphstream.ui.graphicGraph.GraphicNode;

import java.util.HashMap;
import java.util.Set;
import java.util.*;
import java.util.stream.Collectors;

public class EdgeLabelPositioner {
    private final Logger logger = Logger.getLogger(EdgeLabelPositioner.class);

    private Map<Node, Set<GraphicEdge>> initializeCurvedEdgesMap(Set<GraphicEdge> curvedEdges,GraphicNode node) {
        Map<Node, Set<GraphicEdge>> curvedEdgesMap = new HashMap<>();
        curvedEdges.stream().forEach(e -> {
            curvedEdgesMap.merge(
                    e.getOpposite(node),
                    Sets.newHashSet(e),
                    (a, b) -> {
                        a.addAll(b);
                        return a;
                    });
        });
        return curvedEdgesMap;
    }


    public void positionLabels(GraphicNode node,
//                               Point2 oldPosition,
//                               Point2 newPosition,
                               GraphicGraph graphModel) {
        logger.debug("edge positioning is started for "+ node.getId());
        Set<GraphicEdge> curvedEdges = node.edges()
                .map(e -> (GraphicEdge) e)
                .filter(e -> e.isCurve())
                .collect(Collectors.toSet());
        if (curvedEdges.isEmpty()) {return;}
        logger.debug("curved edges of "+node.getId()+": "+curvedEdges);
        Map<Node, Set<GraphicEdge>> curvedEdgesMap = initializeCurvedEdgesMap(curvedEdges,node);
        logger.debug("top key of curved edges map of "+node.getId()+": "+curvedEdgesMap.keySet().stream().findAny());
    }
}
