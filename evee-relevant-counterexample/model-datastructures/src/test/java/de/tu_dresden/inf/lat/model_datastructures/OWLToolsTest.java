package de.tu_dresden.inf.lat.model_datastructures;

import org.junit.Assert;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import de.tu_dresden.inf.lat.model.tools.ToOWLTools;

public class OWLToolsTest {
    	private static final ToOWLTools oWLTools = ToOWLTools.getInstance();
        
        private static final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        private static final OWLDataFactory factory = manager.getOWLDataFactory();

        @Test
        public void test(){
            OWLClassExpression class1 = factory.getOWLClass("http://example.org/Class1");
            OWLClassExpression class2 = factory.getOWLClass("http://example.org/Class2");
            OWLClassExpression class3 = factory.getOWLClass("http://example.org/Class3");
            OWLClassExpression class4 = factory.getOWLClass("http://example.org/Class4");

            OWLObjectPropertyExpression property = factory.getOWLObjectProperty("http://example.org/prop1");


            OWLClassExpression inter = factory.getOWLObjectIntersectionOf(class1,class2);
            OWLObjectPropertyDomainAxiom axiom = factory.getOWLObjectPropertyDomainAxiom(property, inter);

            System.out.println(oWLTools.getAsSubClassOf(axiom));
            System.out.println(axiom.asOWLSubClassOfAxiom());

           // Assert.assertEquals(oWLTools.getAsSubClassOf(axiom), axiom.asOWLSubClassOfAxioms());
        }
}
