package uk.gov.homeoffice.digital.sas.jparest.exceptions;

import java.util.Set;
import java.util.stream.Collectors;

public class ResourceNotFoundException extends RuntimeException {

    private static final String RESOURCE_NOT_FOUND_ERROR_FORMAT = "Resource with id: %s was not found";
    private static final String RELATED_RESOURCE_NOT_FOUND_ERROR_FORMAT = "No related %s resources removed as the following resources could not be found. Ids:[%s]";

    public ResourceNotFoundException() {
        super();
    }

    public ResourceNotFoundException(Object id) {
        super(String.format(RESOURCE_NOT_FOUND_ERROR_FORMAT, id));
    }

    public ResourceNotFoundException(Set<Object> ids, Object relatedType) {
        super(String.format(RELATED_RESOURCE_NOT_FOUND_ERROR_FORMAT, relatedType, formatIdsToCsv(ids)));
    }

    private static String formatIdsToCsv(Set<Object> notDeletableRelatedIds) {
        return notDeletableRelatedIds.stream().map(String::valueOf).collect(Collectors.joining(", "));
    }

}
