package uk.gov.homeoffice.digital.sas.jparest;

import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.transaction.PlatformTransactionManager;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.exceptionhandling.ApiResponseExceptionHandler;
import uk.gov.homeoffice.digital.sas.jparest.swagger.PathItemCreator;
import uk.gov.homeoffice.digital.sas.jparest.swagger.ResourceOpenApiCustomiser;

import javax.persistence.EntityManager;
import java.time.Clock;
import java.util.logging.Logger;

@Configuration
public class Config {

    private static final Logger LOGGER = Logger.getLogger(Config.class.getName());

    @Bean
    public Clock utcClock() {
        return Clock.systemUTC();
    }

    @Bean
    @Lazy(false)
    JpaRestMvcConfigurer jpaRestMvcConfigurer() {
        LOGGER.info(("auto configure"));
        return new JpaRestMvcConfigurer();
    }

    @Bean
    @Lazy(false)
    HandlerMappingConfigurer handlerMappingConfigurer(EntityManager entityManager,
                                                      PlatformTransactionManager transactionManager,
                                                      ApplicationContext context,
                                                      ResourceEndpoint resourceEndpoint) {
        LOGGER.info(("auto configure handler mapping"));
        return new HandlerMappingConfigurer(entityManager, transactionManager, context, resourceEndpoint);
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
    public ApiResponseExceptionHandler apiResponseExceptionHandler(Clock clock) {
        return new ApiResponseExceptionHandler(clock);
    }




}
