package de.tu_dresden.inf.lat.evee.protege.nonEntailment.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class NonEntailmentExplainerManager implements ActionListener {

    private NonEntailmentExplanationService currentNonEntailmentExplanationService = null;
    private final Map<String, NonEntailmentExplanationService> serviceMap;
    private final Logger logger = LoggerFactory.getLogger(NonEntailmentExplainerManager.class);

    public NonEntailmentExplainerManager(){
        this.serviceMap = new HashMap<>();
    }

    public void registerNonEntailmentExplanationService(NonEntailmentExplanationService service){
        this.serviceMap.put(service.getName(), service);
        if (this.currentNonEntailmentExplanationService == null){
            this.currentNonEntailmentExplanationService = service;
        }
    }

    public NonEntailmentExplanationService getCurrentExplainer(){
        return this.currentNonEntailmentExplanationService;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JComboBox comboBox = (JComboBox) e.getSource();
        Object selectedItem = comboBox.getSelectedItem();
        NonEntailmentExplanationService newService = this.serviceMap.get((String) selectedItem);
        if (newService != null){
            this.currentNonEntailmentExplanationService = newService;
        }
    }

    public Vector<String> getExplanationServiceNames(){
        return new Vector<>(this.serviceMap.keySet());
    }

}
