package uk.gov.homeoffice.digital.sas.jparest.exceptions;

public class ResourceConstraintViolationException extends RuntimeException {

    public ResourceConstraintViolationException() {
        super();
    }

    public ResourceConstraintViolationException(String s) {
        super(s);
    }

}
