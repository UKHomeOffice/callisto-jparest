package uk.gov.homeoffice.digital.sas.jparest.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.PlatformTransactionManager;
import uk.gov.homeoffice.digital.sas.jparest.EntityUtils;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityA;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityB;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityC;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityD;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.InvalidFilterException;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.ResourceConstraintViolationException;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.ResourceNotFoundException;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.UnknownResourcePropertyException;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@Transactional
@ContextConfiguration(locations = "/test-context.xml")
class ResourceApiControllerTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private static final String RESOURCE_NOT_FOUND_ERROR_FORMAT = "Resource with id: %s was not found";

    // region list

    @Test
    void list_withoutFilter_returnsAllEntities() {

        var controller = getResourceApiController(DummyEntityA.class, Integer.class);
        var response = controller.list(null, Pageable.ofSize(100));

        assertThat(response).isNotNull();
        assertThat(response.getItems()).hasSize(10);
    }

    @ParameterizedTest
    @MethodSource("filters")
    void list_withFilter_returnsFilteredEntities(SpelExpression expression, int expectedItems) {

        var controller = getResourceApiController(DummyEntityA.class, Integer.class);
        var response = controller.list(expression, Pageable.ofSize(100));

        assertThat(response).isNotNull();
        assertThat(response.getItems()).hasSize(expectedItems);
    }

    @Test
    void list_sorted_returnsItemsSortedInCorrectDirection() {

        var controller = getResourceApiController(DummyEntityA.class, Integer.class);

        var sort = Sort.by(Direction.ASC, "id");
        var pageable = PageRequest.ofSize(100).withSort(sort);

        var response = controller.list(null, pageable);
        var items = response.getItems().toArray(new DummyEntityA[0]);

        assertThat(items).hasSizeGreaterThanOrEqualTo(2);
        for (var i = 1; i < items.length; i++) {
            assertThat(items[i].getId()).isGreaterThan(items[i - 1].getId());
        }

        sort = Sort.by(Direction.DESC, "id");
        pageable = PageRequest.ofSize(100).withSort(sort);

        response = controller.list(null, pageable);
        items = response.getItems().toArray(new DummyEntityA[0]);

        assertThat(items).hasSizeGreaterThanOrEqualTo(2);
        for (var i = 1; i < items.length; i++) {
            assertThat(items[i].getId()).isLessThan(items[i - 1].getId());
        }

    }

    // endregion

    // region get

    @Test
    void get_resourceWithIdExists_returnsEntity() {

        var controller = getResourceApiController(DummyEntityA.class, Integer.class);

        var apiResponse = controller.get(2);
        var dummy = apiResponse.getItems().get(0);

        assertThat(apiResponse.getItems()).hasSize(1);
        assertThat(dummy.getId()).isEqualTo(2);
    }

    @Test
    void get_idIsNull_throwsIllegalArugmentException() {
        var controller = getResourceApiController(DummyEntityA.class, Integer.class);

        assertThatIllegalArgumentException().isThrownBy(
                () -> controller.get(null))
                .withMessage("identifier must not be null");
    }

    @ParameterizedTest
    @MethodSource("invalidIDSource")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    void get_idCantBeConvertedToExpectedType_throwsTypeMismatchException(Class<?> clazz, Object id) {
        ResourceApiController controller = getResourceApiController(DummyEntityA.class, clazz);
        assertThatThrownBy(() -> controller.get(id)).isInstanceOf(TypeMismatchException.class);
    }

    // endregion

    // region create

    @Test
    @Transactional
    void create_resourceIsValid_resourceIsPersisted() {
        String payload = "{\n" +
                "            \"id\": -1\n" +
                "        }";

        var controller = getResourceApiController(DummyEntityA.class, Integer.class);

        assertThrows(ResourceNotFoundException.class, () -> controller.get(-1));

        var apiResponse = assertDoesNotThrow(() -> controller.create(payload));
        var dummy = apiResponse.getItems().get(0);

        assertThat(apiResponse.getItems()).hasSize(1);
        assertThat(dummy).isNotNull();
        assertThat(dummy.getId()).isNotNull();

        var retryResponse = controller.get(-1);
        var retryResource = retryResponse.getItems().get(0);
        assertThat(retryResource).isEqualTo(dummy);

    }

    @Test
    void create_emptyPayload_jsonExceptionThrown() {
        var controller = getResourceApiController(DummyEntityA.class, Integer.class);
        assertThrows(JsonProcessingException.class, () -> controller.create(""));
    }

    @Test
    void create_invalidPayload_persistenceExceptionThrown() {
        var controller = getResourceApiController(DummyEntityA.class, Integer.class);
        assertThatThrownBy(() -> controller.create("{}")).isInstanceOf(PersistenceException.class);
    }

    @Test
    void create_unrecognizedPropertyOnPayload_unknownResourcePropertyExceptionThrown() {
        var controller = getResourceApiController(DummyEntityA.class, Integer.class);
        assertThatThrownBy(() -> controller.create("{\"otherUnknownProperty\": 1}")).isInstanceOf(UnknownResourcePropertyException.class);
    }

    @Test
    void create_payloadViolatesEntityConstraints_resourceConstraintViolationExceptionThrown() {
        var controller = getResourceApiController(DummyEntityD.class, Integer.class);
        assertThatThrownBy(() -> controller.create("{}")).isInstanceOf(ResourceConstraintViolationException.class)
                .hasMessageContainingAll("description", "telephone", "has the following error(s):");
    }

    // endregion

    // region update

    @Test
    @Transactional
    void update_resourceExists_persistsChanges() {

        String payload = "{" +
                "            \"id\": 100," +
                "            \"description\": \"Dummy Entity C 100\"," +
                "            \"index\": 1" +
                "        }";

        String updatedPayload = "{" +
                "            \"id\": 100," +
                "            \"description\": \"Updated Dummy Entity C 100\"," +
                "            \"index\": 2" +
                "        }";

        var controller = getResourceApiController(DummyEntityC.class, Integer.class);

        assertDoesNotThrow(() -> controller.create(payload));
        var getResponse = assertDoesNotThrow(() -> controller.get(100));
        var getResource = getResponse.getItems().get(0);
        assertThat(getResource.getDescription()).isEqualTo("Dummy Entity C 100");

        var updateResponse = assertDoesNotThrow(() -> controller.update(100, updatedPayload));
        var dummy = updateResponse.getItems().get(0);

        assertThat(updateResponse.getItems()).hasSize(1);
        assertThat(dummy).isNotNull();
        assertThat(dummy.getId()).isEqualTo(100);
        assertThat(dummy.getIndex()).isEqualTo(2);
        assertThat(dummy.getDescription()).isEqualTo("Updated Dummy Entity C 100");

        var checkResponse = controller.get(100);
        var checkResource = checkResponse.getItems().get(0);
        assertThat(checkResource).isEqualTo(dummy);

    }

    @ParameterizedTest
    @MethodSource("invalidPayloads")
    @Transactional
    void update_resourceExistsInvalidPayload_jsonExceptionThrown(String payload) {
        var controller = getResourceApiController(DummyEntityA.class, Integer.class);
        assertThrows(JsonProcessingException.class, () -> controller.update(1, payload));
    }

    @ParameterizedTest
    @MethodSource("invalidPayloads")
    @Transactional
    void update_resourceDoesntExistInvalidPayload_jsonExceptionThrown(String payload) {

        var controller = getResourceApiController(DummyEntityA.class, Integer.class);
        assertThrows(ResourceNotFoundException.class, () -> controller.get(-1));
        assertThrows(JsonProcessingException.class, () -> controller.update(-1, payload));
    }

    @Test
    void update_unrecognizedPropertyOnPayload_unknownResourcePropertyExceptionThrown() {
        var controller = getResourceApiController(DummyEntityA.class, Integer.class);
        assertThatThrownBy(() -> controller.update(-1, "{\"someprop\": \"somevalue\"}")).isInstanceOf(UnknownResourcePropertyException.class);
    }

    @Test
    @Transactional
    void update_resourceDoesntExist_resourceNotFoundExceptionThrown() {
        var controller = getResourceApiController(DummyEntityA.class, Integer.class);
        assertThrows(ResourceNotFoundException.class, () -> controller.update(-1, "{}"));
    }

    @Test
    @Transactional
    void update_payloadIdDoesNotMatchUrlPathId_throwsError() {
        var controller = getResourceApiController(DummyEntityA.class, Integer.class);

        var payload = "{\"id\": 2 }";
        assertThatIllegalArgumentException()
                .isThrownBy(() -> controller.update(1, payload))
                .withMessageContaining("payload resource id value must match the url id");
    }

    @Test
    @Transactional
    void update_payloadOmitsId_noIdMissMatchErrorThrown() {
        var controller = getResourceApiController(DummyEntityA.class, Integer.class);
        assertDoesNotThrow(() -> controller.update(1, "{}"));
    }

    @Test
    void update_payloadViolatesEntityConstraints_resourceConstraintViolationExceptionThrown() {
        var controller = getResourceApiController(DummyEntityD.class, Integer.class);
        assertThatThrownBy(() -> controller.update(-1, "{}")).isInstanceOf(ResourceConstraintViolationException.class)
                .hasMessageContainingAll("description", "telephone", "has the following error(s):");
    }

    // endregion

    // region delete

    @Test
    @Transactional
    void delete_resourceExists_resourceIsDeleted() {
        String payload = "{" +
                "            \"id\": 100," +
                "            \"description\": \"Dummy Entity C 100\"," +
                "            \"index\": 1" +
                "        }";

        var controller = getResourceApiController(DummyEntityC.class, Integer.class);
        assertDoesNotThrow(() -> controller.create(payload));
        assertDoesNotThrow(() -> controller.get(100));
        assertDoesNotThrow(() -> controller.delete(100));
        assertThrows(ResourceNotFoundException.class, () -> controller.get(100));
    }

    @Test
    @Transactional
    void delete_resourceDoesNotExist_resourceNotFoundExceptionThrown() {

        var controller = getResourceApiController(DummyEntityC.class, Integer.class);
        assertThatThrownBy(() -> controller.delete(-1)).isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Error accessing data for deletion for entity with id: -1");
    }

    // endregion

    // region addRelated

    @Test
    @Transactional
    @SuppressWarnings("unchecked")
    void addRelated_allResourcesExist_addsRelatedItems() {

        var controller = getResourceApiController(DummyEntityA.class, Integer.class);

        var getRelatedResponse = controller.getRelated(10, "dummyEntityBSet", null, Pageable.ofSize(100));
        assertThat(getRelatedResponse.getItems()).isEmpty();

        assertDoesNotThrow(
                () -> controller.addRelated(10, "dummyEntityBSet", new Object[] { 1 }));

        getRelatedResponse = controller.getRelated(10, "dummyEntityBSet", null, Pageable.ofSize(100));
        assertThat(getRelatedResponse.getItems()).hasSize(1);
        var resource = (DummyEntityB) getRelatedResponse.getItems().get(0);
        assertThat(resource.getId()).isEqualTo(1);

    }

    @Test
    @Transactional
    void addRelated_resourceDoesntExist_resourceNotFoundExceptionThrown() {

        var controller = getResourceApiController(DummyEntityA.class, Integer.class);

        assertThatThrownBy(() -> controller.addRelated(-1, "dummyEntityBSet", new Object[] { 1 }))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(String.format(RESOURCE_NOT_FOUND_ERROR_FORMAT, -1));
    }

    @Test
    @Transactional
    void addRelated_relatedResourceDoesntExist_resourceNotFoundExceptionThrown() {
        var controller = getResourceApiController(DummyEntityA.class, Integer.class);

        assertDoesNotThrow(() -> controller.get(1));

        assertThatThrownBy(() ->  controller.addRelated(1, "dummyEntityBSet", new Object[] { -1 }))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Error adding related resources for resource with id: 1");

    }

    // endregion

    // region getRelated

    @ParameterizedTest
    @MethodSource("relatedResourceFilters")
    @SuppressWarnings("unchecked")
    void getRelated_filterExpressionProvided_returnsFilteredResources(int resourceId, SpelExpression expression,
            int expectedItems) {

        var controller = getResourceApiController(DummyEntityA.class, Integer.class);

        var apiResponse = controller.getRelated(resourceId, "dummyEntityBSet", expression, Pageable.ofSize(100));

        assertThat(apiResponse).isNotNull();
        assertThat(apiResponse.getItems()).hasSize(expectedItems);

    }

    // endregion

    // region deleteRelated

    @Test
    @Transactional
    void deleteRelated_relationshipExists_deletesRelationshipButAllResourcesStillExist() {

        var controllerA = getResourceApiController(DummyEntityA.class, Integer.class);

        var getRelatedResponse = controllerA.getRelated(2, "dummyEntityBSet", null, Pageable.ofSize(100));
        @SuppressWarnings("unchecked")
        var items = (List<DummyEntityB>) getRelatedResponse.getItems();
        assertThat(items).isNotEmpty()
                .anyMatch((item) -> item.getId().equals(2L));

        assertDoesNotThrow(
                () -> controllerA.deleteRelated(2, "dummyEntityBSet", new Object[] { 2 }));

        getRelatedResponse = controllerA.getRelated(2, "dummyEntityBSet", null, Pageable.ofSize(100));
        @SuppressWarnings("unchecked")
        var checkItems = (List<DummyEntityB>) getRelatedResponse.getItems();
        assertThat(checkItems).noneMatch((item) -> item.getId().equals(2L));

        var controllerB = getResourceApiController(DummyEntityB.class, Integer.class);
        assertDoesNotThrow(() -> controllerB.get(2));

    }

    @Test
    @Transactional
    void deleteRelated_resourceDoesntExist_resourceNotFoundExceptionThrown() {

        var controllerA = getResourceApiController(DummyEntityA.class, Integer.class);

        assertThrows(ResourceNotFoundException.class, () -> controllerA.get(-1));

        assertThatThrownBy(() ->  controllerA.deleteRelated(-1, "dummyEntityBSet", new Object[] { 2 }))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(String.format(RESOURCE_NOT_FOUND_ERROR_FORMAT, -1));
    }

    @Test
    @Transactional
    void deleteRelated_resourceExistsButRelationshipDoesnt_resourceNotFoundExceptionThrown() {

        var controllerA = getResourceApiController(DummyEntityA.class, Integer.class);

        assertDoesNotThrow(() -> controllerA.get(1));

        var getRelatedResponse = controllerA.getRelated(1, "dummyEntityBSet", null, Pageable.ofSize(100));
        @SuppressWarnings("unchecked")
        var checkItems = (List<DummyEntityB>) getRelatedResponse.getItems();
        assertThat(checkItems).noneMatch((item) -> item.getId().equals(-1L));

        assertThatThrownBy(() -> controllerA.deleteRelated(1, "dummyEntityBSet", new Object[] { -1 }))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("No related resources removed as the following resources could not be found. Ids:[-1]");
    }

    @Test
    @Transactional
    void deleteRelated_resourceExists_relatedIdsResultInFoundAndNotFound_exceptionIncludesNotFoundIdsInMessage() {

        var controllerA = getResourceApiController(DummyEntityA.class, Integer.class);

        assertDoesNotThrow(() -> controllerA.get(1));

        var getRelatedResponse = controllerA.getRelated(1, "dummyEntityBSet", null, Pageable.ofSize(100));
        @SuppressWarnings("unchecked")
        var checkItems = (List<DummyEntityB>) getRelatedResponse.getItems();
        assertThat(checkItems).noneMatch((item) -> item.getId().equals(-1L))
                .noneMatch((item) -> item.getId().equals(-2L))
                .isNotEmpty().anyMatch((item) -> item.getId().equals(2L));

        assertThatThrownBy(() -> controllerA.deleteRelated(1, "dummyEntityBSet", new Object[] { -1, -2, 2 }))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContainingAll("No related resources removed as the following resources could not be found. Ids:", "-1", "-2");
    }

    // endregion


    // region Method Sources

    private static Stream<Arguments> filters() {
        SpelExpressionParser expressionParser = new SpelExpressionParser();

        return Stream.of(
                Arguments.of(expressionParser.parseRaw("id==2"), 1),
                Arguments.of(expressionParser.parseRaw("id<0"), 0),
                // Arguments.of(expressionParser.parseRaw("id==-1"), 0),
                Arguments.of(null, 10));
    }

    private static Stream<Arguments> relatedResourceFilters() {
        SpelExpressionParser expressionParser = new SpelExpressionParser();

        return Stream.of(
                Arguments.of(1, expressionParser.parseRaw("id==2"), 1),
                Arguments.of(1, expressionParser.parseRaw("id<0"), 0),
                Arguments.of(1, null, 2));
    }

    private static Stream<Arguments> invalidIDSource() {
        return Stream.of(
                Arguments.of(Integer.class, "blah"),
                Arguments.of(UUID.class, "blah"));
    }

    private static Stream<Arguments> exceptionSource() {
        return Stream.of(
                Arguments.of(new InvalidFilterException()),
                Arguments.of(new InvalidFilterException("Another reason")),
                Arguments.of(new InvalidFilterException("Invalid Filter Exception")));
    }

    private static Stream<String> invalidPayloads() {
        return Stream.of("");

    }
    // endregion

    private <T, U> ResourceApiController<T, U> getResourceApiController(Class<T> clazz, Class<U> clazzU) {
        var entityUtils = new EntityUtils<T>(clazz, entityManager);
        return new ResourceApiController<T, U>(clazz, entityManager, transactionManager, entityUtils);
    }

}