package uk.gov.homeoffice.digital.sas.jparest;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.addresource.AddResourceErrorCode;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.addresource.AddResourceException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ResourceEndpoint {

    public static final String RESOURCE_ALREADY_ADDED = "Resource has already been added";
    public static final String CALL_ADD_RELATED_ONLY_ON_EXISTING_RESOURCES = "You can only call AddRelated on resources already passed to the Add method";
    public static final String RELATED_RESOURCE_ALREADY_ADDED = "Related resource has already been added";


    @Getter
    private List<Class<?>> resourceTypes = new ArrayList<>();

    @Getter
    private Map<Class<?>, RootDescriptor> descriptors = new HashMap<>();

    public void add(Class<?> clazz, String path, Class<?> idFieldType) {

        if (descriptors.containsKey(clazz)) {
            throw new AddResourceException(RESOURCE_ALREADY_ADDED, AddResourceErrorCode.RESOURCE_ALREADY_EXISTS);
        }

        var rootDescriptor = new RootDescriptor(idFieldType, path);
        descriptors.put(clazz, rootDescriptor);

    }

    public void addRelated(Class<?> clazz, Class<?> relatedClazz, String path, Class<?> idFieldType) {

        if (!descriptors.containsKey(clazz)) {
            throw new AddResourceException(CALL_ADD_RELATED_ONLY_ON_EXISTING_RESOURCES, AddResourceErrorCode.RESOURCE_DOES_NOT_EXIST);
        }

        var rootDescriptor = descriptors.get(clazz);

        if (rootDescriptor.getRelations().containsKey(relatedClazz)) {
            throw new AddResourceException(RELATED_RESOURCE_ALREADY_ADDED, AddResourceErrorCode.RELATED_RESOURCE_ALREADY_EXISTS);
        }

        var descriptor = new Descriptor(idFieldType, path);
        rootDescriptor.getRelations().put(relatedClazz, descriptor);
    }


    @AllArgsConstructor
    public class Descriptor {
        @Getter
        private Class<?> idFieldType;

        @Getter
        private String path;
    }

    public class RootDescriptor extends Descriptor {
        @Getter
        private Map<Class<?>, Descriptor> relations = new HashMap<>();

        public RootDescriptor(Class<?> idFieldType, String path) {
            super(idFieldType, path);
        }
    }
}
