package de.tu_dresden.inf.lat.evee.nemo.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import com.google.common.collect.Sets;

import de.tu_dresden.inf.lat.evee.nemo.parser.exceptions.ConceptTranslationError;
import de.tu_dresden.inf.lat.evee.nemo.parser.tools.ParsingHelper;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInference;

public class ELKAtomParser extends AbstractAtomParser{

    private final String
        SUBOF_MAIN = "mainSubClassOf",
        SUBOF_INF = "http://rulewerk.semantic-web.org/inferred/subClassOf",
        SUBOF_NF = "http://rulewerk.semantic-web.org/normalForm/subClassOf",
        SUBOF_PREPARE = "prepareSco",

        SUBOF_EXIST = "http://rulewerk.semantic-web.org/inferred/ex",
    
        SUBPROP = "http://rulewerk.semantic-web.org/normalForm/subProp",
        SUBPROP_DIR = "directSubProp",
        
        SUBPROP_CHAIN = "http://rulewerk.semantic-web.org/normalForm/subPropChain",
        SUBPROP_CHAIN_AUX = "auxPropChain",

        TRIPLE = "TRIPLE",
        EQUIV_TRIPLE = "<http://www.w3.org/2002/07/owl#equivalentClass>",
        SUBOF_TRIPLE= "<http://www.w3.org/2000/01/rdf-schema#subClassOf>",
        SUBPROP_TRIPLE = "<http://www.w3.org/2000/01/rdf-schema#subPropertyOf>",
        PROPCHAIN_TRIPLE = "<http://www.w3.org/2002/07/owl#propertyChainAxiom>",
        TRANSPROP_TRIPLE = "<http://www.w3.org/2002/07/owl#TransitiveProperty>",
        DISJOINT_TRIPLE = "<http://www.w3.org/2002/07/owl#disjointWith>";
        
    private final Set<String> subClassOfNames = Sets.newHashSet(SUBOF_MAIN, SUBOF_INF, SUBOF_NF, SUBOF_PREPARE);

    private final ParsingHelper parsingHelper = ParsingHelper.getInstance();
    private final PlaceholderParser placeholderParser;    
    
    public ELKAtomParser(){
        placeholderParser = new PlaceholderParser();
    }

    public void initFacts(List<IInference<String>> inferences){
        placeholderParser.initParsingBase(inferences);
    }

    public OWLAxiom toOwlAxiom(String atom){
        String predName = parsingHelper.getPredicateName(atom);
        List<String> args = parsingHelper.getPredicateArguments(atom);
        
        if(subClassOfNames.contains(predName))
            return parseSubClassAxiom(args);
        else if(predName.equals(SUBPROP) || predName.equals(SUBPROP_DIR))
            return parseSubProperty(args);
        else if(predName.equals(SUBOF_EXIST))
            return parseSubOfExistential(args);
        else if(predName.equals(SUBPROP_CHAIN) || predName.equals(SUBPROP_CHAIN_AUX))
             return parsePropChain(args);
        else if(predName.equals(TRIPLE) && args.get(1).equals(EQUIV_TRIPLE))
            return parseEquivTriple(args);
        else if(predName.equals(TRIPLE) && args.get(1).equals(SUBOF_TRIPLE))
            return parseSubOfTriple(args);
        else if(predName.equals(TRIPLE) && args.get(1).equals(SUBPROP_TRIPLE))
            return parseSubPropTriple(args);
        else if(predName.equals(TRIPLE) && args.get(1).equals(PROPCHAIN_TRIPLE))
            return parsePropChainTriple(args);
        else if(predName.equals(TRIPLE) && args.get(2).equals(TRANSPROP_TRIPLE))
            return parseTransPropTriple(args);
        else if(predName.equals(TRIPLE) && args.get(1).equals(DISJOINT_TRIPLE))
            return parseDisjointTriple(args);

        return defaultAxiom;
    }
    
    private OWLAxiom parseSubClassAxiom(List<String> args){
        OWLClassExpression sub, sup;

        try{
            sub = placeholderParser.parseConceptOrPlaceholder(args.get(0));
            sup = placeholderParser.parseConceptOrPlaceholder(args.get(1));
        } catch (ConceptTranslationError e) {
            return defaultAxiom;
        }

       return owlHelper.getOWLSubClassOfAxiom(sub, sup);
    }

    
    private OWLAxiom parseSubProperty(List<String> args) {
        String supStr = parsingHelper.format(args.get(1));
        if(parsingHelper.isPlaceholder(supStr)) // rolechains on right hand side not supported
            return defaultAxiom;

        OWLObjectPropertyExpression sup = parseProp(supStr);
        OWLObjectPropertyExpression sub = parseProp(parsingHelper.format(args.get(0)));
        
		return owlHelper.getOWLSubObjectPropertyAxiom(sub, sup);
	}
    
    private OWLAxiom parseSubOfExistential(List<String> args){
        OWLClassExpression sub, existCls;
        List<OWLObjectPropertyExpression> props; 
        
        try{
            sub = placeholderParser.parseConceptOrPlaceholder(args.get(0));
            existCls = placeholderParser.parseConceptOrPlaceholder(args.get(2));
            props = placeholderParser.parseRoleOrPlaceholder(args.get(1));
        } catch (ConceptTranslationError e) {
            return defaultAxiom;
        }

        OWLClassExpression restriction = existCls;
        for(int i=props.size()-1; i>=0; i--){ //dealing with role chains by nested Ex.Restrictions
            restriction = owlHelper.getOWLExistentialRestriction(props.get(i), restriction);
        }
        
        return owlHelper.getOWLSubClassOfAxiom(sub,restriction);
    }

    private OWLAxiom parsePropChain(List<String> args) {
        String supStr = parsingHelper.format(args.get(2));
        if(parsingHelper.isPlaceholder(supStr)) // rolechains on right hand side not supported
            return defaultAxiom;
        
        OWLObjectPropertyExpression sup = parseProp(supStr);
        List<OWLObjectPropertyExpression> chain = new ArrayList<>();
        try {
            chain.addAll(placeholderParser.parseRoleOrPlaceholder(args.get(0)));
            chain.addAll(placeholderParser.parseRoleOrPlaceholder(args.get(1)));
        } catch (Exception e) {
            return defaultAxiom;
        }
    
        return owlHelper.getOWLSubPropertyChainOfAxiom(chain, sup);
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
        return parseSubClassAxiom(Arrays.asList(args.get(0), args.get(2)));
    }

    private OWLAxiom parseSubPropTriple(List<String> args){
        return parseSubProperty(Arrays.asList(args.get(0), args.get(2)));
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