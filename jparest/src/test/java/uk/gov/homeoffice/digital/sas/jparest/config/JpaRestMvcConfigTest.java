package uk.gov.homeoffice.digital.sas.jparest.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.smile.MappingJackson2SmileHttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import uk.gov.homeoffice.digital.sas.jparest.web.SpelExpressionArgumentResolver;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JpaRestMvcConfigTest {

    @Mock
    MappingJackson2HttpMessageConverter messageConverter;

    @Mock
    MappingJackson2SmileHttpMessageConverter mappingJackson2SmileHttpMessageConverter;

    @Mock
    private ObjectMapper objectMapper;

    @Spy
    private JpaRestMvcConfig jpaRestMvcConfig = new JpaRestMvcConfig(objectMapper);

    @Test
    void addArgumentResolvers_shouldAddArgumentResolvers() {
        List<HandlerMethodArgumentResolver> argumentResolvers = new ArrayList<>();
        jpaRestMvcConfig.addArgumentResolvers(argumentResolvers);
        assertThat(argumentResolvers).hasSize(1);
        assertThat(argumentResolvers.get(0)).isInstanceOf(SpelExpressionArgumentResolver.class);
    }

    @Test
    void extendMessageConverters_shouldCallRegisterObjectMappersForType() {
        List<HttpMessageConverter<?>> converters = List.of(messageConverter);
        jpaRestMvcConfig.extendMessageConverters(converters);
        verify(messageConverter, times(1)).registerObjectMappersForType(any(), any());
    }

    @Test
    void extendMessageConverters_converterTypeNotApplicable_objectMappersNotRegistered() {
        List<HttpMessageConverter<?>> converters = List.of(mappingJackson2SmileHttpMessageConverter);
        jpaRestMvcConfig.extendMessageConverters(converters);
        verifyNoInteractions(messageConverter);
    }

}