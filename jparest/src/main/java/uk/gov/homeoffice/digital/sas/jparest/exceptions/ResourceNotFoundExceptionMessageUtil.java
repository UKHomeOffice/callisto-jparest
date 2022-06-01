package uk.gov.homeoffice.digital.sas.jparest.exceptions;

import java.io.Serializable;
import java.util.Collection;
import java.util.stream.Collectors;

public final class ResourceNotFoundExceptionMessageUtil {


    private static final String RESOURCES_NOT_FOUND_ERROR_FORMAT = "Not all related resources could be found for the following Ids:[%s]";
    private static final String DELETABLE_RELATED_RESOURCES_NOT_FOUND_ERROR_FORMAT = "No related %s resources removed as the following related resources could not be found. Ids:[%s]";


    private ResourceNotFoundExceptionMessageUtil() {}


    public static String relatedResourcesMessage(Collection<Serializable> ids) {
        var idsCsv = ids.stream().map(String::valueOf).collect(Collectors.joining(", "));
        return String.format(RESOURCES_NOT_FOUND_ERROR_FORMAT, idsCsv);
    }


    public static String deletableRelatedResourcesMessage(Class<?> resourceType, Collection<Object> ids) {
        var idsCsv = ids.stream().map(String::valueOf).collect(Collectors.joining(", "));
        return String.format(DELETABLE_RELATED_RESOURCES_NOT_FOUND_ERROR_FORMAT, resourceType.getSimpleName(), idsCsv);
    }



}
