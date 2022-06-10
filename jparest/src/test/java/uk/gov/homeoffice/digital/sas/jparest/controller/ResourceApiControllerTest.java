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
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityF;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.ResourceConstraintViolationException;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.ResourceNotFoundException;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.ResourceNotFoundExceptionMessageUtil;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.TenantIdMismatchException;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.UnknownResourcePropertyException;
import uk.gov.homeoffice.digital.sas.jparest.models.BaseEntity;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.junit.jupiter.api.Named.named;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@Transactional
@ContextConfiguration(locations = "/test-context.xml")
class ResourceApiControllerTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private PlatformTransactionManager transactionManager;
    
    
    public static final UUID NON_EXISTENT_ID = UUID.fromString("7a7c7da4-bb29-11ec-1000-0242ac120001");
    public static final UUID NON_EXISTENT_ID_2 = UUID.fromString("7a7c7da4-bb29-11ec-1001-0242ac120002");
    public static final UUID NEW_RESOURCE_ID = UUID.fromString("7a7c7da4-bb29-11ec-1002-0242ac120003");

    public static final UUID DUMMY_A_ID_1 = UUID.fromString("b7e813a2-bb28-11ec-8422-0242ac110001");
    public static final UUID DUMMY_A_ID_2 = UUID.fromString("b7e813a2-bb28-11ec-8422-0242ac110002");
    public static final UUID DUMMY_A_ID_10 = UUID.fromString("b7e813a2-bb28-11ec-8422-0242ac110010");

    public static final UUID DUMMY_B_ID_2 = UUID.fromString("b7e813a2-bb28-11ec-8422-0242ac120002");
    public static final UUID DUMMY_B_ID_3 = UUID.fromString("b7e813a2-bb28-11ec-8422-0242ac120003");
    public static final UUID DUMMY_B_ID_4 = UUID.fromString("b7e813a2-bb28-11ec-8422-0242ac120004");

    public static final UUID DUMMY_C_ID_1 = UUID.fromString("b7e813a2-bb28-11ec-8422-0242ac130001");

    private static final UUID TENANT_ID = UUID.fromString("b7e813a2-bb28-11ec-8422-0242ac120002");
    private static final UUID INVALID_TENANT_ID = UUID.fromString("7a7c7da4-bb29-11ec-1003-0242ac120004");
    
    private static final String ID_FIELD_NAME = "id";
    private static final String TENANT_ID_FIELD_NAME = "tenantId";
    private static final String DESCRIPTION_FIELD_NAME = "description";
    private static final String INDEX_FIELD_NAME = "index";
    private static final String PROFILE_ID_FIELD_NAME = "profileId";
    private static final String DUMMY_B_SET_FIELD_NAME = "dummyEntityBSet";
    
    private static final String RESOURCE_NOT_FOUND_ERROR_FORMAT = "Resource with id: %s was not found";



    // region list

    @Test
    void list_withoutFilter_returnsAllEntities() {

        var controller = getResourceApiController(DummyEntityA.class);
        var response = controller.list(TENANT_ID, null, Pageable.ofSize(100));

        assertThat(response).isNotNull();
        assertThat(response.getItems()).hasSize(10);
    }

    @ParameterizedTest
    @MethodSource("filters")
    void list_withFilter_returnsFilteredEntities(SpelExpression expression, int expectedItems) {

        var controller = getResourceApiController(DummyEntityA.class);
        var response = controller.list(TENANT_ID, expression, Pageable.ofSize(100));

        assertThat(response).isNotNull();
        assertThat(response.getItems()).hasSize(expectedItems);
    }

    @Test
    void list_sorted_returnsItemsSortedInCorrectDirection() {

        var controller = getResourceApiController(DummyEntityA.class);

        var sort = Sort.by(Direction.ASC, "id");
        var pageable = PageRequest.ofSize(100).withSort(sort);

        var response = controller.list(TENANT_ID, null, pageable);
        final var items = response.getItems().toArray(new DummyEntityA[0]);

        assertThat(items).hasSizeGreaterThanOrEqualTo(2);
        IntStream.range(1, items.length).forEach(i ->
                assertThat(items[i].getId()).isGreaterThan(items[i-1].getId()));

        sort = Sort.by(Direction.DESC, "id");
        pageable = PageRequest.ofSize(100).withSort(sort);


        response = controller.list(TENANT_ID, null, pageable);
        final var items2 = response.getItems().toArray(new DummyEntityA[0]);

        assertThat(items2).hasSizeGreaterThanOrEqualTo(2);
        IntStream.range(1, items2.length).forEach(i ->
                assertThat(items2[i].getId()).isLessThan(items2[i-1].getId()));
    }

    @Test
    void list_requestTenantIdMatchesResourceTenantIds_noExceptionThrown() {

        var controller = getResourceApiController(DummyEntityA.class);
        assertThatNoException().isThrownBy(() -> controller.list(TENANT_ID, null, Pageable.ofSize(100)));
    }

    @Test
    void list_requestTenantIdDoesNotMatchResourceTenantIds_noResourcesReturned() {

        var controller = getResourceApiController(DummyEntityA.class);

        var response = controller.list(INVALID_TENANT_ID, null, Pageable.ofSize(100));
        assertThat(response.getItems().toArray(new DummyEntityA[0])).isEmpty();
    }

    // endregion

    // region get


    @Test
    void get_resourceWithIdExists_returnsEntity() {

        var controller = getResourceApiController(DummyEntityA.class);

        var apiResponse = controller.get(TENANT_ID, DUMMY_A_ID_2);
        var dummy = apiResponse.getItems().get(0);

        assertThat(apiResponse.getItems()).hasSize(1);
        assertThat(dummy.getId()).isEqualTo(DUMMY_A_ID_2);
    }

    @Test
    void get_idIsNull_throwsIllegalArgumentException() {
        var controller = getResourceApiController(DummyEntityA.class);

        assertThatIllegalArgumentException().isThrownBy(
                        () -> controller.get(TENANT_ID, null))
                .withMessage("identifier must not be null");
    }

    @ParameterizedTest
    @MethodSource("invalidIDSource")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    <U> void get_idCantBeConvertedToExpectedType_throwsTypeMismatchException(U clazz, U id) {
        var entityUtils = new EntityUtils<>(DummyEntityA.class, entityManager);
        var controller = new ResourceApiController<DummyEntityA, U>(DummyEntityA.class, entityManager, transactionManager, entityUtils);
        assertThatExceptionOfType(TypeMismatchException.class).isThrownBy(() -> controller.get(TENANT_ID, id));
    }


    @Test
    void get_requestTenantIdMatchesResourceTenantId_noExceptionThrown() {

        var controller = getResourceApiController(DummyEntityA.class);
        assertThatNoException().isThrownBy(() -> controller.get(TENANT_ID, DUMMY_A_ID_2));
    }

    @Test
    void get_requestTenantIdDoesNotMatchResourceTenantId_resourceNotFoundExceptionThrown() {

        var controller = getResourceApiController(DummyEntityA.class);
        assertThatExceptionOfType(ResourceNotFoundException.class).isThrownBy(() -> controller.get(INVALID_TENANT_ID, DUMMY_A_ID_2));
    }

    // endregion

    // region create

    @Test
    @Transactional
    void create_resourceIsValid_resourceIsPersisted() throws JsonProcessingException {

        var controller = getResourceApiController(DummyEntityA.class);
        var apiResponse = controller.create(TENANT_ID, "{}");

        assertThat(apiResponse.getItems()).hasSize(1);
        var dummy = apiResponse.getItems().get(0);
        assertThat(dummy).isNotNull();
        assertThat(dummy.getId()).isNotNull();

        var getResponse = controller.get(TENANT_ID, dummy.getId());
        assertThat(getResponse.getItems().get(0)).isEqualTo(dummy);
    }


    @Test
    void create_emptyPayload_jsonExceptionThrown() {
        var controller = getResourceApiController(DummyEntityA.class);
        assertThatExceptionOfType(JsonProcessingException.class).isThrownBy(() -> controller.create(TENANT_ID, ""));
    }

    @Test
    void create_invalidPayload_persistenceExceptionThrown() {
        var controller = getResourceApiController(DummyEntityF.class);
        assertThatExceptionOfType(PersistenceException.class).isThrownBy(() -> controller.create(TENANT_ID, "{}"));
    }


    @Test
    void create_unrecognizedPropertyOnPayload_unknownResourcePropertyExceptionThrown() {
        var controller = getResourceApiController(DummyEntityA.class);
        assertThatExceptionOfType(UnknownResourcePropertyException.class).isThrownBy(() ->
                controller.create(TENANT_ID, "{\"otherUnknownProperty\": 1}"));
    }

    @Test
    void create_payloadViolatesEntityConstraints_resourceConstraintViolationExceptionThrown() {
        var controller = getResourceApiController(DummyEntityD.class);
        assertThatExceptionOfType(ResourceConstraintViolationException.class).isThrownBy(() -> controller.create(TENANT_ID, "{}"))
                .withMessageContainingAll("description", "telephone", "has the following error(s):");
    }

    @Test
    @Transactional
    void create_requestTenantIdMatchesPayloadTenantId_resourceIsCreatedWithTenantId() throws JsonProcessingException {
        String payload = "{\n" +
                "            \"" + ID_FIELD_NAME + "\": \"" + NON_EXISTENT_ID + "\",\n" +
                "            \"" + TENANT_ID_FIELD_NAME + "\": \"" + TENANT_ID + "\"" +
                "        }";

        var controller = getResourceApiController(DummyEntityA.class);
        var apiResponse = controller.create(TENANT_ID, payload);

        assertThat(apiResponse.getItems()).hasSize(1);
        assertThat(apiResponse.getItems().get(0).getTenantId()).isEqualTo(TENANT_ID);
    }

    @Test
    @Transactional
    void create_requestTenantIdDoesNotMatchPayloadTenantId_tenantIdMismatchExceptionThrown() {
        String payload = "{\n" +
                "            \"" + ID_FIELD_NAME + "\": \"" + NON_EXISTENT_ID + "\",\n" +
                "            \"" + TENANT_ID_FIELD_NAME + "\": \"" + TENANT_ID + "\"" +
                "        }";

        var controller = getResourceApiController(DummyEntityA.class);
        assertThatExceptionOfType(TenantIdMismatchException.class).isThrownBy(() -> controller.create(INVALID_TENANT_ID, payload));
    }

    @Test
    @Transactional
    void create_requestTenantIdIsPresent_payloadTenantIdIsNotPresent_tenantIdIsSavedWithResource() throws JsonProcessingException {
        String payload = "{\n" +
                "            \"" + ID_FIELD_NAME + "\": \"" + NON_EXISTENT_ID + "\"\n" +
                "        }";

        var controller = getResourceApiController(DummyEntityA.class);
        var apiResponse = controller.create(TENANT_ID, payload);

        assertThat(apiResponse.getItems()).hasSize(1);
        assertThat(apiResponse.getItems().get(0).getTenantId()).isEqualTo(TENANT_ID);
    }

    // endregion

    // region update

    @Test
    @Transactional
    void update_resourceExists_persistsChanges() throws JsonProcessingException {

        String payload = "{" +
                "            \"" + ID_FIELD_NAME + "\": \""+ DUMMY_C_ID_1 +"\"," +
                "            \"" + DESCRIPTION_FIELD_NAME + "\": \"Dummy Entity C\"," +
                "            \"" + INDEX_FIELD_NAME + "\": 1" +
                "        }";

        String updatedPayload = "{" +
                "            \"" + ID_FIELD_NAME + "\": \""+ DUMMY_C_ID_1 +"\"," +
                "            \"" + DESCRIPTION_FIELD_NAME + "\": \"Updated Dummy Entity C\"," +
                "            \"" + INDEX_FIELD_NAME + "\": 2" +
                "        }";

        var controller = getResourceApiController(DummyEntityC.class);


        assertThatNoException().isThrownBy(() -> controller.create(TENANT_ID, payload));
        var getResponse = controller.get(TENANT_ID, DUMMY_C_ID_1);
        var getResource = getResponse.getItems().get(0);
        assertThat(getResource.getDescription()).isEqualTo("Dummy Entity C");

        var updateResponse = controller.update(TENANT_ID, DUMMY_C_ID_1, updatedPayload);

        var dummy = updateResponse.getItems().get(0);

        assertThat(updateResponse.getItems()).hasSize(1);
        assertThat(dummy).isNotNull();
        assertThat(dummy.getId()).isEqualTo(DUMMY_C_ID_1);
        assertThat(dummy.getIndex()).isEqualTo(2);
        assertThat(dummy.getDescription()).isEqualTo("Updated Dummy Entity C");

        var checkResponse = controller.get(TENANT_ID, DUMMY_C_ID_1);
        var checkResource = checkResponse.getItems().get(0);
        assertThat(checkResource).isEqualTo(dummy);

    }

    @ParameterizedTest(name="{0}")
    @MethodSource("invalidPayloads")
    @Transactional
    void update_resourceExistsInvalidPayload_jsonExceptionThrown(String payload) {
        var controller = getResourceApiController(DummyEntityA.class);
        assertThatExceptionOfType(JsonProcessingException.class).isThrownBy(() -> controller.update(TENANT_ID, DUMMY_A_ID_1, payload));
    }

    @ParameterizedTest(name="{0}")
    @MethodSource("invalidPayloads")
    @Transactional
    void update_resourceDoesntExistInvalidPayload_jsonExceptionThrown(String payload) {

        var controller = getResourceApiController(DummyEntityA.class);

        assertThatExceptionOfType(ResourceNotFoundException.class).isThrownBy(() -> controller.get(TENANT_ID, NON_EXISTENT_ID));
        assertThatExceptionOfType(JsonProcessingException.class).isThrownBy(() -> controller.update(TENANT_ID, NON_EXISTENT_ID, payload));
    }

    @ParameterizedTest(name="{0}")
    @MethodSource("invalidProperty")
    void update_unrecognizedPropertyOnPayload_unknownResourcePropertyExceptionThrown(String payload) {
        var controller = getResourceApiController(DummyEntityA.class);
        assertThatExceptionOfType(UnknownResourcePropertyException.class).isThrownBy(() -> controller.update(TENANT_ID, NON_EXISTENT_ID, payload));
    }

    @Test
    @Transactional
    void update_resourceDoesntExist_resourceNotFoundExceptionThrown() {
        var controller = getResourceApiController(DummyEntityA.class);
        assertThatExceptionOfType(ResourceNotFoundException.class).isThrownBy(() -> controller.update(TENANT_ID, NON_EXISTENT_ID, "{}"));
    }

    @Test
    @Transactional
    void update_payloadIdDoesNotMatchUrlPathId_throwsError() {
        var controller = getResourceApiController(DummyEntityA.class);

        var payload = "{\"" + ID_FIELD_NAME + "\": \"" + DUMMY_A_ID_2 + "\" }";
        assertThatIllegalArgumentException()
                .isThrownBy(() -> controller.update(TENANT_ID, DUMMY_A_ID_1, payload))
                .withMessageContaining("payload resource id value must match the url id");
    }

    @Test
    @Transactional
    void update_payloadOmitsId_noIdMissMatchErrorThrown() {
        var controller = getResourceApiController(DummyEntityA.class);
        assertThatNoException().isThrownBy(() -> controller.update(TENANT_ID, DUMMY_A_ID_1, "{}"));
    }

    @Test
    void update_payloadViolatesEntityConstraints_resourceConstraintViolationExceptionThrown() {
        var controller = getResourceApiController(DummyEntityD.class);
        assertThatExceptionOfType(ResourceConstraintViolationException.class).isThrownBy(() -> controller.update(TENANT_ID, NON_EXISTENT_ID, "{}"))
                .withMessageContainingAll("description", "telephone", "has the following error(s):");
    }

    @Test
    @Transactional
    void update_requestTenantIdMatchesResourceTenantId_noExceptionThrown() throws JsonProcessingException {

        String payload = "{" +
                "            \"" + DESCRIPTION_FIELD_NAME + "\": \"Dummy Entity C 100\"," +
                "            \"" + INDEX_FIELD_NAME + "\": 1" +
                "        }";
        var controller = getResourceApiController(DummyEntityC.class);
        var resource = createResource(controller, payload, TENANT_ID);

        String updatedPayload = "{" +
                "            \"" + ID_FIELD_NAME + "\": \" "+ resource.getId() + "\"," +
                "            \"" + DESCRIPTION_FIELD_NAME + "\": \"Updated Dummy Entity C 100\"," +
                "            \"" + INDEX_FIELD_NAME + "\": 2" +
                "        }";

        assertThatNoException().isThrownBy(() -> controller.get(TENANT_ID, resource.getId()));
        assertThatNoException().isThrownBy(() -> controller.update(TENANT_ID, resource.getId(), updatedPayload));
    }


    @Test
    @Transactional
    void update_requestTenantIdDoesNotMatchResourceTenantId_resourceNotFoundExceptionThrown() throws JsonProcessingException {

        String payload = "{" +
                "            \"" + DESCRIPTION_FIELD_NAME + "\": \"Dummy Entity C 100\"," +
                "            \"" + INDEX_FIELD_NAME + "\": 1" +
                "        }";
        var controller = getResourceApiController(DummyEntityC.class);
        var resource = createResource(controller, payload, TENANT_ID);
        UUID id = resource.getId();

        String updatedPayload = "{" +
                "            \"" + ID_FIELD_NAME + "\": \"" + id + "\"," +
                "            \"" + DESCRIPTION_FIELD_NAME + "\": \"Updated Dummy Entity C 100\"," +
                "            \"" + INDEX_FIELD_NAME + "\": 2" +
                "        }";


        assertThatNoException().isThrownBy(() -> controller.create(TENANT_ID, payload));
        assertThatNoException().isThrownBy(() -> controller.get(TENANT_ID, id));
        assertThatExceptionOfType(ResourceNotFoundException.class).isThrownBy(() -> controller.update(INVALID_TENANT_ID, id, updatedPayload));
    }

    @Test
    @Transactional
    void update_requestTenantIdMatchesPayloadTenantId_noExceptionThrown() throws JsonProcessingException {

        String payload = "{" +
                "            \"" + TENANT_ID_FIELD_NAME + "\": \"" + TENANT_ID + "\"," +
                "            \"" + DESCRIPTION_FIELD_NAME + "\": \"Dummy Entity C 100\"," +
                "            \"" + INDEX_FIELD_NAME + "\": 1" +
                "        }";
        var controller = getResourceApiController(DummyEntityC.class);
        var resource = createResource(controller, payload, TENANT_ID);

        String updatedPayload = "{" +
                "            \"" + ID_FIELD_NAME + "\": \"" + resource.getId() + "\"," +
                "            \"" + TENANT_ID_FIELD_NAME + "\": \"" + TENANT_ID + "\"," +
                "            \"" + DESCRIPTION_FIELD_NAME + "\": \"Updated Dummy Entity C 100\"," +
                "            \"" + INDEX_FIELD_NAME + "\": 2" +
                "        }";

        assertThatNoException().isThrownBy(() -> controller.create(TENANT_ID, payload));
        assertThatNoException().isThrownBy(() -> controller.update(TENANT_ID, resource.getId(), updatedPayload));
    }

    @Test
    @Transactional
    void update_requestTenantIdDoesNotMatchPayloadTenantId_tenantIdMismatchExceptionThrown() {

        String payload = "{" +
                "            \"" + ID_FIELD_NAME + "\": \"" + NEW_RESOURCE_ID + "\"," +
                "            \"" + TENANT_ID_FIELD_NAME + "\": \"" + TENANT_ID + "\"," +
                "            \"" + DESCRIPTION_FIELD_NAME + "\": \"Dummy Entity C 100\"," +
                "            \"" + INDEX_FIELD_NAME + "\": 1" +
                "        }";

        String updatedPayload = "{" +
                "            \"" + ID_FIELD_NAME + "\": \"" + NEW_RESOURCE_ID + "\"," +
                "            \"" + TENANT_ID_FIELD_NAME + "\": \"" + TENANT_ID + "\"," +
                "            \"" + DESCRIPTION_FIELD_NAME + "\": \"Updated Dummy Entity C 100\"," +
                "            \"" + INDEX_FIELD_NAME + "\": 2" +
                "        }";

        var controller = getResourceApiController(DummyEntityC.class);

        assertThatNoException().isThrownBy(() -> controller.create(TENANT_ID, payload));
        assertThatExceptionOfType(TenantIdMismatchException.class).isThrownBy(() -> controller.update(INVALID_TENANT_ID, NEW_RESOURCE_ID, updatedPayload));
    }

    @Test
    @Transactional
    void update_requestTenantIdIsPresentAndPayloadTenantIdIsNotPresent_tenantIdIsSavedWithResource() throws JsonProcessingException {

        String payload = "{" +
                "            \"" + TENANT_ID_FIELD_NAME + "\": \"" + TENANT_ID + "\"," +
                "            \"" + DESCRIPTION_FIELD_NAME + "\": \"Dummy Entity C 100\"," +
                "            \"" + INDEX_FIELD_NAME + "\": 1" +
                "        }";
        var controller = getResourceApiController(DummyEntityC.class);
        var resource = createResource(controller, payload, TENANT_ID);

        String updatedPayload = "{" +
                "            \"" + ID_FIELD_NAME + "\": \"" + resource.getId() + "\"," +
                "            \"" + DESCRIPTION_FIELD_NAME + "\": \"Updated Dummy Entity C 100\"," +
                "            \"" + INDEX_FIELD_NAME + "\": 2" +
                "        }";


        assertThatNoException().isThrownBy(() -> controller.create(TENANT_ID, payload));
        var updateResponse = controller.update(TENANT_ID, resource.getId(), updatedPayload);

        var dummy = updateResponse.getItems().get(0);
        assertThat(updateResponse.getItems()).hasSize(1);
        assertThat(dummy).isNotNull();
        assertThat(dummy.getTenantId()).isEqualTo(TENANT_ID);
    }


    // endregion

    // region delete

    @Test
    @Transactional
    void delete_resourceExists_resourceIsDeleted() throws JsonProcessingException {
        String payload = "{" +
                "            \"" + DESCRIPTION_FIELD_NAME + "\": \"Dummy Entity C 100\"," +
                "            \"" + INDEX_FIELD_NAME + "\": 1" +
                "        }";

        var controller = getResourceApiController(DummyEntityC.class);
        var resource = createResource(controller, payload, TENANT_ID);
        UUID id = resource.getId();
        assertThatNoException().isThrownBy(() -> controller.get(TENANT_ID, id));
        assertThatNoException().isThrownBy(() -> controller.delete(TENANT_ID, id));
        assertThatExceptionOfType(ResourceNotFoundException.class).isThrownBy(() -> controller.get(TENANT_ID, id));
    }

    @Test
    @Transactional
    void delete_resourceDoesNotExist_resourceNotFoundExceptionThrown() {

        var controller = getResourceApiController(DummyEntityC.class);
        assertThatExceptionOfType(ResourceNotFoundException.class).isThrownBy(() -> controller.delete(TENANT_ID, NON_EXISTENT_ID))
                .withMessage(String.format(RESOURCE_NOT_FOUND_ERROR_FORMAT, NON_EXISTENT_ID));
    }

    @Test
    @Transactional
    void delete_requestTenantIdMatchesResourceTenantId_noExceptionThrown() throws JsonProcessingException {
        String payload = "{" +
                "            \"" + DESCRIPTION_FIELD_NAME + "\": \"Dummy Entity C 100\"," +
                "            \"" + INDEX_FIELD_NAME + "\": 1" +
                "        }";

        var controller = getResourceApiController(DummyEntityC.class);
        var resource = createResource(controller, payload, TENANT_ID);
        assertThatNoException().isThrownBy(() -> controller.get(TENANT_ID, resource.getId()));
        assertThatNoException().isThrownBy(() -> controller.delete(TENANT_ID, resource.getId()));
    }

    @Test
    @Transactional
    void delete_requestTenantIdDoesNotMatchResourceTenantId_resourceNotFoundExceptionThrown() throws JsonProcessingException {
        String payload = "{" +
                "            \"" + DESCRIPTION_FIELD_NAME + "\": \"Dummy Entity C 100\"," +
                "            \"" + INDEX_FIELD_NAME + "\": 1" +
                "        }";

        var controller = getResourceApiController(DummyEntityC.class);
        var resource = createResource(controller, payload, TENANT_ID);
        UUID id = resource.getId();

        assertThatNoException().isThrownBy(() -> controller.create(TENANT_ID, payload));
        assertThatNoException().isThrownBy(() -> controller.get(TENANT_ID, id));
        assertThatExceptionOfType(ResourceNotFoundException.class).isThrownBy(() -> controller.delete(INVALID_TENANT_ID, id));
    }

    // endregion

    // region addRelated

    @Test
    @Transactional
    @SuppressWarnings("unchecked")
    void addRelated_allResourcesExist_addsRelatedItems() {

        var controller = getResourceApiController(DummyEntityA.class);

        var getRelatedResponse = controller.getRelated(TENANT_ID, DUMMY_A_ID_10, DUMMY_B_SET_FIELD_NAME, null, Pageable.ofSize(100));
        assertThat(getRelatedResponse.getItems()).isEmpty();

        assertThatNoException()
                .isThrownBy(() -> controller.addRelated(TENANT_ID, DUMMY_A_ID_10, DUMMY_B_SET_FIELD_NAME, new Object[] {DUMMY_B_ID_2}));

        getRelatedResponse = controller.getRelated(TENANT_ID, DUMMY_A_ID_10, DUMMY_B_SET_FIELD_NAME, null, Pageable.ofSize(100));
        assertThat(getRelatedResponse.getItems()).hasSize(1);
        var resource = (DummyEntityB) getRelatedResponse.getItems().get(0);
        assertThat(resource.getId()).isEqualTo(DUMMY_B_ID_2);

    }

    @Test
    @Transactional
    void addRelated_resourceDoesntExist_resourceNotFoundExceptionThrown() {

        var controller = getResourceApiController(DummyEntityA.class);

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> controller.addRelated(TENANT_ID, NON_EXISTENT_ID, DUMMY_B_SET_FIELD_NAME, new Object[] { DUMMY_B_ID_2 }))
                .withMessage(String.format(RESOURCE_NOT_FOUND_ERROR_FORMAT, NON_EXISTENT_ID));
    }

    @Test
    @Transactional
    void addRelated_relatedResourceDoesntExist_resourceNotFoundExceptionThrown() {
        var controller = getResourceApiController(DummyEntityA.class);

        assertThatNoException().isThrownBy(() -> controller.get(TENANT_ID, DUMMY_A_ID_1));

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() ->  controller.addRelated(TENANT_ID, DUMMY_A_ID_1, DUMMY_B_SET_FIELD_NAME, new Object[] { NON_EXISTENT_ID }))
                .withMessageContaining(ResourceNotFoundExceptionMessageUtil.relatedResourcesMessage(List.of(NON_EXISTENT_ID)));
    }

    @Test
    @Transactional
    void addRelated_requestTenantIdMatchesParentAndRelatedResourcesTenantIds_noExceptionThrown() {

        var controller = getResourceApiController(DummyEntityA.class);
        assertThatNoException().isThrownBy(
                () -> controller.addRelated(TENANT_ID, DUMMY_A_ID_10, DUMMY_B_SET_FIELD_NAME, new Object[] { DUMMY_B_ID_2 }));
    }

    @Test
    @Transactional
    void addRelated_requestTenantIdDoesNotMatchParentTenantId_resourceNotFoundExceptionThrown() {

        var controller = getResourceApiController(DummyEntityA.class);
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> controller.addRelated(INVALID_TENANT_ID, DUMMY_A_ID_10, DUMMY_B_SET_FIELD_NAME, new Object[] { DUMMY_A_ID_1 }))
                .withMessageContaining(DUMMY_A_ID_10.toString());
    }

    @Test
    @Transactional
    void addRelated_requestTenantIdMatchesParentAndAllRelatedResourcesTenantIdsDoNotMatch_resourceNotFoundExceptionThrown() {

        var controller = getResourceApiController(DummyEntityA.class);
        assertThatExceptionOfType(ResourceNotFoundException.class).isThrownBy(() ->
                controller.addRelated(TENANT_ID, DUMMY_A_ID_10, DUMMY_B_SET_FIELD_NAME, new Object[] { NON_EXISTENT_ID, NON_EXISTENT_ID_2 }))
                .withMessageContainingAll("Not all related resources", NON_EXISTENT_ID.toString(), NON_EXISTENT_ID_2.toString());
    }

    @Test
    @Transactional
    void addRelated_requestTenantIdMatchesParentAndSomeRelatedResourcesTenantIdsDoNotMatch_resourceNotFoundExceptionThrown() {

        var controller = getResourceApiController(DummyEntityA.class);
        assertThatExceptionOfType(ResourceNotFoundException.class).isThrownBy(() ->
                controller.addRelated(TENANT_ID, 
                        DUMMY_A_ID_10,
                        DUMMY_B_SET_FIELD_NAME,
                        new Object[] { DUMMY_B_ID_3, NON_EXISTENT_ID, NON_EXISTENT_ID_2 }))
                .withMessageContainingAll("Not all related resources", DUMMY_B_ID_3.toString(), NON_EXISTENT_ID.toString(), NON_EXISTENT_ID_2.toString());
    }


    // endregion

    // region getRelated

    @ParameterizedTest
    @MethodSource("relatedResourceFilters")
    @SuppressWarnings("unchecked")
    void getRelated_filterExpressionProvided_returnsFilteredResources(UUID resourceId,
                                                                      SpelExpression expression,
                                                                      int expectedItems) {

        var controller = getResourceApiController(DummyEntityA.class);

        var apiResponse = controller.getRelated(TENANT_ID, resourceId, DUMMY_B_SET_FIELD_NAME, expression, Pageable.ofSize(100));

        assertThat(apiResponse).isNotNull();
        assertThat(apiResponse.getItems()).hasSize(expectedItems);

    }

    @Test
    void getRelated_requestTenantIdMatchesParentAndRelatedResourceTenantIds_noExceptionThrown() {

        var controller = getResourceApiController(DummyEntityA.class);
        assertThatNoException().isThrownBy(() ->
                controller.getRelated(TENANT_ID, DUMMY_A_ID_1, DUMMY_B_SET_FIELD_NAME, null, Pageable.ofSize(100)));
    }

    @Test
    void getRelated_requestTenantIdDoesNotMatchParentAndRelatedResourceTenantIds_noResourcesReturned() {

        var controller = getResourceApiController(DummyEntityA.class);
        var apiResponse = controller.getRelated(INVALID_TENANT_ID, DUMMY_A_ID_1, DUMMY_B_SET_FIELD_NAME, null, Pageable.ofSize(100));

        assertThat(apiResponse).isNotNull();
        assertThat(apiResponse.getItems()).isEmpty();
    }

    // endregion

    // region deleteRelated

    @Test
    @Transactional
    void deleteRelated_relationshipExists_deletesRelationshipButAllResourcesStillExist() {

        var controllerA = getResourceApiController(DummyEntityA.class);

        var getRelatedResponse = controllerA.getRelated(TENANT_ID, DUMMY_A_ID_2, DUMMY_B_SET_FIELD_NAME, null, Pageable.ofSize(100));
        @SuppressWarnings("unchecked")
        var items = (List<DummyEntityB>) getRelatedResponse.getItems();
        assertThat(items).isNotEmpty()
                .anyMatch((item) -> item.getId().equals(DUMMY_B_ID_2));


        assertThatNoException().isThrownBy(
                () -> controllerA.deleteRelated(TENANT_ID, DUMMY_A_ID_2, DUMMY_B_SET_FIELD_NAME, new Object[] { DUMMY_B_ID_2 }));


        getRelatedResponse = controllerA.getRelated(TENANT_ID, DUMMY_A_ID_2, DUMMY_B_SET_FIELD_NAME, null, Pageable.ofSize(100));
        @SuppressWarnings("unchecked")
        var checkItems = (List<DummyEntityB>) getRelatedResponse.getItems();
        assertThat(checkItems).noneMatch((item) -> item.getId().equals(DUMMY_B_ID_2));

        var controllerB = getResourceApiController(DummyEntityB.class);
        assertThatNoException().isThrownBy(() -> controllerB.get(TENANT_ID, DUMMY_B_ID_2));
    }

    @Test
    @Transactional
    void deleteRelated_resourceDoesntExist_resourceNotFoundExceptionThrown() {

        var controllerA = getResourceApiController(DummyEntityA.class);

        assertThatExceptionOfType(ResourceNotFoundException.class).isThrownBy(() -> controllerA.get(TENANT_ID, NON_EXISTENT_ID));

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() ->  controllerA.deleteRelated(TENANT_ID, NON_EXISTENT_ID, DUMMY_B_SET_FIELD_NAME, new Object[] { DUMMY_B_ID_2 }))
                .withMessage(String.format(RESOURCE_NOT_FOUND_ERROR_FORMAT, NON_EXISTENT_ID));

    }

    @Test
    @Transactional
    void deleteRelated_resourceExistsButRelationshipDoesnt_resourceNotFoundExceptionThrown() {

        var controllerA = getResourceApiController(DummyEntityA.class);

        assertThatNoException().isThrownBy(() -> controllerA.get(TENANT_ID, DUMMY_A_ID_1));


        var getRelatedResponse = controllerA.getRelated(TENANT_ID, DUMMY_A_ID_1, DUMMY_B_SET_FIELD_NAME, null, Pageable.ofSize(100));
        @SuppressWarnings("unchecked")
        var checkItems = (List<DummyEntityB>) getRelatedResponse.getItems();
        assertThat(checkItems).noneMatch((item) -> item.getId().equals(NON_EXISTENT_ID));

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> controllerA.deleteRelated(TENANT_ID, DUMMY_A_ID_1, DUMMY_B_SET_FIELD_NAME, new Object[] { NON_EXISTENT_ID }))
                .withMessageContainingAll("related resources", NON_EXISTENT_ID.toString());

    }

    @Test
    @Transactional
    void deleteRelated_resourceExists_relatedIdsResultInFoundAndNotFound_exceptionIncludesNotFoundIdsInMessage() {

        var controllerA = getResourceApiController(DummyEntityA.class);

        assertThatNoException().isThrownBy(() -> controllerA.get(TENANT_ID, DUMMY_A_ID_1));


        var getRelatedResponse = controllerA.getRelated(TENANT_ID, DUMMY_A_ID_1, DUMMY_B_SET_FIELD_NAME, null, Pageable.ofSize(100));
        @SuppressWarnings("unchecked")
        var checkItems = (List<DummyEntityB>) getRelatedResponse.getItems();

        assertThat(checkItems)
                .noneMatch((item) -> item.getId().equals(DUMMY_B_ID_3))
                .noneMatch((item) -> item.getId().equals(DUMMY_B_ID_4))
                .isNotEmpty().anyMatch((item) -> item.getId().equals(DUMMY_B_ID_2));

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> controllerA.deleteRelated(TENANT_ID, 
                        DUMMY_A_ID_1, DUMMY_B_SET_FIELD_NAME, new Object[] { DUMMY_B_ID_3, DUMMY_B_ID_4, DUMMY_B_ID_2 }))
                .withMessageContainingAll(
                        "No related",
                        DummyEntityB.class.getSimpleName(),
                        "resources removed",
                        DUMMY_B_ID_3.toString(), DUMMY_B_ID_4.toString());
    }

    @Test
    @Transactional
    void deleteRelated_requestTenantIdMatchesResourceTenantId_noExceptionThrown() {

        var controllerA = getResourceApiController(DummyEntityA.class);

        var getRelatedResponse = controllerA.getRelated(TENANT_ID, DUMMY_A_ID_2, DUMMY_B_SET_FIELD_NAME, null, Pageable.ofSize(100));
        @SuppressWarnings("unchecked")
        var items = (List<DummyEntityB>) getRelatedResponse.getItems();
        assertThat(items).isNotEmpty()
                .anyMatch((item) -> item.getId().equals(DUMMY_B_ID_2));

        assertThatNoException().isThrownBy(
                () -> controllerA.deleteRelated(TENANT_ID, DUMMY_A_ID_2, DUMMY_B_SET_FIELD_NAME, new Object[] { DUMMY_B_ID_2 }));

        getRelatedResponse = controllerA.getRelated(TENANT_ID, DUMMY_A_ID_2, DUMMY_B_SET_FIELD_NAME, null, Pageable.ofSize(100));
        items = (List<DummyEntityB>) getRelatedResponse.getItems();
        assertThat(items).noneMatch((item) -> item.getId().equals(DUMMY_B_ID_2));
    }

    @Test
    @Transactional
    void deleteRelated_requestTenantIdDoesNotMatchParentTenantId_resourceNotFoundExceptionThrown() {

        var controllerA = getResourceApiController(DummyEntityA.class);

        var getRelatedResponse = controllerA.getRelated(TENANT_ID, DUMMY_A_ID_2, DUMMY_B_SET_FIELD_NAME, null, Pageable.ofSize(100));
        @SuppressWarnings("unchecked")
        var items = (List<DummyEntityB>) getRelatedResponse.getItems();
        assertThat(items).isNotEmpty()
                .anyMatch((item) -> item.getId().equals(DUMMY_B_ID_2));

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> controllerA.deleteRelated(INVALID_TENANT_ID, DUMMY_A_ID_2, DUMMY_B_SET_FIELD_NAME, new Object[] { DUMMY_B_ID_2 }))
                .withMessageContaining(DUMMY_A_ID_2.toString());
    }

    // endregion


    // region Method Sources

    private static Stream<Arguments> filters() {
        SpelExpressionParser expressionParser = new SpelExpressionParser();

        return Stream.of(
                Arguments.of(expressionParser.parseRaw(String.format("%s=='%s'", ID_FIELD_NAME, DUMMY_A_ID_2)), 1),
                Arguments.of(expressionParser.parseRaw(String.format("%s<%s", PROFILE_ID_FIELD_NAME, 0)), 0),
                // Arguments.of(expressionParser.parseRaw("id==-1"), 0),
                Arguments.of(null, 10));
    }

    private static Stream<Arguments> relatedResourceFilters() {
        SpelExpressionParser expressionParser = new SpelExpressionParser();

        return Stream.of(
                Arguments.of(DUMMY_A_ID_1, expressionParser.parseRaw(String.format("%s=='%s'", ID_FIELD_NAME, DUMMY_B_ID_2)), 1),
                Arguments.of(DUMMY_A_ID_1, expressionParser.parseRaw(String.format("%s=='%s'", ID_FIELD_NAME, NON_EXISTENT_ID)), 0),
                Arguments.of(DUMMY_A_ID_1, null, 2));
    }

    private static Stream<Arguments> invalidIDSource() {
        return Stream.of(
                Arguments.of(Integer.class, "blah"),
                Arguments.of(Long.class, "blah"));
    }

    private static Stream<Arguments> invalidPayloads() {
        return Stream.of(
                Arguments.of(named("empty string", "")),
                Arguments.of(named("a string is not valid json", "example string")),
                Arguments.of(named("array not closed", "{ \"" + ID_FIELD_NAME + "\": [ \"string\" }")),
                Arguments.of(named("string not quoted", "{ \"" + ID_FIELD_NAME + "\": [ unquoted string ] }")),
                Arguments.of(named("back ticks not valid", "{ `id`: [ \"string\" ] }")),
                Arguments.of(named("no opening and closing braces with invalid property", "\"problem\": [ \"string\" ]"))
        );
    }


    private static Stream<Arguments> invalidProperty() {
        return Stream.of(
                Arguments.of(named("invalid property",  "{\"someProp\": \"someValue\"}")),
                Arguments.of(named("array not closed and property is invalid", "{ \"someProp\": [ \"string\" }")),
                Arguments.of(named("string not quoted and property is invalid", "{ \"someProp\": [ unquoted string ] }"))
        );
    }
    // endregion

    private <T extends BaseEntity, U> ResourceApiController<T, U> getResourceApiController(Class<T> clazz) {
        var entityUtils = new EntityUtils<>(clazz, entityManager);
        return new ResourceApiController<>(clazz, entityManager, transactionManager, entityUtils);
    }

    private <T extends BaseEntity, U> T createResource(ResourceApiController<T, U> controller,
                                                       String payload,
                                                       UUID tenantId) throws JsonProcessingException {
        
        var response = controller.create(tenantId, payload);
        assertThat(response).isNotNull();
        assertThat(response.getItems()).isNotEmpty();
        return response.getItems().get(0);
    }


}
