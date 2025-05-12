package de.tu_dresden.inf.lat.evee.nemo.parser;

import java.util.List;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import de.tu_dresden.inf.lat.evee.nemo.parser.exceptions.ConceptTranslationError;
import de.tu_dresden.inf.lat.evee.nemo.parser.tools.OWLHelper;
import de.tu_dresden.inf.lat.evee.nemo.parser.tools.ParsingHelper;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInference;

abstract class AbstractAtomParser {

    protected final String TRIPLE = "TRIPLE";
    private final String
        EQUIV_TRIPLE = "<http://www.w3.org/2002/07/owl#equivalentClass>",
        SUBOF_TRIPLE= "<http://www.w3.org/2000/01/rdf-schema#subClassOf>",
        SUBPROP_TRIPLE = "<http://www.w3.org/2000/01/rdf-schema#subPropertyOf>",
        PROPCHAIN_TRIPLE = "<http://www.w3.org/2002/07/owl#propertyChainAxiom>",
        TRANSPROP_TRIPLE = "<http://www.w3.org/2002/07/owl#TransitiveProperty>",
        DISJOINT_TRIPLE = "<http://www.w3.org/2002/07/owl#disjointWith>";
    
    protected final OWLHelper owlHelper = OWLHelper.getInstance();
    protected final ParsingHelper parsingHelper = ParsingHelper.getInstance();
    protected final PlaceholderParser placeholderParser;

    protected final OWLSubClassOfAxiom defaultAxiom = owlHelper.getOWLSubClassOfAxiom(owlHelper.getOWLBot(), owlHelper.getOWLTop());

    public AbstractAtomParser(){
        placeholderParser = new PlaceholderParser();
    }

    public void initFacts(List<IInference<String>> inferences){
        placeholderParser.initParsingBase(inferences);
    }

    abstract public OWLAxiom toOwlAxiom(String axiom);
    
    public OWLSubClassOfAxiom getDefaultAxiom() {
        return this.defaultAxiom;
    }

    protected OWLAxiom parseTriple(List<String> args){
        if(args.get(1).equals(EQUIV_TRIPLE))
            return parseEquivTriple(args);
        else if(args.get(1).equals(SUBOF_TRIPLE))
            return parseSubOfTriple(args);
        else if(args.get(1).equals(SUBPROP_TRIPLE))
            return parseSubPropTriple(args);
        else if(args.get(1).equals(PROPCHAIN_TRIPLE))
            return parsePropChainTriple(args);
        else if(args.get(2).equals(TRANSPROP_TRIPLE))
            return parseTransPropTriple(args);
        else if(args.get(1).equals(DISJOINT_TRIPLE))
            return parseDisjointTriple(args);

        return defaultAxiom;
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

    protected OWLAxiom parseSubClassAxiom(String subClass, String superClass){
        OWLClassExpression sub, sup;

        try{
            sub = placeholderParser.parseConceptOrPlaceholder(subClass);
            sup = placeholderParser.parseConceptOrPlaceholder(superClass);
        } catch (ConceptTranslationError e) {
            return defaultAxiom;
        }

       return owlHelper.getOWLSubClassOfAxiom(sub, sup);
    }

    protected OWLAxiom parseSubProperty(String subProp, String superProp) {

        OWLObjectPropertyExpression sub = parseProp(parsingHelper.format(subProp));
        OWLObjectPropertyExpression sup = parseProp(parsingHelper.format(superProp));
        
		return owlHelper.getOWLSubObjectPropertyAxiom(sub, sup);
	}

    private OWLAxiom parseEquivTriple(List<String> args){
        OWLClassExpression cls1, cls2;
    
        try{
            cls1 = placeholderParser.parseConceptOrPlaceholder(args.get(0));
            cls2 = placeholderParser.parseConceptOrPlaceholder(args.get(2));
        } catch (ConceptTranslationError e) {
            return defaultAxiom;
        }
    
       return owlHelper.getOWLEquivalenceAxiom(cls1, cls2);
    }
    
    private OWLAxiom parseSubOfTriple(List<String> args){
        return parseSubClassAxiom(args.get(0), args.get(2));
    }

    private OWLAxiom parseSubPropTriple(List<String> args){
        return parseSubProperty(args.get(0), args.get(2));
    }

    private OWLAxiom parsePropChainTriple(List<String> args){
        OWLObjectPropertyExpression sup = parseProp(parsingHelper.format(args.get(0)));
        List<OWLObjectPropertyExpression> chain;

        try {
            chain = placeholderParser.getRoleChainFromPlaceholder(args.get(2));
        } catch (ConceptTranslationError e) {
            return defaultAxiom;
        }

        return owlHelper.getOWLSubPropertyChainOfAxiom(chain, sup);     
    }

    private OWLAxiom parseTransPropTriple(List<String> args){
        OWLObjectPropertyExpression prop = parseProp(parsingHelper.format(args.get(0)));

        return owlHelper.getOWOwlTransitivePropertyAxiom(prop);
    }


    private OWLAxiom parseDisjointTriple(List<String> args){
        OWLClassExpression cls1 = parseCls(parsingHelper.format(args.get(0)));
        OWLClassExpression cls2 = parseCls(parsingHelper.format(args.get(2)));

        return owlHelper.getOWLDisjointAxiom(cls1, cls2);
    }

}