package de.tu_dresden.inf.lat.evee.proofs;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import de.tu_dresden.inf.lat.prettyPrinting.parsing.OWLParser;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProof;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProofGenerator;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.ISignatureBasedProofGenerator;
import de.tu_dresden.inf.lat.evee.proofs.json.JsonProofWriter;

public class GenerateProof {
	public static void main(String[] args) throws Exception {

		if ((args.length < 5)) {
			System.err.println(
					"5-6 arguments expected: proof-generator-class ontology-file axiom-string output-file-directory output-file-prefix [signature-file]");
			return;
		}
		String className = args[0];
		System.out.println("Proof generator class: " + className);

		String ontology_file = args[1];
		System.out.println("Ontology file: " + ontology_file);

		String axiom_string = args[2];
		System.out.println("Axiom to prove: " + axiom_string);

		String output_file_directory = args[3];
		System.out.println("Output directory: " + output_file_directory);

		String output_file_prefix = args[4];
		System.out.println("Proof prefix: " + output_file_prefix);

		Optional<String> signature_file = Optional.empty();
		if (args.length > 5) {
			signature_file = Optional.of(args[5]);
			System.out.println("Signature: " + signature_file);
		}

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File(ontology_file));

		Optional<Set<OWLEntity>> signature = Optional.empty();
		if (signature_file.isPresent())
			signature = signature_file.map(s -> loadSignature(s, ontology, manager.getOWLDataFactory()));

		IProofGenerator<OWLAxiom, OWLOntology> generator = getGenerator(className, signature);

		OWLAxiom axiom = (new OWLParser()).parse(axiom_string);

		generator.setOntology(ontology);

		if (!generator.supportsProof(axiom)) {
			System.out.println("Proofs of " + SimpleOWLFormatter.format(axiom)+" are not supported by "+ generator.class.getName());
			System.exit(3);
		}

		IProof<OWLAxiom> proof = generator.getProof(axiom);

		Files.createDirectories(Paths.get(output_file_directory));

		(new JsonProofWriter<OWLAxiom>()).writeToFile(proof,
				output_file_directory + File.separator + output_file_prefix);
	}

	private static Set<OWLEntity> loadSignature(String signatureFile, OWLOntology ontology, OWLDataFactory factory) {
		try {
			Collection<String> signature = Files.readAllLines(Paths.get(signatureFile));

			Set<OWLClass> classes = ontology.getClassesInSignature();
			Set<OWLObjectProperty> properties = ontology.getObjectPropertiesInSignature();

			Set<OWLEntity> result = new HashSet<>();

			signature.forEach(s -> {
				IRI iri = IRI.create(s);
				OWLClass cl = factory.getOWLClass(iri);
				OWLObjectProperty pr = factory.getOWLObjectProperty(iri);
				if (classes.contains(cl))
					result.add(cl);
				if (properties.contains(pr))
					result.add(pr);
			});

			return result;
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
			return null;
		}
	}

	private static IProofGenerator<OWLAxiom, OWLOntology> getGenerator(String className,
			Optional<Set<OWLEntity>> knownSignature) throws Exception {
		Class<?> generatorClass = Class.forName(className);
		Constructor<?> constructor = generatorClass.getConstructor();
		IProofGenerator<OWLAxiom, OWLOntology> generator = (IProofGenerator<OWLAxiom, OWLOntology>) constructor
				.newInstance();

		if (knownSignature.isPresent() && (generator instanceof ISignatureBasedProofGenerator)) {
			((ISignatureBasedProofGenerator<OWLEntity, OWLAxiom, OWLOntology>) generator)
					.setSignature(knownSignature.get());
		}
		return generator;
	}

}
