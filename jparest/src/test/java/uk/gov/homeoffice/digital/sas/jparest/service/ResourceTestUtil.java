package uk.gov.homeoffice.digital.sas.jparest.service;

import uk.gov.homeoffice.digital.sas.jparest.models.BaseEntity;

import java.util.UUID;

public final class ResourceTestUtil {

    private ResourceTestUtil() {
        // no instantiation
    }

    public static final String ID_FIELD_NAME = "id";

    static <T extends BaseEntity> T getResource(Class<?> resourceClass) {
        try {
            return (T) resourceClass.getConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Error creating object of resource type: " + resourceClass.getSimpleName());
        }
    }

    static <T extends BaseEntity> T getResourceWithId(Class<?> resourceClass, UUID resourceId) {
        T resource = getResource(resourceClass);
        resource.setId(resourceId);
        return resource;
    }

    static <T extends BaseEntity> T getResourceWithId(Class<?> resourceClass, UUID resourceId, UUID tenantId) {
        T resource = getResourceWithId(resourceClass, resourceId);
        resource.setTenantId(tenantId);
        return resource;
    }

}
