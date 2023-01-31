package uk.gov.homeoffice.digital.sas.jparest.factory;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.homeoffice.digital.sas.jparest.controller.ResourceApiController;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityA;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(locations = "/test-context.xml")
class ResourceApiControllerFactoryTest {

  @Autowired
  private GenericApplicationContext context;

  @Mock
  private ObjectMapper objectMapper;

  @Mock
  private ResourceApiServiceFactory resourceApiServiceFactory;

  private ResourceApiControllerFactory resourceApiControllerFactory;

  @BeforeEach
  void setup() {
    resourceApiControllerFactory = new ResourceApiControllerFactory(
        objectMapper, context, resourceApiServiceFactory);
  }

  @Test
  void getBean_controllerDependenciesProvided_controllerBeanRegistered() {

    ResourceApiController<DummyEntityA> actualController = resourceApiControllerFactory.getControllerBean(
        DummyEntityA.class);

    assertThat(context.getBean("DummyEntityAResourceApiController"))
        .isNotNull().isInstanceOf(ResourceApiController.class);
    assertThat(actualController).isNotNull();
  }

}
