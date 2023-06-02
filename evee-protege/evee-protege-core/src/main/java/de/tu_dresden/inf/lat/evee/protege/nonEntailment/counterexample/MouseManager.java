package de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample;

import org.apache.log4j.Logger;
import org.graphstream.ui.geom.Point2;
import org.graphstream.ui.geom.Point3;
import org.graphstream.ui.graphicGraph.GraphicGraph;
import org.graphstream.ui.spriteManager.Sprite;
import org.graphstream.ui.spriteManager.SpriteManager;
import org.graphstream.ui.swing_viewer.util.DefaultMouseManager;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.camera.Camera;
import org.graphstream.ui.view.util.InteractiveElement;
import org.semanticweb.owlapi.model.OWLClass;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.*;
import java.util.stream.Collectors;

public class MouseManager extends DefaultMouseManager implements MouseWheelListener {
    private long curTime;
    private Set<Sprite> classLabels;
    private DefaultListModel<OWLClass> classListModel;
    private Map<String, List<OWLClass>> classMap;
    private final EnumSet<InteractiveElement> interactiveEliments = EnumSet.of(InteractiveElement.NODE);
    private final Logger logger = Logger.getLogger(MouseManager.class);
    private String previousNodeID ="";
    private Sprite selectionSprite;
    private Map<Sprite,Point2> labelCoordinates;
    private Camera camera;
    private MouseEvent last;
    private Viewer viewer;
    private boolean isFirstClick;
    public MouseManager(Set<Sprite> classLabels,
                        Map<String, List<OWLClass>> classMap,
                        DefaultListModel<OWLClass> classListModel,
                        Viewer viewer) {
        this.viewer = viewer;
        this.classLabels = classLabels;
        this.classMap = classMap;
        this.classListModel = classListModel;
        this.labelCoordinates = new HashMap<>();
        this.labelCoordinates = classLabels.stream()
                .collect(Collectors.toMap(l ->l, l-> new Point2(l.getX(),l.getY())));
        this.isFirstClick = true;
    }
    @Override
    public void init(GraphicGraph graph, View view) {
        logger.debug("init method is started");
        this.view = view;
        this.graph = graph;
        this.camera = view.getCamera();

        this.createSelectionSprite();
        view.addListener("Mouse", this);
        view.addListener("MouseMotion", this);

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
//        logger.debug("mouse is pressed");
        if(isFirstClick) {
            viewer.disableAutoLayout();
            this.isFirstClick = false;
        }
    }

    @Override
    public void mouseReleased(MouseEvent event) {
        logger.debug("mouse released");
        last = null;
        if (curElement != null) {
            if(System.currentTimeMillis()-curTime <300) {
                selectNewNode(curElement.getId());
            }
            curElement = null;

        }

    }
    @Override
    public void mouseDragged(MouseEvent event) {
        if (curElement != null) {
            elementMoving(curElement, event);
//            double zoom = camera.getViewPercent();
////            logger.debug("current zoom is "+zoom);
//            classLabels.forEach(l -> adjustLabelPosition(l,zoom));
        } else {
            if(last!=null) {

                Point3 viewCenterGu = camera.getViewCenter();
                logger.debug("center Gu: "+viewCenterGu);
                Point3 viewCenterPx=camera.transformGuToPx(viewCenterGu.x,viewCenterGu.y,0);
                logger.debug("center Px: "+viewCenterPx);
                logger.debug("current mouse position:"+event.getX()+", "+event.getY());
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
        classLabels.forEach(l -> adjustLabelPosition(l,zoom));
    }
    public void selectNewNode(String nodeID) {
        logger.debug("button is released");
        if (classMap.containsKey(nodeID)) {

            List<OWLClass> classList = classMap.get(nodeID);
            classListModel.removeAllElements();
            for (OWLClass cl : classList) {
                classListModel.addElement(cl);
            }
            if(previousNodeID.isEmpty()) {
                selectionSprite.setAttribute("ui.style","stroke-color:#000000;");
            }
            selectionSprite.attachToNode(nodeID);
            selectionSprite.setPosition(0,0,0);
            previousNodeID = nodeID;
            logger.debug("node is selected");
        }
    }
    private void adjustLabelPosition(Sprite label, double zoom) {
        double X = labelCoordinates.get(label).x*zoom;
        double Y = labelCoordinates.get(label).y*zoom;
        label.setPosition(X,Y,0);
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
}
