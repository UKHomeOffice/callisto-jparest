package uk.gov.homeoffice.digital.sas.jparest.swagger;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.homeoffice.digital.sas.jparest.ResourceEndpoint;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityA;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityB;
import uk.gov.homeoffice.digital.sas.jparest.swagger.testutils.OpenApiTestUtil;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.homeoffice.digital.sas.jparest.utils.ConstantHelper.URL_ID_PATH_PARAM;
import static uk.gov.homeoffice.digital.sas.jparest.utils.ConstantHelper.URL_RELATED_ID_PATH_PARAM;


@ExtendWith(MockitoExtension.class)
class ResourceOpenApiCustomiserTest {


    @Mock
    private ResourceEndpoint resourceEndpoint;

    @Mock
    private PathItemCreator pathItemCreator;

    private ResourceOpenApiCustomiser resourceOpenApiCustomiser;

    private ResourceEndpoint.RootDescriptor rootDescriptor;
    private ResourceEndpoint.RootDescriptor relatedDescriptor;

    private static final Class<?> RESOURCE_CLASS = DummyEntityA.class;
    private static final Class<?> RELATED_RESOURCE_CLASS = DummyEntityB.class;


    @BeforeEach
    public void setup() {
        rootDescriptor = resourceEndpoint.new RootDescriptor(Long.class, "rootPath");
        relatedDescriptor = resourceEndpoint.new RootDescriptor(RELATED_RESOURCE_CLASS, "relatedPath");
        resourceOpenApiCustomiser = new ResourceOpenApiCustomiser(resourceEndpoint, pathItemCreator);
        when(resourceEndpoint.getResourceTypes()).thenReturn(List.of());
        when(resourceEndpoint.getDescriptors()).thenReturn(Map.of(RESOURCE_CLASS, rootDescriptor));
    }


    @ParameterizedTest
    @MethodSource("pathItemsSource")
    void customise_resourceRootPathAddedToOpenApi(PathItem.HttpMethod pathItemHttpMethod,
                                                  PathItem resourceRootPath,
                                                  Operation operation) {

        when(pathItemCreator.createRootPath(RESOURCE_CLASS.getSimpleName(), RESOURCE_CLASS)).thenReturn(resourceRootPath);

        var openApi = OpenApiTestUtil.createDefaultOpenAPI();
        resourceOpenApiCustomiser.customise(openApi);

        assertThat(openApi.getPaths()).containsKey(rootDescriptor.getPath());
        var actualRootPath = openApi.getPaths().get(rootDescriptor.getPath());
        assertThat(actualRootPath.readOperationsMap()).containsEntry(pathItemHttpMethod, operation);
    }

    @ParameterizedTest
    @MethodSource("pathItemsSource")
    void customise_resourceItemPathAddedToOpenApi(PathItem.HttpMethod pathItemHttpMethod,
                                                  PathItem resourceItemPath,
                                                  Operation operation) {

        when(pathItemCreator.createItemPath(RESOURCE_CLASS.getSimpleName(), RESOURCE_CLASS, Long.class)).thenReturn(resourceItemPath);

        var openApi = OpenApiTestUtil.createDefaultOpenAPI();
        resourceOpenApiCustomiser.customise(openApi);

        assertThat(openApi.getPaths()).containsKey(rootDescriptor.getPath() + URL_ID_PATH_PARAM);
        var actualItemPath = openApi.getPaths().get(rootDescriptor.getPath() + URL_ID_PATH_PARAM);
        assertThat(actualItemPath.readOperationsMap()).containsEntry(pathItemHttpMethod, operation);

    }

    @ParameterizedTest
    @MethodSource("pathItemsSource")
    void customise_relatedResourceRootPathAddedToOpenApi(PathItem.HttpMethod pathItemHttpMethod,
                                                         PathItem relatedResourceRootPath,
                                                         Operation operation) {

        rootDescriptor.getRelations().put(RELATED_RESOURCE_CLASS, relatedDescriptor);
        when(pathItemCreator.createRelatedRootPath(RESOURCE_CLASS.getSimpleName(), RELATED_RESOURCE_CLASS, rootDescriptor.getIdFieldType()))
                .thenReturn(relatedResourceRootPath);

        var openApi = OpenApiTestUtil.createDefaultOpenAPI();
        resourceOpenApiCustomiser.customise(openApi);

        assertThat(openApi.getPaths()).containsKey(relatedDescriptor.getPath());
        var actualRelatedRootPath = openApi.getPaths().get(relatedDescriptor.getPath());
        assertThat(actualRelatedRootPath.readOperationsMap()).containsEntry(pathItemHttpMethod, operation);
    }

    @ParameterizedTest
    @MethodSource("pathItemsSource")
    void customise_relatedResourceItemPathAddedToOpenApi(PathItem.HttpMethod pathItemHttpMethod,
                                                         PathItem relatedResourceItemPath,
                                                         Operation operation) {

        rootDescriptor.getRelations().put(RELATED_RESOURCE_CLASS, relatedDescriptor);
        when(pathItemCreator.createRelatedItemPath(RESOURCE_CLASS.getSimpleName(), rootDescriptor.getIdFieldType(), relatedDescriptor.getIdFieldType()))
                .thenReturn(relatedResourceItemPath);

        var openApi = OpenApiTestUtil.createDefaultOpenAPI();
        resourceOpenApiCustomiser.customise(openApi);

        assertThat(openApi.getPaths()).containsKey(relatedDescriptor.getPath() + URL_RELATED_ID_PATH_PARAM);
        var actualRelatedItemPath = openApi.getPaths().get(relatedDescriptor.getPath() + URL_RELATED_ID_PATH_PARAM);
        assertThat(actualRelatedItemPath.readOperationsMap()).containsEntry(pathItemHttpMethod, operation);
    }



    private static Stream<Arguments> pathItemsSource() {

        var pathItemGet = OpenApiTestUtil.createGetPathItem();
        var pathItemPost = OpenApiTestUtil.createPostPathItem();
        var pathItemPut = OpenApiTestUtil.createPutPathItem();
        var pathItemDelete = OpenApiTestUtil.createDeletePathItem();

        return Stream.of(
                Arguments.of(PathItem.HttpMethod.GET, pathItemGet, pathItemGet.getGet()),
                Arguments.of(PathItem.HttpMethod.POST, pathItemPost, pathItemPost.getPost()),
                Arguments.of(PathItem.HttpMethod.PUT, pathItemPut, pathItemPut.getPut()),
                Arguments.of(PathItem.HttpMethod.DELETE, pathItemDelete, pathItemDelete.getDelete())
        );
    }

}