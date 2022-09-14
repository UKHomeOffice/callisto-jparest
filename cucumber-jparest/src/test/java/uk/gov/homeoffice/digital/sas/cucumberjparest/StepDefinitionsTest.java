package uk.gov.homeoffice.digital.sas.cucumberjparest;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;

import io.cucumber.java.en.Then;

/**
 * 
 * Step definitions required to fullfil the features
 * for testing the library but will not be packaged
 * for use in the cucumber-jparest package
 * 
 */
public class StepDefinitionsTest {
    
    private final PersonaManager personaManager;

    @Autowired
    public StepDefinitionsTest(PersonaManager personaManager ) {
        this.personaManager = Objects.requireNonNull( personaManager, "personas must not be null" );
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
