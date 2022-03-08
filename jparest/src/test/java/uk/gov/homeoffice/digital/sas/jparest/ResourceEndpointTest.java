package uk.gov.homeoffice.digital.sas.jparest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class ResourceEndpointTest {


    private ResourceEndpoint resourceEndpoint;

    private final static Class<?> RESOURCE = String.class;
    private final static Class<?> RELATED_RESOURCE = String.class;
    private final static Class<?> ID_FIELD_TYPE = String.class;
    private final static String PATH = "myPath";


    @BeforeEach
    public void setUp() {
        this.resourceEndpoint = new ResourceEndpoint();
    }



    @Test
    public void add_descriptorsAlreadyContainsResourceToBeAdded_errorThrown() {

        resourceEndpoint.Add(RESOURCE, PATH, ID_FIELD_TYPE);

        assertThrows(
                IllegalArgumentException.class,
                () -> resourceEndpoint.Add(RESOURCE, PATH, ID_FIELD_TYPE));
    }

    @Test
    public void add_descriptorsDoesNotContainResourceToBeAdded_resourceAdded() {

        assertDoesNotThrow(
                () -> resourceEndpoint.Add(RESOURCE, PATH, ID_FIELD_TYPE));

        assertTrue(resourceEndpoint.getDescriptors().containsKey(RESOURCE));

        var actualDescriptor = resourceEndpoint.getDescriptors().get(RESOURCE);
        var expectedDescriptor = resourceEndpoint.new RootDescriptor(ID_FIELD_TYPE, PATH);
        assertEquals(expectedDescriptor.getPath(), actualDescriptor.getPath());
        assertEquals(expectedDescriptor.getIdFieldType(), actualDescriptor.getIdFieldType());
    }


    @Test
    public void addRelated_descriptorsDoesNotContainResource_errorThrown() {

        var thrown = assertThrows(
                IllegalArgumentException.class,
                () -> resourceEndpoint.AddRelated(RESOURCE, RELATED_RESOURCE, PATH, ID_FIELD_TYPE));

        assertEquals(thrown.getMessage(), "You can only call AddRelated on resources already passed to the Add method");
    }

    @Test
    public void addRelated_descriptorRelationsAlreadyContainsRelatedResource_errorThrow() {

        resourceEndpoint.Add(RESOURCE, PATH, ID_FIELD_TYPE);
        resourceEndpoint.AddRelated(RESOURCE, RELATED_RESOURCE, PATH, String.class);

        var thrown = assertThrows(
                IllegalArgumentException.class,
                () -> resourceEndpoint.AddRelated(RESOURCE, RELATED_RESOURCE, PATH, ID_FIELD_TYPE));

        assertEquals(thrown.getMessage(), "Related resource as already been added");

    }

    @Test
    public void addRelated_descriptorRelationsDoesNotContainRelatedResourceToBeAdded_relatedResourceAdded() {

        resourceEndpoint.Add(RESOURCE, PATH, ID_FIELD_TYPE);

        assertDoesNotThrow(
                () -> resourceEndpoint.AddRelated(RESOURCE, RELATED_RESOURCE, PATH, ID_FIELD_TYPE));

        var actualDescriptor = resourceEndpoint.getDescriptors().get(RESOURCE).getRelations().get(RELATED_RESOURCE);
        var expectedDescriptor = resourceEndpoint.new RootDescriptor(ID_FIELD_TYPE, PATH);
        assertEquals(expectedDescriptor.getPath(), actualDescriptor.getPath());
        assertEquals(expectedDescriptor.getIdFieldType(), actualDescriptor.getIdFieldType());
    }






}
