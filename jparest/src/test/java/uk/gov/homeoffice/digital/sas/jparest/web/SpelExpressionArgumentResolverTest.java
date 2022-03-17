package uk.gov.homeoffice.digital.sas.jparest.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class SpelExpressionArgumentResolverTest {

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


    @ParameterizedTest
    @ValueSource(strings = {
        "1 != 2",
        "index == 1",
        "name == \"Ricardo\"",
        "1 != 2 && 2 != 3 || 3 != 4"
    })
    void shouldResolveArgument(String parameterValue){

        given(methodParameter.getParameterName()).willReturn(PARAMETER_NAME);
        given(nativeWebRequest.getParameter(PARAMETER_NAME)).willReturn(parameterValue);

        SpelExpression expression = (SpelExpression) assertDoesNotThrow(() -> expressionArgumentResolver
                .resolveArgument(methodParameter, modelAndViewContainer, nativeWebRequest, webDataBinderFactory));

        String value = expression.getExpressionString();
        assertThat(value).isEqualTo(parameterValue);

                        
        verifyNoInteractions(modelAndViewContainer);
        verifyNoInteractions(webDataBinderFactory);
        verify(methodParameter).getParameterName();
        verify(nativeWebRequest).getParameter(PARAMETER_NAME);

    }

    @ParameterizedTest
    @ValueSource(strings = {
        "1 !== 2",
        "index == 1)",
        "name == \"Ric\"ardo\"",
        "1 != 2 &(&) 2 != 3 || 3 != 4"
    })
    void shouldThrowParseException(String parameterValue) {
        given(methodParameter.getParameterName()).willReturn(PARAMETER_NAME);
        given(nativeWebRequest.getParameter(PARAMETER_NAME)).willReturn(parameterValue);

        assertThatExceptionOfType(MethodArgumentTypeMismatchException.class)
                .isThrownBy(() -> expressionArgumentResolver.resolveArgument(methodParameter, modelAndViewContainer,
                        nativeWebRequest, webDataBinderFactory));

        verifyNoInteractions(modelAndViewContainer);
        verifyNoInteractions(webDataBinderFactory);
        verify(methodParameter).getParameterName();
        verify(nativeWebRequest).getParameter(PARAMETER_NAME);

    }


}