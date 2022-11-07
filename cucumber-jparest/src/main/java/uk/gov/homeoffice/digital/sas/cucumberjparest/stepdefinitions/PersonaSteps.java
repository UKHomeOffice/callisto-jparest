package uk.gov.homeoffice.digital.sas.cucumberjparest.stepdefinitions;

import io.cucumber.java.en.Given;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.homeoffice.digital.sas.cucumberjparest.PersonaManager;

/**
 * Provides steps setting up a persona with a name and any configuration required within the
 * service.
 */
public class PersonaSteps {

  private final PersonaManager personaManager;

  @Autowired
  public PersonaSteps(@NonNull PersonaManager personaManager) {
    this.personaManager = personaManager;
  }

  /**
   * Creates a new persona for the given name.
   *
   * @param name The name of the persona to create
   */
  @Given("^(?:the )?(\\S*) is a user$")
  public void personaIsUser(String name) {
    personaManager.createPersona(name);
  }

}
