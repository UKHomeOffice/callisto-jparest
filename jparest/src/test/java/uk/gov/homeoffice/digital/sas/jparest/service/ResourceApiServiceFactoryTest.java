package uk.gov.homeoffice.digital.sas.jparest.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import uk.gov.homeoffice.digital.sas.jparest.EntityUtils;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityA;
import uk.gov.homeoffice.digital.sas.jparest.repository.TenantRepository;
import uk.gov.homeoffice.digital.sas.jparest.validation.EntityValidator;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ResourceApiServiceFactoryTest  {

  @Mock
  private EntityValidator entityValidator;

  @Mock
  private TenantRepository<DummyEntityA> tenantRepository;

  private ResourceApiServiceFactory resourceApiServiceFactory;

  @Mock
  GenericApplicationContext context;

  @Mock
  PlatformTransactionManager transactionManager;

  @BeforeEach
  void setup() {
    resourceApiServiceFactory = new ResourceApiServiceFactory(entityValidator, context, transactionManager);
  }


  @Test
  <T> void getBean_serviceDependenciesProvided_serviceBeanRegistered() {

    var resourceClass = DummyEntityA.class;
    var entityUtils = new EntityUtils<>(resourceClass, (clazz) -> {return true;});

    ResourceApiService<DummyEntityA> service = resourceApiServiceFactory.getBean(
        resourceClass, entityUtils, tenantRepository);

    assertThat(service).isNotNull();
    Supplier<?> expectedServiceLambda = () -> service;
    verify(context).registerBean(
        eq("DummyEntityAResourceApiService"),
        eq(ResourceApiService.class),
            eq(expectedServiceLambda),
        any());
  }

}
