package uk.gov.homeoffice.digital.sas.jparest.service;

import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.transaction.PlatformTransactionManager;
import uk.gov.homeoffice.digital.sas.jparest.EntityUtils;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityA;
import uk.gov.homeoffice.digital.sas.jparest.repository.TenantRepository;
import uk.gov.homeoffice.digital.sas.jparest.validation.EntityValidator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ResourceApiServiceFactoryTest  {

  @Mock
  private PlatformTransactionManager transactionManager;

  @Mock
  private EntityValidator entityValidator;

  @Mock
  private ConfigurableBeanFactory configurableBeanFactory;

  @Mock
  private TenantRepository<DummyEntityA> tenantRepository;

  private ResourceApiServiceFactory resourceApiServiceFactory;

  @BeforeEach
  void setup() {
    resourceApiServiceFactory = new ResourceApiServiceFactory(
        transactionManager,
        entityValidator);
    resourceApiServiceFactory.setBeanFactory(configurableBeanFactory);
  }


  @Test
  void getBean_serviceDependenciesProvided_serviceBeanRegistered() {

    var resourceClass = DummyEntityA.class;
    var entityUtils = new EntityUtils<>(resourceClass, (clazz) -> {return true;});

    ResourceApiService<DummyEntityA> service = resourceApiServiceFactory.getBean(
        resourceClass, entityUtils, tenantRepository);

    assertThat(service).isNotNull();
    verify(configurableBeanFactory).registerSingleton("DummyEntityAResourceApiService", service);
  }

}
