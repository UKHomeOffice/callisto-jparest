package uk.gov.homeoffice.digital.sas.jparest.exceptions.addresourcedescriptor;

public class AddResourceDescriptorException extends RuntimeException {

    private Integer errorCode;

    public AddResourceDescriptorException(String message) {
        super(message);
    }

    public AddResourceDescriptorException(String message, Throwable cause) {
        super(message, cause);
    }

    public AddResourceDescriptorException(String message, Throwable cause, AddResourceDescriptorErrorCode errorCode) {
        super(message, cause);
        this.errorCode = errorCode.getCode();
    }

    public AddResourceDescriptorException(String message, AddResourceDescriptorErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode.getCode();
    }

    public Integer getErrorCode() {
        return errorCode;
    }



}
