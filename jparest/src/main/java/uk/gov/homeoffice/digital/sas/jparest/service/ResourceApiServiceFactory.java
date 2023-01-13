package uk.gov.homeoffice.digital.sas.jparest.service;

import javax.persistence.EntityManager;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import uk.gov.homeoffice.digital.sas.jparest.EntityUtils;
import uk.gov.homeoffice.digital.sas.jparest.models.BaseEntity;
import uk.gov.homeoffice.digital.sas.jparest.repository.TenantRepository;
import uk.gov.homeoffice.digital.sas.jparest.validation.EntityValidator;

@Component
public class ResourceApiServiceFactory implements BeanFactoryAware {

  private final EntityManager entityManager;
  private final PlatformTransactionManager transactionManager;
  private final EntityValidator entityValidator;
  private ConfigurableBeanFactory configurableBeanFactory;

  public ResourceApiServiceFactory(EntityManager entityManager,
                                   PlatformTransactionManager transactionManager,
                                   EntityValidator entityValidator) {
    this.entityManager = entityManager;
    this.transactionManager = transactionManager;
    this.entityValidator = entityValidator;
  }

  @Override
  public void setBeanFactory(BeanFactory beanFactory) {
    this.configurableBeanFactory = (ConfigurableBeanFactory) beanFactory;
  }

  public <T extends BaseEntity> ResourceApiService<T> getBean(Class<T> resourceClass,
                                                              EntityUtils<T, ?> entityUtils,
                                                              TenantRepository tenantRepository) {

    var resourceApiService = new ResourceApiService<T>(
            entityManager,
            entityUtils,
            transactionManager,
            tenantRepository,
            entityValidator);

    configurableBeanFactory.registerSingleton(
        resourceClass.getSimpleName() + ResourceApiService.class.getSimpleName(),
        resourceApiService);

    return resourceApiService;
  }

}
