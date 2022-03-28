package uk.gov.homeoffice.digital.sas.jparest.exceptions.addresourcedescriptor;

public enum AddResourceDescriptorErrorCode {

    RESOURCE_ALREADY_EXISTS(10),
    RESOURCE_DOES_NOT_EXIST(20),
    RELATED_RESOURCE_ALREADY_EXISTS(30);

    private final int code;

    AddResourceDescriptorErrorCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

}
