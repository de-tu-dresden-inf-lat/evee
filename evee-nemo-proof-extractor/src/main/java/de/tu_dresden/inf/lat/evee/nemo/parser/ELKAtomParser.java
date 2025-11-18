package de.tu_dresden.inf.lat.evee.nemo.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import com.google.common.collect.Sets;

import de.tu_dresden.inf.lat.evee.nemo.parser.exceptions.ConceptTranslationError;
import de.tu_dresden.inf.lat.evee.proofs.data.exceptions.ProofNotSupportedException;

public class ELKAtomParser extends AbstractAtomParser{

    private static final String
        EQUIVALENCE_MAIN = "mainEquivClass",
        SUBOF_MAIN = "mainSubClassOf",
        SUBOF_INF = "http://rulewerk.semantic-web.org/inferred/subClassOf",
        SUBOF_NF = "http://rulewerk.semantic-web.org/normalForm/subClassOf",
        SUBOF_PREPARE = "prepareSco",

        SUBOF_EXIST = "http://rulewerk.semantic-web.org/inferred/ex",
    
        SUBPROP = "http://rulewerk.semantic-web.org/normalForm/subProp",
        SUBPROP_DIR = "directSubProp",
        
        SUBPROP_CHAIN = "http://rulewerk.semantic-web.org/normalForm/subPropChain",
        SUBPROP_CHAIN_AUX = "auxPropChain";
        
    private final Set<String> subClassOfNames = Sets.newHashSet(SUBOF_MAIN, SUBOF_INF, SUBOF_NF, SUBOF_PREPARE);
  
    public ELKAtomParser(){}

    public OWLAxiom toOwlAxiom(String atom) throws ProofNotSupportedException, ConceptTranslationError{
        String predName = parsingHelper.getPredicateName(atom);
        List<String> args = parsingHelper.getPredicateArguments(atom);
        
        if(predName.equals(EQUIVALENCE_MAIN))
            return parseEquivalenceClassesAxiom(args);
        if(subClassOfNames.contains(predName))
            return parseSubClassAxiom(args);
        else if(predName.equals(SUBPROP) || predName.equals(SUBPROP_DIR))
            return parseSubProperty(args);
        else if(predName.equals(SUBOF_EXIST))
            return parseSubOfExistential(args);
        else if(predName.equals(SUBPROP_CHAIN) || predName.equals(SUBPROP_CHAIN_AUX))
             return parsePropChain(args);
        else if(predName.equals(TRIPLE))
            return parseTriple(args);
     
        return defaultAxiom;
    }

    private OWLAxiom parseEquivalenceClassesAxiom(List<String> args) throws ProofNotSupportedException, ConceptTranslationError {
        return parseEquivalenceClassesAxiom(args.get(0), args.get(1));
    }

    private OWLAxiom parseSubClassAxiom(List<String> args) throws ProofNotSupportedException, ConceptTranslationError{
        return parseSubClassAxiom(args.get(0), args.get(1));
    }
    
    private OWLAxiom parseSubProperty(List<String> args) throws ProofNotSupportedException {
        if(parsingHelper.isPlaceholder(args.get(1))) // rolechains on right hand side not supported
            throw new ProofNotSupportedException("role chains on right hand side not supported");

		return parseSubProperty(args.get(0), args.get(1));
	}
    
    private OWLAxiom parseSubOfExistential(List<String> args) throws ProofNotSupportedException, ConceptTranslationError{
        OWLClassExpression sub, existCls;
        List<OWLObjectPropertyExpression> props; 
        

        sub = placeholderParser.parseConceptOrPlaceholder(args.get(0));
        existCls = placeholderParser.parseConceptOrPlaceholder(args.get(2));
        props = placeholderParser.parseRoleOrPlaceholder(args.get(1));

        OWLClassExpression restriction = existCls;
        for(int i=props.size()-1; i>=0; i--){ //dealing with role chains by nested Ex.Restrictions
            restriction = owlHelper.getOWLExistentialRestriction(props.get(i), restriction);
        }
        
        return owlHelper.getOWLSubClassOfAxiom(sub,restriction);
    }

    private OWLAxiom parsePropChain(List<String> args) throws ConceptTranslationError, ProofNotSupportedException {
        String supStr = parsingHelper.format(args.get(2));
        if(parsingHelper.isPlaceholder(supStr))
            throw new ProofNotSupportedException("role chains on right hand side not supported");

        OWLObjectPropertyExpression sup = parseProp(supStr);
        List<OWLObjectPropertyExpression> chain = new ArrayList<>();
     
        chain.addAll(placeholderParser.parseRoleOrPlaceholder(args.get(0)));
        chain.addAll(placeholderParser.parseRoleOrPlaceholder(args.get(1)));
    
        return owlHelper.getOWLSubPropertyChainOfAxiom(chain, sup);
    }  
}