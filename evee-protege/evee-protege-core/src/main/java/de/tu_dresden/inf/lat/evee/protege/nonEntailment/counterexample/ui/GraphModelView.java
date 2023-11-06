package de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample.ui;

import de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample.listener.GraphViewMouseListener;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.counterexample.IGraphModelControlPanel;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.counterexample.IGraphView;
import org.graphstream.ui.view.View;

import java.awt.*;

public class GraphModelView implements IGraphView {

    private final GraphViewMouseListener mouseListener;
    private final View view;
    private final Component viewComponent;

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
        this.viewComponent = (Component) this.view;
        this.viewComponent.addMouseWheelListener(listener);
    }
    public GraphModelView(View view) {
        this.mouseListener = null;
        this.view = view;
        this.viewComponent = (Component) this.view;
    }

    @Override
    public void setControlPanel(IGraphModelControlPanel panel) {
        if(mouseListener != null) {
            mouseListener.setNodeSelectionEventSink(panel);
        }
    }

    @Override
    public Component toComponent() {
        return viewComponent;
    }
}
