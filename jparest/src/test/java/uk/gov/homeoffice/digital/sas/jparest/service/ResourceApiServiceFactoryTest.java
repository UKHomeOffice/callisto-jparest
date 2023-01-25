package uk.gov.homeoffice.digital.sas.jparest.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.PlatformTransactionManager;
import uk.gov.homeoffice.digital.sas.jparest.EntityUtils;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityA;
import uk.gov.homeoffice.digital.sas.jparest.validation.EntityValidator;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.function.Supplier;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@ContextConfiguration(locations = "/test-context.xml")
class ResourceApiServiceFactoryTest {

  @PersistenceContext
  private EntityManager entityManager;

  @Mock
  private EntityValidator entityValidator;

  private ResourceApiServiceFactory resourceApiServiceFactory;

  @Mock
  GenericApplicationContext context;

  @Mock
  PlatformTransactionManager transactionManager;

  @Captor
  ArgumentCaptor<String> serviceNameCaptor;

  @Captor
  ArgumentCaptor<Class<ResourceApiService>> serviceClassCaptor;

  @Captor
  ArgumentCaptor<Supplier<ResourceApiService>> supplierCaptor;

  @BeforeEach
  void setup() {
    resourceApiServiceFactory = new ResourceApiServiceFactory(
        entityManager, entityValidator, context, transactionManager);
  }


  @Test
  void getBean_serviceDependenciesProvided_serviceBeanRegistered() {

    var resourceClass = DummyEntityA.class;
    var entityUtils = new EntityUtils<>(resourceClass, (clazz) -> true);

    ResourceApiService<DummyEntityA> service = resourceApiServiceFactory.getBean(
        resourceClass, entityUtils);

    verify(context).registerBean(
        serviceNameCaptor.capture(),
        serviceClassCaptor.capture(),
        supplierCaptor.capture(),
        any()
    );

    assertThat(serviceNameCaptor.getValue()).isEqualTo("DummyEntityAResourceApiService");
    assertThat(serviceClassCaptor.getValue()).isEqualTo(ResourceApiService.class);
    assertThat(supplierCaptor.getValue().get()).isEqualTo(service);
  }

}
