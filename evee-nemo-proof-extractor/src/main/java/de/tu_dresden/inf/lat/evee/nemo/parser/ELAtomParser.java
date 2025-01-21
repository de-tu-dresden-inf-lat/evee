package de.tu_dresden.inf.lat.evee.nemo.parser;

public class ELAtomParser extends AbstractAtomParser{

    private final String
        PRED_NAME_SUBOF_MAIN = "MainSubClassOf",
        PRED_NAME_SUBOF_INF = "http://rulewerk.semantic-web.org/inferred/subClassOf",
        PRED_NAME_SUBOF_NF = "http://rulewerk.semantic-web.org/normalForm/subClassOf",

        PRED_NAME_EXISTS_NF = "http://rulewerk.semantic-web.org/normalForm/exists",
        PRED_NAME_EXITS_INF = "http://rulewerk.semantic-web.org/inferred/ex",

        PRED_NAME_CONJUNCTION_NF = "http://rulewerk.semantic-web.org/normalForm/conj",

        PRED_NAME_MAINCLASS = "http://rulewerk.semantic-web.org/normalForm/isMainClass",
        PRED_NAME_SUBCLASS = "http://rulewerk.semantic-web.org/normalForm/isSubClass"; 

        //TODO role inclusion
    
    public ELAtomParser(){
        super();
        addAtomParsing(PRED_NAME_SUBOF_MAIN, parseSubClassAxiom);
        addAtomParsing(PRED_NAME_SUBOF_INF, parseSubClassAxiom);
        addAtomParsing(PRED_NAME_SUBOF_NF, parseSubClassAxiom);

    }

}