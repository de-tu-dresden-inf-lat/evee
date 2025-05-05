package de.tu_dresden.inf.lat.evee.nemo.parser;

import java.util.List;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import de.tu_dresden.inf.lat.evee.nemo.parser.tools.OWLHelper;
import de.tu_dresden.inf.lat.evee.nemo.parser.tools.ParsingHelper;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInference;

abstract class AbstractAtomParser {
    
    protected final OWLHelper owlHelper = OWLHelper.getInstance();
    protected final ParsingHelper parsingHelper = ParsingHelper.getInstance();

    protected final OWLSubClassOfAxiom defaultAxiom = owlHelper.getOWLSubClassOfAxiom(owlHelper.getOWLBot(), owlHelper.getOWLTop());

    public AbstractAtomParser(){}

    abstract public OWLAxiom toOwlAxiom(String axiom);

    abstract public void initFacts(List<IInference<String>> inferences);
    
    public OWLSubClassOfAxiom getDefaultAxiom() {
        return this.defaultAxiom;
    }

    protected OWLClassExpression parseCls(Object o) {
		if (o instanceof OWLClassExpression)
			return (OWLClassExpression) o;
		return owlHelper.getOWLConceptName((String) o);
	}

    protected OWLObjectPropertyExpression parseProp(Object o){
        if (o instanceof OWLObjectPropertyExpression)
			return (OWLObjectPropertyExpression) o;

		return owlHelper.getPropertyName((String) o);
    }
}