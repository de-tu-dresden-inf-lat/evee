package de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample.listener;

import de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample.util.EdgeLabelPositioner;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.counterexample.IGraphModelControlPanel;
import org.apache.log4j.Logger;
import org.graphstream.ui.geom.Point2;
import org.graphstream.ui.geom.Point3;
import org.graphstream.ui.graphicGraph.GraphicGraph;
import org.graphstream.ui.graphicGraph.GraphicNode;
import org.graphstream.ui.spriteManager.Sprite;
import org.graphstream.ui.spriteManager.SpriteManager;
import org.graphstream.ui.swing_viewer.util.DefaultMouseManager;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.camera.Camera;
import org.graphstream.ui.view.util.InteractiveElement;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObjectProperty;

import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

public class GraphViewMouseListener extends DefaultMouseManager implements MouseWheelListener {
    private long curTime;

    private Map<String, List<OWLClass>> individualClassMap;
    private Map<String[],List<OWLObjectProperty>> objectPropertyMap;
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

    public GraphViewMouseListener(Map<String, List<OWLClass>> classMap,
                                  Map<String[],List<OWLObjectProperty>> objectPropertyMap) {

        this.individualClassMap = classMap;
        this.objectPropertyMap = objectPropertyMap;
        this.isFirstClick = true;


    }
    @Override
    public void init(GraphicGraph graph, View view) {
        logger.info("init method of mouse manager is started");
        this.view = view;
        this.graph = graph;
        this.camera = view.getCamera();
        view.addListener("Mouse", this);
        view.addListener("MouseMotion", this);
        this.createSelectionSprite();
    }
    @Override
    public void release() {
        view.removeListener("Mouse", this);
        view.removeListener("MouseMotion", this);

    }
    @Override
    public void mousePressed(MouseEvent event) {
        curElement = view.findGraphicElementAt(interactiveEliments,event.getX(), event.getY());


        curTime = System.currentTimeMillis();
        if (curElement != null) {
                elementMoving= true;
                curNode = (GraphicNode) graph.getNode(curElement.getId());
        }
        if(isFirstClick) {
            this.isFirstClick = false;
        }
    }

    @Override
    public void mouseReleased(MouseEvent event) {
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
    @Override
    public void mouseDragged(MouseEvent event) {
        if(elementMoving) {
            double oldPositionX = curNode.getX();

            elementMoving(curElement, event);
            double newPositionX = curNode.getX();
            EdgeLabelPositioner.positionLabelsOnNodeMove(curNode,
                    oldPositionX,
                    newPositionX);
//
        } else {
            cameraMoving(event);
        }

    }
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
//                JOptionPane.showMessageDialog(new JPanel(), "Please select at least 2 OWLCLasses");
        e.consume();
        int i = e.getWheelRotation();
        double factor = Math.pow(1.25, i);
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
            int xdelta=event.getX()-last.getX();//determine direction
            int ydelta=event.getY()-last.getY();//determine direction
            logger.debug("dx:"+xdelta);
            logger.debug("dy:"+xdelta);
            viewCenterPx.x-=xdelta;
            viewCenterPx.y-=ydelta;
            Point3 newViewCenterGu =camera.transformPxToGu(viewCenterPx.x,viewCenterPx.y);
            camera.setViewCenter(newViewCenterGu.x,newViewCenterGu.y, 0);
        }
        last = event;
        logger.debug("new last: "+last.getX()+", "+last.getY());
    }
}
