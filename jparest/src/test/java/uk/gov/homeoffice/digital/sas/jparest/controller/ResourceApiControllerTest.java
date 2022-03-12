package uk.gov.homeoffice.digital.sas.jparest.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.text.SimpleDateFormat;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.PlatformTransactionManager;

import uk.gov.homeoffice.digital.sas.jparest.EntityUtils;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityA;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityB;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityC;
import uk.gov.homeoffice.digital.sas.jparest.web.ApiResponse;

// @ExtendWith(SpringExtension.class)
// @SpringBootTest(classes= EntitiesApplication.class)
// @WebAppConfiguration

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

        ResourceApiController<DummyEntityA, Integer> controller = getResourceApiController(DummyEntityA.class);
        ApiResponse<DummyEntityA> response = controller.list(null, Pageable.ofSize(100));

        assertThat(response).isNotNull();
        assertThat(response.getItems().size()).isEqualTo(2);
    }

    @Test
    void shouldReturnEntityById() {
        ResourceApiController<DummyEntityA, Integer> controller = getResourceApiController(DummyEntityA.class);
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

        ResourceApiController<DummyEntityA, Integer> controller = getResourceApiController(DummyEntityA.class);
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

        ResourceApiController<DummyEntityC, Integer> controller = getResourceApiController(DummyEntityC.class);

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

        ResourceApiController<DummyEntityC, Integer> controller = getResourceApiController(DummyEntityC.class);
        assertDoesNotThrow(() -> controller.create(payload));
        ResponseEntity<ApiResponse<DummyEntityC>> response = (ResponseEntity<ApiResponse<DummyEntityC>>) controller
                .get(100);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        controller.delete(100);

        response = (ResponseEntity<ApiResponse<DummyEntityC>>) controller.get(100);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldGetRelatedEntities() {
        ResourceApiController<DummyEntityA, Integer> controller = getResourceApiController(DummyEntityA.class);

        SpelExpressionParser expressionParser = new SpelExpressionParser();
        SpelExpression expression = expressionParser.parseRaw("id==2");

        ApiResponse<?> apiResponse = controller.getRelated(2, "dummyEntityBSet", expression, Pageable.ofSize(100));
        DummyEntityB dummyB = (DummyEntityB) apiResponse.getItems().get(0);
        assertThat(apiResponse.getItems().size()).isEqualTo(1);
        assertThat(dummyB.getId()).isEqualTo(2);
    }

    @Test
    void shouldAddRelatedEntities() {
        ResourceApiController<DummyEntityA, Integer> controller = getResourceApiController(DummyEntityA.class);

        ResponseEntity<?> response = assertDoesNotThrow(
                () -> controller.addRelated(2, "dummyEntityBSet", new Object[] { 1 }));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldDeleteRelatedEntities() {
        ResourceApiController<DummyEntityA, Integer> controller = getResourceApiController(DummyEntityA.class);

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
        ResourceApiController<DummyEntityA, Integer> controller = getResourceApiController(DummyEntityA.class);

        ResponseEntity<?> response = assertDoesNotThrow(
                () -> controller.deleteRelated(entityId, "dummyEntityBSet", new Object[] { relatedEntityId }));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private <T> ResourceApiController<T, Integer> getResourceApiController(Class<T> clazz) {
        EntityUtils<T> entityUtils = new EntityUtils<T>(clazz, entityManager);
        return new ResourceApiController<T, Integer>(clazz, entityManager, transactionManager, entityUtils);
    }
}