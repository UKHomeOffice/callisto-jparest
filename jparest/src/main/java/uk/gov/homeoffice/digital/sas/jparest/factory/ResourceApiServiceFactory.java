package uk.gov.homeoffice.digital.sas.jparest.factory;

import javax.persistence.EntityManager;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import uk.gov.homeoffice.digital.sas.jparest.EntityUtils;
import uk.gov.homeoffice.digital.sas.jparest.models.BaseEntity;
import uk.gov.homeoffice.digital.sas.jparest.repository.TenantRepositoryImpl;
import uk.gov.homeoffice.digital.sas.jparest.service.BaseEntityCheckerService;
import uk.gov.homeoffice.digital.sas.jparest.service.ResourceApiService;
import uk.gov.homeoffice.digital.sas.jparest.validation.EntityValidator;

@Component
public class ResourceApiServiceFactory {

  private final EntityManager entityManager;
  private final EntityValidator entityValidator;
  private final GenericApplicationContext context;
  private final TransactionTemplate transactionTemplate;
  private final BaseEntityCheckerService baseEntityCheckerService;

  public ResourceApiServiceFactory(EntityManager entityManager,
                                   EntityValidator entityValidator,
                                   GenericApplicationContext context,
                                   PlatformTransactionManager transactionManager,
                                   BaseEntityCheckerService baseEntityCheckerService) {
    this.entityManager = entityManager;
    this.entityValidator = entityValidator;
    this.context = context;
    this.transactionTemplate = new TransactionTemplate(transactionManager);
    this.baseEntityCheckerService = baseEntityCheckerService;
  }

  public <T extends BaseEntity> ResourceApiService<T> getServiceBean(Class<T> resourceClass) {

    var entityUtils = new EntityUtils<>(resourceClass, baseEntityCheckerService);

    var resourceApiService = new ResourceApiService<>(
            entityUtils,
            new TenantRepositoryImpl<>(resourceClass, entityManager),
            entityValidator,
            transactionTemplate);

    context.registerBean(
        resourceClass.getSimpleName() + ResourceApiService.class.getSimpleName(),
        ResourceApiService.class,
        () -> resourceApiService,
        beanDefinition -> beanDefinition.setAutowireCandidate(true));

    return resourceApiService;
  }
}
