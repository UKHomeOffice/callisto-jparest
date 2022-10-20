package uk.gov.homeoffice.digital.sas.jparest.config;

import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import uk.gov.homeoffice.digital.sas.jparest.ResourceEndpoint;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.exceptionhandling.ApiResponseExceptionHandler;
import uk.gov.homeoffice.digital.sas.jparest.swagger.PathItemCreator;
import uk.gov.homeoffice.digital.sas.jparest.swagger.ResourceOpenApiCustomiser;
import uk.gov.homeoffice.digital.sas.jparest.validation.EntityValidator;

@Configuration
@Import({
  ObjectMapperConfig.class,
  JpaRestMvcConfig.class,
  HandlerMappingConfig.class,
})
public class ApplicationConfig {

  @Bean
  public OpenApiCustomiser resourceOpenApiCustomiser(ResourceEndpoint endpoint,
                                                     PathItemCreator pathItemCreator) {
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

  @Bean
  public EntityValidator entityValidator() {
    return new EntityValidator();
  }
}