package de.tu_dresden.inf.lat.evee.protege.tools.ui;

import org.semanticweb.owlapi.model.OWLObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class OWLObjectListModel<O extends OWLObject>
        extends AbstractListModel<O>{

    private final List<O> owlObjects;
    private final Logger logger = LoggerFactory.getLogger(OWLObjectListModel.class);

    public OWLObjectListModel(){
        this.owlObjects = new ArrayList<>();
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
        Collections.sort(this.owlObjects);
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

}
