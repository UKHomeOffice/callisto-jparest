package uk.gov.homeoffice.digital.sas.jparest;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

public class JpaRestMvcConfigurer implements WebMvcConfigurer {
    
    private final static Logger LOGGER = Logger.getLogger(JpaRestMvcConfigurer.class.getName());

     /**
     * Registers the {@link com.example.misc.ApiRequestParamArgumentResolver}.
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        LOGGER.info("addArgumentResolvers");

       argumentResolvers.add(new ApiRequestParamArgumentResolver());
    }

    /**
     * Modify the list of converters after it has been initialized with
     * a default list to register a specialised object mapper for the
     * {@link com.example.misc.ApiResponse}.
     * 
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
        LOGGER.info("extendMessageConverters");
        for (HttpMessageConverter<?> converter : converters) {
            if (converter instanceof MappingJackson2HttpMessageConverter) {
                Hibernate5Module hibernateModule = new Hibernate5Module();
//                hibernateModule.configure(Hibernate5Module.Feature.SERIALIZE_IDENTIFIER_FOR_LAZY_NOT_LOADED_OBJECTS, true);

                final ObjectMapper om = new ObjectMapper();
                om.registerModule(hibernateModule);
                om.registerModule(new JavaTimeModule());
//                om.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

                Consumer<Map<MediaType, ObjectMapper>> consumer = new Consumer<Map<MediaType, ObjectMapper>>() { 
                    public void accept(Map<MediaType, ObjectMapper> map) { 
                        map.put(MediaType.APPLICATION_JSON, om);; 
                    } 
                };

                ((MappingJackson2HttpMessageConverter) converter).registerObjectMappersForType(ApiResponse.class, consumer);
            }
        }
    }

}