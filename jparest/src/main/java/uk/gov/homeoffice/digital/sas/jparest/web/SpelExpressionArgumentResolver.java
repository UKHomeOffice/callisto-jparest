package uk.gov.homeoffice.digital.sas.jparest.web;

import java.util.Objects;

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
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws MethodArgumentTypeMismatchException {

        String parameterName = Objects.requireNonNull(parameter.getParameterName());
        String paramValue = webRequest.getParameter(parameterName);

        if (paramValue != null) {
            try {
                return expressionParser.parseExpression(paramValue);
            } catch (ParseException ex) {
                throw new MethodArgumentTypeMismatchException(paramValue, parameter.getParameterType(), parameterName, parameter, ex.getCause());
            }
        }
        return null;
    }

}
