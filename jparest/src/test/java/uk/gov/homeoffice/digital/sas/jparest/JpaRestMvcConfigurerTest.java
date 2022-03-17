package uk.gov.homeoffice.digital.sas.jparest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.smile.MappingJackson2SmileHttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JpaRestMvcConfigurerTest {

    @Spy
    private JpaRestMvcConfigurer jpaRestMvcConfigurer;

    @Test
    void shouldAddArgumentResolvers() {

        List<HandlerMethodArgumentResolver> argumentResolvers = new ArrayList<>();

        jpaRestMvcConfigurer.addArgumentResolvers(argumentResolvers);
        Assertions.assertEquals(1, argumentResolvers.size());

    }

    @Test
    void shouldCallRegisterObjectMappersForType() {
        MappingJackson2HttpMessageConverter messageConverter = Mockito.mock(MappingJackson2HttpMessageConverter.class);
        List<HttpMessageConverter<?>> converters = List.of(messageConverter);

        jpaRestMvcConfigurer.extendMessageConverters(converters);
        verify(messageConverter, times(1)).registerObjectMappersForType(any(), any());
    }


    @Test
    void extendMessageConverters_converterTypeNotApplicable_objectMappersNotRegistered() {
        var messageConverter = Mockito.mock(MappingJackson2SmileHttpMessageConverter.class);
        List<HttpMessageConverter<?>> converters = List.of(messageConverter);

        jpaRestMvcConfigurer.extendMessageConverters(converters);
        verifyNoInteractions(messageConverter);
    }

}