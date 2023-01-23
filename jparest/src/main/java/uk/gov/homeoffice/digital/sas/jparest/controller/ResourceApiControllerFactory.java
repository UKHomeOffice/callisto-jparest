package uk.gov.homeoffice.digital.sas.jparest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Component;
import uk.gov.homeoffice.digital.sas.jparest.models.BaseEntity;
import uk.gov.homeoffice.digital.sas.jparest.service.ResourceApiService;

@Component
public class ResourceApiControllerFactory {

  private final ObjectMapper objectMapper;
  private final GenericApplicationContext context;


  public ResourceApiControllerFactory(ObjectMapper objectMapper,
                                      GenericApplicationContext context) {
    this.objectMapper = objectMapper;
    this.context = context;
  }


  public <T extends BaseEntity> ResourceApiController<T> getBean(
      Class<T> resourceClass,
      ResourceApiService<T> resourceApiService) {

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
