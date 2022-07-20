package uk.gov.homeoffice.digital.sas.jparest.swagger;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem.HttpMethod;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springdoc.core.SpringDocAnnotationsUtils;
import uk.gov.homeoffice.digital.sas.jparest.ResourceEndpoint;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityA;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityB;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityC;
import uk.gov.homeoffice.digital.sas.jparest.swagger.testutils.OpenApiTestUtil;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.homeoffice.digital.sas.jparest.EntityUtils.ID_FIELD_TYPE;

@ExtendWith(MockitoExtension.class)
class ResourceOpenApiCustomiserTest {

        @Mock
        private ResourceEndpoint resourceEndpoint;

        @Test
        void customise_noResourceEndpoints_noPathsAdded() {
                var resourceEndpoint = new ResourceEndpoint();
                var resourceOpenApiCustomiser = new ResourceOpenApiCustomiser(resourceEndpoint, new PathItemCreator());
                var openApi = OpenApiTestUtil.createDefaultOpenAPI();
                resourceOpenApiCustomiser.customise(openApi);
                assertThat(openApi.getPaths()).isNull();
        }

        @Test
        void customise_noResourceTypes_apiResponseSchemaIsNotSpecialised() {
                var resourceEndpoint = new ResourceEndpoint();
                var resourceOpenApiCustomiser = new ResourceOpenApiCustomiser(resourceEndpoint, new PathItemCreator());
                var openApi = OpenApiTestUtil.createDefaultOpenAPI();
                resourceOpenApiCustomiser.customise(openApi);

                var apiResponse = openApi.getComponents().getSchemas().get("ApiResponse");
                assertThat(apiResponse).isNotNull();

                var arraySchema = (ArraySchema) apiResponse.getProperties().get("items");
                var arraySchemaItems = (ComposedSchema) arraySchema.getItems();
                assertThat(arraySchemaItems.getOneOf()).isNull();
        }

        @ParameterizedTest
        @MethodSource("resourceTypes")
        void customise_resourceTypesExist_apiResponseItemsSchemaUpdated(Stream<Class<?>> clazzes, Object[] refs) {
                var resourceEndpoint = new ResourceEndpoint();
                clazzes.forEach((clazz) -> resourceEndpoint.getResourceTypes().add(clazz));

                var resourceOpenApiCustomiser = new ResourceOpenApiCustomiser(resourceEndpoint, new PathItemCreator());
                var openApi = OpenApiTestUtil.createDefaultOpenAPI();

                resourceOpenApiCustomiser.customise(openApi);
                var apiResponse = openApi.getComponents().getSchemas().get("ApiResponse");
                assertThat(apiResponse).isNotNull();

                var arraySchema = (ArraySchema) apiResponse.getProperties().get("items");
                var arraySchemaItems = (ComposedSchema) arraySchema.getItems();
                assertThat(arraySchemaItems.getOneOf()).extracting("$ref").contains(refs);
        }

        @ParameterizedTest
        @MethodSource("resources")
        void customise_resourceEndpointExists_openApiPathIsCustomised(Class<DummyEntityA> resource,String path) {

                var resourceEndpoint = new ResourceEndpoint();
                resourceEndpoint.add(resource, path);

                var resourceOpenApiCustomiser = new ResourceOpenApiCustomiser(resourceEndpoint, new PathItemCreator());
                var openApi = OpenApiTestUtil.createDefaultOpenAPI();
                resourceOpenApiCustomiser.customise(openApi);

                var paths = openApi.getPaths();
                validateCreate(paths, path, resource);
                validateRead(paths, path, resource);
                validateUpdate(paths, path, resource);
                validateDelete(paths, path, resource);
        }

        @ParameterizedTest
        @MethodSource("resources")
        void customise_resourceEndpointHasRelatedResources_openApiPathIsCustomised(Class<DummyEntityA> resource, String path) {

                var resourceEndpoint = new ResourceEndpoint();
                resourceEndpoint.add(resource, path);

                var firstRelationPath = path + "/{id}/dummyb";
                var firstRelatedResourceType = DummyEntityB.class;

                var secondRelationPath = path + "/{id}/dummyc";
                var secondRelatedResourceType = DummyEntityC.class;

                resourceEndpoint.addRelated(resource, firstRelatedResourceType, firstRelationPath);
                resourceEndpoint.addRelated(resource, secondRelatedResourceType, secondRelationPath);

                var resourceOpenApiCustomiser = new ResourceOpenApiCustomiser(resourceEndpoint, new PathItemCreator());
                var openApi = OpenApiTestUtil.createDefaultOpenAPI();
                resourceOpenApiCustomiser.customise(openApi);

                var paths = openApi.getPaths();
                validateRelatedRead(paths, firstRelationPath, firstRelatedResourceType);
                validateRelatedUpdate(paths, firstRelationPath, resource);
                validateRelatedDelete(paths, firstRelationPath, resource);

                validateRelatedRead(paths, secondRelationPath, secondRelatedResourceType);
                validateRelatedUpdate(paths, secondRelationPath, resource);
                validateRelatedDelete(paths, secondRelationPath, resource);
        }

        // region validation

        // region validation operations
        private void validateCreate(Paths paths, String path, Class<?> resource) {
                var pathItem = paths.get(path);
                assertThat(pathItem).isNotNull();
                var post = pathItem.getPost();
                assertThat(post).isNotNull();
                var parameters = post.getParameters();
                assertThat(parameters).isNotNull();
                validateTenantIdParameter(parameters);
                validateRequestBody(post, resource);
                validateApiResponse(post, resource);

        }

        private void validateRead(Paths paths, String path, Class<?> resource) {
                var pathItem = paths.get(path);
                assertThat(pathItem).isNotNull();
                var get = pathItem.getGet();
                assertThat(get).isNotNull();
                var parameters = get.getParameters();
                validatePageableParameter(parameters);
                validateFilterParameter(parameters);
                validateApiResponse(get, resource);

        }

        private void validateUpdate(Paths paths, String path, Class<?> resource) {
                var pathItem = paths.get(path + "/{id}");
                assertThat(pathItem).isNotNull();
                var update = pathItem.getPut();
                assertThat(update).isNotNull();
                var parameters = update.getParameters();
                validateIdParameter(parameters);
                validateRequestBody(update, resource);
                validateApiResponse(update, resource);

        }

        private void validateDelete(Paths paths, String path, Class<?> resource) {
                var pathItem = paths.get(path + "/{id}");
                assertThat(pathItem).isNotNull();
                var delete = pathItem.getDelete();
                assertThat(delete).isNotNull();
                var parameters = delete.getParameters();
                validateIdParameter(parameters);
        }

        // endregion validation operations

        // region validation related operations

        private void validateRelatedRead(Paths paths, String path, Class<?> resource) {
                var pathItem = paths.get(path);
                assertThat(pathItem).isNotNull();
                var get = pathItem.getGet();
                assertThat(get).isNotNull();
                var parameters = get.getParameters();
                validateIdParameter(parameters);
                validatePageableParameter(parameters);
                validateFilterParameter(parameters);
                validateApiResponse(get, resource);
        }

        private void validateRelatedUpdate(Paths paths, String path, Class<?> resource) {
                validateRelatedModify(HttpMethod.PUT, paths, path, resource);
        }

        private void validateRelatedDelete(Paths paths, String path, Class<?> resource) {
                validateRelatedModify(HttpMethod.DELETE, paths, path, resource);
        }

        private void validateRelatedModify(HttpMethod method, Paths paths, String path, Class<?> resource) {
                var pathItem = paths.get(path + "/{relatedIds}");
                assertThat(pathItem).isNotNull();
                var operation = pathItem.readOperationsMap().get(method);
                assertThat(operation).isNotNull();
                var parameters = operation.getParameters();
                validateIdParameter(parameters);
                validateRelatedIdParameter(parameters);
        }

        // endregion validation related operations

        // region validation parameter
        private void validateIdParameter(List<Parameter> parameters) {
                var idSchema = SpringDocAnnotationsUtils.extractSchema(null, ID_FIELD_TYPE, null, null);
                assertThat(parameters).filteredOn(p -> p.getName().equals("id")).first().extracting(
                                p -> p.getIn(),
                                p -> p.getSchema())
                                .containsExactly(
                                                "path",
                                                idSchema);
        }

        private void validateRelatedIdParameter(List<Parameter> parameters) {
                var idSchema = SpringDocAnnotationsUtils.extractSchema(null, ID_FIELD_TYPE, null, null);
                assertThat(parameters).filteredOn(p -> p.getName().equals("relatedIds")).first().extracting(
                                p -> p.getIn(),
                                p -> ((ArraySchema) p.getSchema()).getItems())
                                .containsExactly(
                                                "path",
                                                idSchema);
        }

        private void validateTenantIdParameter(List<Parameter> parameters) {
                assertThat(parameters).filteredOn(p -> p.getName().equals("tenantId")).first().extracting(
                                p -> p.getIn(),
                                p -> p.getSchema().getType())
                        .containsExactly(
                                "query",
                                "string");
        }

        private void validatePageableParameter(List<Parameter> parameters) {
                assertThat(parameters).filteredOn(p -> p.getName().equals("pageable")).first().extracting(
                                p -> p.getIn(),
                                p -> p.getSchema().get$ref())
                                .containsExactly(
                                                "query",
                                                "#/components/schemas/Pageable");
        }

        private void validateFilterParameter(List<Parameter> parameters) {
                assertThat(parameters).filteredOn(p -> p.getName().equals("filter")).first().extracting(
                                p -> p.getIn(),
                                p -> p.getSchema().getType())
                                .containsExactly(
                                                "query",
                                                "string");
        }

        // endregion validation parameter

        private void validateRequestBody(Operation operation, Class<?> clazz) {
                assertThat(operation).extracting(
                                p -> p.getRequestBody().getContent()
                                                .get(org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
                                                .getSchema().get$ref())
                                .isEqualTo("#/components/schemas/" + clazz.getSimpleName());

        }

        private void validateApiResponse(Operation operation, Class<?> resource) {
                var responses = operation.getResponses();
                var successResponse = responses.get("200");
                assertThat(successResponse).isNotNull();
                assertThat(successResponse)
                                .extracting(r -> r.getContent()
                                                .get(org.springframework.http.MediaType.APPLICATION_JSON_VALUE))
                                .extracting(c -> c.getSchema())
                                .extracting(
                                                s -> s.getName(),
                                                s -> ((ArraySchema) s.getProperties().get("items")).getItems()
                                                                .get$ref())
                                .containsExactly(
                                                "ApiResponse",
                                                "#/components/schemas/" + resource.getSimpleName());

        }

        // endregion validation

        private static Stream<Arguments> resourceTypes() {
                var clazzes = new Class<?>[] {
                                DummyEntityA.class,
                                DummyEntityB.class,
                                DummyEntityC.class
                };
                var refs = new Object[] {
                                "#/components/schemas/DummyEntityA",
                                "#/components/schemas/DummyEntityB",
                                "#/components/schemas/DummyEntityC"
                };

                return Stream.of(
                                Arguments.of(take(clazzes, 1), take(refs, 1).toArray()),
                                Arguments.of(take(clazzes, 2), take(refs, 2).toArray()),
                                Arguments.of(take(clazzes, 3), take(refs, 3).toArray()));
        }

        private static Stream<Arguments> resources() {
                return Stream.of(
                                Arguments.of(DummyEntityA.class, "somepath", UUID.class),
                                Arguments.of(DummyEntityB.class, "somepath", UUID.class));
        }

        private static <T> Stream<T> take(T[] items, int i) {
                return Arrays.stream(items).limit(i);
        }

}