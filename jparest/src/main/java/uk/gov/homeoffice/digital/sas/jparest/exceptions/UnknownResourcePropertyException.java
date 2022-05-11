package uk.gov.homeoffice.digital.sas.jparest.exceptions;

public class UnknownResourcePropertyException extends RuntimeException {

    private static final String UNKNOWN_PROPERTY_ERROR_FORMAT = "%s is an unknown property for the resource entity: %s";

    public UnknownResourcePropertyException() {
        super();
    }

    public UnknownResourcePropertyException(String s1, String s2) {
        super(String.format(UNKNOWN_PROPERTY_ERROR_FORMAT, s1, s2));
    }

}
