package de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample.ui;

import de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample.GraphViewGenerator;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample.listener.GraphViewMouseListener;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.counterexample.IGraphModelControlPanel;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.counterexample.IGraphView;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import org.apache.log4j.Logger;
import java.lang.ModuleLayer;

import org.graphstream.ui.view.View;

import java.awt.*;

public class GraphModelView implements IGraphView {

    private final Logger logger = Logger.getLogger(GraphViewGenerator.class);


    private final GraphViewMouseListener mouseListener;
    private final View view;

    public GraphViewMouseListener getMouseListener() {
        return mouseListener;
    }

    public View getView() {
        return view;
    }

    public GraphModelView(View view, GraphViewMouseListener listener) {
        this.mouseListener = listener;
        this.view = view;
        this.view.setMouseManager(listener);
        //this.viewComponent.addMouseWheelListener(listener); //TODO
    }
    
    public GraphModelView(View view) {
        this.mouseListener = null;
        this.view = view;
    }

    @Override
    public void setControlPanel(IGraphModelControlPanel panel) {
        if(mouseListener != null) {
            mouseListener.setNodeSelectionEventSink(panel);
        }
    }



    @Override
    public Component toComponent() {
        logger.warn(System.getProperty("java.vendor")); //debugLog
        logger.warn(System.getProperty("java.version"));//debugLog

        logger.warn(ModuleLayer.boot().findModule("jdk.unsupported.desktop"));//debugLog
        try {//debugLog
            logger.warn(ClassLoader.getSystemClassLoader().loadClass("jdk.swing.interop.SwingInterOpUtils"));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            logger.warn("Could not find class jdk.swing.interop.SwingInterOpUtils. This may cause issues with JavaFX integration.");
        }
        
        
        if (!(view instanceof Node)) {
            logger.warn("View is not a Node"); //debugLog
            return null;
        }

        JFXPanel panel = new JFXPanel();

        Platform.runLater(() -> {
            Scene scene = new Scene((Parent) view);
            panel.setScene(scene);
        });

        return panel;
    }
    
}
