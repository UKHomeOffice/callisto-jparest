package uk.gov.homeoffice.digital.sas.jparest.exceptions.addresource;

public class AddResourceException extends RuntimeException {

    private Integer errorCode;

    public AddResourceException(String message) {
        super(message);
    }

    public AddResourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public AddResourceException(String message, Throwable cause, AddResourceErrorCode errorCode) {
        super(message, cause);
        this.errorCode = errorCode.getCode();
    }

    public AddResourceException(String message, AddResourceErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode.getCode();
    }

    public Integer getErrorCode() {
        return errorCode;
    }



}
