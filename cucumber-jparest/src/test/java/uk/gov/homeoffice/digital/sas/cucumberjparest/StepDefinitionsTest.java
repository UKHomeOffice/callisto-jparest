package uk.gov.homeoffice.digital.sas.cucumberjparest;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Objects;

import io.cucumber.java.Before;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import io.cucumber.java.AfterAll;
import io.cucumber.java.en.Then;
import uk.gov.homeoffice.digital.sas.cucumberjparesttestapi.TestApiRunner;

/**
 * 
 * Step definitions required to fulfill the features
 * for testing the library but will not be packaged
 * for use in the cucumber-jparest package
 * 
 */
public class StepDefinitionsTest { //NOSONAR

    /**
     * Holds a reference to the application context
     * so that it can be stopped when the test run
     * completes
     */
    private static ConfigurableApplicationContext context = null;
    private static boolean initialised = false;

    private final PersonaManager personaManager;

    private final ServiceRegistry serviceRegistry;

    public StepDefinitionsTest(PersonaManager personaManager, ServiceRegistry serviceRegistry) {
        this.personaManager = Objects.requireNonNull(personaManager, "personas must not be null");
        this.serviceRegistry = serviceRegistry;
    }

    /**
     * Runs the Spring Boot application with the JpaRest module
     * application properties etc.
     * This creates a Test API service to run tests against
     * 
     */
    @Before
    public void before_all() {
        if (!initialised) {

            Class<?>[] primarySources = { TestApiRunner.class };
            String[] args = new String[0];
            context = SpringApplication.run(primarySources, args);

            // The port is set to be ephemeral, so we need to retrieve the
            // port number and then set the address of the service
            // in the service registry.
            int port = ((ServletWebServerApplicationContext) context).getWebServer().getPort();

            serviceRegistry.setServices(Map.of("test", "http://localhost:" + port));
            initialised = true;
        }
    }

    /**
     * Runs after all cucumber tests and ensures the Test API
     * has been stopped
     */
    @AfterAll
    public static void after_all() {
        // If a context exists the Test API was successfully started
        // shut it down
        if (context != null) {
            SpringApplication.exit(context);
        }

    }

    
    /** 
     * 
     * Step used to demonstrate the persona contexts are isolated.
     * This step is not packaged as itpurely for the purpose of testing
     * cucumber-jparest
     * 
     * @param nameA The name of the persona to compare
     * @param nameB The name of the persona to be compared with
     */
    @Then("^(\\S*) is a different persona to (\\S*)$")
    public void personas_are_not_the_same(String nameA, String nameB) {
        Persona personaA = personaManager.getPersona(nameA);
        Persona personaB = personaManager.getPersona(nameB);

        assertThat(personaA)
                .withFailMessage("Expected %1s to not be %2s", nameA, nameB)
                .isNotEqualTo(personaB);
    }
}
