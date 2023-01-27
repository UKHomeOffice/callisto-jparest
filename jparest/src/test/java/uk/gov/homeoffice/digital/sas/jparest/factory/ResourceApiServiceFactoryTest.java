package uk.gov.homeoffice.digital.sas.jparest.factory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.function.Predicate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.PlatformTransactionManager;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityA;
import uk.gov.homeoffice.digital.sas.jparest.service.BaseEntityCheckerService;
import uk.gov.homeoffice.digital.sas.jparest.service.ResourceApiService;
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

  @Mock
  private BaseEntityCheckerService baseEntityCheckerService;

  @Mock
  Predicate<Class<?>> mockPredicate;

  private ResourceApiServiceFactory resourceApiServiceFactory;

  @BeforeEach
  void setup() {
    resourceApiServiceFactory = new ResourceApiServiceFactory(
        entityManager, entityValidator, context, transactionManager, baseEntityCheckerService);
  }


  @Test
  void getBean_serviceDependenciesProvided_serviceBeanRegistered() {

    var resourceClass = DummyEntityA.class;

    when(mockPredicate.test(any())).thenReturn(true);
    when(baseEntityCheckerService.getPredicateForBaseEntitySubclassesMap(any()))
        .thenReturn(mockPredicate);

    ResourceApiService<DummyEntityA> actualService = resourceApiServiceFactory.getServiceBean(
        resourceClass);

    assertThat(context.getBean("DummyEntityAResourceApiService"))
        .isNotNull().isInstanceOf(ResourceApiService.class);
    assertThat(actualService).isNotNull();
  }

}
