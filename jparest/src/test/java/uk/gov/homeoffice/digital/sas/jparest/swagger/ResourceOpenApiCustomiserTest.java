package uk.gov.homeoffice.digital.sas.jparest.swagger;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import io.swagger.v3.oas.models.PathItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import uk.gov.homeoffice.digital.sas.jparest.ResourceEndpoint;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityA;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityB;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityC;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityD;
import uk.gov.homeoffice.digital.sas.jparest.swagger.testutils.ApiResponseTestUtil;
import uk.gov.homeoffice.digital.sas.jparest.swagger.testutils.HttpOperationTestUtil;
import uk.gov.homeoffice.digital.sas.jparest.swagger.testutils.OpenApiTestUtil;
import uk.gov.homeoffice.digital.sas.jparest.testutils.logging.LoggerMemoryAppender;
import uk.gov.homeoffice.digital.sas.jparest.testutils.logging.LoggingUtils;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.homeoffice.digital.sas.jparest.utils.ConstantHelper.URL_ID_PATH_PARAM;
import static uk.gov.homeoffice.digital.sas.jparest.utils.ConstantHelper.URL_RELATED_ID_PATH_PARAM;


@ExtendWith(MockitoExtension.class)
class ResourceOpenApiCustomiserTest {


    @Mock
    private ResourceEndpoint resourceEndpoint;

    private static final String ROOT_PATH = "rootPath";
    private static final String RELATED_ROOT_PATH = "relatedPath";



    @Test
    void customise_resourceRootPathAddedToOpenApi() {

        var resourceOpenApiCustomiser = new ResourceOpenApiCustomiser(resourceEndpoint);
        when(resourceEndpoint.getResourceTypes()).thenReturn(List.of());

        var resourceClass = DummyEntityC.class;
        var rootDescriptor = resourceEndpoint.new RootDescriptor(Long.class, ROOT_PATH);
        when(resourceEndpoint.getDescriptors()).thenReturn(Map.of(
                resourceClass, rootDescriptor));

        var openApi = OpenApiTestUtil.createDefaultOpenAPI();
        resourceOpenApiCustomiser.customise(openApi);


        assertThat(openApi.getPaths()).containsKey(rootDescriptor.getPath());
        var actualRootPath = openApi.getPaths().get(rootDescriptor.getPath());
        assertThat(actualRootPath.readOperationsMap()).isNotEmpty();

        var actualGetOperation = actualRootPath.readOperationsMap().get(PathItem.HttpMethod.GET);
        var expectedGetOperation = HttpOperationTestUtil.createSuccessOperation(
                resourceClass, ApiResponseTestUtil.createDefaultApiResponse(resourceClass));
        HttpOperationTestUtil.addPageableParameterToOperation(expectedGetOperation);
        HttpOperationTestUtil.addFilterParameterToOperation(expectedGetOperation, resourceClass);
        assertThat(actualGetOperation).isEqualTo(expectedGetOperation);

        var actualPostOperation = actualRootPath.readOperationsMap().get(PathItem.HttpMethod.POST);
        var expectedPostOperation = HttpOperationTestUtil.createSuccessOperation(
                resourceClass, ApiResponseTestUtil.createDefaultApiResponse(resourceClass));
        HttpOperationTestUtil.addRequestBody(expectedPostOperation, resourceClass);
        assertThat(actualPostOperation).isEqualTo(expectedPostOperation);
    }

    @Test
    void customise_resourceItemPathAddedToOpenApi() {

        var resourceOpenApiCustomiser = new ResourceOpenApiCustomiser(resourceEndpoint);
        when(resourceEndpoint.getResourceTypes()).thenReturn(List.of());

        var resourceClass = DummyEntityC.class;
        var rootDescriptor = resourceEndpoint.new RootDescriptor(Long.class, ROOT_PATH);
        when(resourceEndpoint.getDescriptors()).thenReturn(Map.of(
                resourceClass, rootDescriptor));

        var openApi = OpenApiTestUtil.createDefaultOpenAPI();
        resourceOpenApiCustomiser.customise(openApi);


        assertThat(openApi.getPaths()).containsKey(rootDescriptor.getPath() + URL_ID_PATH_PARAM);
        var actualItemPath = openApi.getPaths().get(rootDescriptor.getPath() + URL_ID_PATH_PARAM);
        assertThat(actualItemPath.readOperationsMap()).isNotEmpty();

        var actualGetOperation = actualItemPath.readOperationsMap().get(PathItem.HttpMethod.GET);
        var expectedGetOperation = HttpOperationTestUtil.createSuccessOperation(
                resourceClass, ApiResponseTestUtil.createDefaultApiResponse(resourceClass));
        HttpOperationTestUtil.addIdParameterToOperation(expectedGetOperation, rootDescriptor.getIdFieldType());
        assertThat(actualGetOperation).isEqualTo(expectedGetOperation);

        var actualPutOperation = actualItemPath.readOperationsMap().get(PathItem.HttpMethod.PUT);
        var expectedPutOperation = HttpOperationTestUtil.createSuccessOperation(
                resourceClass, ApiResponseTestUtil.createDefaultApiResponse(resourceClass));
        HttpOperationTestUtil.addRequestBody(expectedPutOperation, resourceClass);
        HttpOperationTestUtil.addIdParameterToOperation(expectedPutOperation, rootDescriptor.getIdFieldType());
        assertThat(actualPutOperation).isEqualTo(expectedPutOperation);

        var actualDeleteOperation = actualItemPath.readOperationsMap().get(PathItem.HttpMethod.DELETE);
        var expectedDeleteOperation = HttpOperationTestUtil.createSuccessOperation(
                resourceClass, ApiResponseTestUtil.createEmptyApiResponse());
        HttpOperationTestUtil.addIdParameterToOperation(expectedDeleteOperation, rootDescriptor.getIdFieldType());
        assertThat(actualDeleteOperation).isEqualTo(expectedDeleteOperation);
    }

    @Test
    void customise_relatedResourceRootPathAddedToOpenApi() {

        var resourceOpenApiCustomiser = new ResourceOpenApiCustomiser(resourceEndpoint);
        when(resourceEndpoint.getResourceTypes()).thenReturn(List.of());

        var resourceClass = DummyEntityA.class;
        var relatedResourceClass = DummyEntityB.class;
        var rootDescriptor = resourceEndpoint.new RootDescriptor(Long.class, ROOT_PATH);
        var relatedDescriptor = resourceEndpoint.new Descriptor(relatedResourceClass, RELATED_ROOT_PATH);
        rootDescriptor.getRelations().put(relatedResourceClass, relatedDescriptor);
        when(resourceEndpoint.getDescriptors()).thenReturn(Map.of(
                resourceClass, rootDescriptor));

        var openApi = OpenApiTestUtil.createDefaultOpenAPI();
        resourceOpenApiCustomiser.customise(openApi);


        assertThat(openApi.getPaths()).containsKey(relatedDescriptor.getPath());
        var actualRelatedRootPath = openApi.getPaths().get(relatedDescriptor.getPath());
        assertThat(actualRelatedRootPath.readOperationsMap()).isNotEmpty();

        var actualGetOperation = actualRelatedRootPath.readOperationsMap().get(PathItem.HttpMethod.GET);
        var expectedGetOperation = HttpOperationTestUtil.createSuccessOperation(
                resourceClass, ApiResponseTestUtil.createDefaultApiResponse(relatedResourceClass));
        HttpOperationTestUtil.addIdParameterToOperation(expectedGetOperation, rootDescriptor.getIdFieldType());
        HttpOperationTestUtil.addPageableParameterToOperation(expectedGetOperation);
        HttpOperationTestUtil.addFilterParameterToOperation(expectedGetOperation, resourceClass);
        assertThat(actualGetOperation).isEqualTo(expectedGetOperation);
    }

    @Test
    void customise_relatedResourceItemPathAddedToOpenApi() {

        var resourceOpenApiCustomiser = new ResourceOpenApiCustomiser(resourceEndpoint);
        when(resourceEndpoint.getResourceTypes()).thenReturn(List.of());

        var resourceClass = DummyEntityA.class;
        var relatedResourceClass = DummyEntityB.class;
        var rootDescriptor = resourceEndpoint.new RootDescriptor(Long.class, ROOT_PATH);
        var relatedDescriptor = resourceEndpoint.new Descriptor(relatedResourceClass, RELATED_ROOT_PATH);
        rootDescriptor.getRelations().put(relatedResourceClass, relatedDescriptor);
        when(resourceEndpoint.getDescriptors()).thenReturn(Map.of(
                resourceClass, rootDescriptor));

        var openApi = OpenApiTestUtil.createDefaultOpenAPI();
        resourceOpenApiCustomiser.customise(openApi);


        assertThat(openApi.getPaths()).containsKey(relatedDescriptor.getPath() + URL_RELATED_ID_PATH_PARAM);
        var actualRelatedItemPath = openApi.getPaths().get(relatedDescriptor.getPath() + URL_RELATED_ID_PATH_PARAM);
        assertThat(actualRelatedItemPath.readOperationsMap()).isNotEmpty();

        var actualDeleteOperation = actualRelatedItemPath.readOperationsMap().get(PathItem.HttpMethod.DELETE);
        var expectedDeleteOperation = HttpOperationTestUtil.createSuccessOperation(
                resourceClass, ApiResponseTestUtil.createEmptyApiResponse());
        HttpOperationTestUtil.addIdParameterToOperation(expectedDeleteOperation, rootDescriptor.getIdFieldType());
        HttpOperationTestUtil.addArrayRelatedIdParameterToOperation(expectedDeleteOperation, relatedDescriptor.getIdFieldType());
        assertThat(actualDeleteOperation).isEqualTo(expectedDeleteOperation);

        var actualPutOperation = actualRelatedItemPath.readOperationsMap().get(PathItem.HttpMethod.PUT);
        var expectedPutOperation = HttpOperationTestUtil.createSuccessOperation(
                resourceClass, ApiResponseTestUtil.createEmptyApiResponse());
        HttpOperationTestUtil.addIdParameterToOperation(expectedPutOperation, rootDescriptor.getIdFieldType());
        HttpOperationTestUtil.addArrayRelatedIdParameterToOperation(expectedPutOperation, relatedDescriptor.getIdFieldType());
        assertThat(actualPutOperation).isEqualTo(expectedPutOperation);
    }

    @Test
    void customise_resourceHasBlankFilterExampleObject_errorLogged() {

        var resourceOpenApiCustomiser = new ResourceOpenApiCustomiser(resourceEndpoint);
        when(resourceEndpoint.getResourceTypes()).thenReturn(List.of());

        var resourceClass = DummyEntityD.class;
        var rootDescriptor = resourceEndpoint.new RootDescriptor(Long.class, ROOT_PATH);
        when(resourceEndpoint.getDescriptors()).thenReturn(Map.of(
                resourceClass, rootDescriptor));

        var logger = (Logger) LoggerFactory.getLogger(ResourceOpenApiCustomiser.class);
        var loggerMemoryAppender = new LoggerMemoryAppender();
        LoggingUtils.startMemoryAppender(logger, loggerMemoryAppender, Level.ERROR);

        resourceOpenApiCustomiser.customise(OpenApiTestUtil.createDefaultOpenAPI());

        var expectedLogMessage = "Example could not be found in ExampleObject from resource: " +
                resourceClass.getSimpleName();
        assertThat(loggerMemoryAppender.countEventsForLogger(ResourceOpenApiCustomiser.class.getName())).isEqualTo(1);
        assertThat(loggerMemoryAppender.search(expectedLogMessage, Level.ERROR).size()).isEqualTo(1);
    }

}