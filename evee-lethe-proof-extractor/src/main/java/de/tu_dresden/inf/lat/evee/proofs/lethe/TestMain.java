package de.tu_dresden.inf.lat.evee.proofs.lethe;

import de.tu_dresden.inf.lat.evee.general.data.exceptions.FormattingException;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProof;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.semanticweb.owlapi.model.OWLAxiom;


public class TestMain {
    public static void main(String[] args) {
        findFails(Paths.get("../evaluation-data/alc-tasks"));
    }

    private static void runTestingTask(Path taskPath) {
        System.out.println("Running testTask " + taskPath.getFileName());
        try {
            ProofUtils.showProof(TestTasksRunner.runTask(taskPath.toString()));
        } catch (FormattingException | IOException e) {
            e.printStackTrace();
        }
    }

    private static void runTestingTasks(Path testingDirPath) {
        try (Stream<Path> filesStream = Files.list(testingDirPath)) {
            filesStream.sorted()
                       .filter(path -> path.toString().endsWith(".json"))
                       .forEach(path -> runTestingTask(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean proofExists(Path taskPath) {
        IProof<OWLAxiom> proof = TestTasksRunner.runTask(taskPath.toString());
        if (proof != null) {
            System.out.println(taskPath.getFileName() + " SUCCESS\n");
            return true;
        } else {
            System.out.println(taskPath.getFileName() + " FAIL\n");
            return false;
        }
    }

    private static void findFails(Path testingDirPath) {
        List<String> fails = new ArrayList<>();
        
        try (Stream<Path> filesStream = Files.list(testingDirPath)) {
            filesStream.sorted()
                       .filter(path -> path.toString().endsWith(".json"))
                       .forEach(path -> {
                           if (!proofExists(path)) fails.add(path.toString());
                        });
            System.out.println("Fails: ");
            fails.forEach(path -> System.out.println(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
