package uk.gov.homeoffice.digital.sas.jparest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.PlatformTransactionManager;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.exceptionhandling.ApiResponseExceptionHandler;
import uk.gov.homeoffice.digital.sas.jparest.swagger.PathItemCreator;
import uk.gov.homeoffice.digital.sas.jparest.swagger.ResourceOpenApiCustomiser;

import javax.persistence.EntityManager;
import java.util.logging.Logger;

@Configuration
public class Config {

    private static final Logger LOGGER = Logger.getLogger(Config.class.getName());

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new Hibernate5Module());
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }

    @Bean
    @Lazy(false)
    JpaRestMvcConfigurer jpaRestMvcConfigurer(ObjectMapper objectMapper) {
        LOGGER.info(("auto configure"));
        return new JpaRestMvcConfigurer(objectMapper);
    }

    @Bean
    @Lazy(false)
    HandlerMappingConfigurer handlerMappingConfigurer(EntityManager entityManager,
                                                      PlatformTransactionManager transactionManager,
                                                      ApplicationContext context,
                                                      ResourceEndpoint resourceEndpoint) {
        LOGGER.info(("auto configure handler mapping"));
        return new HandlerMappingConfigurer(entityManager, transactionManager, context, resourceEndpoint, objectMapper());
    }

    @Bean
    public OpenApiCustomiser resourceOpenApiCustomiser(ResourceEndpoint endpoint, PathItemCreator pathItemCreator) {
        return new ResourceOpenApiCustomiser(endpoint, pathItemCreator);
    }

    @Bean
    public PathItemCreator pathItemCreator() {
        return new PathItemCreator();
    }

    @Bean
    public ResourceEndpoint singletonBean() {
        return new ResourceEndpoint();
    }

    @Bean
    public ApiResponseExceptionHandler apiResponseExceptionHandler() {
        return new ApiResponseExceptionHandler();
    }

}