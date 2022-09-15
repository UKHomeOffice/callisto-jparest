package uk.gov.homeoffice.digital.sas.cucumberjparest;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import io.cucumber.java.AfterAll;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.en.Then;

/**
 * 
 * Step definitions required to fullfil the features
 * for testing the library but will not be packaged
 * for use in the cucumber-jparest package
 * 
 */
public class StepDefinitionsTest {
    
    /**
     * Holds a reference to the application context
     * so that it can be stopped when the test run
     * completes
     */
    private static ConfigurableApplicationContext context = null;

    private final PersonaManager personaManager;
    @Autowired
    public StepDefinitionsTest(PersonaManager personaManager ) {
        this.personaManager = Objects.requireNonNull( personaManager, "personas must not be null" );
    }

    /**
     * Runs the Spring Boot application with the JpaRest module
     * application properties etc.
     * This creates a Test API service to run tests against  
     * 
     */
    @BeforeAll
    public static void before_all() {
        Class<?>[] primarySources = {TestApiRunner.class};
        String[] args = new String[0];
        context = SpringApplication.run(primarySources, args);

        // The port is set to be ephemeral so we need to retrieve the
        // port number and then set the address of the service
        // in the service registry.
        int port = ((ServletWebServerApplicationContext)context).getWebServer().getPort();

        JpaTestContext.serviceRegistry.addService("test", "http://localhost:" + port);

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
    
    @Then("^(\\S*) is a different persona to (\\S*)$")
    public void personas_are_not_the_same(String nameA, String nameB) {
        Persona personaA = personaManager.getPersona(nameA);
        Persona personaB = personaManager.getPersona(nameB);

        assertThat(personaA)
            .withFailMessage("Expected %1s to not be %2s", nameA, nameB)
            .isNotEqualTo(personaB);
    }
}
