package de.tu_dresden.inf.lat.evee.nemo.parser;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import de.tu_dresden.inf.lat.evee.nemo.parser.tools.OWLHelper;

abstract class AbstractAtomParser {
    
    protected final OWLHelper owlHelper = OWLHelper.getInstance();
    protected final OWLSubClassOfAxiom defaultAxiom = owlHelper.getOWLSubClassOfAxiom(owlHelper.getOWLBot(), owlHelper.getOWLTop());

    public AbstractAtomParser(){}

    abstract public OWLAxiom toOwlAxiom(String axiom);
    
    public OWLSubClassOfAxiom getDefaultAxiom() {
        return this.defaultAxiom;
    }

    protected OWLClassExpression getCls(Object o) {
		if (o instanceof OWLClassExpression)
			return (OWLClassExpression) o;
		return owlHelper.getOWLConceptName((String) o);
	}

    protected OWLObjectPropertyExpression getRole(Object o){
        if (o instanceof OWLObjectPropertyExpression)
			return (OWLObjectPropertyExpression) o;

		return owlHelper.getPropertyName((String) o);
    }

    abstract public void printCache();

}