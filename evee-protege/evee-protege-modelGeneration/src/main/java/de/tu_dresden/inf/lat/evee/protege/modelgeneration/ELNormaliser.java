package de.tu_dresden.inf.lat.evee.protege.modelgeneration;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Normalise EL ontologies
 * (axioms outside of EL are removed)
 */

public class ELNormaliser {

    private int numRemoved;
    private OWLOntology ontology;
    private OWLOntologyManager manager;
    private OWLDataFactory factory;
    private Set<OWLClass> usedNames;
    private OWLOntology normalisedOntology;
    private int freshConceptCounter = 0;
    private OWLClass nextFreshConcept;
    /**
     * the following are used so that we can reuse the same concept name
     * for the same complex concept even if it appears several times.
     */
    private Set<OWLClassExpression> lhsAxiomAdded;
    private Set<OWLClassExpression> rhsAxiomAdded;
    private Map<OWLClassExpression, OWLClass> mappedConceptNames;

    public static void main(String[] args) throws OWLOntologyCreationException, FileNotFoundException, OWLOntologyStorageException {
        if (args.length != 1) {
            System.out.println("You need to provide an ontology file name");
            System.exit(1);
        }
        String filename = args[0];
        File file = new File(filename);
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = manager
                .loadOntologyFromOntologyDocument(file);

        ELNormaliser normaliser = new ELNormaliser();
        normaliser.setOntology(ontology);
        OWLOntology normalised = normaliser.normalise();

        manager.saveOntology(normalised,
                new FileOutputStream(new File(filename + ".normalised.owl")));

    }

    public void setOntology(OWLOntology ontology) {
        this.ontology = ontology;
        this.manager = ontology.getOWLOntologyManager();

        this.factory = manager.getOWLDataFactory();

        this.nextFreshConcept = factory.getOWLClass(IRI.create("X"));
        this.usedNames = ontology.getClassesInSignature(Imports.INCLUDED)
                .stream().collect(Collectors.toSet());

        this.numRemoved = 0;
    }

    public OWLOntology normalise() throws OWLOntologyCreationException {
        lhsAxiomAdded = new HashSet<>();
        rhsAxiomAdded = new HashSet<>();
        mappedConceptNames = new HashMap<>();

        normalisedOntology = manager.createOntology();
        ontology.getLogicalAxioms(Imports.INCLUDED).stream().forEach(this::addNormalised);
        return normalisedOntology;
    }

    public int getNumRemoved() {
        return numRemoved;
    }


    private void addNormalised(OWLAxiom axiom) {
        //System.out.println("Process: "+axiom);

        if (axiom instanceof OWLSubObjectPropertyOfAxiom) {
            manager.addAxiom(normalisedOntology, axiom);

        } else if (axiom instanceof OWLSubClassOfAxiom) {
            OWLSubClassOfAxiom subAxiom = (OWLSubClassOfAxiom) axiom;
            OWLClassExpression lhs = subAxiom.getSubClass();
            OWLClassExpression rhs = subAxiom.getSuperClass();
            if (lhs instanceof OWLClass && rhs instanceof OWLClass) {
                manager.addAxiom(normalisedOntology, axiom);
            /*else if(lhs instanceof OWLObjectUnionOf) {
                OWLObjectUnionOf union = (OWLObjectUnionOf) lhs;
                union.operands().forEach(x -> addNormalised(
                        factory.getOWLSubClassOfAxiom(x,rhs)
                ));*/
            } else if (rhs instanceof OWLObjectIntersectionOf) {
                OWLObjectIntersectionOf inters = (OWLObjectIntersectionOf) rhs;
                inters.getOperands().stream().forEach(x -> addNormalised(
                        factory.getOWLSubClassOfAxiom(lhs, x)
                ));
            } else if (lhs instanceof OWLObjectIntersectionOf) {
                OWLObjectIntersectionOf inters = (OWLObjectIntersectionOf) lhs;
                Collection<OWLClass> conjuncts =
                        inters.getOperands().stream()
                                .map(this::toOWLClassLHS)
                                .collect(Collectors.toSet());

                OWLClass rhsClass = toOWLClassRHS(rhs);
                manager.addAxiom(normalisedOntology, factory.getOWLSubClassOfAxiom(
                        factory.getOWLObjectIntersectionOf((Set<? extends OWLClassExpression>) conjuncts),
                        rhsClass
                ));
            } else if (lhs instanceof OWLObjectSomeValuesFrom) {
                OWLObjectSomeValuesFrom exists = (OWLObjectSomeValuesFrom) lhs;
                OWLClass filler = toOWLClassLHS(exists.getFiller());
                OWLClass rhsClass = toOWLClassRHS(rhs);
                manager.addAxiom(normalisedOntology, factory.getOWLSubClassOfAxiom(
                        factory.getOWLObjectSomeValuesFrom(
                                exists.getProperty(),
                                filler),
                        rhsClass
                ));

            } else if (rhs instanceof OWLObjectSomeValuesFrom) {
                OWLObjectSomeValuesFrom exists = (OWLObjectSomeValuesFrom) rhs;
                OWLClass filler = toOWLClassRHS(exists.getFiller());
                OWLClass lhsClass = toOWLClassLHS(lhs);
                manager.addAxiom(normalisedOntology, factory.getOWLSubClassOfAxiom(
                        lhsClass,
                        factory.getOWLObjectSomeValuesFrom(
                                exists.getProperty(),
                                filler)
                ));
           /* } else if (rhs instanceof OWLObjectAllValuesFromImpl) {
                OWLObjectAllValuesFrom forall = (OWLObjectAllValuesFrom) rhs;
                OWLClass filler = toOWLClassRHS(forall.getFiller());
                OWLClass lhsClass = toOWLClassLHS(lhs);
                normalisedOntology.add(factory.getOWLSubClassOfAxiom(
                        lhsClass,
                        factory.getOWLObjectSomeValuesFrom(
                                forall.getProperty(),
                                filler)
                ));*/
            } else {
//                System.out.println("Not supported: " + axiom);
                this.numRemoved = this.numRemoved + 1;
            }
        } else if (axiom instanceof OWLDisjointClassesAxiom) {
            OWLDisjointClassesAxiom disj = (OWLDisjointClassesAxiom) axiom;
            disj.asPairwiseAxioms().forEach(ax -> {
                OWLClassExpression first = ax.getClassExpressionsAsList()
                        .get(0);
                OWLClassExpression second = ax.getClassExpressionsAsList().get(1);
                addNormalised(factory.getOWLSubClassOfAxiom(
                        factory.getOWLObjectIntersectionOf(first, second),
                        factory.getOWLNothing()
                ));
            });
        } else if (axiom instanceof OWLSubClassOfAxiomShortCut) {
            OWLSubClassOfAxiomShortCut shortCut = (OWLSubClassOfAxiomShortCut) axiom;
            addNormalised(shortCut.asOWLSubClassOfAxiom());
        } else if (axiom instanceof OWLSubClassOfAxiomSetShortCut) {
            OWLSubClassOfAxiomSetShortCut shortCut =
                    (OWLSubClassOfAxiomSetShortCut) axiom;
            shortCut.asOWLSubClassOfAxioms().forEach(this::addNormalised);
        } else {
//            System.out.println("Not supported: "+axiom);
            this.numRemoved = this.numRemoved + 1;
        }
    }

    private OWLClass toOWLClassLHS(OWLClassExpression owlClassExpression) {
        if (owlClassExpression instanceof OWLClass)
            return (OWLClass) owlClassExpression;
        else if (lhsAxiomAdded.contains(owlClassExpression)) {
            return mappedConceptNames.get(owlClassExpression);
        } else {
            OWLClass cl;
            if (mappedConceptNames.containsKey(owlClassExpression))
                cl = mappedConceptNames.get(owlClassExpression);
            else {
                cl = getFreshConcept();
                mappedConceptNames.put(owlClassExpression, cl);
            }
            addNormalised(factory.getOWLSubClassOfAxiom(owlClassExpression, cl));
            lhsAxiomAdded.add(owlClassExpression);
            return cl;
        }
    }

    private OWLClass toOWLClassRHS(OWLClassExpression owlClassExpression) {
        if (owlClassExpression instanceof OWLClass)
            return (OWLClass) owlClassExpression;
        else if (rhsAxiomAdded.contains(owlClassExpression)) {
            return mappedConceptNames.get(owlClassExpression);
        } else {
            OWLClass cl;
            if (mappedConceptNames.containsKey(owlClassExpression))
                cl = mappedConceptNames.get(owlClassExpression);
            else {
                cl = getFreshConcept();
                mappedConceptNames.put(owlClassExpression, cl);
            }
            addNormalised(factory.getOWLSubClassOfAxiom(cl, owlClassExpression));
            rhsAxiomAdded.add(owlClassExpression);
            return cl;
        }
    }

    private OWLClass getFreshConcept() {
        while (usedNames.contains(nextFreshConcept)) {
            nextFreshConcept = factory.getOWLClass(IRI.create("X" + freshConceptCounter));
            freshConceptCounter++;
        }
        usedNames.add(nextFreshConcept);
        return nextFreshConcept;
    }

}

