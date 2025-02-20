package de.tu_dresden.inf.lat.evee.nemo.parser;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;

import com.google.common.collect.Sets;

import de.tu_dresden.inf.lat.evee.nemo.parser.exceptions.ConceptTranslationError;
import de.tu_dresden.inf.lat.evee.nemo.parser.tools.ParsingHelper;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInference;

public class ELAtomParser extends AbstractAtomParser{

    private final String
        PRED_NAME_SUBOF_MAIN = "mainSubClassOf",
        PRED_NAME_SUBOF_INF = "http://rulewerk.semantic-web.org/inferred/subClassOf",
        PRED_NAME_SUBOF_NF = "http://rulewerk.semantic-web.org/normalForm/subClassOf",
    

        PRED_NAME_EXISTS_NF = "http://rulewerk.semantic-web.org/normalForm/exists",
        PRED_NAME_EXITS_INF = "http://rulewerk.semantic-web.org/inferred/ex",

        PRED_NAME_CONJUNCTION_NF = "http://rulewerk.semantic-web.org/normalForm/conj",

        PRED_NAME_MAINCLASS = "http://rulewerk.semantic-web.org/normalForm/isMainClass",
        PRED_NAME_SUBCLASS = "http://rulewerk.semantic-web.org/normalForm/isSubClass";



        //TODO role inclusion

    private final Set<String> subClassOfNames = Sets.newHashSet(PRED_NAME_SUBOF_MAIN, PRED_NAME_SUBOF_INF, PRED_NAME_SUBOF_NF);

    private final ParsingHelper parsingHelper = ParsingHelper.getInstance();
    private final PlaceholderParser placeholderParser;    
    
    public ELAtomParser(List<IInference<String>> inferences){
        placeholderParser = new PlaceholderParser(inferences);
    }

    public OWLAxiom toOwlAxiom(String atom){
        String predName = parsingHelper.getPredicateName(atom);
        
        if(subClassOfNames.contains(predName))
            return parseSubClassAxiom(parsingHelper.getPredicateArguments(atom));
        
        return defaultAxiom;
        
    }

    public void printCache(){
        placeholderParser.printCache();
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

}
