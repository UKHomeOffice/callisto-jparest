package uk.gov.homeoffice.digital.sas.jparest.exceptions;

public class TenantIdMismatchException extends IllegalArgumentException {


    public TenantIdMismatchException() {
        super();
    }

    public TenantIdMismatchException(String s) {
        super(s);
    }

}
