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

    public void registerNonEntailmentExplanationService(NonEntailmentExplanationService service, String serviceName){
        this.serviceMap.put(serviceName, service);
        if (this.currentNonEntailmentExplanationService == null){
            this.currentNonEntailmentExplanationService = service;
        }
    }

    public NonEntailmentExplanationService getCurrentExplainer(){
        return this.currentNonEntailmentExplanationService;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof JComboBox){
            JComboBox comboBox = (JComboBox) e.getSource();
            String serviceName = (String) comboBox.getSelectedItem();
            NonEntailmentExplanationService newService = this.serviceMap.get(serviceName);
            if (newService != null){
                this.currentNonEntailmentExplanationService = newService;
            }
        }
    }

    public Vector<String> getExplanationServiceNames(){
        return new Vector<>(this.serviceMap.keySet());
    }

    public void dispose(){
        for (String serviceName : this.serviceMap.keySet()){
            try {
                this.serviceMap.get(serviceName).dispose();
            } catch (Exception ex) {
                this.logger.error("Error when disposing Non-Entailment Explanation service named \"{}\"", serviceName);
                this.logger.error(ex.getMessage());
            }
        }
    }

}
