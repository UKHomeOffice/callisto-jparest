package uk.gov.homeoffice.digital.sas.cucumberjparest.persona;

import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Manages instances of Personas
 * When new personas are created they can be given any
 * name. This name is used as a variable to reference
 * the persona instance in later steps within a scenario.
 * 
 */
@Component
public class PersonaManager {

  // Holds that state for Personas against the provide persona name
  private final Map<String, Persona> personas = new HashMap<>();

  /**
   * Instantiates the instance and adds the someone persona.
   * This is the persona that is used to create anonymous
   * requests.
   */
  public PersonaManager() {
    this.createPersona("someone");
  }

  /**
   * Creates a new persona with the given name.
   *
   * @param name The name used to reference the persona
   * @return Persona The new persona
   */
  public Persona createPersona(String name) {
    if (personas.containsKey(name)) {
      throw new IllegalArgumentException(name + " already exists");
    }
    Persona persona = new Persona();
    personas.put(name, persona);
    return persona;
  }

  /**
   * Retrieves a persona instance added by the {@link PersonaManager#createPersona(String)} method.
   *
   * @param name The name used to reference the persona
   * @return Persona The persona for the given persona name
   */
  public Persona getPersona(String name) {
    if (!personas.containsKey(name)) {
      throw new IllegalArgumentException(name + " does not exist");
    }
    return personas.get(name);
  }
}