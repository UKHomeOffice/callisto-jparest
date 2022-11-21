package uk.gov.homeoffice.digital.sas.cucumberjparest.persona;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
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
    this.tenantId = System.getProperty(TENANT_ID_SYSTEM_PROPERTY_NAME);
    this.createPersona("someone");
  }

  public static final String TENANT_ID_SYSTEM_PROPERTY_NAME = "cucumber.jparest.tenantId";

  private final String tenantId;

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
    persona.setTenantId(UUID.fromString(tenantId));
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