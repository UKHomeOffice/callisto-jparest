package uk.gov.homeoffice.digital.sas.jparest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityA;
import uk.gov.homeoffice.digital.sas.jparest.service.ResourceApiService;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(locations = "/test-context.xml")
class ResourceApiControllerFactoryTest {

  @Autowired
  private GenericApplicationContext context;

  @Mock
  private ObjectMapper objectMapper;

  @Mock
  private ResourceApiService<DummyEntityA> resourceApiService;

  private ResourceApiControllerFactory resourceApiControllerFactory;

  @BeforeEach
  void setup() {
    resourceApiControllerFactory = new ResourceApiControllerFactory(objectMapper, context);
  }

  @Test
  void getBean_serviceDependenciesProvided_controllerBeanRegistered() {

    ResourceApiController<DummyEntityA> actualController = resourceApiControllerFactory.getBean(
        DummyEntityA.class, resourceApiService);

    assertThat(context.getBean("DummyEntityAResourceApiController"))
        .isNotNull().isInstanceOf(ResourceApiController.class);
    assertThat(actualController).isNotNull();
  }

}
