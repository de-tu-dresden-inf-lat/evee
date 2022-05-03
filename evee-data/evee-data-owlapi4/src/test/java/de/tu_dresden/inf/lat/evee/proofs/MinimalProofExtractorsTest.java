package de.tu_dresden.inf.lat.evee.proofs;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
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
import de.tu_dresden.inf.lat.evee.proofs.json.JsonProofWriter;
import de.tu_dresden.inf.lat.evee.proofs.tools.MinimalHypergraphProofExtractor;
import de.tu_dresden.inf.lat.evee.proofs.tools.MinimalProofExtractor;
import de.tu_dresden.inf.lat.evee.proofs.tools.OWLSignatureBasedMinimalProofExtractor;
import de.tu_dresden.inf.lat.evee.proofs.tools.measures.TreeSizeMeasure;

/**
 * @author Stefan Borgwardt
 *
 */
public class MinimalProofExtractorsTest {

	private static final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	private static final OWLDataFactory factory = manager.getOWLDataFactory();

	@Test
	public void MinimalProofExtractorsTest1() throws JsonGenerationException, JsonMappingException, IOException,
			FormattingException, ParsingException, ProofGenerationFailedException {

		OWLClass a = factory.getOWLClass(IRI.create("A"));
		OWLClass b = factory.getOWLClass(IRI.create("B"));
		OWLClass c = factory.getOWLClass(IRI.create("C"));
		OWLClassExpression aAndB = factory.getOWLObjectIntersectionOf(a, b);

		OWLSubClassOfAxiom aInB = factory.getOWLSubClassOfAxiom(a, b);
		OWLSubClassOfAxiom bInA = factory.getOWLSubClassOfAxiom(b, a);
		OWLSubClassOfAxiom bInC = factory.getOWLSubClassOfAxiom(b, c);
		OWLSubClassOfAxiom cInB = factory.getOWLSubClassOfAxiom(c, b);
		OWLSubClassOfAxiom cInA = factory.getOWLSubClassOfAxiom(c, a);

		Inference prem1 = new Inference(aInB, "Asserted", new LinkedList<>());
		Inference prem2 = new Inference(bInA, "Asserted", new LinkedList<>());
		Inference prem3 = new Inference(bInC, "Asserted", new LinkedList<>());
		Inference prem4 = new Inference(cInB, "Asserted", new LinkedList<>());
		Inference prem5 = new Inference(cInA, "Asserted", new LinkedList<>());

		OWLAxiom aInA = factory.getOWLSubClassOfAxiom(a, a);
		OWLAxiom bInB = factory.getOWLSubClassOfAxiom(b, b);
		OWLAxiom concl = factory.getOWLSubClassOfAxiom(aAndB, aAndB);

		Inference inf1 = new Inference(concl, "z", Arrays.asList(aInA, bInB));
		Inference inf2 = new Inference(aInA, "x1", Arrays.asList(aInB, bInA));
		Inference inf3 = new Inference(aInA, "x2", Arrays.asList(aInB, bInC, cInA));
		Inference inf4 = new Inference(bInB, "y1", Arrays.asList(bInC, cInB));
		Inference inf5 = new Inference(bInB, "y2", Arrays.asList(bInC, cInA, aInB));

		IProof<OWLAxiom> proof = new Proof<OWLAxiom>(concl);
		proof.addInferences(Arrays.asList(prem1, prem2, prem3, prem4, prem5, inf1, inf2, inf3, inf4, inf5));

		IProof<OWLAxiom> minHyp = MinimalHypergraphProofExtractor.makeUnique(proof);
		IProof<OWLAxiom> minTree = new MinimalProofExtractor<OWLAxiom>(new TreeSizeMeasure<OWLAxiom>()).extract(proof);
		Set<OWLEntity> knownSignature = new HashSet<OWLEntity>();
		knownSignature.add(b);
		IProof<OWLAxiom> minKnownTree = new OWLSignatureBasedMinimalProofExtractor(new TreeSizeMeasure<OWLAxiom>())
				.extract(proof, knownSignature);

		JsonProofWriter writer = JsonProofWriter.getInstance();
		System.out.println("HYP:\n" + writer.toString(minHyp));
		System.out.println("TREE:\n" + writer.toString(minTree));
		System.out.println("KNOWN-TREE:\n" + writer.toString(minKnownTree));

		assertEquals(6, minHyp.getNumberOfAxioms());
		assertEquals(7, minTree.getNumberOfAxioms());
		assertEquals(5, minKnownTree.getNumberOfAxioms());
	}

}
