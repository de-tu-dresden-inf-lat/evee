package de.tu_dresden.inf.lat.evee.protege.abduction;

import de.tu_dresden.inf.lat.evee.proofs.interfaces.OWLAbductionSolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

public class AbductionGeneratorManager implements ActionListener {

    private AbstractAbductionSolver currentAbductionSolver;
    private final LetheAbductionSolver letheAbductionSolver;
    private final String LETHE_ABDUCTION_SOLVER = "Abduction Solver (Lethe)";
    private final Logger logger = LoggerFactory.getLogger(AbductionGeneratorManager.class);

    public AbductionGeneratorManager(){
        this.letheAbductionSolver = new LetheAbductionSolver();
        this.currentAbductionSolver = this.letheAbductionSolver;
    }

    public AbstractAbductionSolver getCurrentAbductionGenerator(){
        return this.currentAbductionSolver;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JComboBox comboBox = (JComboBox) e.getSource();
        Object selectedItem = comboBox.getSelectedItem();
        if (LETHE_ABDUCTION_SOLVER.equals(selectedItem)) {
            this.currentAbductionSolver = this.letheAbductionSolver;
        }
    }

    public Vector<String> getAbductionGeneratorNames(){
        Vector<String> nameList = new Vector<>();
        nameList.add(this.LETHE_ABDUCTION_SOLVER);
        return nameList;
    }

    public String getCurrentAbductionGeneratorName(){
        if (this.currentAbductionSolver.equals(this.letheAbductionSolver)) {
            return this.LETHE_ABDUCTION_SOLVER;
        }
        else{
            return "";
        }
    }

}
