package de.tu_dresden.inf.lat.evee.protege.abduction;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;
import java.util.Vector;

public class AbductionGeneratorManager implements ActionListener {

    private AbductionSolver<Set<OWLObject>, Set<OWLEntity>, OWLOntology, Set<Set<OWLAxiom>>> currentAbductionSolver;
    private final SillyAbductionSolver sillyAbductionGenerator;
    private final SlowAbductionSolver slowAbductionGenerator;
    private final String SILLY_NAME = "Silly Abduction Generator";
    private final String SLOW_NAME = "Slow Abduction Generator";
    private final Logger logger = LoggerFactory.getLogger(AbductionGeneratorManager.class);

    public AbductionGeneratorManager(){
        this.sillyAbductionGenerator = new SillyAbductionSolver();
        this.slowAbductionGenerator = new SlowAbductionSolver();
        this.currentAbductionSolver = this.sillyAbductionGenerator;
    }

    public AbductionSolver<Set<OWLObject>, Set<OWLEntity>, OWLOntology, Set<Set<OWLAxiom>>> getCurrentAbductionGenerator(){
        if (this.currentAbductionSolver.equals(this.sillyAbductionGenerator)){
        }
        else if (this.currentAbductionSolver.equals(this.slowAbductionGenerator)){
        }
        return this.currentAbductionSolver;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JComboBox comboBox = (JComboBox) e.getSource();
        Object selectedItem = comboBox.getSelectedItem();
        if (SILLY_NAME.equals(selectedItem)) {
            this.currentAbductionSolver = this.sillyAbductionGenerator;
        } else if (SLOW_NAME.equals(selectedItem)) {
            this.currentAbductionSolver = this.slowAbductionGenerator;
        }
    }

    public Set<Set<OWLAxiom>> generateAbductions(){
        return this.currentAbductionSolver.generateHypotheses();
    }

    public Vector<String> getAbductionGeneratorNames(){
        Vector<String> nameList = new Vector<>();
        nameList.add(this.SILLY_NAME);
        nameList.add(this.SLOW_NAME);
        return nameList;
    }

    public String getCurrentAbductionGeneratorName(){
        if (this.currentAbductionSolver.equals(this.sillyAbductionGenerator)) {
            return this.SILLY_NAME;
        }
        else if (this.currentAbductionSolver.equals(this.slowAbductionGenerator)){
            return this.SLOW_NAME;
        }
        else{
            return "";
        }
    }

}
