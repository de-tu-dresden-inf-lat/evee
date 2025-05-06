package de.tu_dresden.inf.lat.evee.nemo.parser;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

import com.google.common.collect.Sets;

import de.tu_dresden.inf.lat.evee.nemo.parser.exceptions.ConceptTranslationError;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInference;

public class TextbookAtomParser extends AbstractAtomParser{

    private final String
        SUBOF_MAIN = "mainSubClassOf",
        SUBOF_INF = "http://rulewerk.semantic-web.org/inferred/subClassOf",
        SUBOF_NF = "http://rulewerk.semantic-web.org/normalForm/subClassOf",
        SUBOF_PREPARE = "prepareSco",

        SUBPROP = "http://rulewerk.semantic-web.org/normalForm/subProp",
        SUBPROP_DIR = "directSubProp",

        SUB_CONJ_NF = "http://rulewerk.semantic-web.org/normalForm/subClassConj",
        SUB_CONJ_INF = "http://rulewerk.semantic-web.org/inferred/subClassConj",

        SUB_EX_NF = "http://rulewerk.semantic-web.org/normalForm/subClassEx",
        SUB_EX_INF = "http://rulewerk.semantic-web.org/inferred/subClassEx",

        SUP_EX_NF = "http://rulewerk.semantic-web.org/normalForm/supClassEx",
        SUP_EX_INF = "http://rulewerk.semantic-web.org/inferred/supClassEx",

        CONJ_NF = "http://rulewerk.semantic-web.org/normalForm/conj",

        TRIPLE = "TRIPLE",
        EQUIV_TRIPLE = "<http://www.w3.org/2002/07/owl#equivalentClass>",
        SUBOF_TRIPLE= "<http://www.w3.org/2000/01/rdf-schema#subClassOf>",
        SUBPROP_TRIPLE = "<http://www.w3.org/2000/01/rdf-schema#subPropertyOf>",
        PROPCHAIN_TRIPLE = "<http://www.w3.org/2002/07/owl#propertyChainAxiom>",
        TRANSPROP_TRIPLE = "<http://www.w3.org/2002/07/owl#TransitiveProperty>",
        DISJOINT_TRIPLE = "<http://www.w3.org/2002/07/owl#disjointWith>";
        


    private final Set<String> subClassOfNames = Sets.newHashSet(SUBOF_MAIN, SUBOF_INF, SUBOF_NF, SUBOF_PREPARE);

    private final PlaceholderParser placeholderParser;

    public TextbookAtomParser(){
        placeholderParser = new PlaceholderParser();
    }

    @Override
    public OWLAxiom toOwlAxiom(String atom) {
        String predName = parsingHelper.getPredicateName(atom);
        List<String> args = parsingHelper.getPredicateArguments(atom);

        if(subClassOfNames.contains(predName))
            return parseSubClassAxiom(args);
        else if(predName.equals(SUBPROP) || predName.equals(SUBPROP_DIR))
            return parseSubProperty(args);
        else if(/* predName.equals(SUB_CONJ_NF) || */ predName.equals(SUB_CONJ_INF))
            return parseSubClassConjunction(args);
        else if(predName.equals(SUB_EX_INF) || predName.equals(SUB_EX_NF))
            return parseSubClassExistential(args);
        else if(predName.equals(SUP_EX_INF) || predName.equals(SUP_EX_NF))
            return parseSupClassExistential(args);
        // else if(predName.equals(CONJ_NF))
        //     return parseConjEqiv(args);
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

    @Override
    public void initFacts(List<IInference<String>> inferences) {
        placeholderParser.initParsingBase(inferences);
    }
    
    private OWLAxiom parseSubClassConjunction(List<String> args){
        OWLClassExpression sup;
        Set<OWLClassExpression> conjuncts = new HashSet<>();

        try {
            sup = placeholderParser.parseConceptOrPlaceholder(args.get(2));
            conjuncts.add(placeholderParser.parseConceptOrPlaceholder(args.get(0)));
            conjuncts.add(placeholderParser.parseConceptOrPlaceholder(args.get(1)));
        } catch (ConceptTranslationError e) {
            return defaultAxiom;
        }
        
        return owlHelper.getOWLSubClassOfAxiom(owlHelper.getOWLConjunction(conjuncts), sup);
    }

    private OWLAxiom parseConjEqiv(List<String> args){
        OWLClassExpression eqClass;
        Set<OWLClassExpression> conjuncts = new HashSet<>();

        try {
            eqClass = placeholderParser.parseConceptOrPlaceholder(args.get(0));
            conjuncts.add(placeholderParser.parseConceptOrPlaceholder(args.get(1)));
            conjuncts.add(placeholderParser.parseConceptOrPlaceholder(args.get(2)));
        } catch (ConceptTranslationError e) {
            return defaultAxiom;
        }

        String eqClassStr = eqClass.toString();
        
        OWLObjectIntersectionOf conj = owlHelper.getOWLConjunction(conjuncts);
        String conjStr = conj.toString();

        OWLAxiom res = owlHelper.getOWLEquivalenceAxiom(eqClass, conj);
        String resStr = res.toString();
        return res;
    }


    private OWLAxiom parseSubClassExistential(List<String> args){
        OWLClassExpression sup, existCls;
        OWLObjectPropertyExpression prop = parseProp(parsingHelper.format(args.get(0)));

        try{
            sup = placeholderParser.parseConceptOrPlaceholder(args.get(2));
            existCls = placeholderParser.parseConceptOrPlaceholder(args.get(1));
        } catch (ConceptTranslationError e) {
            return defaultAxiom;
        }

        OWLClassExpression restriction = owlHelper.getOWLExistentialRestriction(prop, existCls);
        
        return owlHelper.getOWLSubClassOfAxiom(restriction, sup);
    }

    private OWLAxiom parseSupClassExistential(List<String> args){
        OWLClassExpression sub, existCls;
        OWLObjectPropertyExpression prop = parseProp(parsingHelper.format(args.get(1)));
        
        try{
            sub = placeholderParser.parseConceptOrPlaceholder(args.get(0));
            existCls = placeholderParser.parseConceptOrPlaceholder(args.get(2));
        } catch (ConceptTranslationError e) {
            return defaultAxiom;
        }

        OWLClassExpression restriction = owlHelper.getOWLExistentialRestriction(prop, existCls);
        
        return owlHelper.getOWLSubClassOfAxiom(sub, restriction);
    }

    //////////////////////////////////////////////////
    //TODO everything below copied from ELKAtomParser
    /////////////////////////////////////////////////

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
