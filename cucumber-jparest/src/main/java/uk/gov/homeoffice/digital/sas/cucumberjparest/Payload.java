package uk.gov.homeoffice.digital.sas.cucumberjparest;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 
 * Stores a payload to be sent in an api request
 * 
 */
@AllArgsConstructor
public class Payload {

    @Getter
    @Setter
    private String resourceType;

    @Getter
    @Setter
    private String content;
}