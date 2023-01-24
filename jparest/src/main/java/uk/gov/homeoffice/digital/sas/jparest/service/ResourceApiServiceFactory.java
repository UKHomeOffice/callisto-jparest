package uk.gov.homeoffice.digital.sas.jparest.service;

import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import uk.gov.homeoffice.digital.sas.jparest.EntityUtils;
import uk.gov.homeoffice.digital.sas.jparest.models.BaseEntity;
import uk.gov.homeoffice.digital.sas.jparest.repository.TenantRepository;
import uk.gov.homeoffice.digital.sas.jparest.validation.EntityValidator;

@Component
public class ResourceApiServiceFactory {

  private final EntityValidator entityValidator;
  private final GenericApplicationContext context;
  private final PlatformTransactionManager transactionManager;

  public ResourceApiServiceFactory(EntityValidator entityValidator,
                                   GenericApplicationContext context,
                                   PlatformTransactionManager transactionManager) {
    this.entityValidator = entityValidator;
    this.context = context;
    this.transactionManager = transactionManager;
  }

  public <T extends BaseEntity> ResourceApiService<T> getBean(
      Class<T> resourceClass,
      EntityUtils<T, ?> entityUtils,
      TenantRepository<T> tenantRepository) {

    var resourceApiService = new ResourceApiService<T>(
            entityUtils,
            tenantRepository,
            entityValidator,
            transactionManager);

    context.registerBean(
        resourceClass.getSimpleName() + ResourceApiService.class.getSimpleName(),
        ResourceApiService.class,
        () -> resourceApiService,
        beanDefinition -> beanDefinition.setAutowireCandidate(true));

    return resourceApiService;
  }

}
