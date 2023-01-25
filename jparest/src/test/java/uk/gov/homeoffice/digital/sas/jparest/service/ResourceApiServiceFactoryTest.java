package uk.gov.homeoffice.digital.sas.jparest.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.PlatformTransactionManager;
import uk.gov.homeoffice.digital.sas.jparest.EntityUtils;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityA;
import uk.gov.homeoffice.digital.sas.jparest.validation.EntityValidator;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@SpringBootTest
@ContextConfiguration(locations = "/test-context.xml")
class ResourceApiServiceFactoryTest {

  @PersistenceContext
  private EntityManager entityManager;

  @Mock
  private EntityValidator entityValidator;

  @Autowired
  private GenericApplicationContext context;

  @Mock
  private PlatformTransactionManager transactionManager;

  private ResourceApiServiceFactory resourceApiServiceFactory;

  @BeforeEach
  void setup() {
    resourceApiServiceFactory = new ResourceApiServiceFactory(
        entityManager, entityValidator, context, transactionManager);
  }


  @Test
  void getBean_serviceDependenciesProvided_serviceBeanRegistered() {

    var resourceClass = DummyEntityA.class;
    var entityUtils = new EntityUtils<>(resourceClass, (clazz) -> true);

    ResourceApiService<DummyEntityA> actualService = resourceApiServiceFactory.getBean(
        resourceClass, entityUtils);

    assertThat(context.getBean("DummyEntityAResourceApiService"))
        .isNotNull().isInstanceOf(ResourceApiService.class);
    assertThat(actualService).isNotNull();
  }

}
