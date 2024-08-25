package de.tu_dresden.inf.lat.model.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.expression.OWLEntityChecker;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.util.mansyntax.ManchesterOWLSyntaxParser;

import com.google.common.collect.Sets;

import de.tu_dresden.inf.lat.exceptions.EntityCheckerException;

/**
 * @author Christian Alrabbaa
 *
 */
public class ToOWLTools {

	private static final Logger logger = Logger.getLogger(ToOWLTools.class);

	private static final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	private static final OWLDataFactory factory = manager.getOWLDataFactory();

	private static OWLOntology emptyOntology;

	private ToOWLTools() {
		try {
			emptyOntology = manager.createOntology(new HashSet<>(), IRI.create("http://empty.owl"));
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
	}

	private static class LazyHolder {
		static ToOWLTools instance = new ToOWLTools();
	}

	public static ToOWLTools getInstance() {
		return LazyHolder.instance;
	}

//	public String getManFormat(OWLAxiom axiom) {
//
//		ManchesterOWLSyntaxOWLObjectRendererImpl rendering = new ManchesterOWLSyntaxOWLObjectRendererImpl();
//
//		return rendering.render(axiom);
//	}

	/**
	 * Get the equivalent OWl Axiom of the provided axiom (Manchester syntax)
	 * 
	 * @throws EntityCheckerException
	 */
	public OWLAxiom getOWLAxiomFromStr(String axiomStr, OWLOntology ontology) throws EntityCheckerException {

		OWLEntityChecker entityChecker = new EntityChecker(ontology);
		ManchesterOWLSyntaxParser parser = OWLManager.createManchesterParser();
		parser.setOWLEntityChecker(entityChecker);
		parser.setStringToParse(axiomStr);

		OWLAxiom res = null;

//		try {
		res = parser.parseAxiom();
//		} catch (Exception e) {
//			throw new EntityCheckerException();
//		}

		return res;
	}

	/**
	 * Parse the prided string as an OWLEntity
	 * 
	 * @param entityStr
	 * @param ontology
	 * @return
	 */
	public Optional<OWLEntity> getOWLEntityFromStr(String entityStr, OWLOntology ontology) {
		Optional<OWLEntity> res = ontology.getSignature().stream().filter(x -> x.getIRI().toString().equals(entityStr))
				.findFirst();
		return res;
	}

	/**
	 * Parse the provided file as set of OWLEntity
	 * 
	 * @param signatureFile
	 * @param ontology
	 * @return
	 */
	public Set<OWLEntity> getOWLEntityFromFile(File signatureFile, OWLOntology ontology) {
		Set<OWLEntity> res = new HashSet<>();
		FileInputStream in = null;
		try {
			in = new FileInputStream(signatureFile);
		} catch (FileNotFoundException e) {
			logger.error("Could not load " + signatureFile.getAbsolutePath(), e);
			e.printStackTrace();
		}

		assert in != null : "Something went wrong!";

		String line = "";
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		try {
			while ((line = br.readLine()) != null) {
				if (!line.isEmpty()) {
					Optional<OWLEntity> e = ToOWLTools.getInstance().getOWLEntityFromStr(line, ontology);
					if (e.isPresent())
						res.add(e.get());
					else if (line.equals(getOWLBot().toString()))
						res.add(getOWLBot());
					else if (line.equals(getOWLTop().toString()))
						res.add(getOWLTop());
				}
			}
		} catch (IOException e) {
			logger.error("Error while reading form " + signatureFile.getAbsolutePath() + "\nLast line was -> \"" + line
					+ "\"", e);
			e.printStackTrace();
		} finally {
			try {
				br.close();
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return res;
	}

	/**
	 * Get the equivalent OWl SubClassOf Axiom(s) of the provided axiom
	 */
	public Set<OWLSubClassOfAxiom> getAsSubClassOf(OWLAxiom generalAxiom) {

		if (generalAxiom.getAxiomType() == AxiomType.SUBCLASS_OF)
			return Sets.newHashSet((OWLSubClassOfAxiom) generalAxiom);

		if (generalAxiom.getAxiomType() == AxiomType.OBJECT_PROPERTY_DOMAIN)
			return Sets.newHashSet(((OWLObjectPropertyDomainAxiom) generalAxiom).asOWLSubClassOfAxiom());

		if (generalAxiom.getAxiomType() == AxiomType.OBJECT_PROPERTY_RANGE)
			return Sets.newHashSet(((OWLObjectPropertyRangeAxiom) generalAxiom).asOWLSubClassOfAxiom());

		if (generalAxiom.getAxiomType() == AxiomType.DISJOINT_CLASSES)
			return Sets.newHashSet(((OWLDisjointClassesAxiom) generalAxiom).asOWLSubClassOfAxioms());

		if (generalAxiom.getAxiomType() == AxiomType.EQUIVALENT_CLASSES)
			return Sets.newHashSet(((OWLEquivalentClassesAxiom) generalAxiom).asOWLSubClassOfAxioms());

//		if (generalAxiom.getAxiomType() == AxiomType.DECLARATION)
//			return Sets.newHashSet(((OWLDeclarationAxiom) generalAxiom)..asOWLSubClassOfAxioms());

		return new HashSet<>();
	}

	/**
	 * Get the equivalent OWl SubClassOf Axiom(s) of the provided axiom. If the
	 * axiom does not change, then return it
	 */
	public Set<? extends OWLAxiom> getSimplifiedAxiom(OWLAxiom generalAxiom) {

		Set<? extends OWLAxiom> res = getAsSubClassOf(generalAxiom);

		if (res.isEmpty()) {
			if (generalAxiom.getAxiomType() == AxiomType.CLASS_ASSERTION) {
				logger.debug("Axioms of type Class Assertion are not rewritten into equivalent SubClassOf Axiom(s)!");
				return Sets.newHashSet(generalAxiom);
			}
			if (generalAxiom.getAxiomType() == AxiomType.SUB_OBJECT_PROPERTY)
				return Sets.newHashSet(generalAxiom);

			logger.debug("Axioms of type " + generalAxiom.getAxiomType() + "are not supported yet!");
			return new HashSet<>();
		}
		return res;
	}

	/**
	 * Return a class of the provided string
	 * 
	 * @param string
	 * @return OWLClass
	 */
	public OWLClass getOWLConceptName(String string) {
		return factory.getOWLClass(IRI.create(string));
	}

	/**
	 * Return a property of the provided string
	 * 
	 * @param string
	 * @return OWLObjectProperty
	 */
	public OWLObjectProperty getPropertyName(String string) {
		return factory.getOWLObjectProperty(IRI.create(string));
	}

	/**
	 * Return an equivalence axiom of the provided class expressions
	 * 
	 * @param exp1
	 * @param exp2
	 * @return OWLEquivalentClassesAxiom
	 */
	public OWLEquivalentClassesAxiom getOWLEquivalenceAxiom(OWLClassExpression exp1, OWLClassExpression exp2) {
		return factory.getOWLEquivalentClassesAxiom(exp1, exp2);
	}

	/**
	 * Return an equivalence axiom of the provided Property expressions
	 * 
	 * @param exp1
	 * @param exp2
	 * @return OWLEquivalentClassesAxiom
	 */
	public OWLEquivalentObjectPropertiesAxiom getOWLEquivalenceAxiom(OWLObjectPropertyExpression exp1,
			OWLObjectPropertyExpression exp2) {
		return factory.getOWLEquivalentObjectPropertiesAxiom(exp1, exp2);
	}

	/**
	 * Return a sub-class-of axiom of the provided class expressions
	 * 
	 * @param subClass
	 * @param superClass
	 * @return OWLSubClassOfAxiom
	 */
	public OWLSubClassOfAxiom getOWLSubClassOfAxiom(OWLClassExpression subClass, OWLClassExpression superClass) {
		return factory.getOWLSubClassOfAxiom(subClass, superClass);
	}

	/**
	 * Return an OWL conjunction object
	 * 
	 * @param conjuncts
	 * @return
	 */
	public OWLClassExpression getOWLConjunction(Set<OWLClassExpression> conjuncts) {
		return factory.getOWLObjectIntersectionOf(conjuncts);
	}

	/**
	 * Return an OWL existential restriction
	 * 
	 * @param property
	 * @param concept
	 * @return
	 */
	public OWLClassExpression getOWLExistentialRestriction(OWLObjectPropertyExpression property,
			OWLClassExpression concept) {
		return factory.getOWLObjectSomeValuesFrom(property, concept);
	}

	/**
	 * Returns an owl:Thing object
	 * 
	 * @return
	 */
	public OWLClass getOWLTop() {
		return factory.getOWLThing();
	}

	/**
	 * Returns an owl:Nothing object
	 * 
	 * @return
	 */
	public OWLClass getOWLBot() {
		return factory.getOWLNothing();
	}

	/**
	 * Return an empty OWLOntology object
	 * 
	 * @return
	 * @throws OWLOntologyCreationException
	 */
	public OWLOntology getEmptyOntology() {
		return emptyOntology;
	}

	/**
	 * Returns owl:Nothing SubClassOf: owl:Thing
	 * 
	 * @return
	 */
	public OWLSubClassOfAxiom getBotIsTopCI() {
		return factory.getOWLSubClassOfAxiom(getOWLBot(), getOWLTop());
	}

	/**
	 * Add the provided axiom to the provided ontology
	 * 
	 * @param axiom
	 * @param ontology
	 * @throws OWLOntologyStorageException
	 */
	public void addAxiom(OWLAxiom axiom, OWLOntology ontology) throws OWLOntologyStorageException {
		manager.applyChange(new AddAxiom(ontology, axiom));
	}

	/**
	 * Remove the provided axiom to the provided ontology
	 * 
	 * @param axiom
	 * @param ontology
	 * @throws OWLOntologyStorageException
	 */
	public void removeAxiom(OWLAxiom axiom, OWLOntology ontology) throws OWLOntologyStorageException {
		manager.applyChange(new RemoveAxiom(ontology, axiom));
	}

	/**
	 * Shorten an IRI string
	 * 
	 * @param string
	 * @return
	 */
	public String getShortIRIString(String string) {

		if (string.trim().toLowerCase().equals("owl:thing"))
			return "\u22A4";

		if (string.trim().toLowerCase().equals("owl:nothing"))
			return "\u22A5";

		return string.substring(string.indexOf("#") + 1).replace("<", "").replace(">", "");
	}

	/**
	 * Return entities on the left hand side of an axiom
	 * 
	 * @param conclusion
	 * @return
	 */
//	public Set<OWLEntity> getLHS(OWLAxiom conclusion) {
//
//		// TODO: need to do something about complex class expressions (rename and add
//		// equivalences) but not in this function, here it should only return the
//		// concepts on the left hand side.
//		// Note: Also in case A & B, the LHS concepts are A, B and A & B
//		if (conclusion instanceof OWLSubClassOfAxiom)
//			return Sets.newHashSet(((OWLSubClassOfAxiom) conclusion).getSubClass().asConjunctSet().stream()
//					.map(OWLClass.class::cast).collect(Collectors.toSet()));
//		else
//			assert false : "Not implemented yet for axioms of type " + conclusion.getClass().getSimpleName();
//		return null;
//	}

	public OWLClassExpression getLHS(OWLAxiom conclusion) {
		if (conclusion.getAxiomType() == AxiomType.SUBCLASS_OF) {
			return ((OWLSubClassOfAxiom) conclusion).getSubClass();

		} else
			assert false : "Not implemented yet for axioms of type " + conclusion.getClass().getSimpleName();
		return null;
	}

	public OWLClassExpression getRHS(OWLAxiom conclusion) {
		if (conclusion.getAxiomType() == AxiomType.SUBCLASS_OF) {
			return ((OWLSubClassOfAxiom) conclusion).getSuperClass();
		} else
			assert false : "Not implemented yet for axioms of type " + conclusion.getClass().getSimpleName();
		return null;
	}

	/**
	 * Return a set of all proper sub-concepts of the provided concept expression
	 * 
	 * @param conjunct
	 * @return
	 */
	public Collection<? extends OWLClassExpression> getProperSubConcepts(OWLClassExpression conjunct) {

		Set<OWLClassExpression> res = new HashSet<>();

		getProperSubConcepts(conjunct, res);

		return res;
	}

	/**
	 * Recursively iterate over all Proper sub-concepts of the provided expression
	 * and store them in the provided list
	 * 
	 * @param expression
	 * @param expressions
	 */
	private void getProperSubConcepts(OWLClassExpression expression, Set<OWLClassExpression> expressions) {

		expression.asConjunctSet().forEach(conjunct -> {
			if (conjunct instanceof OWLObjectSomeValuesFrom) {
				getSubConcepts(((OWLObjectSomeValuesFrom) conjunct).getFiller(), expressions);
			}
		});
	}

	/**
	 * Return a set of all sub-concepts that appear in the input axiom
	 * @param axiom
	 * @return
	 */
	public Set<OWLClass> getConceptNamesInAxiom(OWLSubClassOfAxiom axiom){
		Set<OWLClassExpression> res = new HashSet<>();

		res.addAll(getSubConcepts(axiom.getSubClass()));
		res.addAll(getSubConcepts(axiom.getSuperClass()));

		return res.stream().filter(x->x instanceof OWLClass).map(OWLClass.class::cast).collect(Collectors.toSet());
	}

	/**
	 * Return a set of all sub-concepts of the provided concept expression
	 * 
	 * @param conjunct
	 * @return
	 */
	public Set<? extends OWLClassExpression> getSubConcepts(OWLClassExpression conjunct) {

		Set<OWLClassExpression> res = new HashSet<>();

		getSubConcepts(conjunct, res);

		return res;
	}

	/**
	 * Recursively iterate over all sub-concepts of the provided expression and
	 * store them in the provided list
	 * 
	 * @param expression
	 * @param expressions
	 */
	private void getSubConcepts(OWLClassExpression expression, Set<OWLClassExpression> expressions) {
		expressions.add(expression);

		expression.asConjunctSet().forEach(conjunct -> {

			if (conjunct instanceof OWLClass)
				expressions.add(conjunct);

			else if (conjunct instanceof OWLObjectSomeValuesFrom) {
				expressions.add(conjunct);
				getSubConcepts(((OWLObjectSomeValuesFrom) conjunct).getFiller(), expressions);
			}
		});
	}
}
