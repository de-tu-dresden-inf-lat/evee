package de.tu_dresden.inf.lat.evee.nemo.parser;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TransferQueue;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

import com.google.common.collect.Sets;

import de.tu_dresden.inf.lat.evee.nemo.parser.exceptions.ConceptTranslationError;
import de.tu_dresden.inf.lat.evee.nemo.parser.tools.ParsingHelper;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInference;

public class ELAtomParser extends AbstractAtomParser{

    private final String
        SUBOF_MAIN = "mainSubClassOf",
        SUBOF_INF = "http://rulewerk.semantic-web.org/inferred/subClassOf",
        SUBOF_NF = "http://rulewerk.semantic-web.org/normalForm/subClassOf",
        SUBOF_PREPARE = "prepareSco",
    
        SUBPROP = "http://rulewerk.semantic-web.org/normalForm/subProp",
        SUBPROP_DIR = "directSubProp",
        
        SUBOF_EXIST = "http://rulewerk.semantic-web.org/inferred/ex",

        TRIPLE = "TRIPLE",
        EQUIV_TRIPLE = "<http://www.w3.org/2002/07/owl#equivalentClass>",
        SUBOF_TRIPLE= "<http://www.w3.org/2000/01/rdf-schema#subClassOf>";
        
    private final Set<String> subClassOfNames = Sets.newHashSet(SUBOF_MAIN, SUBOF_INF, SUBOF_NF, SUBOF_PREPARE);

    private final ParsingHelper parsingHelper = ParsingHelper.getInstance();
    private final PlaceholderParser placeholderParser;    
    
    public ELAtomParser(){
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
        else if(predName.equals(TRIPLE) && args.get(1).equals(EQUIV_TRIPLE))
            return parseEquivTriple(args);
        else if(predName.equals(TRIPLE) && args.get(1).equals(SUBOF_TRIPLE))
            return parseSubOfTriple(args);

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
        OWLObjectPropertyExpression sub = parseProp(parsingHelper.format(args.get(0)));
        OWLObjectPropertyExpression sup = parseProp(parsingHelper.format(args.get(1)));
        
		return owlHelper.getOWLSubObjectPropertyAxiom(sub, sup);
	}
    
    private OWLAxiom parseSubOfExistential(List<String> args){
        OWLClassExpression sub, existCls;
        OWLObjectPropertyExpression prop = parseProp(parsingHelper.format(args.get(1)));
        
        try{
            sub = placeholderParser.parseConceptOrPlaceholder(args.get(0));
            existCls = placeholderParser.parseConceptOrPlaceholder(args.get(2));
        } catch (ConceptTranslationError e) {
            return defaultAxiom;
        }
        
        return owlHelper.getOWLSubClassOfAxiom(sub,
        owlHelper.getOWLExistentialRestriction(prop, existCls));
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
}
