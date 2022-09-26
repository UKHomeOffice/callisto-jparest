package uk.gov.homeoffice.digital.sas.cucumberjparest;

import static java.util.Map.entry;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Bean;

import io.cucumber.spring.ScenarioScope;

/**
 * 
 * Class used by the {Link ContextConfiguration} annotation
 * to configure an {@link org.springframework.context.ApplicationContext
 * ApplicationContext} for integration tests. In this case for
 * the cucumber tests.
 */
public class JpaTestContext {

    /**
     * The service registry needs to be accessed to by the
     * Cucumber context and the test runner so that the
     * test runner can spin up an api and pass it's address
     * to the serice registry. As cucumber controls how the context
     * is created for step definitions it was difficult to scope this
     * to one instance for the entire test fixture and so a static
     * reference was used.
     */
    public static final ServiceRegistry serviceRegistry = new ServiceRegistry();

    public static final Map<String, Class<?>> classSimpleStrings = Map.ofEntries(
            entry("String", String.class),
            entry("Object", Object.class),
            entry("Integer", Integer.class),
            entry("Boolean", Boolean.class),
            entry("Decimal", Double.class),
            entry("Map", Map.class),
            entry("List", List.class),
            entry("Instant", Instant.class));

    /**
     * 
     * PersonaManager per scenario
     * 
     * @return PersonaManager
     */
    @ScenarioScope
    @Bean
    public PersonaManager personaManager() {
        return new PersonaManager();
    }

    /**
     * 
     * HttpResponseManager per scenario
     * 
     * @return HttpResponseManager
     */
    @ScenarioScope
    @Bean
    public HttpResponseManager httpResponseManager() {
        return new HttpResponseManager();
    }

    /**
     * 
     * ScenarioState per scenario
     * 
     * @return ScenarioState
     */
    @ScenarioScope
    @Bean
    public ScenarioState scenarioState() {
        return new ScenarioState();
    }

    /**
     * 
     * PayloadManager per scenario
     * 
     * @return PayloadManager
     */
    @ScenarioScope
    @Bean
    public PayloadManager payloadManager() {
        return new PayloadManager();
    }

    /**
     * 
     * Singleton JpaRestApiClient
     * 
     * @return JpaRestApiClient
     */
    @Bean
    public JpaRestApiClient jpaRestApiClient() {
        return new JpaRestApiClient(JpaTestContext.serviceRegistry);
    }

}
