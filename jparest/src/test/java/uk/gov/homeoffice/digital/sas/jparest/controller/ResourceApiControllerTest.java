package uk.gov.homeoffice.digital.sas.jparest.controller;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.PlatformTransactionManager;
import uk.gov.homeoffice.digital.sas.jparest.EntityUtils;
import uk.gov.homeoffice.digital.sas.jparest.InvalidFilterException;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityA;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityB;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityC;
import uk.gov.homeoffice.digital.sas.jparest.web.ApiResponse;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.transaction.Transactional;
import java.text.SimpleDateFormat;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@Transactional
@ContextConfiguration(locations = "/test-context.xml")
public class ResourceApiControllerTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    @Test
    void shouldReturnAllEntitiesList() {

        ResourceApiController<DummyEntityA, Integer> controller = getResourceApiController(DummyEntityA.class, Integer.class);
        ApiResponse<DummyEntityA> response = controller.list(null, Pageable.ofSize(100));

        assertThat(response).isNotNull();
        assertThat(response.getItems().size()).isEqualTo(2);
    }

    @Test
    void list_filterProvided_filteredEntitiesReturned() {

        var filteredEntity1 = new DummyEntityA();
        filteredEntity1.setId(998L);
        entityManager.persist(filteredEntity1);
        var filteredEntity2 = new DummyEntityA();
        filteredEntity2.setId(999L);
        entityManager.persist(filteredEntity2);

        SpelExpressionParser expressionParser = new SpelExpressionParser();
        SpelExpression expression = expressionParser.parseRaw(String.format("id>=%s", filteredEntity1.getId()));

        ResourceApiController<DummyEntityA, Integer> controller = getResourceApiController(DummyEntityA.class, Integer.class);
        ApiResponse<DummyEntityA> response = controller.list(expression, Pageable.ofSize(100));

        assertThat(response).isNotNull();
        assertThat(response.getItems().size()).isEqualTo(2);
        assertThat(response.getItems().get(0).getId()).isEqualTo(filteredEntity1.getId());
        assertThat(response.getItems().get(1).getId()).isEqualTo(filteredEntity2.getId());
    }

    @Test
    void shouldReturnEntityById() {
        ResourceApiController<DummyEntityA, Integer> controller = getResourceApiController(DummyEntityA.class, Integer.class);
        ResponseEntity<ApiResponse<DummyEntityA>> response = (ResponseEntity<ApiResponse<DummyEntityA>>) controller
                .get(2);
        ApiResponse<DummyEntityA> apiResponse = response.getBody();
        DummyEntityA dummy = apiResponse.getItems().get(0);

        assertThat(apiResponse.getItems().size() == 1).isTrue();
        assertThat(dummy.getId()).isEqualTo(2);
    }

    @Test
    void shouldSaveData() {
        String payload = "{\n" +
                "            \"id\": 7\n" +
                "        }";

        ResourceApiController<DummyEntityA, Integer> controller = getResourceApiController(DummyEntityA.class, Integer.class);
        ResponseEntity<ApiResponse<DummyEntityA>> response = (ResponseEntity<ApiResponse<DummyEntityA>>) assertDoesNotThrow(
                () -> controller.create(payload));
        ApiResponse<DummyEntityA> apiResponse = response.getBody();
        DummyEntityA dummy = apiResponse.getItems().get(0);

        assertThat(apiResponse.getItems().size()).isEqualTo(1);
        assertThat(dummy).isNotNull();
        assertThat(dummy.getId()).isNotNull();
    }

    @Test
    void shouldUpdateData() {

        String payload = "{" +
                "            \"id\": 100," +
                "            \"description\": \"Dummy Entity C 100\"" +
                "        }";

        String updatedPayload = "{" +
                "            \"id\": 100," +
                "            \"description\": \"Updated Dummy Entity C 100\"" +
                "        }";

        ResourceApiController<DummyEntityC, Integer> controller = getResourceApiController(DummyEntityC.class, Integer.class);

        assertDoesNotThrow(() -> controller.create(payload));

        ResponseEntity<ApiResponse<DummyEntityC>> response = (ResponseEntity<ApiResponse<DummyEntityC>>) assertDoesNotThrow(
                () -> controller.update(100, updatedPayload));
        ApiResponse<DummyEntityC> apiResponse = response.getBody();
        DummyEntityC dummy = apiResponse.getItems().get(0);

        assertThat(apiResponse.getItems().size()).isEqualTo(1);
        assertThat(dummy).isNotNull();
        assertThat(dummy.getId()).isEqualTo(100);
        assertThat(dummy.getDescription()).isEqualTo("Updated Dummy Entity C 100");

        response = (ResponseEntity<ApiResponse<DummyEntityC>>) controller.get(100);
        apiResponse = response.getBody();
        dummy = apiResponse.getItems().get(0);
        assertThat(dummy).isNotNull();
        assertThat(dummy.getId()).isEqualTo(100);
        assertThat(dummy.getDescription()).isEqualTo("Updated Dummy Entity C 100");

    }

    @Test
    @Transactional
    void shouldDeleteData() {
        String payload = "{" +
                "            \"id\": 100," +
                "            \"description\": \"Dummy Entity C 100\"" +
                "        }";

        ResourceApiController<DummyEntityC, Integer> controller = getResourceApiController(DummyEntityC.class, Integer.class);
        assertDoesNotThrow(() -> controller.create(payload));
        ResponseEntity<ApiResponse<DummyEntityC>> response = (ResponseEntity<ApiResponse<DummyEntityC>>) controller
                .get(100);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        controller.delete(100);

        response = (ResponseEntity<ApiResponse<DummyEntityC>>) controller.get(100);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldAddRelatedEntities() {
        ResourceApiController<DummyEntityA, Integer> controller = getResourceApiController(DummyEntityA.class, Integer.class);

        ResponseEntity<?> response = assertDoesNotThrow(
                () -> controller.addRelated(2, "dummyEntityBSet", new Object[] { 1 }));
    }

    @ParameterizedTest
    @MethodSource("filterSource")
    void shouldGetRelatedEntities_With_And_Without_Filters(SpelExpression expression) {
        ResourceApiController<DummyEntityA, Integer> controller = getResourceApiController(DummyEntityA.class, Integer.class);

        ApiResponse<?> apiResponse = controller.getRelated(2, "dummyEntityBSet", expression, Pageable.ofSize(100));
        DummyEntityB dummyB = (DummyEntityB) apiResponse.getItems().get(0);
        assertThat(apiResponse.getItems().size()).isEqualTo(1);
        assertThat(dummyB.getId()).isEqualTo(2);
    }

    private static Stream filterSource(){
        return Stream.of(
                new SpelExpressionParser().parseRaw("id==2"),
                new SpelExpressionParser().parseRaw("id>0"),
                null
        );
    }

    @ParameterizedTest
    @CsvSource({"1","2","3"})
    void shouldReturnEntityNotFound_AddRelated(int id) {
        ResourceApiController<DummyEntityA, Integer> controller = getResourceApiController(DummyEntityA.class, Integer.class);

        ResponseEntity<?> response = assertDoesNotThrow(
                () -> controller.addRelated(id, "dummyEntityBSet", new Object[] { id }));

        if(id == 3){
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        } else {
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @Test
    void shouldDeleteRelatedEntities() {
        ResourceApiController<DummyEntityA, Integer> controller = getResourceApiController(DummyEntityA.class, Integer.class);

        ResponseEntity<?> response = assertDoesNotThrow(
                () -> controller.deleteRelated(2, "dummyEntityBSet", new Object[] { 2 }));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @ParameterizedTest
    @CsvSource({
            "2,3",
            "3,1"
    })
    void deleteRelatedEntities_whenEntityNotFound_throws(int entityId, int relatedEntityId) {
        ResourceApiController<DummyEntityA, Integer> controller = getResourceApiController(DummyEntityA.class, Integer.class);

        ResponseEntity<?> response = assertDoesNotThrow(
                () -> controller.deleteRelated(entityId, "dummyEntityBSet", new Object[] { relatedEntityId }));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getIdentifier_identifierIsNull_exceptionThrown() {
        ResourceApiController<DummyEntityA, Integer> controller = getResourceApiController(DummyEntityA.class, Integer.class);

        var thrown = assertThrows(
                IllegalArgumentException.class,
                () -> controller.get(null));

        assertThat(thrown.getMessage()).isEqualTo("identifier must not be null");
    }

    @ParameterizedTest
    @MethodSource("invalidIDSource")
    void shouldThrowIllegalArgumentExceptionForInvalidId(Class<?> clazz, Object id) {
        ResourceApiController controller = getResourceApiController(DummyEntityA.class, clazz);
        assertThatThrownBy( () ->
                controller.get(id)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("identifier must not be null");
    }

    @ParameterizedTest
    @MethodSource("exceptionSource")
    void shouldReturnBadRequest(Exception exception){
        ResourceApiController controller = getResourceApiController(DummyEntityA.class, String.class);
        ResponseEntity<String> response = controller.handleException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getStatusCode().getReasonPhrase()).isEqualTo(HttpStatus.BAD_REQUEST.getReasonPhrase());

        if(exception instanceof InvalidFilterException){
            assertThat(response.getBody()).isEqualTo("Invalid Filter Exception");
        } else {
            assertThat(response.getBody()).isNull();
        }
    }

    @SneakyThrows
    @Test
    void shouldReturnBadRequestForInvalidPayload_SaveData() {
        ResourceApiController<DummyEntityA, Integer> controller = getResourceApiController(DummyEntityA.class, Integer.class);
        ResponseEntity response = controller.create("");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNull();
    }

    @SneakyThrows
    @Test
    void shouldThrowPersistenceExceptionForEmptyPayload_SaveData(){
        ResourceApiController<DummyEntityA, Integer> controller = getResourceApiController(DummyEntityA.class, Integer.class);
        assertThatThrownBy( () -> controller.create("{}")).isInstanceOf(PersistenceException.class);
    }

    @Test
    void handleException_exceptionIsInvalidFilterException_messageContainedInResponse() {

        var exception = new InvalidFilterException("my message");
        ResourceApiController<DummyEntityA, Integer> controller = getResourceApiController(DummyEntityA.class, Integer.class);
        var actualResponse = controller.handleException(exception);
        assertThat(actualResponse.getBody()).isEqualTo(exception.getMessage());
        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void handleException_exceptionIsNotInvalidFilterException_responseBodyIsNull() {

        ResourceApiController<DummyEntityA, Integer> controller = getResourceApiController(DummyEntityA.class, Integer.class);
        var actualResponse = controller.handleException(new Exception());
        assertThat(actualResponse.getBody()).isNull();
        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @ParameterizedTest
    @MethodSource("sortSource")
    void shouldReturnAllEntitiesListByDescOrder(Sort sort) {

        ResourceApiController<DummyEntityA, Integer> controller = getResourceApiController(DummyEntityA.class, Integer.class);
        Pageable pageable = PageRequest.ofSize(100).withSort(sort);
        ApiResponse<DummyEntityA> response = controller.list(null, pageable);

        assertThat(response).isNotNull();
        assertThat(response.getItems().size()).isEqualTo(2);

        if(sort.getOrderFor("id").isDescending()){
            assertThat(response.getItems().get(0).getId()).isEqualTo(2);
            assertThat(response.getItems().get(1).getId()).isEqualTo(1);
        } else {
            assertThat(response.getItems().get(1).getId()).isEqualTo(2);
            assertThat(response.getItems().get(0).getId()).isEqualTo(1);
        }
    }

    @Test
    void shouldReturnEntityNotFound() {
        ResourceApiController<DummyEntityC, String> controller = getResourceApiController(DummyEntityC.class, String.class);
        ResponseEntity<String> response = controller.delete("10");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @SneakyThrows
    @Test
    void shouldReturnBadRequestForInvalidPayload_UpdateData() {
        ResourceApiController<DummyEntityA, Integer> controller = getResourceApiController(DummyEntityA.class, Integer.class);
        ResponseEntity response = controller.update(1, "");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNull();
    }

    @SneakyThrows
    @Test
    void shouldReturnNotFound_UpdateData(){
        ResourceApiController<DummyEntityA, String> controller = getResourceApiController(DummyEntityA.class, String.class);
        ResponseEntity response = controller.update("10", "{}");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @ParameterizedTest
    @CsvSource({"1","3","4","5"})
    void shouldReturnNotFoundForInvalidIdElseOK_AddRelated(int id) {
        ResourceApiController<DummyEntityA, Integer> controller = getResourceApiController(DummyEntityA.class, Integer.class);

        ResponseEntity<?> response = assertDoesNotThrow(
                () -> controller.addRelated(id, "dummyEntityBSet", new Object[] { id }));

        if(id == 1){
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        } else {
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Test
    void shouldReturnFilteredEntitiesList() {
        SpelExpressionParser expressionParser = new SpelExpressionParser();
        SpelExpression expression = expressionParser.parseRaw("id==2");

        ResourceApiController<DummyEntityA, Integer> controller = getResourceApiController(DummyEntityA.class, Integer.class);
        ApiResponse<DummyEntityA> response = controller.list(expression, Pageable.ofSize(100));

        assertThat(response).isNotNull();
        assertThat(response.getItems().size()).isEqualTo(1);
    }

    private static Stream sortSource(){
        return Stream.of(
                Arguments.of(Sort.by("id").descending()),
                Arguments.of(Sort.by("id").ascending())
        );
    }

    private static Stream exceptionSource(){
        return Stream.of(
                Arguments.of(new IndexOutOfBoundsException()),
                Arguments.of(new InvalidFilterException("Invalid Filter Exception"))
        );
    }

    private static Stream invalidIDSource(){
        return Stream.of(
                Arguments.of(String.class, ""),
                Arguments.of(Integer.class, null)
        );
    }

    private <T, U> ResourceApiController<T, U> getResourceApiController(Class<T> clazz, Class<U> clazzU) {
        EntityUtils<T> entityUtils = new EntityUtils<T>(clazz, entityManager);
        return new ResourceApiController<T, U>(clazz, entityManager, transactionManager, entityUtils);
    }

}