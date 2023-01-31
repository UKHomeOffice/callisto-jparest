package uk.gov.homeoffice.digital.sas.jparest.factory;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Component;
import uk.gov.homeoffice.digital.sas.jparest.controller.ResourceApiController;
import uk.gov.homeoffice.digital.sas.jparest.models.BaseEntity;

@Component
public class ResourceApiControllerFactory {

  private final ObjectMapper objectMapper;
  private final GenericApplicationContext context;
  private final ResourceApiServiceFactory resourceApiServiceFactory;

  public ResourceApiControllerFactory(ObjectMapper objectMapper,
                                      GenericApplicationContext context,
                                      ResourceApiServiceFactory resourceApiServiceFactory) {
    this.objectMapper = objectMapper;
    this.context = context;
    this.resourceApiServiceFactory = resourceApiServiceFactory;
  }


  public <T extends BaseEntity> ResourceApiController<T> getControllerBean(
      Class<T> resourceClass) {

    var resourceApiService = resourceApiServiceFactory.getServiceBean(resourceClass);

    var controller = new ResourceApiController<>(
        resourceClass, resourceApiService, objectMapper);

    context.registerBean(
        resourceClass.getSimpleName() + ResourceApiController.class.getSimpleName(),
        ResourceApiController.class,
        () -> controller,
        beanDefinition -> beanDefinition.setAutowireCandidate(true));

    return controller;
  }
}
