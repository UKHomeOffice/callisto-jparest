package uk.gov.homeoffice.digital.sas.jparest.exceptions;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public final class ResourceNotFoundExceptionMessageUtil {


    private static final String RESOURCES_NOT_FOUND_ERROR_FORMAT = "Not all related resources could be found for the following Ids:[%s]";
    private static final String DELETABLE_RELATED_RESOURCES_NOT_FOUND_ERROR_FORMAT = "No related %s resources removed as the following related resources could not be found. Ids:[%s]";


    private ResourceNotFoundExceptionMessageUtil() {}


    public static String relatedResourcesMessage(Collection<UUID> ids) {
        return String.format(RESOURCES_NOT_FOUND_ERROR_FORMAT, formatIdsToCsv(ids));
    }


    public static String deletableRelatedResourcesMessage(Class<?> resourceType, Collection<UUID> ids) {
        return String.format(DELETABLE_RELATED_RESOURCES_NOT_FOUND_ERROR_FORMAT, resourceType.getSimpleName(), formatIdsToCsv(ids));
    }


    private static String formatIdsToCsv(Collection<UUID> ids) {
        return ids.stream().map(String::valueOf).collect(Collectors.joining(", "));
    }


}
