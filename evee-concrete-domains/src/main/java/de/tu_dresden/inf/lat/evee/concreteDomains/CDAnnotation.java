package de.tu_dresden.inf.lat.evee.concreteDomains;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

public class cdAnnotation {

    OWLOntology ontology;
    OWLSubClassOfAxiom subAxiom;
    OWLClassExpression classExpression;
    OWLOntologyManager man;
    OWLDataFactory factory;

    public cdAnnotation(OWLOntology ontology, OWLSubClassOfAxiom axiom, OWLClassExpression classExpression) {
        this.ontology = ontology;
        this.subAxiom = axiom;
        this.classExpression = classExpression;
        this.man = ontology.getOWLOntologyManager();
        this.factory = man.getOWLDataFactory();
    }

    public void wtfAmIDoining(){
        OWLAnnotationProperty a = factory.getOWLAnnotationProperty("http://example.com/cd#domain");
        OWLAnnotationAssertionAxiom annoAxiom = factory.getOWLAnnotationAssertionAxiom(a, ontology.getOntologyID().getOntologyIRI().get(), factory.getOWLLiteral("concreteDomain1"));
        OWLAnnotation ontAnno = annoAxiom.getAnnotation();

        //manual define concrete domain
        OWLAnnotationProperty domainElement = factory.getOWLAnnotationProperty("http://example.com/cd#domainElement");
        OWLAnnotationAssertionAxiom annoAxiom2 = factory.getOWLAnnotationAssertionAxiom(domainElement, ontAnno.iriValue().get(), factory.getOWLLiteral(1));
        OWLAnnotationAssertionAxiom annoAxiom3 = factory.getOWLAnnotationAssertionAxiom(domainElement, ontAnno.iriValue().get(), factory.getOWLLiteral(2));

        //use range to define concrete domain; would need new annotation prop for every new concrete domain
        OWLAnnotationPropertyRangeAxiom rangeAxiom = factory.getOWLAnnotationPropertyRangeAxiom(domainElement, OWL2Datatype.XSD_INTEGER.getIRI());

        // use custom annotaion prop to dedfine concrete domain
        OWLAnnotationProperty domainDef = factory.getOWLAnnotationProperty("http://example.com/cd#domainDefinition");
            //better to annotate ontology directly?
        OWLAnnotationAssertionAxiom annoAxiom4 = factory.getOWLAnnotationAssertionAxiom(domainDef, ontAnno.iriValue().get(), OWL2Datatype.XSD_INTEGER.getIRI());


        //define predicates for concrete domain
        OWLAnnotationProperty domainPreds = factory.getOWLAnnotationProperty("http://example.com/cd#predicates");
        OWLAnnotationAssertionAxiom annoAxiom5 = factory.getOWLAnnotationAssertionAxiom(domainPreds, ontAnno.iriValue().get(), factory.getOWLLiteral("x+q=y"));
        OWLAnnotationAssertionAxiom annoAxiom6 = factory.getOWLAnnotationAssertionAxiom(domainPreds, ontAnno.iriValue().get(), factory.getOWLLiteral("x>q"));
        OWLAnnotationAssertionAxiom annoAxiom7 = factory.getOWLAnnotationAssertionAxiom(domainPreds, ontAnno.iriValue().get(), factory.getOWLLiteral("x=q"));

        /////////////////////////////////////////////////////////////////////////////////////////
        
        OWLClassExpression clazz = factory.getOWLClass("http://example.com/cd#clazz");
        OWLDataProperty dataPropX = factory.getOWLDataProperty("http://example.com/cd#x");
        OWLDataProperty dataPropY = factory.getOWLDataProperty("http://example.com/cd#y");

        OWLAnnotationProperty constraint = factory.getOWLAnnotationProperty("http://example.com/cd#constraint");

        OWLAnnotationValue annoValue = factory.getOWLLiteral(dataPropX.getIRI().toString() + " + 900 = " + dataPropY.getIRI().toString());

        OWLAnnotationAssertionAxiom annoAxiom8 = factory.getOWLAnnotationAssertionAxiom(constraint, clazz.asOWLClass().getIRI(), annoValue);


    }

}
