package uk.gov.homeoffice.digital.sas.jparest.exceptions;

import java.util.UUID;

public class UnexpectedQueryResultException extends RuntimeException {

    public UnexpectedQueryResultException() {
        super();
    }

    public UnexpectedQueryResultException(UUID id) {
        super("An unexpected result occurred whilst querying resource with id: " + id.toString());
    }

}
