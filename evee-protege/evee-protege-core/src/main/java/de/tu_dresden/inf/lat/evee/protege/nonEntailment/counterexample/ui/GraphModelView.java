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
import java.util.concurrent.CountDownLatch;

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
        if (!(view instanceof Node)) {
            return null;
        }

        JFXPanel panel = new JFXPanel();

        Platform.runLater(() -> {
            panel.setScene(new Scene((Parent) view));
            logger.info("graph scene set");
        });

        return panel;
    }
    
}
