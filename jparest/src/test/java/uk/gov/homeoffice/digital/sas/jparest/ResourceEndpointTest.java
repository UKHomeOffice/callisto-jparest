package uk.gov.homeoffice.digital.sas.jparest;

import org.junit.jupiter.api.Test;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityA;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityB;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.addresource.AddResourceErrorCode;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.addresource.AddResourceException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;


class ResourceEndpointTest {

    private final static Class<?> RESOURCE = DummyEntityA.class;
    private final static Class<?> RELATED_RESOURCE = DummyEntityB.class;
    private final static Class<?> ID_FIELD_TYPE = long.class;
    private final static String PATH = "DummyEntityA";
    private final static String RELATED_RESOURCE_PATH = "dummyEntityBSet";

    @Test
    void add_descriptorsAlreadyContainsResourceToBeAdded_errorThrown() {

        var resourceEndpoint = new ResourceEndpoint();
        resourceEndpoint.add(RESOURCE, PATH, ID_FIELD_TYPE);

        var thrown = assertThrows(
                AddResourceException.class,
                () -> resourceEndpoint.add(RESOURCE, PATH, ID_FIELD_TYPE));

        assertThat(thrown.getErrorCode()).isEqualTo(AddResourceErrorCode.RESOURCE_ALREADY_EXISTS.getCode());
    }

    @Test
    void add_descriptorsDoesNotContainResourceToBeAdded_resourceAdded() {

        var resourceEndpoint = new ResourceEndpoint();
        assertDoesNotThrow(
                () -> resourceEndpoint.add(RESOURCE, PATH, ID_FIELD_TYPE));

        assertThat(resourceEndpoint.getDescriptors().containsKey(RESOURCE));

        var actualDescriptor = resourceEndpoint.getDescriptors().get(RESOURCE);
        var expectedDescriptor = resourceEndpoint.new RootDescriptor(ID_FIELD_TYPE, PATH);
        assertThat(actualDescriptor.getPath()).isEqualTo(expectedDescriptor.getPath());
        assertThat(actualDescriptor.getIdFieldType()).isEqualTo(expectedDescriptor.getIdFieldType());
    }


    @Test
    void addRelated_descriptorsDoesNotContainResource_errorThrown() {

        var resourceEndpoint = new ResourceEndpoint();
        var thrown = assertThrows(
                AddResourceException.class,
                () -> resourceEndpoint.addRelated(RESOURCE, RELATED_RESOURCE, RELATED_RESOURCE_PATH, ID_FIELD_TYPE));

        assertThat(thrown.getErrorCode()).isEqualTo(AddResourceErrorCode.RESOURCE_DOES_NOT_EXIST.getCode());
    }

    @Test
    void addRelated_descriptorRelationsAlreadyContainsRelatedResource_errorThrown() {

        var resourceEndpoint = new ResourceEndpoint();
        resourceEndpoint.add(RESOURCE, PATH, ID_FIELD_TYPE);
        resourceEndpoint.addRelated(RESOURCE, RELATED_RESOURCE, RELATED_RESOURCE_PATH, String.class);

        var thrown = assertThrows(
                AddResourceException.class,
                () -> resourceEndpoint.addRelated(RESOURCE, RELATED_RESOURCE, RELATED_RESOURCE_PATH, ID_FIELD_TYPE));

        assertThat(thrown.getErrorCode()).isEqualTo(AddResourceErrorCode.RELATED_RESOURCE_ALREADY_EXISTS.getCode());

    }

    @Test
    void addRelated_descriptorRelationsDoesNotContainRelatedResourceToBeAdded_relatedResourceAdded() {

        var resourceEndpoint = new ResourceEndpoint();
        resourceEndpoint.add(RESOURCE, PATH, ID_FIELD_TYPE);

        assertDoesNotThrow(
                () -> resourceEndpoint.addRelated(RESOURCE, RELATED_RESOURCE, RELATED_RESOURCE_PATH, ID_FIELD_TYPE));

        var actualDescriptor = resourceEndpoint.getDescriptors().get(RESOURCE).getRelations().get(RELATED_RESOURCE);
        assertThat(actualDescriptor.getPath()).isEqualTo(RELATED_RESOURCE_PATH);
        assertThat(actualDescriptor.getIdFieldType()).isEqualTo(ID_FIELD_TYPE);
    }

}