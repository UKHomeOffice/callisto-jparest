package uk.gov.homeoffice.digital.sas.jparest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.homeoffice.digital.sas.jparest.web.SpelExpressionArgumentResolver;

import java.util.List;

public class JpaRestMvcConfigurer implements WebMvcConfigurer {

    private final ObjectMapper objectMapper = getObjectMapper();

    /**
     * Registers the {@link com.example.misc.ApiRequestParamArgumentResolver}.
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(new SpelExpressionArgumentResolver());
    }

    private static ObjectMapper getObjectMapper() {
        var om = new ObjectMapper();
        om.registerModule(new Hibernate5Module());
        om.registerModule(new JavaTimeModule());
        return om;
    }

    /**
     * Modify the list of converters after it has been initialized with
     * a default list to register a specialised object mapper for the
     * {@link com.example.misc.ApiResponse}.
     * <p>
     * The specialised ObjectMapper registers the
     * {@link com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module}
     * module. This prevents lazy loading of related entities.The ObjectMapper
     * is also configured to not serialise empty objects.
     *
     * @param converters the list of configured converters to be extended
     * @since 4.1.3
     */
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        for (HttpMessageConverter<?> converter : converters) {
            if (converter instanceof MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter) {
                mappingJackson2HttpMessageConverter.registerObjectMappersForType(
                        ApiResponse.class,
                        map -> map.put(MediaType.APPLICATION_JSON, objectMapper)
                );
            }
        }
    }

}