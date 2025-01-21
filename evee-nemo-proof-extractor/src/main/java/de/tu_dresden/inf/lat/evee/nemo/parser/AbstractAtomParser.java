package de.tu_dresden.inf.lat.evee.nemo.parser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import de.tu_dresden.inf.lat.evee.nemo.parser.tools.OWLHelper;

abstract class AbstractAtomParser {
    
    protected final OWLHelper owlHelper = OWLHelper.getInstance();
    private final OWLSubClassOfAxiom defaultAxiom = owlHelper.getOWLSubClassOfAxiom(owlHelper.getOWLBot(), owlHelper.getOWLTop());
    
    /*
        parsingMap is a map of all predicateNames and the matching parsing function. 
        The input of the function is a list of the predicate arguments.
        Add predicates by calling addAtomParsing()
     */
    private Map<String, Function<List<String>, OWLAxiom>> parsingMap;

    // basic parsing Functions
    protected final Function<List<String>, OWLAxiom> parseSubClassAxiom = args -> 
        owlHelper.getOWLSubClassOfAxiom(getCls(args.get(0)), getCls(args.get(1)));
    

    public AbstractAtomParser(){
        this.parsingMap = new HashMap<String, Function<List<String>, OWLAxiom>>();
    }

    public OWLAxiom parse(String predicateName, List<String> args){
        Function<List<String>, OWLAxiom> parsingFunc = parsingMap.get(predicateName);

        if (parsingFunc == null)
            return defaultAxiom;
        
        return parsingFunc.apply(args);
    }
    
    protected void addAtomParsing(String predicateName, Function<List<String>, OWLAxiom> parsingFunc){
        parsingMap.put(predicateName, parsingFunc);
    }
    
    public OWLSubClassOfAxiom getDefaultAxiom() {
        return this.defaultAxiom;
    }

    // helper method
    protected OWLClassExpression getCls(Object o) {
		if (o instanceof OWLClassExpression)
			return (OWLClassExpression) o;
		return owlHelper.getOWLConceptName((String) o);
	}

}