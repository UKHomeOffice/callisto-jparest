
package uk.gov.homeoffice.digital.sas.jparest;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.criteria.Predicate;

/**
 * Argument resolver used for parameters annotated with {@link ApiRequestParam}
 * It uses {@link RequestParamMapMethodArgumentResolver} to parse the querystring
 * and then applies post processing to extract paging and filters.
 */
public class ApiRequestParamCriteriaParser {

    private static Map<Pattern, CriteriaFunction> criteria = getCriteriaPatterns();
    
    private static Map<Pattern, CriteriaFunction> getCriteriaPatterns() {
        Map<Pattern, CriteriaFunction> result = new HashMap<Pattern, CriteriaFunction>();
        for (CriteriaFunction cf : CriteriaFunction.values()) {
            result.put(Pattern.compile(Pattern.quote(cf.name())+ "\\(([^)]*)\\)"), cf);
        }
        return result;

    }
        
    static boolean isCriteria(String requestParamValue) {
        for (Map.Entry<Pattern, CriteriaFunction> entry : criteria.entrySet()) {
            if (entry.getKey().matcher(requestParamValue).matches()){
                return true;
            }
        }
        return false;
    }

    static Criteria parse(String fieldName, String requestParamValue){

        for (Map.Entry<Pattern, CriteriaFunction> entry : criteria.entrySet()) {
            Matcher matcher = entry.getKey().matcher(requestParamValue);
            if (matcher.matches()){
                return new Criteria(fieldName, entry.getValue(), matcher.group(1));
            }
        }
        return null;

    }

}
