package uk.gov.homeoffice.digital.sas.cucumberjparest;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * For future implementation for storing state about a 
 * persona within a scenario. An example would
 * be to store an auth token for the user which will
 * then be added to subsequent requests.
 * 
 */
public class Persona {

    @Getter
    @Setter
    private String authToken;
}