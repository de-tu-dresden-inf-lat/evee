package de.tu_dresden.inf.lat.evee.nemo.parser;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

import com.google.common.collect.Sets;

import de.tu_dresden.inf.lat.evee.nemo.parser.exceptions.ConceptTranslationError;

public class TextbookAtomParser extends AbstractAtomParser{

    private final String
        EQUIVALENCE_MAIN = "mainEquivClass",
        SUBOF_MAIN = "mainSubClassOf",
        SUBOF_INF = "http://rulewerk.semantic-web.org/inferred/subClassOf",
        SUBOF_NF = "http://rulewerk.semantic-web.org/normalForm/subClassOf",
        SUBOF_PREPARE = "prepareSco",

        SUBPROP = "http://rulewerk.semantic-web.org/normalForm/subProp",
        SUBPROP_DIR = "directSubProp",

        SUB_CONJ_INF = "http://rulewerk.semantic-web.org/inferred/subClassConj",
        SUB_EX_INF = "http://rulewerk.semantic-web.org/inferred/subClassEx",
        SUP_EX_INF = "http://rulewerk.semantic-web.org/inferred/supClassEx";
        
    private final Set<String> subClassOfNames = Sets.newHashSet(SUBOF_MAIN, SUBOF_INF, SUBOF_NF, SUBOF_PREPARE);

    public TextbookAtomParser(){}

    @Override
    public OWLAxiom toOwlAxiom(String atom) {
        String predName = parsingHelper.getPredicateName(atom);
        List<String> args = parsingHelper.getPredicateArguments(atom);

        if(subClassOfNames.contains(predName))
            return parseSubClassAxiom(args);
        else if(predName.equals(EQUIVALENCE_MAIN))
            return parseEquivalenceClassesAxiom(args);
        else if(predName.equals(SUBPROP) || predName.equals(SUBPROP_DIR))
            return parseSubProperty(args);
        else if(predName.equals(SUB_CONJ_INF))
            return parseSubClassConjunction(args);
        else if(predName.equals(SUB_EX_INF))
            return parseSubClassExistential(args);
        else if(predName.equals(SUP_EX_INF))
            return parseSupClassExistential(args);
        else if(predName.equals(TRIPLE))
            return parseTriple(args);

        return defaultAxiom;
    }

    private OWLAxiom parseEquivalenceClassesAxiom(List<String> args) {
        return parseEquivalenceClassesAxiom(args.get(0), args.get(1));
    }

    private OWLAxiom parseSubClassAxiom(List<String> args){
        return parseSubClassAxiom(args.get(0), args.get(1));
    }
    
    private OWLAxiom parseSubProperty(List<String> args) {      
		return parseSubProperty(args.get(0), args.get(1));
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
    
}
