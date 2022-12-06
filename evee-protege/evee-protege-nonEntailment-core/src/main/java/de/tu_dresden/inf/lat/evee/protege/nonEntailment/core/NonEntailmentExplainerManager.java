package de.tu_dresden.inf.lat.evee.protege.nonEntailment.core;

import de.tu_dresden.inf.lat.evee.proofs.interfaces.OWLNonEntailmentExplainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

public class NonEntailmentExplainerManager implements ActionListener {

    private OWLNonEntailmentExplainer currentNonEntailmentExplainer;
//    private final LetheAbductionSolver letheAbductionSolver;
    private final String LETHE_ABDUCTION_SOLVER = "Abduction Solver (Lethe)";
    private final Logger logger = LoggerFactory.getLogger(NonEntailmentExplainerManager.class);

    public NonEntailmentExplainerManager(){
//        this.letheAbductionSolver = new LetheAbductionSolver();
//        this.currentNonEntailmentExplainer = this.letheAbductionSolver;
    }

    public OWLNonEntailmentExplainer getCurrentExplainer(){
        return this.currentNonEntailmentExplainer;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JComboBox comboBox = (JComboBox) e.getSource();
        Object selectedItem = comboBox.getSelectedItem();
        if (LETHE_ABDUCTION_SOLVER.equals(selectedItem)) {
//            this.currentNonEntailmentExplainer = this.letheAbductionSolver;
        }
    }

    public Vector<String> getAbductionGeneratorNames(){
        Vector<String> nameList = new Vector<>();
        nameList.add(this.LETHE_ABDUCTION_SOLVER);
        return nameList;
    }

    public String getCurrentAbductionGeneratorName(){
//        if (this.currentNonEntailmentExplainer.equals(this.letheAbductionSolver)) {
//            return this.LETHE_ABDUCTION_SOLVER;
//        }
        return "";
    }

}
