package de.tu_dresden.inf.lat.evee.nemo.parser.tools;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Christian Alrabbaa
 *
 */
public class ParsingHelper {
    private static final String idReg = "[_:]*[0-9a-zA-Z]+[0-9a-zA-Z-]*";
    private static final String predicateNameReg = "<?[a-zA-z0-9]+>?(?=\\()";
    private static final String predicateArgumentsReg = "(?<=\\()[^()]*(?=\\))";
    private static final String predicateReg = "<?[a-zA-z0-9]+>?[(][^()]*[)]";
    private ParsingHelper() {
    }

    private static class LazyHolder {
        static ParsingHelper instance = new ParsingHelper();
    }

    public static ParsingHelper getInstance() {
        return ParsingHelper.LazyHolder.instance;
    }

    public boolean isID(String arg) {
        arg = arg.replaceAll("\"", "");
        return !arg.contains("#") && arg.matches(idReg);
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

        return new ArrayList<>(Arrays.asList(matcher.group().split(",")));
    }
}
