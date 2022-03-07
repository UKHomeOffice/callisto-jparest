package uk.gov.homeoffice.digital.sas.jparest.web;

import org.springframework.core.MethodParameter;
import org.springframework.expression.ParseException;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class SpelExpressionArgumentResolver implements HandlerMethodArgumentResolver {

    SpelExpressionParser expressionParser = new SpelExpressionParser();

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return SpelExpression.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {

        String parameterName = parameter.getParameterName();
        String paramValue = webRequest.getParameter(parameterName);

        if (paramValue != null) {
            try {
                SpelExpression result = (SpelExpression) expressionParser.parseExpression(paramValue);
                return result;
            } catch (ParseException ex) {
                throw new MethodArgumentTypeMismatchException(paramValue, parameter.getParameterType(), parameterName, parameter, ex.getCause());
            }
        }
        return null;
    }

}
