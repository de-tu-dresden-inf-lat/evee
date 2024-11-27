package de.tu_dresden.inf.lat.evee.nemo.parser.tools;

import java.util.*;
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
        Pattern pattern = Pattern.compile(predicateNameReg);
        if(isPredicate(predicateStr))
            return pattern.matcher(predicateStr).group(1);
        return "";
    }

    public List<String> getPredicateArguments(String predicateStr){
        Pattern pattern = Pattern.compile(predicateArgumentsReg);
        if(isPredicate(predicateStr))
            return new ArrayList<>(Arrays.asList(pattern.matcher(predicateStr).group(1).split(",")));
        return Collections.emptyList();
    }
}
