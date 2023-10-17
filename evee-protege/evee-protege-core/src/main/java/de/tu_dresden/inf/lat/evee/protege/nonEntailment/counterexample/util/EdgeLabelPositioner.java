package de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample.util;

import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.graphstream.ui.graphicGraph.GraphicEdge;
import org.graphstream.ui.graphicGraph.GraphicGraph;
import org.graphstream.ui.graphicGraph.GraphicNode;

import java.util.HashMap;
import java.util.Set;
import java.util.*;
import java.util.stream.Collectors;

public class EdgeLabelPositioner {
    private final static Logger logger = Logger.getLogger(EdgeLabelPositioner.class);


    public static void initializeLabelsPositions(GraphicGraph graphModel) {

        graphModel.edges().map(e -> (GraphicEdge) e)
                .filter(e -> e.getGroup()!=null)
                .filter(e -> e.getNode0()!= e.getNode1())
                .forEach(e ->{
                    boolean isPositiveOffset = true;
                    if(e.getStyle().getTextOffset().get(1)<0) {
                        isPositiveOffset = false;
                    }

                    if(needChangeLabelsPositions( (GraphicNode) e.getNode0(),
                            (GraphicNode) e.getNode1(),
                            e,
                            isPositiveOffset)) {

                        positionLabel(e);
                    }
                });
    }


    public static void positionLabelsOnNodeMove(GraphicNode node,
                                         double oldPositionX,
                                         double newPositionX) {

        Set<GraphicEdge> curvedEdges = node.edges()
                .map(e -> (GraphicEdge) e)
                .filter(e -> e.getGroup()!=null)
                .filter(e -> e.getNode0()!= e.getNode1())
                .collect(Collectors.toSet());


        Map<GraphicNode, Set<GraphicEdge>> curvedEdgesMap = initializeCurvedEdgesMap(curvedEdges,node);
        curvedEdgesMap.entrySet().stream()
                .filter(e -> needChangeLabelsPositionsOnMove(oldPositionX,
                        newPositionX,
                        e.getKey()))
                .forEach(e -> e.getValue().forEach(ed -> positionLabel(ed)));


    }
    private static Map<GraphicNode, Set<GraphicEdge>> initializeCurvedEdgesMap(Set<GraphicEdge> curvedEdges,GraphicNode node) {
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
    private static boolean needChangeLabelsPositionsOnMove(double oldPositionX,
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
    private static boolean needChangeLabelsPositions(GraphicNode n0,
                                                     GraphicNode n1,
                                                     GraphicEdge e,
                                                     boolean isPositiveOffset) {



        boolean beforeMovingIsOnTop = !isPositiveOffset;

        if((n0.getX() < n1.getX()) && !beforeMovingIsOnTop) {
            return true;
        }
        if(!(n0.getX() < n1.getX()) && beforeMovingIsOnTop) {
            return true;
        }




        logger.debug("current edge:" +n0.getId()+" to "+n1.getId());
        logger.debug("before positioning offset is positive: "+isPositiveOffset);
        return false;

    }

    private static boolean needChangeLabelsPositions(GraphicNode n0,
                                                     GraphicNode n1,
                                                     boolean isPositiveOffset) {
        if(n0.getX()>= n1.getX()) {
            isPositiveOffset = !isPositiveOffset;
        }
        if(isPositiveOffset) {
            return false;
        }
        return true;
    }
    private static void positionLabel(GraphicEdge edge) {

        double CurrentOffset = edge.getStyle().getTextOffset().get(1);
        edge.getStyle().getTextOffset().setValue(1, -CurrentOffset);

    }
}
