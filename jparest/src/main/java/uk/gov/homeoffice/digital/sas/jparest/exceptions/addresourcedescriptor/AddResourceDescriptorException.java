package uk.gov.homeoffice.digital.sas.jparest.exceptions.addresourcedescriptor;

public class AddResourceDescriptorException extends RuntimeException {

    private final Integer errorCode;

    public AddResourceDescriptorException(String message, Integer errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public Integer getErrorCode() {
        return errorCode;
    }



}
