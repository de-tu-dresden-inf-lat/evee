package de.tu_dresden.inf.lat.evee.proofs;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;

import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;

import de.tu_dresden.inf.lat.evee.proofs.data.Inference;
import de.tu_dresden.inf.lat.evee.proofs.data.Proof;
import de.tu_dresden.inf.lat.evee.proofs.data.exceptions.FormattingException;
import de.tu_dresden.inf.lat.evee.proofs.data.exceptions.ParsingException;
import de.tu_dresden.inf.lat.evee.proofs.data.exceptions.ProofGenerationFailedException;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProof;
import de.tu_dresden.inf.lat.evee.proofs.json.JsonProofParser;
import de.tu_dresden.inf.lat.evee.proofs.json.JsonProofWriter;
import de.tu_dresden.inf.lat.evee.proofs.json.JsonStringProofParser;
import de.tu_dresden.inf.lat.evee.proofs.tools.MinimalProofExtractor;
import de.tu_dresden.inf.lat.evee.proofs.tools.measures.TreeSizeMeasure;

/**
 * @author Christian Alrabbaa
 *
 */
public class JsonProofReadingAndWritingTests {

	private static final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	private static final OWLDataFactory factory = manager.getOWLDataFactory();

	@Test
	public void jsonProofWriterTest1()
			throws JsonGenerationException, JsonMappingException, IOException, FormattingException, ParsingException {

		OWLClass a = factory.getOWLClass(IRI.create("A"));
		OWLClass b = factory.getOWLClass(IRI.create("B"));
		OWLClass c = factory.getOWLClass(IRI.create("C"));
		OWLClass d = factory.getOWLClass(IRI.create("D"));

		OWLObjectProperty r = factory.getOWLObjectProperty(IRI.create("r"));

		OWLObjectSomeValuesFrom existsRB = factory.getOWLObjectSomeValuesFrom(r, b);
		OWLObjectSomeValuesFrom existsRC = factory.getOWLObjectSomeValuesFrom(r, c);

		OWLSubClassOfAxiom aInExistsrB = factory.getOWLSubClassOfAxiom(a, existsRB);
		OWLSubClassOfAxiom bInC = factory.getOWLSubClassOfAxiom(b, c);
		OWLSubClassOfAxiom dInA = factory.getOWLSubClassOfAxiom(d, a);

		Inference prem1 = new Inference(aInExistsrB, "Asserted", new LinkedList<>());
		Inference prem2 = new Inference(bInC, "Asserted", new LinkedList<>());
		Inference prem3 = new Inference(dInA, "Asserted", new LinkedList<>());

		OWLAxiom con1 = factory.getOWLSubClassOfAxiom(a, existsRC);
		OWLAxiom con2 = factory.getOWLSubClassOfAxiom(d, existsRC);

		Inference inner = new Inference(con1, "rule 1", Arrays.asList(aInExistsrB, bInC));
		Inference outer = new Inference(con2, "rule 2", Arrays.asList(dInA, con1));

		IProof<OWLAxiom> proof = new Proof<OWLAxiom>(con2);
		proof.addInference(outer);
		proof.addInference(prem3);
		proof.addInference(inner);
		proof.addInference(prem1);
		proof.addInference(prem2);

		JsonProofWriter writer = JsonProofWriter.getInstance();

		String formatted = writer.toString(proof);

		System.out.println(formatted);

		JsonProofParser parser = new JsonProofParser();

		IProof<OWLAxiom> parsed = parser.parseProof(formatted);

		assertEquals(proof, parsed);
	}

	@Test
	public void jsonStringProofReadWriteTest()
			throws ParsingException, FormattingException, IOException, ProofGenerationFailedException {
		Inference<String> inf1 = new Inference<>("Socrates is mortal.", "Modus Ponens",
				Arrays.asList("Socrates is a man.", "All men are mortal."));
		Inference<String> inf2 = new Inference<>("I am mortal.", "Modus Ponens",
				Arrays.asList("I am Socrates.", "Socrates is mortal."));
		Inference<String> inf3 = new Inference<>("Socrates is a man.", "Assumption", Collections.emptyList());
		Inference<String> inf4 = new Inference<>("All men are mortal.", "Assumption", Collections.emptyList());
		Inference<String> inf5 = new Inference<>("I am Socrates.", "Assumption", Collections.emptyList());

		IProof<String> proof = new Proof<>("I am mortal.", Arrays.asList(inf1, inf2, inf3, inf4, inf5));

		// "minimize" the proof by removing uneccessary edges
		proof = new MinimalProofExtractor<String>(new TreeSizeMeasure<>()).extract(proof);

		JsonProofWriter<String> proofWriter = JsonProofWriter.getInstance();

		proofWriter.writeToFile(proof, "socrates-proof"); // save proof in file "socrates-proof.json"

		String jsonString = proofWriter.toString(proof);

		System.out.println(jsonString);

		JsonStringProofParser proofParser = JsonStringProofParser.getInstance();

		IProof<String> parsedProof = proofParser.parseProof(jsonString);

		assertEquals(parsedProof, proof);
	}
}
