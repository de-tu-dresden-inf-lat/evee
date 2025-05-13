package de.tu_dresden.inf.lat.evee.nemo.parser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

import com.google.common.collect.Sets;

import de.tu_dresden.inf.lat.evee.nemo.parser.exceptions.ConceptTranslationError;


public class EnvelopeAtomParser extends AbstractAtomParser {

    private final String 
        EQUIVALENCE_MAIN = "mainEquivClass",
        SUBOF_MAIN = "mainSubClassOf",
        SUBOF_INF = "http://rulewerk.semantic-web.org/inferred/subClassOf",
        SUBOF_NF = "http://rulewerk.semantic-web.org/normalForm/subClassOf",
        SUBOF_PREPARE = "prepareSco",
        S_PRED = "S",

        R_PRED = "R",

        SUB_CONJ = "http://rulewerk.semantic-web.org/normalForm/subClassConj",
        SUB_EX = "http://rulewerk.semantic-web.org/normalForm/subClassEx",
        SUP_EX = "http://rulewerk.semantic-web.org/normalForm/supClassEx",

        SUBPROP = "http://rulewerk.semantic-web.org/normalForm/subProp",
        SUBPROP_DIR = "directSubProp",

        SUBPROP_CHAIN = "http://rulewerk.semantic-web.org/normalForm/subPropChain",
        SUBPROP_CHAIN_AUX = "auxPropChain";

    private final Set<String> subClassOfNames = Sets.newHashSet(SUBOF_MAIN, SUBOF_INF, SUBOF_NF, SUBOF_PREPARE, S_PRED);

    @Override
    public OWLAxiom toOwlAxiom(String atom) {
        String predName = parsingHelper.getPredicateName(atom);
        List<String> args = parsingHelper.getPredicateArguments(atom);

        if(predName.equals(EQUIVALENCE_MAIN))
            return parseEquivalenceClassesAxiom(args);
        else if(subClassOfNames.contains(predName))
            return parseSubClassAxiom(args);
        else if(predName.equals(SUB_CONJ))
            return parseSubClassConjunction(args);
        else if(predName.equals(SUB_EX))
            return parseSubClassExistential(args);
        else if(predName.equals(SUP_EX) || predName.equals(R_PRED))
            return parseSupClassExistential(args);
        else if(predName.equals(SUBPROP) || predName.equals(SUBPROP_DIR))
            return parseSubProperty(args);
        else if(predName.equals(SUBPROP_CHAIN) || predName.equals(SUBPROP_CHAIN_AUX))
            return parsePropChain(args);
        else if(predName.equals(TRIPLE))
            return parseTriple(args);

        return defaultAxiom;
    }

    private OWLAxiom parseEquivalenceClassesAxiom(List<String> args) {
        OWLClassExpression cls1, cls2;

        try{
            cls1 = placeholderParser.parseConceptOrPlaceholder(args.get(0));
            cls2 = placeholderParser.parseConceptOrPlaceholder(args.get(1));
        } catch (ConceptTranslationError e) {
            return defaultAxiom;
        }

        return owlHelper.getOWLEquivalenceAxiom(cls1, cls2);
    }

    private OWLAxiom parseSubClassAxiom(List<String> args){
        return parseSubClassAxiom(args.get(0), args.get(1));
    }

    private OWLAxiom parseSubProperty(List<String> args) {
        if(parsingHelper.isPlaceholder(args.get(1))) // rolechains on right hand side not supported
            return defaultAxiom;
        
		return parseSubProperty(args.get(0), args.get(1));
	}

    /////TODO just copied from textbookParser
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
        List<OWLObjectPropertyExpression> props;

        try{
            sub = placeholderParser.parseConceptOrPlaceholder(args.get(0));
            props = placeholderParser.parseRoleOrPlaceholder(args.get(1));
            existCls = placeholderParser.parseConceptOrPlaceholder(args.get(2));
        } catch (ConceptTranslationError e) {
            return defaultAxiom;
        }

        OWLClassExpression restriction = existCls;
        for(int i=props.size()-1; i>=0; i--){ //dealing with role chains by parsing as nested Ex.Restrictions
            restriction = owlHelper.getOWLExistentialRestriction(props.get(i), restriction);
        }        
        return owlHelper.getOWLSubClassOfAxiom(sub, restriction);
    } 

    //TODO copied from ELK parser
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
}
