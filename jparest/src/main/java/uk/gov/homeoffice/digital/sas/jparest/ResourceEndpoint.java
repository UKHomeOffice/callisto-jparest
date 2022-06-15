package uk.gov.homeoffice.digital.sas.jparest;

import lombok.Getter;
import org.springframework.stereotype.Component;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.addresourcedescriptor.AddResourceDescriptorErrorCode;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.addresourcedescriptor.AddResourceDescriptorException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ResourceEndpoint {

    public static final String RESOURCE_ALREADY_ADDED = "Resource has already been added";
    public static final String CALL_ADD_RELATED_ONLY_ON_EXISTING_RESOURCES = "You can only call AddRelated on resources already passed to the Add method";
    public static final String RELATED_RESOURCE_ALREADY_ADDED = "Related resource has already been added";
    public static final String PATH_ALREADY_EXISTS = "Path has already been used";

    @Getter
    private List<Class<?>> resourceTypes = new ArrayList<>();

    @Getter
    private Map<Class<?>, RootDescriptor> descriptors = new HashMap<>();

    private List<String> paths = new ArrayList<>();

    public void add(Class<?> clazz, String path, Class<?> idFieldType) {

        if (descriptors.containsKey(clazz)) {
            throw new AddResourceDescriptorException(RESOURCE_ALREADY_ADDED,
                    AddResourceDescriptorErrorCode.RESOURCE_ALREADY_EXISTS.getCode());
        }

        if (paths.contains(path)) {
            throw new AddResourceDescriptorException(PATH_ALREADY_EXISTS,
                    AddResourceDescriptorErrorCode.PATH_ALREADY_EXISTS.getCode());
        }

        paths.add(path);
        var rootDescriptor = new RootDescriptor(path);
        descriptors.put(clazz, rootDescriptor);

    }

    public void addRelated(Class<?> clazz, Class<?> relatedClazz, String path) {

        if (!descriptors.containsKey(clazz)) {
            throw new AddResourceDescriptorException(CALL_ADD_RELATED_ONLY_ON_EXISTING_RESOURCES,
                    AddResourceDescriptorErrorCode.RESOURCE_DOES_NOT_EXIST.getCode());
        }

        var rootDescriptor = descriptors.get(clazz);

        if (rootDescriptor.getRelations().containsKey(relatedClazz)) {
            throw new AddResourceDescriptorException(RELATED_RESOURCE_ALREADY_ADDED,
                    AddResourceDescriptorErrorCode.RELATED_RESOURCE_ALREADY_EXISTS.getCode());
        }

        if (paths.contains(path)) {
            throw new AddResourceDescriptorException(PATH_ALREADY_EXISTS,
                    AddResourceDescriptorErrorCode.PATH_ALREADY_EXISTS.getCode());
        }

        paths.add(path);
        rootDescriptor.getRelations().put(relatedClazz, path);
    }

    @Getter
    public class RootDescriptor {
        String path;
        private Map<Class<?>, String> relations = new HashMap<>();

        public RootDescriptor(String path) {
            this.path = path;
        }
    }
}
