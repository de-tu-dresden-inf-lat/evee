/**
 * 
 */
package de.tu_dresden.inf.lat.proofs.data;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import de.tu_dresden.inf.lat.proofs.data.exceptions.ProofGenerationException;
import de.tu_dresden.inf.lat.proofs.interfaces.IInference;
import de.tu_dresden.inf.lat.proofs.interfaces.IProof;
import de.tu_dresden.inf.lat.proofs.interfaces.IProofGenerator;
import de.tu_dresden.inf.lat.proofs.interfaces.ISignatureBasedProofGenerator;
import de.tu_dresden.inf.lat.proofs.json.JsonProofParser;
import de.tu_dresden.inf.lat.proofs.json.JsonProofWriter;
import de.tu_dresden.inf.lat.proofs.proofGenerators.MinimalTreeProofGenerator;
import de.tu_dresden.inf.lat.proofs.proofGenerators.OWLSignatureBasedMinimalTreeProofGenerator;

/**
 * @author stefborg
 *
 */
public class ProofGeneratorMain {

	private static boolean Used = false;
	private static long start;

	public static void main(String[] args) throws Exception {

		Used = true;

		if ((args.length < 4) || (args.length > 5)) {
			System.err.println(
					"4-5 arguments expected: class name, input (task) file prefix, output (proof) file prefix, 'minimal'/'original', [optional: signature file]");
			return;
		}
		String className = args[0];
		String inputFilePrefix = args[1];
		String outputFilePrefix = args[2];
		boolean minimal = args[3].equals("minimal");

		IInference<OWLAxiom> task = loadTask(inputFilePrefix);

		Optional<Collection<OWLEntity>> signature = (args.length > 4) ? Optional.of(loadSignature(args[4], task))
				: Optional.empty();

		IProofGenerator<OWLAxiom, OWLOntology> generator = getGenerator(className, minimal, signature);

		try {
			start = System.nanoTime();
			IProof<OWLAxiom> proof = generateProof(generator, task);
			if (!minimal) {
				logTime("Finished");
			}
			writeProof(proof, outputFilePrefix);
		} catch (RuntimeException | AssertionError e) {
			e.printStackTrace();
			Path p = Paths.get(outputFilePrefix + JsonProofWriter.FILE_ENDING);
			Files.deleteIfExists(p);
			Files.createFile(p);
		}
	}

	public static void logTime(String message) {
		if (Used) {
			long current = System.nanoTime();
			double ms = (current - start) / 1e6;
			System.out.println(String.format("%.0f ms -- %s", ms, message));
		}
	}

	private static Collection<OWLEntity> loadSignature(String signatureFile, IInference<OWLAxiom> task) {

		try {

			Collection<String> signature = Files.readAllLines(Paths.get(signatureFile));
			Collection<OWLEntity> filteredSignature = new HashSet<>();

			for (OWLAxiom axiom : task.getPremises()) {
				filterSignature(axiom, signature, filteredSignature);
			}
			filterSignature(task.getConclusion(), signature, filteredSignature);

			return filteredSignature;

		} catch (IOException e) {
			System.err.println("Could not open signature file: " + signatureFile);
			e.printStackTrace();
			return null;
		}
	}

	private static void filterSignature(OWLAxiom axiom, Collection<String> signature,
			Collection<OWLEntity> filteredSignature) {
		axiom.getSignature().stream().filter(entity -> signature.contains(entity.getIRI().toString()))
				.forEach(filteredSignature::add);
	}

	private static IProofGenerator<OWLAxiom, OWLOntology> getGenerator(String className, boolean minimal,
			Optional<Collection<OWLEntity>> knownSignature) throws Exception {

		Class<?> generatorClass = Class.forName(className);
		Constructor<?> constructor = generatorClass.getConstructor();
		IProofGenerator<OWLAxiom, OWLOntology> generator = (IProofGenerator<OWLAxiom, OWLOntology>) constructor
				.newInstance();

		if (knownSignature.isPresent() && (generator instanceof ISignatureBasedProofGenerator)) {
			((ISignatureBasedProofGenerator<OWLEntity, OWLAxiom, OWLOntology>) generator)
					.setSignature(knownSignature.get());
		}

		if (minimal) {
			if (knownSignature.isPresent()) {

				OWLSignatureBasedMinimalTreeProofGenerator wrapper = new OWLSignatureBasedMinimalTreeProofGenerator(
						generator);
				wrapper.setSignature(knownSignature.get());
				return wrapper;

			} else {
				return new MinimalTreeProofGenerator<>(generator);
			}
		} else {
			return generator;
		}
	}

	private static IInference<OWLAxiom> loadTask(String filePrefix) throws IOException {
		return JsonProofParser.getInstance().fromFile(new File(filePrefix + JsonProofWriter.FILE_ENDING))
				.getInferences().get(0);
	}

	private static IProof<OWLAxiom> generateProof(IProofGenerator<OWLAxiom, OWLOntology> generator,
			IInference<OWLAxiom> task) throws OWLOntologyCreationException, ProofGenerationException {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.createOntology();
		manager.addAxioms(ontology, new HashSet<OWLAxiom>(task.getPremises()));
		generator.setOntology(ontology);

		OWLAxiom goal = task.getConclusion();

		if (generator.supportsProof(goal)) {
			return generator.getProof(goal);
		} else {
			throw new ProofGenerationException("Goal axiom is not supported by the proof generator.");
		}

	}

	private static void writeProof(IProof<OWLAxiom> proof, String filePrefix) throws IOException {
		JsonProofWriter.<OWLAxiom>getInstance().writeToFile(proof, filePrefix);
	}

}
