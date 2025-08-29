package de.tu_dresden.inf.lat.evee.nemo.parser.tools;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Christian Alrabbaa
 *
 */
public class ParsingHelper {
    private static final String predicateNameReg = "<?[a-zA-z0-9:/.-]+>?(?=\\()";
    private static final String predicateArgumentsReg = "(?<=\\()[^()]*(?=\\))";
    private static final String predicateReg = "<?[a-zA-z0-9:/.-]+>?[(][^()]*[)]";
    private static final String tripleReq = "TRIPLE[(][^()]*[)]";
    private static final String placeholderReg = "_:[0-9]{1,3}";
    private static final String rulenameReg = "[a-zA-Z^+-_\\\\∃⊥⊤∘⊑⊓]+(?=: )";

    private ParsingHelper() {}

    private static class LazyHolder {
        static ParsingHelper instance = new ParsingHelper();
    }

    public static ParsingHelper getInstance() {
        return ParsingHelper.LazyHolder.instance;
    }

    public boolean isRdfTriple(String predicateStr){
        return predicateStr.matches(tripleReq);
    }

    public boolean isPredicate(String predicateStr){
        return predicateStr.matches(predicateReg);
    }

    public String getPredicateName(String predicateStr){
        if(!isPredicate(predicateStr))
            return "";

        Pattern pattern = Pattern.compile(predicateNameReg);
        Matcher matcher = pattern.matcher(predicateStr);

        if (!matcher.find()) //no match
            return "";
        
        return matcher.group();
    }

    public List<String> getPredicateArguments(String predicateStr){
        if(!isPredicate(predicateStr))
            return Collections.emptyList();

        Pattern pattern = Pattern.compile(predicateArgumentsReg);
        Matcher matcher = pattern.matcher(predicateStr);

        if (!matcher.find()) //no match
            return Collections.emptyList();

        String [] args =  matcher.group().split(",");

        //removing whitespaces
        return Arrays.stream(args).map(String::trim).collect(Collectors.toList());
    }

    public boolean isPlaceholder(String arg){
        return arg.matches(placeholderReg);
    }

    public boolean containsPlaceholders(String predicateStr){

        Pattern pattern = Pattern.compile(placeholderReg);
        Matcher matcher = pattern.matcher(predicateStr);

        return matcher.find();
    }

    public int countPlaceholders(String predicateStr){
        Pattern pattern = Pattern.compile(placeholderReg);
        Matcher matcher = pattern.matcher(predicateStr);

        int count = 0;
        while (matcher.find())
            count++;
        
        return count;
    }

    public List<String> getPlaceholders(String predicateStr){

        Pattern pattern = Pattern.compile(placeholderReg);
        Matcher matcher = pattern.matcher(predicateStr);
            
        List<String> placeholders = new  ArrayList<String>();
        while(matcher.find()){
            placeholders.add(matcher.group());
        }

        return placeholders;
    }

    public String getRuleName(String ruleStr){
        Pattern pattern = Pattern.compile(rulenameReg);
        Matcher matcher = pattern.matcher(ruleStr);

        if (!matcher.find()) //no match
            return "";
        
        return matcher.group();
    }

    public String format(String arg) {
		if (arg.startsWith("<") && arg.endsWith(">"))
			return arg.substring(1, arg.length() - 1);
		return arg;
	}

}
