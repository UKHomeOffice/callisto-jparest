package uk.gov.homeoffice.digital.sas.jparest.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.method.support.ModelAndViewContainer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class SpelExpressionArgumentResolverTest {

    @Spy
    private SpelExpressionArgumentResolver expressionArgumentResolver;

    @Mock
    private MethodParameter methodParameter;

    @Mock
    private ModelAndViewContainer modelAndViewContainer;

    @Mock
    private NativeWebRequest nativeWebRequest;

    @Mock
    private WebDataBinderFactory webDataBinderFactory;

    public static final String PARAMETER_NAME = "ParamName";

    public static final String PARAMETER_VALUE = "Hi";

    public static final String PARAMETER_VALUE_2 = "Hi There!!";

    @Test
    void shouldResolveArgument() throws Exception {

        given(methodParameter.getParameterName()).willReturn(PARAMETER_NAME);
        given(nativeWebRequest.getParameter(PARAMETER_NAME)).willReturn(PARAMETER_VALUE);

        SpelExpression expression = (SpelExpression) expressionArgumentResolver.resolveArgument(methodParameter, modelAndViewContainer, nativeWebRequest, webDataBinderFactory);
        String value = expression.getExpressionString();

        assertThat(value).isEqualTo(PARAMETER_VALUE);
        assertDoesNotThrow(() ->
                expressionArgumentResolver.resolveArgument(methodParameter, modelAndViewContainer, nativeWebRequest, webDataBinderFactory));
    }

    @Test
    void shouldThrowParseException() throws Exception {
        given(methodParameter.getParameterName()).willReturn(PARAMETER_NAME);
        given(nativeWebRequest.getParameter(PARAMETER_NAME)).willReturn(PARAMETER_VALUE_2);

        assertThatExceptionOfType(MethodArgumentTypeMismatchException.class)
                .isThrownBy( () -> expressionArgumentResolver.resolveArgument(methodParameter, modelAndViewContainer, nativeWebRequest, webDataBinderFactory));
    }

    @Test
    void shouldNotThrowParseException(){
        given(methodParameter.getParameterName()).willReturn(PARAMETER_NAME);
        given(nativeWebRequest.getParameter(PARAMETER_NAME)).willReturn(PARAMETER_VALUE);

        assertDoesNotThrow(() ->
                expressionArgumentResolver.resolveArgument(methodParameter, modelAndViewContainer, nativeWebRequest, webDataBinderFactory));
    }

}