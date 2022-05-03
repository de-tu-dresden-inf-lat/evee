package de.tu_dresden.inf.lat.evee.proofs.lethe;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInference;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProof;
import de.tu_dresden.inf.lat.evee.proofs.json.JsonProofParser;
import de.tu_dresden.inf.lat.evee.proofs.data.exceptions.ProofGenerationCancelledException;

public class TestTasksRunner {

    public static IProof<OWLAxiom>  runTask(String taskPath) {
        JsonProofParser parser = new JsonProofParser();
        IProof<OWLAxiom> inputProof = parser.fromFile(new File(taskPath));

        OWLAxiom conclusion = inputProof.getFinalConclusion();
        List<IInference<OWLAxiom>> inferences = inputProof.getInferences();

        assert inferences.size() == 1;
        assert inferences.get(0).getConclusion().equals(conclusion);
        
        List<? extends OWLAxiom> premises = inferences.get(0).getPremises();

        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        LetheProofGenerator proofGenerator = new LetheProofGenerator();
        OWLOntology ontology;
        try {
            ontology = man.createOntology();
            man.addAxioms(ontology, new HashSet<>(premises));

            proofGenerator.setOntology(ontology);
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
            return null;
        }

        if (conclusion instanceof OWLSubClassOfAxiom) {
            OWLSubClassOfAxiom conclusionAxiom = (OWLSubClassOfAxiom) conclusion;

            assert (conclusionAxiom.getSubClass() instanceof OWLClass);
            assert (conclusionAxiom.getSuperClass() instanceof OWLClass);
            try{
                return proofGenerator.proveSubsumption((OWLClass) conclusionAxiom.getSubClass(),
                        (OWLClass) conclusionAxiom.getSuperClass());
            }
            catch (ProofGenerationCancelledException e){
                System.out.println(e);
                return null;
            }
        } else if (conclusion instanceof OWLEquivalentClassesAxiom && ((OWLEquivalentClassesAxiom) conclusion).getClassExpressions().size() == 2) {
            OWLEquivalentClassesAxiom conclusionAxiom = (OWLEquivalentClassesAxiom) conclusion;
            List<OWLClassExpression> classes = new ArrayList<>(conclusionAxiom.getClassExpressions());
            try{
                return proofGenerator.proveEquivalence((OWLClass) classes.get(0), (OWLClass) classes.get(1));
            }
            catch (ProofGenerationCancelledException e){
                System.out.println(e);
                return null;
            }
        } else {
            System.out.println("Not implemented yet!");
            return null;
        }
    }
}
