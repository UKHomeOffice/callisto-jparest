package uk.gov.homeoffice.digital.sas.jparest.swagger;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityB;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityC;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityD;
import uk.gov.homeoffice.digital.sas.jparest.testutils.logging.LoggerMemoryAppender;
import uk.gov.homeoffice.digital.sas.jparest.testutils.logging.LoggingUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.homeoffice.digital.sas.jparest.utils.ConstantHelper.ID_PARAM_NAME;
import static uk.gov.homeoffice.digital.sas.jparest.utils.ConstantHelper.RELATED_PARAM_NAME;


class PathItemCreatorTest {


    private static final Class<?> RESOURCE_CLASS = DummyEntityC.class;
    private static final Class<?> RELATED_RESOURCE_CLASS = DummyEntityB.class;
    private static final String TAG = RESOURCE_CLASS.getSimpleName();
    private static final String HTTP_200_KEY = "200";


    @Test
    void createRootPath_getOperationDataIsSet() {

        var pathItemCreator = new PathItemCreator();
        var pathItem = pathItemCreator.createRootPath(TAG, RESOURCE_CLASS);

        //operation
        assertThat(pathItem.readOperationsMap()).containsKey(PathItem.HttpMethod.GET);
        var actualGetOperation = pathItem.readOperationsMap().get(PathItem.HttpMethod.GET);
        assertThat(actualGetOperation.getTags()).containsExactly(TAG);

        //params
        assertThat(actualGetOperation.getParameters()).hasSize(3);
        assertTenantIdParameterValues(actualGetOperation.getParameters().get(0));
        assertPageableParameterValues(actualGetOperation.getParameters().get(1));
        assertFilterParameterValues(actualGetOperation.getParameters().get(2));

        //responses
        assertThat(actualGetOperation.getResponses()).containsKey(HTTP_200_KEY);
        assertResourceResponse(actualGetOperation.getResponses().get(HTTP_200_KEY));
    }

    @Test
    void createRootPath_postOperationDataIsSet() {

        var pathItemCreator = new PathItemCreator();
        var pathItem = pathItemCreator.createRootPath(TAG, RESOURCE_CLASS);

        //operation
        assertThat(pathItem.readOperationsMap()).containsKey(PathItem.HttpMethod.POST);
        var actualPostOperation = pathItem.readOperationsMap().get(PathItem.HttpMethod.POST);
        assertThat(actualPostOperation.getTags()).containsExactly(TAG);

        //request / responses
        assertRequestBody(actualPostOperation.getRequestBody());
        assertThat(actualPostOperation.getResponses()).containsKey(HTTP_200_KEY);
        assertResourceResponse(actualPostOperation.getResponses().get(HTTP_200_KEY));
    }

    @Test
    void createRelatedRootPath_getOperationDataIsSet() {

        var pathItemCreator = new PathItemCreator();
        var pathItem = pathItemCreator.createRelatedRootPath(TAG, RELATED_RESOURCE_CLASS, Long.class);

        //operation
        assertThat(pathItem.readOperationsMap()).containsKey(PathItem.HttpMethod.GET);
        var actualGetOperation = pathItem.readOperationsMap().get(PathItem.HttpMethod.GET);
        assertThat(actualGetOperation.getTags()).containsExactly(TAG);

        //params
        assertThat(actualGetOperation.getParameters()).hasSize(4);
        assertIdParameterValues(actualGetOperation.getParameters().get(0));
        assertTenantIdParameterValues(actualGetOperation.getParameters().get(1));
        assertPageableParameterValues(actualGetOperation.getParameters().get(2));
        assertFilterParameterValues(actualGetOperation.getParameters().get(3));

        //responses
        assertThat(actualGetOperation.getResponses()).containsKey(HTTP_200_KEY);
        assertResourceResponse(actualGetOperation.getResponses().get(HTTP_200_KEY));
    }


    @Test
    void createItemPath_getOperationDataIsSet() {

        var pathItemCreator = new PathItemCreator();
        var pathItem = pathItemCreator.createItemPath(TAG, RESOURCE_CLASS, Long.class);

        //operation
        assertThat(pathItem.readOperationsMap()).containsKey(PathItem.HttpMethod.GET);
        var actualGetOperation = pathItem.readOperationsMap().get(PathItem.HttpMethod.GET);
        assertThat(actualGetOperation.getTags()).containsExactly(TAG);

        //params
        assertThat(actualGetOperation.getParameters()).hasSize(2);
        assertIdParameterValues(actualGetOperation.getParameters().get(0));
        assertTenantIdParameterValues(actualGetOperation.getParameters().get(1));

        //responses
        assertThat(actualGetOperation.getResponses()).containsKey(HTTP_200_KEY);
        assertResourceResponse(actualGetOperation.getResponses().get(HTTP_200_KEY));
    }

    @Test
    void createItemPath_putOperationDataIsSet() {

        var pathItemCreator = new PathItemCreator();
        var pathItem = pathItemCreator.createItemPath(TAG, RESOURCE_CLASS, Long.class);

        //operation
        assertThat(pathItem.readOperationsMap()).containsKey(PathItem.HttpMethod.PUT);
        var actualPutOperation = pathItem.readOperationsMap().get(PathItem.HttpMethod.PUT);
        assertThat(actualPutOperation.getTags()).containsExactly(TAG);

        //request / responses
        assertRequestBody(actualPutOperation.getRequestBody());
        assertThat(actualPutOperation.getResponses()).containsKey(HTTP_200_KEY);
        assertResourceResponse(actualPutOperation.getResponses().get(HTTP_200_KEY));

        //params
        assertThat(actualPutOperation.getParameters()).hasSize(2);
        assertIdParameterValues(actualPutOperation.getParameters().get(0));
        assertTenantIdParameterValues(actualPutOperation.getParameters().get(1));
    }

    @Test
    void createItemPath_deleteOperationDataIsSet() {

        var pathItemCreator = new PathItemCreator();
        var pathItem = pathItemCreator.createItemPath(TAG, RESOURCE_CLASS, Long.class);

        //operation
        assertThat(pathItem.readOperationsMap()).containsKey(PathItem.HttpMethod.DELETE);
        var actualDeleteOperation = pathItem.readOperationsMap().get(PathItem.HttpMethod.DELETE);
        assertThat(actualDeleteOperation.getTags()).containsExactly(TAG);

        //params
        assertThat(actualDeleteOperation.getParameters()).hasSize(2);
        assertIdParameterValues(actualDeleteOperation.getParameters().get(0));
        assertTenantIdParameterValues(actualDeleteOperation.getParameters().get(1));

        //responses
        assertThat(actualDeleteOperation.getResponses()).containsKey(HTTP_200_KEY);
        assertResourceResponse(actualDeleteOperation.getResponses().get(HTTP_200_KEY));
    }

    @Test
    void createRelatedItemPath_deleteOperationDataIsSet() {

        var pathItemCreator = new PathItemCreator();
        var pathItem = pathItemCreator.createRelatedItemPath(TAG, Long.class, Long.class);

        //operation
        assertThat(pathItem.readOperationsMap()).containsKey(PathItem.HttpMethod.DELETE);
        var actualDeleteOperation = pathItem.readOperationsMap().get(PathItem.HttpMethod.DELETE);
        assertThat(actualDeleteOperation.getTags()).containsExactly(TAG);

        //params
        assertThat(actualDeleteOperation.getParameters()).hasSize(3);
        assertIdParameterValues(actualDeleteOperation.getParameters().get(0));
        assertArrayParameterValues(actualDeleteOperation.getParameters().get(1));
        assertTenantIdParameterValues(actualDeleteOperation.getParameters().get(2));

        //responses
        assertThat(actualDeleteOperation.getResponses()).containsKey(HTTP_200_KEY);
        assertResourceResponse(actualDeleteOperation.getResponses().get(HTTP_200_KEY));
    }

    @Test
    void createRelatedItemPath_putOperationDataIsSet() {

        var pathItemCreator = new PathItemCreator();
        var pathItem = pathItemCreator.createRelatedItemPath(TAG, Long.class, Long.class);

        //operation
        assertThat(pathItem.readOperationsMap()).containsKey(PathItem.HttpMethod.PUT);
        var actualPutOperation = pathItem.readOperationsMap().get(PathItem.HttpMethod.PUT);
        assertThat(actualPutOperation.getTags()).containsExactly(TAG);

        //params
        assertThat(actualPutOperation.getParameters()).hasSize(3);
        assertIdParameterValues(actualPutOperation.getParameters().get(0));
        assertTenantIdParameterValues(actualPutOperation.getParameters().get(1));
        assertArrayParameterValues(actualPutOperation.getParameters().get(2));

        //responses
        assertThat(actualPutOperation.getResponses()).containsKey(HTTP_200_KEY);
        assertResourceResponse(actualPutOperation.getResponses().get(HTTP_200_KEY));
    }

    @Test
    void getFilterParameter_resourceHasBlankFilterExampleObject_errorLogged() {

        var resourceClass = DummyEntityD.class;
        var pathItemCreator = new PathItemCreator();

        var logger = (Logger) LoggerFactory.getLogger(PathItemCreator.class);
        var loggerMemoryAppender = new LoggerMemoryAppender();
        LoggingUtils.startMemoryAppender(logger, loggerMemoryAppender, Level.ERROR);

        pathItemCreator.createRootPath(TAG, resourceClass);

        var expectedLogMessage = "Example could not be found in ExampleObject from resource: " +
                resourceClass.getSimpleName();
        assertThat(loggerMemoryAppender.countEventsForLogger(PathItemCreator.class.getName())).isEqualTo(1);
        assertThat(loggerMemoryAppender.search(expectedLogMessage, Level.ERROR)).hasSize(1);
    }



    private void assertParameterValues(Parameter actualParam, boolean expectedRequired, String expectedIn, String expectedName) {
        assertThat(actualParam.getSchema()).isNotNull();
        assertThat(actualParam.getRequired()).isEqualTo(expectedRequired);
        assertThat(actualParam.getIn()).isEqualTo(expectedIn);
        assertThat(actualParam.getName()).isEqualTo(expectedName);
    }

    private void assertIdParameterValues(Parameter actualParam) {
        assertParameterValues(actualParam, true, "path", ID_PARAM_NAME);
    }

    private void assertTenantIdParameterValues(Parameter actualParam) {
        assertParameterValues(actualParam, true, "query", "tenantId");
    }

    private void assertArrayParameterValues(Parameter actualParam) {
        assertParameterValues(actualParam, true, "path", RELATED_PARAM_NAME);
    }

    private void assertPageableParameterValues(Parameter actualPageableParam) {
        assertParameterValues(actualPageableParam, true, "query", "pageable");
    }

    private void assertFilterParameterValues(Parameter actualFilterParam) {
        assertParameterValues(actualFilterParam, false, "query", "filter");
    }

    private void assertResourceResponse(ApiResponse actualResponse) {
        assertThat(actualResponse.getContent().get(MediaType.APPLICATION_JSON_VALUE)).isNotNull();
        assertThat(actualResponse.getContent().get(MediaType.APPLICATION_JSON_VALUE).getSchema()).isNotNull();
    }

    private void assertRequestBody(RequestBody actualRequestBody) {
        assertThat(actualRequestBody.getContent().get(MediaType.APPLICATION_JSON_VALUE)).isNotNull();
        assertThat(actualRequestBody.getContent().get(MediaType.APPLICATION_JSON_VALUE).getSchema()).isNotNull();
    }



}