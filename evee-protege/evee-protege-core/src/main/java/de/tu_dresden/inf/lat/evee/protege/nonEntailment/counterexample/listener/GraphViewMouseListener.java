package de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample.listener;

import de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample.util.EdgeLabelPositioner;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.counterexample.IGraphModelControlPanel;
import org.apache.log4j.Logger;
import org.graphstream.ui.geom.Point2;
import org.graphstream.ui.geom.Point3;
import org.graphstream.ui.graphicGraph.GraphicElement;
import org.graphstream.ui.graphicGraph.GraphicGraph;
import org.graphstream.ui.graphicGraph.GraphicNode;
import org.graphstream.ui.spriteManager.Sprite;
import org.graphstream.ui.spriteManager.SpriteManager;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.camera.Camera;
import org.graphstream.ui.view.util.InteractiveElement;
import org.graphstream.ui.view.util.MouseManager;
import org.semanticweb.owlapi.model.OWLClass;

import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

// import java.awt.event.MouseWheelEvent;
// import java.awt.event.MouseWheelListener;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

public class GraphViewMouseListener implements MouseManager {

    private View view;
    private GraphicGraph graph;

    private GraphicElement curElement;

    private long curTime;

    private Map<String, List<OWLClass>> individualClassMap;
    private final EnumSet<InteractiveElement> interactiveEliments = EnumSet.of(
            InteractiveElement.NODE);
    private final Logger logger = Logger.getLogger(GraphViewMouseListener.class);
    private String previousNodeID ="";
    private Sprite selectionSprite;
    private boolean elementMoving = false;
    private Camera camera;
    private MouseEvent last;
    private GraphicNode curNode = null;
    private boolean isFirstClick;
    private IGraphModelControlPanel controlPanel;

    public GraphViewMouseListener(Map<String, List<OWLClass>> classMap) {              
        this.individualClassMap = classMap;
        this.isFirstClick = true;
    }

    @Override
    public void init(GraphicGraph graph, View view) {
        this.view = view;
        this.graph = graph;
        this.camera = view.getCamera();
        view.addListener(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
        view.addListener(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
        view.addListener(MouseEvent.MOUSE_DRAGGED, mouseDraggedHandler);
        view.addListener(ScrollEvent.SCROLL, mouseWheelHandler);
        this.createSelectionSprite();
    }

    @Override
    public void release() {
        view.removeListener(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
        view.removeListener(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
        view.removeListener(MouseEvent.MOUSE_DRAGGED, mouseDraggedHandler);
        view.removeListener(ScrollEvent.SCROLL, mouseWheelHandler);
    }   

    private final EventHandler<MouseEvent> mousePressedHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent e) {
            onPress(e);
        }
    };

    private final EventHandler<MouseEvent> mouseDraggedHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent e) {
            onDrag(e);
        }
    };

    private final EventHandler<MouseEvent> mouseReleasedHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent e) {
            onRelease(e);
        }
    };

    private final EventHandler<ScrollEvent> mouseWheelHandler = new EventHandler<ScrollEvent>() {
        @Override
        public void handle(ScrollEvent e) {
            onScroll(e);
        }
    };

    
    private void onPress(MouseEvent event) {

        curElement = view.findGraphicElementAt(interactiveEliments,event.getX(), event.getY());

        curTime = System.currentTimeMillis();
        if (curElement != null) {
                elementMoving= true;
                curNode = (GraphicNode) graph.getNode(curElement.getId());
        }
        if(isFirstClick) {
            isFirstClick = false;
        }
    }



    private void onRelease(MouseEvent event) {

        logger.debug("mouse released");
        last = null;
        if (curElement != null) {
            if(System.currentTimeMillis()-curTime <300) {
                logger.info( curElement.getId() +" is clicked");

                selectNewNode(curElement.getId());
            }
            curElement = null;
            curNode = null;
            elementMoving = false;
        }
    }
 

    private void onDrag(MouseEvent event) {
        if(elementMoving) {
            double oldPositionX = curNode.getX();

            elementMoving(curElement, event);
            double newPositionX = curNode.getX();
            EdgeLabelPositioner.positionLabelsOnNodeMove(curNode,
                    oldPositionX,
                    newPositionX);
        } else {
            cameraMoving(event);
        }
    }


    
    protected void elementMoving(GraphicElement element, MouseEvent event) {
		view.moveElementAtPx(element, event.getX(), event.getY());
	}

    
    private void onScroll(ScrollEvent e) {
        e.consume();
        double i = e.getDeltaY();
        double factor = Math.pow(1.02, i);
        Camera cam = view.getCamera();
        double zoom = cam.getViewPercent() * factor;
        Point2 pxCenter  = cam.transformGuToPx(cam.getViewCenter().x, cam.getViewCenter().y, 0);
        Point3 guClicked = cam.transformPxToGu(e.getX(), e.getY());
        double newRatioPx2Gu = cam.getMetrics().ratioPx2Gu/factor;
        double x = guClicked.x + (pxCenter.x - e.getX())/newRatioPx2Gu;
        double y = guClicked.y - (pxCenter.y - e.getY())/newRatioPx2Gu;
        cam.setViewCenter(x, y, 0);
        cam.setViewPercent(zoom);

    }

    public void selectNewNode(String nodeID) {
        logger.debug("button is released");
        if (individualClassMap.containsKey(nodeID)) {
            List<OWLClass> selection = individualClassMap.get(nodeID);
            controlPanel.refreshSelectedClasses(selection);
            if(previousNodeID.isEmpty()) {
                selectionSprite.setAttribute("ui.style","stroke-color:#000000;");
            }
            selectionSprite.attachToNode(nodeID);
            selectionSprite.setPosition(0,0,0);
            previousNodeID = nodeID;
            logger.debug("node is selected");
        }
    }
    public void setNodeSelectionEventSink(IGraphModelControlPanel controlPanel) {
        this.controlPanel = controlPanel;
    }

    private void createSelectionSprite() {
        SpriteManager sman = new SpriteManager(graph);
        selectionSprite = sman.addSprite("select");
        selectionSprite.setAttribute("ui.class","selection");
        selectionSprite.setAttribute("ui.style","fill-color:rgba(0,0,0,0);");
        selectionSprite.setAttribute("ui.style","stroke-color:rgba(0,0,0,0);");
        selectionSprite.setAttribute("ui.style","fill-mode:plain;");
        selectionSprite.setAttribute("ui.style","stroke-mode:plain;");
        selectionSprite.setAttribute("ui.style","size:30px;");
        selectionSprite.setPosition(0,0,0);
    }

    private void cameraMoving(MouseEvent event) {
        if(last!=null) {

            Point3 viewCenterGu = camera.getViewCenter();
            Point3 viewCenterPx=camera.transformGuToPx(viewCenterGu.x,viewCenterGu.y,0);
            double xdelta=event.getX()-last.getX();//determine direction
            double ydelta=event.getY()-last.getY();//determine direction
            logger.debug("dx:"+xdelta);
            logger.debug("dy:"+ydelta);

            viewCenterPx.x-=xdelta;
            viewCenterPx.y-=ydelta;
            Point3 newViewCenterGu =camera.transformPxToGu(viewCenterPx.x,viewCenterPx.y);
            camera.setViewCenter(newViewCenterGu.x,newViewCenterGu.y, 0);
        }
        last = event;
        logger.debug("new last: "+last.getX()+", "+last.getY());
    }

    @Override
    public EnumSet<InteractiveElement> getManagedTypes() {
       return interactiveEliments;
    }

}
