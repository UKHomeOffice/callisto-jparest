package uk.gov.homeoffice.digital.sas.jparest;

import lombok.Getter;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.addresource.AddResourceErrorCode;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.addresource.AddResourceException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ResourceEndpoint {

    @Getter
    private List<Class<?>> resourceTypes = new ArrayList<>();

    @Getter
    private Map<Class<?>, RootDescriptor> descriptors = new HashMap<>();

    public void add(Class<?> clazz, String path, Class<?> idFieldType) {

        if (descriptors.containsKey(clazz)) {
            throw new AddResourceException("Resource has already been added", AddResourceErrorCode.RESOURCE_ALREADY_EXISTS);
        }

        var rootDescriptor = new RootDescriptor(idFieldType, path);
        descriptors.put(clazz, rootDescriptor);

    }

    public void addRelated(Class<?> clazz, Class<?> relatedClazz, String path, Class<?> idFieldType) {

        if (!descriptors.containsKey(clazz)) {
            throw new AddResourceException(
                    "You can only call AddRelated on resources already passed to the Add method",
                    AddResourceErrorCode.RESOURCE_DOES_NOT_EXIST);
        }

        var rootDescriptor = descriptors.get(clazz);

        if (rootDescriptor.getRelations().containsKey(relatedClazz)) {
            throw new AddResourceException(
                    "Related resource has already been added", AddResourceErrorCode.RELATED_RESOURCE_ALREADY_EXISTS);
        }

        var descriptor = new Descriptor(idFieldType, path);
        rootDescriptor.getRelations().put(relatedClazz, descriptor);
    }


    public class Descriptor {

        @Getter
        private Class<?> idFieldType;

        @Getter
        private String path;

        public Descriptor(Class<?> idFieldType, String path) {
            this.idFieldType = idFieldType;
            this.path = path;
        }
    }

    public class RootDescriptor extends Descriptor {

        public RootDescriptor(Class<?> idFieldType, String path) {
            super(idFieldType, path);
        }

        @Getter
        private Map<Class<?>, Descriptor> relations = new HashMap<>();
    }
}
