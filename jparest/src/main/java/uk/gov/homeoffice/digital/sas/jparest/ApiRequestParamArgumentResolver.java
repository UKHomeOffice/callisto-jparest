package uk.gov.homeoffice.digital.sas.jparest;

import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;

import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;
import org.springframework.util.NumberUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.annotation.RequestParamMapMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Argument resolver used for parameters annotated with {@link ApiRequestParam}
 * It uses {@link RequestParamMapMethodArgumentResolver} to parse the querystring
 * and then applies post processing to extract paging and filters.
 */
public class ApiRequestParamArgumentResolver extends RequestParamMapMethodArgumentResolver {

    /**
     * Only supports parameters of type {@link ApiRequestParams}
     */ 

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return (parameter.getParameterType() == ApiRequestParams.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, @Nullable ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest, @Nullable WebDataBinderFactory binderFactory) throws Exception {

        final ApiRequestParams qp = new ApiRequestParams();

        // Let the supper parse the query string into key value pairs
        Object resolvedArgument = super.resolveArgument(parameter, mavContainer, webRequest, binderFactory);
        if (resolvedArgument instanceof  Map<?,?>){
            @SuppressWarnings("unchecked")
            Map<String, String> superResult = (Map<String,String>)super.resolveArgument(parameter, mavContainer, webRequest,
            binderFactory);

            // Create a result and assign the page and pagesize and sort if present
            setProperty(new Consumer<Integer>(){ public void accept(Integer i) { qp.setPage(i); }}, superResult, "page");
            setProperty(new Consumer<Integer>(){ public void accept(Integer i) { qp.setPageSize(i); }}, superResult, "pageSize");
            // Requires Java 1.8 for lambda
            // setProperty((i) -> { qp.setPage(i); }, superResult, "page");
            // setProperty((i) -> { qp.setPageSize(i); }, superResult, "pageSize");
            qp.setSort(superResult.get("sort"));

            superResult.remove("page");
            superResult.remove("pageSize");
            superResult.remove("sort");

            // Check for filter criteria
            Iterator<Map.Entry<String,String>> requestParams = superResult.entrySet().iterator();
            while (requestParams.hasNext()) {
                Map.Entry<String, String> rp = requestParams.next();
                String value = rp.getValue();
                if (ApiRequestParamCriteriaParser.isCriteria(value))
                {
                    qp.criteria.add(ApiRequestParamCriteriaParser.parse(rp.getKey(),value));
                    requestParams.remove();
                }
            }

            // place the remaining key pairs into filters
            qp.requestParams.putAll(superResult);
        }
        return qp;
    }

    // Helper method to check if key exists and contains an integer
    private void setProperty(Consumer<Integer> integerSetter , Map<String,String> map, String key) {
        if (map.containsKey(key)) {
            int value = NumberUtils.parseNumber(map.get(key), Integer.class);
            integerSetter.accept(value);
        }
    }
}
