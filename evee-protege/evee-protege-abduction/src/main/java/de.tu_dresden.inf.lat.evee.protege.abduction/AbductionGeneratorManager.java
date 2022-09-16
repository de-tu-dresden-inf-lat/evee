package de.tu_dresden.inf.lat.evee.protege.abduction;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

public class AbductionGeneratorManager implements ActionListener {

    private AbductionGenerator<Set<OWLAxiom>, Set<OWLEntity>, OWLOntology, Set<Set<OWLAxiom>>> currentAbductionGenerator;
    private final SillyAbductionGenerator sillyAbductionGenerator;
    private final String SILLY_NAME = "Silly Abduction Generator";
    private final Logger logger = LoggerFactory.getLogger(AbductionGeneratorManager.class);

    public AbductionGeneratorManager(){
        this.sillyAbductionGenerator = new SillyAbductionGenerator();
        this.currentAbductionGenerator = this.sillyAbductionGenerator;
    }

    public AbductionGenerator<Set<OWLAxiom>, Set<OWLEntity>, OWLOntology, Set<Set<OWLAxiom>>> getCurrentAbductionGenerator(){
        return currentAbductionGenerator;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()){
            case SILLY_NAME:
                this.currentAbductionGenerator = this.sillyAbductionGenerator;
                break;
        }
    }

    public Set<Set<OWLAxiom>> generateAbductions(){
        return this.currentAbductionGenerator.generateAbductions();
    }

    public Vector<String> getAbductionGeneratorNames(){
        Vector<String> nameList = new Vector<>();
        nameList.add(this.SILLY_NAME);
        return nameList;
    }

}
