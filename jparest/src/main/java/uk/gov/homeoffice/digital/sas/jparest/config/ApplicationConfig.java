package uk.gov.homeoffice.digital.sas.jparest.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import uk.gov.homeoffice.digital.sas.jparest.ResourceEndpoint;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.exceptionhandling.ApiResponseExceptionHandler;
import uk.gov.homeoffice.digital.sas.jparest.factory.ResourceApiControllerFactory;
import uk.gov.homeoffice.digital.sas.jparest.factory.ResourceApiServiceFactory;
import uk.gov.homeoffice.digital.sas.jparest.service.BaseEntityCheckerService;
import uk.gov.homeoffice.digital.sas.jparest.service.ControllerRegistererService;
import uk.gov.homeoffice.digital.sas.jparest.swagger.PathItemCreator;
import uk.gov.homeoffice.digital.sas.jparest.swagger.ResourceOpenApiCustomiser;
import uk.gov.homeoffice.digital.sas.jparest.validation.EntityValidator;

@Configuration
@Import({
  ObjectMapperConfig.class,
  JpaRestMvcConfig.class,
  HandlerMappingConfig.class
})
public class ApplicationConfig {


  @Bean
  public OpenApiCustomizer resourceOpenApiCustomiser(ResourceEndpoint endpoint,
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

  @Bean
  public ResourceApiServiceFactory resourceApiServiceFactory(
      EntityManager entityManager,
      EntityValidator entityValidator,
      GenericApplicationContext context,
      PlatformTransactionManager transactionManager,
      BaseEntityCheckerService baseEntityCheckerService) {
    return new ResourceApiServiceFactory(
        entityManager, entityValidator, context, transactionManager, baseEntityCheckerService);
  }

  @Bean
  public ResourceApiControllerFactory resourceApiControllerFactory(
      ObjectMapper objectMapper,
      GenericApplicationContext context,
      ResourceApiServiceFactory resourceApiServiceFactory) {
    return new ResourceApiControllerFactory(objectMapper, context, resourceApiServiceFactory);
  }

  @Bean
  public BaseEntityCheckerService baseEntityCheckerService(EntityManager entityManager) {
    return new BaseEntityCheckerService(entityManager);
  }

  @Bean
  public ControllerRegistererService controllerRegistererService(
          RequestMappingHandlerMapping requestMappingHandlerMapping,
          BaseEntityCheckerService baseEntityCheckerService) {
    return new ControllerRegistererService(requestMappingHandlerMapping, baseEntityCheckerService);
  }

}