//import de.tu_dresden.inf.lat.evee.protege.nonEntailment.core.LetheAbductionSolver;
//import org.junit.Assert;
//import org.junit.Test;
//import org.semanticweb.owlapi.apibinding.OWLManager;
//import org.semanticweb.owlapi.model.*;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.File;
//import java.util.HashSet;
//import java.util.Set;
//import java.util.stream.Stream;
//
//import static org.junit.Assert.assertEquals;
//
//public class LetheAbductionSolverTests {
//
//    private Logger logger = LoggerFactory.getLogger(LetheAbductionSolverTests.class);
//
//    public LetheAbductionSolverTests(){
//    }
//
//    @Test
//    public void noInfiniteLoopTest(){
//        String basePath = new File("").getAbsolutePath();
//        File ebolaOntologyFile = new File(basePath,
//                "src/test/resources/EbolaExampleOntology.owl");
//        OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
//        try {
//            OWLOntology ebolaOntology = ontologyManager.loadOntologyFromOntologyDocument(ebolaOntologyFile);
//            OWLDataFactory dataFactory = ebolaOntology.getOWLOntologyManager().getOWLDataFactory();
//            OWLClass ebolaPatient = null;
//            OWLClass ebolaBat = null;
//            OWLObjectProperty infected = null;
//            OWLObjectProperty contactWith = null;
//            OWLNamedIndividual p2 = null;
//            for(OWLEntity entity : ebolaOntology.getSignature()){
//                String iriRemainder = entity.getIRI().getRemainder().get();
//                switch (iriRemainder){
//                    case "EbolaPatient" :
//                        ebolaPatient = (OWLClass) entity;
//                        break;
//                    case "EbolaBat" :
//                        ebolaBat = (OWLClass) entity;
//                        break;
//                    case "infected" :
//                        infected = (OWLObjectProperty) entity;
//                        break;
//                    case "contactWith" :
//                        contactWith = (OWLObjectProperty) entity;
//                        break;
//                    case "p2" :
//                        p2 = (OWLNamedIndividual) entity;
//                        break;
//                }
//            }
//            assert (ebolaPatient != null);
//            assert (ebolaBat != null);
//            assert (infected != null);
//            assert (contactWith != null);
//            assert (p2 != null);
//            Set<OWLAxiom> observation = new HashSet<>();
//            observation.add(dataFactory.getOWLClassAssertionAxiom(ebolaPatient, p2));
//            Set<OWLEntity> abducibles = new HashSet<>();
//            abducibles.add(ebolaBat);
//            abducibles.add(contactWith);
//            abducibles.add(infected);
//            LetheAbductionSolver abductionSolver = new LetheAbductionSolver();
//            abductionSolver.setAbducibles(abducibles);
//            abductionSolver.setObservation(observation);
//            abductionSolver.setOntology(ebolaOntology);
//            Stream<Set<OWLAxiom>> resultStream = abductionSolver.generateHypotheses();
//            Set<Set<OWLAxiom>> results = new HashSet<>();
//            resultStream.limit(10).forEach(result -> {
//                if (result != null){
//                    results.add(result);
//                }
//            });
//            System.out.println("Asserting that 10 results were generated.");
//            Assert.assertEquals(10, results.size());
//        } catch (OWLOntologyCreationException e) {
//            e.printStackTrace();
//        }
//    }
//
//
//}
