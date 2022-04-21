package uk.gov.homeoffice.digital.sas.jparest.exceptions;

public class InvalidTenantIdException extends RuntimeException {


    public InvalidTenantIdException() {
        super();
    }

    public InvalidTenantIdException(String s) {
        super(s);
    }

}
