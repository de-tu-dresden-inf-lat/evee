package de.tu_dresden.inf.lat.proofs.lethe;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import scala.collection.JavaConverters;
import uk.ac.man.cs.lethe.internal.dl.datatypes.BaseConcept;
import uk.ac.man.cs.lethe.internal.dl.datatypes.Concept;
import uk.ac.man.cs.lethe.internal.dl.forgetting.direct.ALCFormulaPreparations;
import uk.ac.man.cs.lethe.internal.dl.forgetting.direct.DefinerFactory;
import uk.ac.man.cs.lethe.internal.dl.owlapi.OWLExporter;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DefinerTranslatingVisitor implements OWLClassExpressionVisitorEx<OWLClassExpression> {

    private Map<BaseConcept, Concept> definerMap = JavaConverters.mapAsJavaMap(ALCFormulaPreparations.definerMap());
    private OWLDataFactory dataFactory = OWLManager.createOWLOntologyManager().getOWLDataFactory();
    private DefinerFactory definerFactory;
    private OWLOntology ontology;
    private OWLExporter owlExporter = new OWLExporter();

    public DefinerTranslatingVisitor(OWLOntology ontology, DefinerFactory definerFactory) {
        this.definerFactory = definerFactory;
        this.ontology = ontology;
    }

    public OWLClassExpression visit(OWLClass cls) {
        String conceptName = cls.getIRI().getFragment();
        BaseConcept bc = BaseConcept.apply(conceptName);
        if (definerFactory.definerBases().contains(bc) || definerMap.containsKey((BaseConcept) bc)) {
            Set<BaseConcept> definers = getBaseDefiners(bc); //JavaConverters.setAsJavaSet(definerFactory.getBaseDefiners(bc));
            OWLClassExpression translation;
            if (definers.size() > 1) {
                // System.out.println(definers);
                // System.out.println(definerMap);
                // More than 1 definer, it has to be a definer standing for an intersection of defined concepts
                Set<OWLClassExpression> definedConcepts = new HashSet<>();
                definers.forEach(definer ->
                        definedConcepts.add(
                                owlExporter.toOwl(ontology, definerMap.get(definer))
                                        .accept(this)));
                translation = dataFactory.getOWLObjectIntersectionOf(definedConcepts);
                // System.out.println("    " + definedConcepts + " → " + translation);
            } else {
                // Just 1 definer, it has to be a definer standing for an atomic concept
                BaseConcept definer = definers.iterator().next();
                translation = owlExporter.toOwl(ontology, definerMap.get(definer));
                // System.out.println("    " + definer + " → " + translation);
            }
            return translation.accept(this);
        }
        
        // System.out.println("    No definer for " + cls);
        if(cls.getIRI().toString().startsWith("_D")){
            assert !definerMap.containsKey(bc);
            if(ALCFormulaPreparations.allGeneratedNames().contains(bc))
                System.out.println("definer was created unusually");
            System.exit(1);
        }
        return cls;
    }

    private Set<BaseConcept> getBaseDefiners(BaseConcept definer) {
        assert definerFactory.definerBases().contains(definer) || definerMap.containsKey(definer) : definer+" unknown! ";
        Set<BaseConcept> definers = new HashSet<>();
        for(BaseConcept definer2: JavaConverters.setAsJavaSet(definerFactory.getBaseDefiners(definer))){
            if(definerMap.containsKey(definer2)) {
                assert definerMap.get(definer2)!=null : "null entry in definerMap for "+definer2;
                definers.add(definer2);
            }
            else {
                assert !definer2.equals(definer) : "loop on "+definer2;
                definers.addAll(getBaseDefiners(definer2));
            }
        }
        return definers;
    }

    public OWLObjectSomeValuesFrom visit(OWLObjectSomeValuesFrom expression) {
        return dataFactory.getOWLObjectSomeValuesFrom(expression.getProperty(), expression.getFiller().accept(this));
    }

    public OWLObjectAllValuesFrom visit(OWLObjectAllValuesFrom expression) {
        return dataFactory.getOWLObjectAllValuesFrom(expression.getProperty(), expression.getFiller().accept(this));
    }

    public OWLObjectComplementOf visit(OWLObjectComplementOf expression) {
        return dataFactory.getOWLObjectComplementOf(expression.getOperand().accept(this));
    }

    public OWLObjectUnionOf visit(OWLObjectUnionOf expression) {
        Set<OWLClassExpression> expressions = new HashSet<>();
        expression.asDisjunctSet().forEach(disjunct -> expressions.add(disjunct.accept(this)));
        return dataFactory.getOWLObjectUnionOf(expressions);
    }

    public OWLObjectIntersectionOf visit(OWLObjectIntersectionOf expression) {
        Set<OWLClassExpression> expressions = new HashSet<>();
        expression.asConjunctSet().forEach(conjunct -> expressions.add(conjunct.accept(this)));
        return dataFactory.getOWLObjectIntersectionOf(expressions);
    }

    @Override
    public OWLClassExpression visit(OWLObjectHasValue ce) {
        return null;
    }

    @Override
    public OWLClassExpression visit(OWLObjectMinCardinality ce) {
        return null;
    }

    @Override
    public OWLClassExpression visit(OWLObjectExactCardinality ce) {
        return null;
    }

    @Override
    public OWLClassExpression visit(OWLObjectMaxCardinality ce) {
        return null;
    }

    @Override
    public OWLClassExpression visit(OWLObjectHasSelf ce) {
        return null;
    }

    @Override
    public OWLClassExpression visit(OWLObjectOneOf ce) {
        return null;
    }

    @Override
    public OWLClassExpression visit(OWLDataSomeValuesFrom ce) {
        return null;
    }

    @Override
    public OWLClassExpression visit(OWLDataAllValuesFrom ce) {
        return null;
    }

    @Override
    public OWLClassExpression visit(OWLDataHasValue ce) {
        return null;
    }

    @Override
    public OWLClassExpression visit(OWLDataMinCardinality ce) {
        return null;
    }

    @Override
    public OWLClassExpression visit(OWLDataExactCardinality ce) {
        return null;
    }

    @Override
    public OWLClassExpression visit(OWLDataMaxCardinality ce) {
        return null;
    }
}
