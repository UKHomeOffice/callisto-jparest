package uk.gov.homeoffice.digital.sas.cucumberjparest.config;

import io.cucumber.spring.ScenarioScope;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.homeoffice.digital.sas.cucumberjparest.api.HttpResponseManager;
import uk.gov.homeoffice.digital.sas.cucumberjparest.api.JpaRestApiClient;
import uk.gov.homeoffice.digital.sas.cucumberjparest.api.PayloadManager;
import uk.gov.homeoffice.digital.sas.cucumberjparest.api.ResourceHelper;
import uk.gov.homeoffice.digital.sas.cucumberjparest.api.ServiceRegistry;
import uk.gov.homeoffice.digital.sas.cucumberjparest.persona.PersonaManager;
import uk.gov.homeoffice.digital.sas.cucumberjparest.scenarios.ScenarioState;
import uk.gov.homeoffice.digital.sas.cucumberjparest.scenarios.interpolation.Interpolation;

/**
 * Class used by the {Link ContextConfiguration} annotation to configure an
 * {@link org.springframework.context.ApplicationContext ApplicationContext} for integration tests.
 * In this case for the cucumber tests.
 */
@Configuration
public class JpaTestContext {

  public static final Map<String, Class<?>> classSimpleStrings = Map.of(
      String.class.getSimpleName(), String.class,
      Object.class.getSimpleName(), Object.class,
      Integer.class.getSimpleName(), Integer.class,
      Boolean.class.getSimpleName(), Boolean.class,
      BigDecimal.class.getSimpleName(), Double.class,
      Map.class.getSimpleName(), Map.class,
      List.class.getSimpleName(), List.class,
      Instant.class.getSimpleName(), Instant.class);

  /**
   * The service registry needs to be accessed to by the Cucumber context and the test runner so
   * that the test runner can spin up an api and pass its address to the service registry.
   */
  @Bean
  public ServiceRegistry serviceRegistry() {
    return new ServiceRegistry();
  }

  /**
   * PersonaManager per scenario.
   *
   * @return PersonaManager
   */
  @ScenarioScope
  @Bean
  public PersonaManager personaManager() {
    return new PersonaManager();
  }

  /**
   * HttpResponseManager per scenario.
   *
   * @return HttpResponseManager
   */
  @ScenarioScope
  @Bean
  public HttpResponseManager httpResponseManager() {
    return new HttpResponseManager();
  }

  /**
   * ScenarioState per scenario.
   *
   * @return ScenarioState
   */
  @ScenarioScope
  @Bean
  public ScenarioState scenarioState() {
    return new ScenarioState();
  }

  /**
   * PayloadManager per scenario.
   *
   * @return PayloadManager
   */
  @ScenarioScope
  @Bean
  public PayloadManager payloadManager() {
    return new PayloadManager();
  }

  /**
   * Singleton JpaRestApiClient.
   *
   * @return JpaRestApiClient
   */
  @Bean
  public JpaRestApiClient jpaRestApiClient() {
    return new JpaRestApiClient(serviceRegistry());
  }

  /**
   * Singleton Interpolation.
   *
   * @return Interpolation
   */
  @Bean
  public Interpolation interpolation(ConfigurableBeanFactory beanFactory) {
    return new Interpolation(beanFactory);
  }

  @Bean
  public ResourceHelper resourceHelper() {
    return new ResourceHelper(jpaRestApiClient(), personaManager());
  }
}
