package de.tu_dresden.inf.lat.evee.proofs.lethe;

import com.clarkparsia.owlapi.explanation.util.SilentExplanationProgressMonitor;
import de.tu_dresden.inf.lat.dltools.ALCHTBoxFilter;
import de.tu_dresden.inf.lat.evee.general.tools.OWLOntologyFilterTool;
import de.tu_dresden.inf.lat.prettyPrinting.formatting.SimpleOWLFormatter;
import de.tu_dresden.inf.lat.evee.proofs.data.AbstractSimpleOWLProofGenerator;
import de.tu_dresden.inf.lat.evee.proofs.data.Inference;
import de.tu_dresden.inf.lat.evee.proofs.data.Proof;
import de.tu_dresden.inf.lat.evee.proofs.data.exceptions.ReasonerNotSupportedException;
import de.tu_dresden.inf.lat.evee.proofs.data.exceptions.ProofGenerationException;
import de.tu_dresden.inf.lat.evee.proofs.data.exceptions.ProofGenerationCancelledException;
import de.tu_dresden.inf.lat.evee.proofs.data.exceptions.ProofGenerationFailedException;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInference;
import de.tu_dresden.inf.lat.evee.general.interfaces.IProgressTracker;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProof;

import de.tu_dresden.inf.lat.evee.general.tools.OWLTools;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.HermiT.Reasoner.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.collection.JavaConverters;

import uk.ac.man.cs.lethe.internal.dl.datatypes.*;
import uk.ac.man.cs.lethe.internal.dl.forgetting.direct.AbstractDerivation;
import uk.ac.man.cs.lethe.internal.dl.forgetting.direct.ConceptClause;
import uk.ac.man.cs.lethe.internal.dl.forgetting.direct.ConceptLiteral;
import uk.ac.man.cs.lethe.internal.dl.forgetting.direct.DefinerFactory;
import uk.ac.man.cs.lethe.internal.dl.owlapi.OWLExporter;
import uk.ac.man.cs.lethe.internal.dl.proofs.InferenceLogger$;
import uk.ac.man.cs.lethe.interpolation.ShKnowledgeBaseInterpolator;

import java.util.*;

import com.clarkparsia.owlapi.explanation.DefaultExplanationGenerator;


/**
 * @author Alexej Popovic
 *
 */
public class LetheProofGenerator extends AbstractSimpleOWLProofGenerator {

    private static String ASSERTED_RULE = "asserted";
    private static String TAUTOLOGY_RULE = "tautology";
    private static String EQUIVALENCE_RULE = "definition of equivalence";
    private static String UNKNOWN_RULE = "???";
    private static String NORMALIZE_RULE = "normalize";
    private static String CONCEPT_RESOLUTION_RULE = "class resolution";
    private static String ROLE_RESOLUTION_RULE = "\"some\" elimination";
    private static String ROLE_PROPAGATION_RULE = "property propagation";
    private static String SUBSUMPTION_WEAKENING_RULE = "subsumption weakening";
    private static String ROLE_MONOTONICITY_RULE = "property resolution";
    private static String ROLE_HIERARCHY = "property hierarchy";


    private static final String LETHE_RESOLUTION_RULE = "ResolutionRule";
    private static final String LETHE_ROLE_RESOLUTION_RULE = "RoleResolutionRule";
    private static final String LETHE_ROLE_PROPAGATION = "SimpleRolePropagationRule";
    private static final String LETHE_MONOTONICITY = "MonotonicityRule";

    private static Logger logger = LoggerFactory.getLogger(LetheProofGenerator.class);

    private OWLOntology ontology;
    private OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
    private OWLDataFactory dataFactory = ontologyManager.getOWLDataFactory();
    LetheProgressBarAdapter letheProgressBar = new LetheProgressBarAdapter();

    private ShKnowledgeBaseInterpolator interpolator = null;
    private boolean computationRunning = false;
    private boolean cancelled = false;

    boolean inferTautologiesFromInput = false;

    private final OWLOntologyFilterTool filterTool = new OWLOntologyFilterTool(new OWLOntologyFilterTool.ALCHFilter());

    @Override
    public void setOntology(OWLOntology owlOntology) {
        filterTool.setOntology(owlOntology);
        ontology = filterTool.filterOntology();
//        ontology = ALCHTBoxFilter.filteredCopy(owlOntology, ontologyManager);
    }

    public OWLOntology getOntology() {
        return ontology;
    }

    private void assertOntologySet() {
        if (ontology == null) {
            throw new Error("Ontology not set.");
        }
    }

    @Override
    public void setReasoner(OWLReasoner owlReasoner) throws ReasonerNotSupportedException {
        throw new ReasonerNotSupportedException("Setting reasoner not supported by LETHE proof generator.");
    }

    @Override
    public boolean supportsProof(OWLAxiom axiom) {
        return super.supportsProof(axiom) && entailed(axiom);
    }

    private boolean entailed(OWLAxiom axiom) {
        OWLReasoner reasoner = new ReasonerFactory().createReasoner(ontology);
        return reasoner.isEntailed(axiom);
    }

    @Override
    public IProof<OWLAxiom> proveSubsumption(OWLClass owlClass, OWLClass owlClass1) throws ProofGenerationException {
        computationStart();
        // blocking call - cancel() might get called in the meantime
        //OWLOntology interpolant = findInterpolant(interpolator, owlClass, owlClass1);
        OWLAxiom targetAxiom = dataFactory.getOWLSubClassOfAxiom(owlClass,owlClass1);
        OWLOntology currentJustification = computeCurrentJustification(targetAxiom);
        OWLOntology interpolant = findInterpolant(interpolator, currentJustification, targetAxiom);


        if (cancelled) {
            computationEnd();
            throw new ProofGenerationCancelledException("Proof generation cancelled");
        }

        logger.info("Subsumption to be proven: "+owlClass+ " subsumed by "+owlClass1);

        if (subsumptionCanBeFound(interpolant, owlClass, owlClass1)) {
            logger.debug("Subsumption holds.");

            // This computation might take some time, check for cancellation again...
            List<IInference<OWLAxiom>> inferences = extractInferences(currentJustification, interpolator);

            if (cancelled) {
                throw new ProofGenerationCancelledException("Proof generation cancelled");
            }

            inferences.addAll(getSubsumptionWeakeningInferences(interpolant, owlClass, owlClass1));

            Proof<OWLAxiom> result =  new Proof<>(targetAxiom, inferences);
            addMissingInferences(currentJustification, result);

            if (cancelled) {
                throw new ProofGenerationCancelledException("Proof generation cancelled");
            }
            
            return result;
        }
        logger.debug("Subsumption doesn't hold.");

        throw new ProofGenerationFailedException("Proof not supported");
    }

    @Override
    public IProof<OWLAxiom> proveEquivalence(OWLClass owlClassA, OWLClass owlClassB) throws ProofGenerationException {
        computationStart();

        // blocking call - cancel() might get called in the meantime
        //OWLOntology interpolant = findInterpolant(interpolator, owlClassA, owlClassB);
        OWLAxiom targetAxiom = dataFactory.getOWLEquivalentClassesAxiom(owlClassA,owlClassB);
        OWLOntology currentJustification = computeCurrentJustification(targetAxiom);
        OWLOntology interpolant = findInterpolant(interpolator, currentJustification, targetAxiom);

        if (cancelled) {
            computationEnd();
            throw new ProofGenerationCancelledException("Proof generation cancelled");
        }

        logger.trace("interpolant:");
        logger.trace(""+interpolant.getLogicalAxioms(Imports.INCLUDED));

        logger.debug("subsumption "+owlClassA+" by "+owlClassB+": "+ subsumptionCanBeFound(interpolant, owlClassA, owlClassB));
        logger.debug("subsumption "+owlClassB+" by "+owlClassA+": "+ subsumptionCanBeFound(interpolant, owlClassB, owlClassA));

        if (subsumptionCanBeFound(interpolant, owlClassA, owlClassB) && subsumptionCanBeFound(interpolant, owlClassB, owlClassA)) {
            System.out.println("Equivalence holds.");

            // This computation might take some time, check for cancellation again...
            List<IInference<OWLAxiom>> inferences = extractInferences(currentJustification, interpolator);

            if (cancelled) {
                logger.debug("Execution canceled");
                throw new ProofGenerationCancelledException("Proof generation cancelled");
            }

            inferences.addAll(getSubsumptionWeakeningInferences(interpolant, owlClassA, owlClassB));
            inferences.addAll(getSubsumptionWeakeningInferences(interpolant, owlClassB, owlClassA));

            inferences.add(new Inference<>(targetAxiom,
                EQUIVALENCE_RULE,
                Arrays.asList(dataFactory.getOWLSubClassOfAxiom(owlClassA, owlClassB),
                              dataFactory.getOWLSubClassOfAxiom(owlClassB, owlClassA))
            ));

            Proof<OWLAxiom> result =  new Proof<>(targetAxiom, inferences);

            addMissingInferences(currentJustification, result); // adds tautologies that are not always derived by LETHE, such as BOTTOM <= A

            if (cancelled) {
                logger.debug("computation canceled");
                throw new ProofGenerationCancelledException("Proof generation cancelled");
            }

            return result;
        }
        System.out.println("Equivalence doesn't hold.");

        throw new ProofGenerationFailedException("Proof not supported");
    }

    @Override
    public void addProgressTracker(IProgressTracker tracker) {
        letheProgressBar.addProgressTracker(tracker);
    }

    @Override
    public void cancel() {
        logger.debug("canceling execution");
        if (computationRunning) {
            logger.debug("canceling forgetter");
            interpolator.forgetter().cancel();
        }
        cancelled = true;
    }

    @Override
    public boolean successful() {
        return !cancelled;
    }

    private void computationStart() {
        cancelled = false;
        assertOntologySet();
        initializeInterpolator();
        computationRunning = true;
    }
    
    private void computationEnd() {
        interpolator = null;
        cancelled = false;
        computationRunning = false;
    }

    public void addMissingInferences(OWLOntology currentJustification, IProof<OWLAxiom> proof) {
        Set<IInference<OWLAxiom>> missing = new HashSet<>();
        for(IInference<OWLAxiom> inference: proof.getInferences()){
            List<OWLAxiom> axioms = new LinkedList<>(inference.getPremises());
            axioms.add(proof.getFinalConclusion());
            for(OWLAxiom premise: axioms){
                if(cancelled)
                    return;
                if(!proof.hasInferenceFor(premise)) {
                    logger.trace(""+inference);
                    if(currentJustification.containsAxiom(premise))
                        missing.add(new Inference<>(premise, ASSERTED_RULE, Collections.emptyList()));
                    else if(premise instanceof OWLSubPropertyAxiom){
                        List<OWLAxiom> premises = new LinkedList<>(justify(currentJustification, premise).getAxioms());
                        missing.add(new Inference<>(premise, ROLE_HIERARCHY, premises));
                    }
                    else {
      //                  assert isTautology(premise, ontology.getOWLOntologyManager().getOWLDataFactory()) :
      //                          "inference missed: " + SimpleOWLFormatter.format(premise);
                        if(isTautology(premise, dataFactory))
                        missing.add(new Inference<>(premise, TAUTOLOGY_RULE, Collections.emptyList()));
                    }
                }
            }
        }
        proof.addInferences(missing);
        assert proof.hasInferenceFor(proof.getFinalConclusion()) : "final conclusion not proved!";
    }

    /**
     * Set the value of the flag controlling the source of tautology checking.
     * True = original input ontology is used
     * False = an empty ontology is used
     */
    public void setInferTautologiesFromInput(boolean flagVal) {
        this.inferTautologiesFromInput = flagVal;
    }

    /**
     * Create and initialize the interpolator to be able to extract inferences from it.
     */
    private void initializeInterpolator() {
        interpolator = new ShKnowledgeBaseInterpolator();
        //ABoxSingleRoleForgetter.alwaysPropagate_$eq(true);
        //interpolator.useUniversalRoles(true);
        interpolator.forgetter().progressBar_$eq(letheProgressBar);
        //interpolator.forgetter().deactivateProgressBar();
        interpolator.forgetter().noFiltration_$eq(true);
        interpolator.forgetter().clausifyOnce_$eq(true);
        interpolator.forgetter().conceptsFirst_$eq(true);
        interpolator.forgetter().inferenceLogger().clear();
    }
    
    private OWLOntology computeCurrentJustification(OWLAxiom axiom) {
        assertOntologySet();
        logger.trace("Ontology:");
        logger.trace(SimpleOWLFormatter.format(ontology));
        OWLOntology currentJustification = justify(ontology, axiom);
        logger.trace("Justification:");
        logger.trace(SimpleOWLFormatter.format(currentJustification));
        return currentJustification;
    }

    /**
     * Use LETHE's uniform interpolant algorithm to find the new ontology.
     */
    private OWLOntology findInterpolant(ShKnowledgeBaseInterpolator interpolator, OWLOntology currentJustification, OWLAxiom axiom) {
        Set<OWLEntity> targetSignature = axiom.getSignature();
        return interpolator.uniformInterpolant(currentJustification, targetSignature);
    }

    private OWLOntology justify(OWLOntology ontology, OWLAxiom axiom) {

        Set<OWLAxiom> explanation;

        if(!(axiom instanceof OWLSubObjectPropertyOfAxiom)) {

            // ensure we find some justification even if name does not appear in ontology
            axiom.getClassesInSignature().forEach(cl ->
                    ontologyManager.addAxiom(ontology,dataFactory.getOWLSubClassOfAxiom(cl, dataFactory.getOWLThing())));


            DefaultExplanationGenerator explanationGenerator =
                    new DefaultExplanationGenerator(
                            ontologyManager,
                            new ReasonerFactory(),
                            ontology,
                            new SilentExplanationProgressMonitor()
                    );

            explanation = explanationGenerator.getExplanation(axiom);


            // those axioms are not needed afterwards, so we can safely remove them, even if they were in the input
            axiom.getClassesInSignature().forEach(cl ->
                    ontologyManager.removeAxiom(ontology,dataFactory.getOWLSubClassOfAxiom(cl, dataFactory.getOWLThing())));


        } else {
            // property inclusions not supported and need special treatment
            OWLSubObjectPropertyOfAxiom pi = (OWLSubObjectPropertyOfAxiom) axiom;
            OWLClassExpression ce = dataFactory.getOWLObjectIntersectionOf(
                    dataFactory.getOWLObjectSomeValuesFrom(pi.getSubProperty(),
                            dataFactory.getOWLThing()),
                    dataFactory.getOWLObjectAllValuesFrom(pi.getSuperProperty(),
                            dataFactory.getOWLNothing())
            );
            OWLOntology rbox = OWLTools.createOntology(ontology.getRBoxAxioms(Imports.INCLUDED));

            DefaultExplanationGenerator explanationGenerator =
                    new DefaultExplanationGenerator(
                            ontologyManager,
                            new ReasonerFactory(),
                            rbox,
                            new SilentExplanationProgressMonitor()
                    );

            explanation = explanationGenerator.getExplanation(ce);
        }


        try {
            return ontologyManager.createOntology(explanation);
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
            return ontology;
        }
    }

    private DefinerFactory definerFactory;

    /**
     * Extract inferences collected during interpolation of the input ontology.
     */
    private List<IInference<OWLAxiom>> extractInferences(OWLOntology currentJustification, ShKnowledgeBaseInterpolator interpolator) {
        assertOntologySet();
        List<IInference<OWLAxiom>> inferences = new ArrayList<>();

        InferenceLogger$ logger = interpolator.forgetter().inferenceLogger();

        // Add input TBox axioms
        inferences.addAll(getTBoxInputInferences(currentJustification));

        if(logger.derivations().isEmpty()) {
            // nothing needed to be derived for the conclusion
            // we then also have no definer factory, but we still may need to add the input inferences linking
            // the tbox input to the representation in LETHE, and potentially the conclusion
            inferences.addAll(getLETHEInputInferences(currentJustification, logger, Optional.empty()));
            return inferences;
        } else {

            // get definer factory from the logger
            Set<DefinerFactory> definerFactories = JavaConverters.setAsJavaSet(logger.definerFactories());
            definerFactory = definerFactories.iterator().next();

            // merge all the other definer factories into one
            // here, we are only interested in the definerBases
            for (DefinerFactory alternativeDefinerFactory : definerFactories) {
                definerFactory.addDefinerBases(alternativeDefinerFactory);
            }

            DefinerTranslatingVisitor translatingVisitor = new DefinerTranslatingVisitor(currentJustification,
                    definerFactory);


            // Add input normalized axioms (used by LETHE)
            inferences.addAll(getLETHEInputInferences(currentJustification, logger, Optional.of(translatingVisitor)));

            // Add derivations created by LETHE
            inferences.addAll(getLETHEDerivationInferences(currentJustification, logger, translatingVisitor));

            return inferences;
        }
    }


    /**
     * Get the inferences from the input TBox (axioms are considered facts, without any premises).
     */
    private Set<Inference<OWLAxiom>> getTBoxInputInferences(OWLOntology currentJustification) {
        if(cancelled)
            return Collections.EMPTY_SET;
        assertOntologySet();
        Set<Inference<OWLAxiom>> inferences = new HashSet<Inference<OWLAxiom>>();
        currentJustification.getTBoxAxioms(Imports.EXCLUDED).forEach(
            axiom -> inferences.add(new Inference<>(axiom, ASSERTED_RULE, new ArrayList<>()))
        );
        return inferences;
    }

    /**
     * Get the inferences expressing normalization done by LETHE.
     */
    private Set<Inference<OWLAxiom>> getLETHEInputInferences(OWLOntology currentJustification, InferenceLogger$ logger,
                                                             Optional<DefinerTranslatingVisitor> translatingVisitor) {
        assertOntologySet();
        Set<Inference<OWLAxiom>> inferences = new HashSet<Inference<OWLAxiom>>();
        ReasonerFactory rf = new Reasoner.ReasonerFactory();
        OWLReasoner reasoner = rf.createReasoner(currentJustification);
        OWLReasoner tautologyReasoner = inferTautologiesFromInput ? reasoner : rf.createReasoner(getEmptyOntology(currentJustification));

        DefaultExplanationGenerator generator =
                new DefaultExplanationGenerator(ontologyManager, rf, currentJustification, reasoner, null);

        for (OWLAxiom letheInputAxiom : letheClausesToOwlAxioms(currentJustification, JavaConverters.setAsJavaSet(logger.inputClauses()))) {

            if (cancelled) {
                return inferences;
            }

            OWLAxiom inputAxiomWOutDefiners;
            if(translatingVisitor.isPresent())
                inputAxiomWOutDefiners = removeDefiners(dataFactory, translatingVisitor.get(), letheInputAxiom);
            else
                inputAxiomWOutDefiners = letheInputAxiom;

            List<OWLAxiom> explanation = new ArrayList<>();
            Set<OWLAxiom> explanationSet = generator.getExplanation(inputAxiomWOutDefiners);

            String ruleName;
            if (tautologyReasoner.isEntailed(inputAxiomWOutDefiners)) {
                ruleName = TAUTOLOGY_RULE;
            } else {
                if (explanationSet.size() > 0) {
                    ruleName = NORMALIZE_RULE;
                } else {
                    ruleName = UNKNOWN_RULE;
                }
                explanation.addAll(explanationSet);
            }

            if (isTranslatedInferenceValid(explanation, inputAxiomWOutDefiners)) {
                inferences.add(new Inference<>(inputAxiomWOutDefiners, ruleName, explanation));
            }
        }

        return inferences;
    }

    public boolean isTautology(OWLAxiom axiom, OWLDataFactory factory) {
        OWLOntology ontology = getEmptyOntology(axiom, factory);
        OWLReasoner reasoner = new Reasoner.ReasonerFactory().createReasoner(ontology);

        return reasoner.isEntailed(axiom);
    }

    /**
     * Get the inferences expressing derivation steps done by LETHE.
     */
    private Set<Inference<OWLAxiom>> getLETHEDerivationInferences(OWLOntology currentJustification, InferenceLogger$ inflogger, DefinerTranslatingVisitor translatingVisitor) {
        Set<Inference<OWLAxiom>> inferences = new HashSet<Inference<OWLAxiom>>();

        for (AbstractDerivation derivation : JavaConverters.seqAsJavaList(inflogger.derivations())) {

            if (cancelled) {
                return inferences;
            }

            // System.out.println("Processing derivation "+derivation);

            Set<Expression> premises = JavaConverters.setAsJavaSet(derivation.premisses().toSet());
            Set<Expression> conclusions = JavaConverters.setAsJavaSet(derivation.conclusions().toSet());

            logger.trace("premises: "+premises);
            logger.trace("conclusions: "+conclusions);

            if (!isRawInferenceAcceptable(premises, conclusions)) {
                continue;
            }

            List<OWLAxiom> inferencePremisesAxioms = new ArrayList<>();

            for (Expression premise : premises) {
                if(cancelled)
                    return inferences;

                OWLAxiom premiseAxiom = (OWLAxiom) letheClauseToOwlAxiom(currentJustification, premise);
                logger.trace("premise "+premise+" becomes "+SimpleOWLFormatter.format(premiseAxiom));
                if(derivation.getRuleName().equals(LETHE_ROLE_RESOLUTION_RULE) && premise instanceof ConceptClause) {

                    Set<Concept> definers = new HashSet<>();
                    Collection<ConceptLiteral> literals = JavaConverters.asJavaCollection(((ConceptClause) premise).literals());
                    for(ConceptLiteral literal: literals){
                        if(literal.polarity()==false){
                            //BaseConcept concept = (BaseConcept) literal.concept(); // only base concept can have negative polarity
                            //if(definerFactory.definerBases().contains(concept))
                                definers.add(literal.concept());
                        }
                    }
                    logger.trace("literals: ${literals}");
                    logger.trace("definers: ${definers}");
                    if(definers.size()==literals.size()){
                        inferences.addAll(createDefinerRules(currentJustification, definers, translatingVisitor));
                    }
                }
                inferencePremisesAxioms.add(removeDefiners(dataFactory, translatingVisitor, premiseAxiom));
                logger.trace(" becomes "+SimpleOWLFormatter.format(removeDefiners(dataFactory, translatingVisitor, premiseAxiom)));
            }

            if(derivation.getRuleName().equals(LETHE_RESOLUTION_RULE) || derivation.getRuleName().equals(LETHE_MONOTONICITY))
                Collections.reverse(inferencePremisesAxioms); // looks nicer in most cases, I think

            OWLAxiom conclusionAxiom = (OWLSubClassOfAxiom) letheClauseToOwlAxiom(currentJustification, conclusions.iterator().next());

            if (!isTranslatedInferenceValid(inferencePremisesAxioms, conclusionAxiom)) {
                continue;
            }

            logger.trace("inference rule translated:");
            logger.trace(""+new Inference<>(removeDefiners(dataFactory, translatingVisitor, conclusionAxiom),
                    translateRuleName(derivation.getRuleName()),
                    inferencePremisesAxioms));

            inferences.add(new Inference<>(removeDefiners(dataFactory, translatingVisitor, conclusionAxiom),
                                           translateRuleName(derivation.getRuleName()),
                                           inferencePremisesAxioms));
        }

        return inferences;
    }

    private Set<Inference<OWLAxiom>> createDefinerRules(OWLOntology currentJustification, Set<Concept> definers, DefinerTranslatingVisitor translatingVisitor) {
        Set<Inference<OWLAxiom>> result = new HashSet<>();
        Queue<Set<Concept>> toProcess = new LinkedList<>();
        Set<Set<Concept>> all = new HashSet<>();
        OWLAxiom conclusion = unsatAxiom(currentJustification,definers,translatingVisitor);

        toProcess.add(definers);
        all.add(definers);
        while(!toProcess.isEmpty()){
            Set<Concept> next = toProcess.poll();
            if(next.size()>1)
            for (Concept definer : next) {
                if(cancelled)
                    return result;

                Set<Concept> without = new HashSet<>(next);
                without.remove(definer);
                result.add(new Inference<OWLAxiom>(conclusion,SUBSUMPTION_WEAKENING_RULE,
                        Arrays.asList(unsatAxiom(currentJustification,without,translatingVisitor))));
                if(!all.contains(without)) {
                    toProcess.add(without);
                    all.add(without);
                }
            }
        }
        return result;
    }

    private OWLAxiom unsatAxiom(OWLOntology currentJustification, Set<Concept> definers, DefinerTranslatingVisitor translatingVisitor) {
        return removeDefiners(dataFactory,translatingVisitor,
                dataFactory.getOWLSubClassOfAxiom(
                        owlExporter.toOwl(currentJustification, new ConceptConjunction(JavaConverters.asScalaSet(definers).<Concept>toSet())),
                        dataFactory.getOWLNothing()));
    }

    private static String translateRuleName(String ruleName) {
        switch(ruleName) {
            case LETHE_RESOLUTION_RULE:  return CONCEPT_RESOLUTION_RULE;
            case LETHE_ROLE_RESOLUTION_RULE:  return ROLE_RESOLUTION_RULE;
            case LETHE_ROLE_PROPAGATION: return ROLE_PROPAGATION_RULE;
            case LETHE_MONOTONICITY: return ROLE_MONOTONICITY_RULE;
            default : return ruleName;
        }
    }

    /**
     * Get inferences of subsumption that must hold because of subsumption weakening
     */
    private Set<Inference<OWLAxiom>> getSubsumptionWeakeningInferences(OWLOntology ontology, OWLClass classA, OWLClass classB) {
        Set<Inference<OWLAxiom>> inferences = new HashSet<Inference<OWLAxiom>>();
        OWLAxiom resultingSubsumption = dataFactory.getOWLSubClassOfAxiom(classA, classB);

        List<OWLAxiom> subsumptionWeakeningCases = Arrays.asList(
            dataFactory.getOWLSubClassOfAxiom(classA, dataFactory.getOWLNothing()),
            dataFactory.getOWLSubClassOfAxiom(dataFactory.getOWLThing(), classB),
            dataFactory.getOWLSubClassOfAxiom(dataFactory.getOWLThing(), dataFactory.getOWLNothing())
        );

        for (OWLAxiom subsumptionWeakeningCase : subsumptionWeakeningCases) {
            if(cancelled)
                return inferences;

            if (ontology.containsAxiom(subsumptionWeakeningCase)
                    && !subsumptionWeakeningCase.equals(resultingSubsumption)) {
                inferences.add(new Inference<>(
                    resultingSubsumption,
                    SUBSUMPTION_WEAKENING_RULE,
                    Arrays.asList(subsumptionWeakeningCase)
                ));
            }
        }

        return inferences;
    }

    /**
     * Helper function to get empty ontology containing only the declaration axioms from a base ontology.
     */
    private static OWLOntology getEmptyOntology(OWLOntology baseOntology) {
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        Set<OWLAxiom> declarations = new HashSet<>();

        Set<OWLEntity> entities = baseOntology.getSignature();
        entities.forEach(entity -> declarations.addAll(baseOntology.getDeclarationAxioms(entity)));
        
        try {
            OWLOntology newOntology = man.createOntology(declarations);
            return newOntology;
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
            System.exit(1); // TODO proper handling of exception
            return null;
        }
        
    }

    /**
     * Helper function to get empty ontology containing only the declaration axioms from a base ontology.
     */
    private static OWLOntology getEmptyOntology(OWLAxiom axiom, OWLDataFactory factory) {
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        Set<OWLAxiom> declarations = new HashSet<>();

        Set<OWLEntity> entities = axiom.getSignature();
        entities.forEach(entity -> declarations.add(factory.getOWLDeclarationAxiom(entity)));

        try {
            OWLOntology newOntology = man.createOntology(declarations);
            return newOntology;
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
            System.exit(1); // TODO proper handling of exception
            return null;
        }

    }
    /**
     * Translate definers back to the concepts they represent.
     */
    private static OWLAxiom removeDefiners(OWLDataFactory df, DefinerTranslatingVisitor translatingVisitor, OWLAxiom inputAxiom) {
        // System.out.println("Removing definers from \"" + SimpleOWLFormatter.format(inputAxiom) + "\"");

        OWLAxiom result = inputAxiom;
        // OWLSubClassOfAxiom oldAxiom = null;

        // repeat operation in case new definers get introduced
        //        while(result==null || result!=oldAxiom) {
        //    oldAxiom = result;
        if(inputAxiom instanceof OWLSubClassOfAxiom) {
            OWLSubClassOfAxiom inputGCI = (OWLSubClassOfAxiom)inputAxiom;
            result = df.getOWLSubClassOfAxiom(
                    inputGCI.getSubClass().accept(translatingVisitor),
                    inputGCI.getSuperClass().accept(translatingVisitor)
            );
        } else
            assert inputAxiom instanceof OWLSubPropertyAxiom;
        //}
        // System.out.println("Result: \"" + SimpleOWLFormatter.format(result) + "\"");
        return result;
    }

    OWLExporter owlExporter = new OWLExporter();
    /**
     * Convert the clause form that LETHE uses to a subsumption axiom.
     * (negated atoms from the disjunction on the LHS, the rest on the RHS)
     */
    private OWLAxiom letheClauseToOwlAxiom(OWLOntology currentJustification, Expression exp) {

        if(exp instanceof RoleSubsumption) {
            return owlExporter.toOwl(currentJustification,(RoleAxiom) exp);
        } else {

            assert exp instanceof ConceptClause;
            ConceptClause letheClause = (ConceptClause) exp;

            assertOntologySet();

            Subsumption newSubsumption = new Subsumption(TopConcept$.MODULE$, letheClause.convertBack());
            Subsumption beautifiedSubsumptionAxiom = (Subsumption) OntologyBeautifier.nice(newSubsumption);

            return (OWLSubClassOfAxiom) owlExporter.toOwl(currentJustification, beautifiedSubsumptionAxiom);
        }
    }

    /**
     * Convert a set of LETHE clauses to axioms.
     */
    private Set<OWLSubClassOfAxiom> letheClausesToOwlAxioms(OWLOntology currentJustification, Set<ConceptClause> letheClauses) {
        Set<OWLSubClassOfAxiom> axioms = new HashSet<>();
        letheClauses.forEach(inputClause -> axioms.add((OWLSubClassOfAxiom) letheClauseToOwlAxiom(currentJustification, inputClause)));
        return axioms;
    }


    /**
     * Check if a subsumption is included in the interpolated ontology.
     */
    private boolean subsumptionCanBeFound(OWLOntology interpolant, OWLClass owlClass, OWLClass owlClass1) {

        if(owlClass.isBottomEntity() || owlClass1.isTopEntity())
            return true;

        List<OWLAxiom> admissibleAxioms = Arrays.asList(
            dataFactory.getOWLSubClassOfAxiom(owlClass, dataFactory.getOWLNothing()),
            dataFactory.getOWLSubClassOfAxiom(dataFactory.getOWLThing(), owlClass1),
            dataFactory.getOWLSubClassOfAxiom(dataFactory.getOWLThing(), dataFactory.getOWLNothing()),
            dataFactory.getOWLSubClassOfAxiom(owlClass, owlClass1),
            dataFactory.getOWLSubClassOfAxiom(
                dataFactory.getOWLThing(),
                dataFactory.getOWLObjectUnionOf(
                    dataFactory.getOWLObjectComplementOf(owlClass),
                        owlClass1
                    )
                )
        );

        for (OWLAxiom admissibleAxiom : admissibleAxioms) {
            if (interpolant.containsAxiom(admissibleAxiom)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check whether a given raw (lethe-generated) inference is valid
     */
    private boolean isRawInferenceAcceptable(Collection<Expression> premises, Collection<Expression> conclusions) {
        if(conclusions.size()==0)
            return false;
        
        assert conclusions.size() == 1;
        //ConceptDisjunction conclusion = conclusions.iterator().next().convertBack();
        Expression conclusion = conclusions.iterator().next();

        for (Expression premise : premises) {
            //ConceptDisjunction converted = premise.convertBack();
//            System.out.println("Checking " + conclusion + " vs. " + converted);
            //if (converted.equals(conclusion))
            if(premise.equals(conclusion))
                return false;
        }

        return true;
    }

    /**
     * Check whether a given translated (beautified + removed definers) inference is valid
     */
    private boolean isTranslatedInferenceValid(List<OWLAxiom> inferencePremisesAxioms, OWLAxiom conclusionAxiom) {
        return !inferencePremisesAxioms.contains(conclusionAxiom);
    }
}
