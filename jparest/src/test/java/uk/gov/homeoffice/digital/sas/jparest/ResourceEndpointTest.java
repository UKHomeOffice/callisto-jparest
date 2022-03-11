package uk.gov.homeoffice.digital.sas.jparest;

import org.junit.jupiter.api.Test;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityA;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityB;

import static org.junit.jupiter.api.Assertions.*;


public class ResourceEndpointTest {


    private final static Class<?> RESOURCE = DummyEntityA.class;
    private final static Class<?> RELATED_RESOURCE = DummyEntityB.class;
    private final static Class<?> ID_FIELD_TYPE = long.class;
    private final static String PATH = "DummyEntityA";
    private final static String RELATED_RESOURCE_PATH = "dummyEntityBSet";



    @Test
    public void add_descriptorsAlreadyContainsResourceToBeAdded_errorThrown() {

        var resourceEndpoint = new ResourceEndpoint();
        resourceEndpoint.Add(RESOURCE, PATH, ID_FIELD_TYPE);

        assertThrows(
                IllegalArgumentException.class,
                () -> resourceEndpoint.Add(RESOURCE, PATH, ID_FIELD_TYPE));
    }

    @Test
    public void add_descriptorsDoesNotContainResourceToBeAdded_resourceAdded() {

        var resourceEndpoint = new ResourceEndpoint();
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

        var resourceEndpoint = new ResourceEndpoint();
        var thrown = assertThrows(
                IllegalArgumentException.class,
                () -> resourceEndpoint.AddRelated(RESOURCE, RELATED_RESOURCE, RELATED_RESOURCE_PATH, ID_FIELD_TYPE));

        assertEquals(thrown.getMessage(), "You can only call AddRelated on resources already passed to the Add method");
    }

    @Test
    public void addRelated_descriptorRelationsAlreadyContainsRelatedResource_errorThrow() {

        var resourceEndpoint = new ResourceEndpoint();
        resourceEndpoint.Add(RESOURCE, PATH, ID_FIELD_TYPE);
        resourceEndpoint.AddRelated(RESOURCE, RELATED_RESOURCE, RELATED_RESOURCE_PATH, String.class);

        var thrown = assertThrows(
                IllegalArgumentException.class,
                () -> resourceEndpoint.AddRelated(RESOURCE, RELATED_RESOURCE, RELATED_RESOURCE_PATH, ID_FIELD_TYPE));

        assertEquals(thrown.getMessage(), "Related resource as already been added");

    }

    @Test
    public void addRelated_descriptorRelationsDoesNotContainRelatedResourceToBeAdded_relatedResourceAdded() {

        var resourceEndpoint = new ResourceEndpoint();
        resourceEndpoint.Add(RESOURCE, PATH, ID_FIELD_TYPE);

        assertDoesNotThrow(
                () -> resourceEndpoint.AddRelated(RESOURCE, RELATED_RESOURCE, RELATED_RESOURCE_PATH, ID_FIELD_TYPE));

        var actualDescriptor = resourceEndpoint.getDescriptors().get(RESOURCE).getRelations().get(RELATED_RESOURCE);
        assertEquals(RELATED_RESOURCE_PATH, actualDescriptor.getPath());
        assertEquals(ID_FIELD_TYPE, actualDescriptor.getIdFieldType());
    }






}
