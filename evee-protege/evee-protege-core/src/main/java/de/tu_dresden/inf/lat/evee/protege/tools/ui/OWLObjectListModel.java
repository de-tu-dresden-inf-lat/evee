package de.tu_dresden.inf.lat.evee.protege.tools.ui;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.*;

public class OWLObjectListModel<O extends OWLObject>
        extends AbstractListModel<O>{

    private final List<O> owlObjects;
    private final OWLEditorKit owlEditorKit;
    private final ListElementRenderingChangeListener renderingChangeListener;
    private final Logger logger = LoggerFactory.getLogger(OWLObjectListModel.class);

    public OWLObjectListModel(OWLEditorKit owlEditorKit){
        this.owlObjects = new ArrayList<>();
        this.owlEditorKit = owlEditorKit;
        this.renderingChangeListener = new ListElementRenderingChangeListener();
        this.owlEditorKit.getOWLModelManager().addListener(this.renderingChangeListener);
    }

    public void dispose(){
        this.owlEditorKit.getOWLModelManager().removeListener(this.renderingChangeListener);
    }

    @Override
    public int getSize() {
        return this.owlObjects.size();
    }

    @Override
    public O getElementAt(int index) {
        return this.owlObjects.get(index);
    }

    public List<O> getOwlObjects(){
        return new ArrayList<>(this.owlObjects);
    }

    private void sort(){
        this.owlObjects.sort(new ListElementComparator());
        this.fireContentsChanged(this, 0, this.owlObjects.size() -1);
    }

    public void removeElements(Collection<? extends O> deletedEntities){
        this.owlObjects.removeAll(deletedEntities);
        this.fireContentsChanged(this, 0, this.owlObjects.size() -1);
    }

    public void addElement(O newEntity){
        this.owlObjects.add(newEntity);
        this.sort();
    }

    public void addElements(Collection<? extends O> newEntities){
        this.owlObjects.addAll(newEntities);
        this.sort();
    }

    public void checkAndAddElements(Collection<? extends O> newEntities){
        newEntities.stream().filter(
                entity -> (! this.owlObjects.contains(entity))).forEach(
                        this.owlObjects::add);
        this.sort();
    }

    public void checkAndAddElement(O newEntity){
        if (! this.owlObjects.contains(newEntity)){
            this.owlObjects.add(newEntity);
            this.sort();
        }
    }

    public void removeAll(){
        this.owlObjects.clear();
        this.fireContentsChanged(this, 0, 0);
    }

    private class ListElementComparator implements Comparator<O>{

        @Override
        public int compare(O elem1, O elem2) {
            if ((elem1 instanceof OWLClass && elem2 instanceof  OWLClass) ||
                    (elem1 instanceof OWLObjectProperty && elem2 instanceof OWLObjectProperty) ||
                    (elem1 instanceof OWLIndividual && elem2 instanceof OWLIndividual) ||
                    (elem1 instanceof OWLAxiom && elem2 instanceof OWLAxiom)){
                String elem1String = owlEditorKit.getOWLModelManager().getRendering(elem1);
                String elem2String = owlEditorKit.getOWLModelManager().getRendering(elem2);
                return elem1String.compareTo(elem2String);
            } else{
                return elem1.compareTo(elem2);
            }
        }
    }

    private class ListElementRenderingChangeListener implements OWLModelManagerListener{

        @Override
        public void handleChange(OWLModelManagerChangeEvent owlModelManagerChangeEvent) {
            if (owlModelManagerChangeEvent.isType(EventType.ENTITY_RENDERER_CHANGED) ||
            owlModelManagerChangeEvent.isType(EventType.ENTITY_RENDERING_CHANGED)){
                sort();
            }
        }

    }

}
