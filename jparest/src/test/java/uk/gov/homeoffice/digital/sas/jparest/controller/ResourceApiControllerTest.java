package uk.gov.homeoffice.digital.sas.jparest.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
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
import org.springframework.transaction.support.TransactionSynchronizationManager;

import org.springframework.transaction.support.TransactionTemplate;
import uk.gov.homeoffice.digital.sas.jparest.EntityUtils;
import uk.gov.homeoffice.digital.sas.jparest.config.BaseEntityCheckerServiceTestConfig;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityA;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityB;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityC;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityD;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityF;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.OperationNotSupportedException;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.ResourceConstraintViolationException;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.ResourceNotFoundException;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.ResourceNotFoundExceptionMessageUtil;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.StructuredError;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.TenantIdMismatchException;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.UnknownResourcePropertyException;
import uk.gov.homeoffice.digital.sas.jparest.models.BaseEntity;
import uk.gov.homeoffice.digital.sas.jparest.repository.TenantRepositoryImpl;
import uk.gov.homeoffice.digital.sas.jparest.service.BaseEntityCheckerService;
import uk.gov.homeoffice.digital.sas.jparest.service.ResourceApiService;
import uk.gov.homeoffice.digital.sas.jparest.testutils.payload.PayloadCreator;
import uk.gov.homeoffice.digital.sas.jparest.validation.EntityValidator;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import uk.gov.homeoffice.digital.sas.jparest.web.PatchOperation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.junit.jupiter.api.Named.named;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@ContextConfiguration(locations = "/test-context.xml", classes = BaseEntityCheckerServiceTestConfig.class)
class ResourceApiControllerTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private EntityValidator entityValidator;

    @Autowired
    BaseEntityCheckerService baseEntityCheckerService;

    @Autowired
    private ObjectMapper objectMapper;

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

    public static final UUID DUMMY_F_ID_1 = UUID.fromString("4424d0e2-e8f2-40b8-a564-f23d67e6f3a1");

    private static final UUID TENANT_ID = UUID.fromString("b7e813a2-bb28-11ec-8422-0242ac120002");
    private static final UUID INVALID_TENANT_ID = UUID.fromString("7a7c7da4-bb29-11ec-1003-0242ac120004");

    private static final String ID_FIELD_NAME = "id";
    private static final String TENANT_ID_FIELD_NAME = "tenantId";
    private static final String DESCRIPTION_FIELD_NAME = "description";
    private static final String INDEX_FIELD_NAME = "index";
    private static final String PROFILE_ID_FIELD_NAME = "profileId";
    private static final String DUMMY_B_SET_FIELD_NAME = "dummyEntityBSet";

    private static final String RESOURCE_NOT_FOUND_ERROR_FORMAT = "Resource with id: %s was not found";

    @Test
    void list_withoutFilter_returnsAllEntities() {

        var controller = getResourceApiController(DummyEntityA.class);
        var response = controller.list(TENANT_ID, Pageable.ofSize(100), null);

        assertThat(response).isNotNull();
        assertThat(response.getItems()).hasSize(10);
    }

    @ParameterizedTest
    @MethodSource("filters")
    void list_withFilter_returnsFilteredEntities(SpelExpression expression, int expectedItems) {

        var controller = getResourceApiController(DummyEntityA.class);
        var response = controller.list(TENANT_ID, Pageable.ofSize(100), expression);

        assertThat(response).isNotNull();
        assertThat(response.getItems()).hasSize(expectedItems);
    }

    @Test
    void list_sorted_returnsItemsSortedInCorrectDirection() {

        var controller = getResourceApiController(DummyEntityA.class);

        var sort = Sort.by(Direction.ASC, "id");
        var pageable = PageRequest.ofSize(100).withSort(sort);

        var response = controller.list(TENANT_ID, pageable, null);
        final var items = response.getItems().toArray(new DummyEntityA[0]);

        assertThat(items).hasSizeGreaterThanOrEqualTo(2);
        IntStream.range(1, items.length).forEach(i ->
                assertThat(items[i].getId()).isGreaterThan(items[i-1].getId()));

        sort = Sort.by(Direction.DESC, "id");
        pageable = PageRequest.ofSize(100).withSort(sort);


        response = controller.list(TENANT_ID, pageable, null);
        final var items2 = response.getItems().toArray(new DummyEntityA[0]);

        assertThat(items2).hasSizeGreaterThanOrEqualTo(2);
        IntStream.range(1, items2.length).forEach(i ->
                assertThat(items2[i].getId()).isLessThan(items2[i-1].getId()));
    }

    @Test
    void list_resourcesExists_requestTenantIdMatchesResourceTenantIds_resourcesReturned() {

        var controller = getResourceApiController(DummyEntityA.class);
        var response = controller.list(TENANT_ID, Pageable.ofSize(100), null);
        assertThat(response).isNotNull();
        assertThat(response.getItems()).isNotEmpty();

    }

    @Test
    void list_requestTenantIdDoesNotMatchResourceTenantIds_noResourcesReturned() {

        var controller = getResourceApiController(DummyEntityA.class);

        var response = controller.list(INVALID_TENANT_ID, Pageable.ofSize(100), null);
        assertThat(response).isNotNull();
        assertThat(response.getItems()).isEmpty();
    }

    @Test
    void get_resourceWithIdExists_returnsEntity() {

        var controller = getResourceApiController(DummyEntityA.class);

        var apiResponse = controller.get(TENANT_ID, DUMMY_A_ID_2);
        var dummy = apiResponse.getItems().get(0);

        assertThat(apiResponse.getItems()).hasSize(1);
        assertThat(dummy.getId()).isEqualTo(DUMMY_A_ID_2);
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
    void create_idProvidedInPayload_illegalArgumentExceptionThrown() {
        var controller = getResourceApiController(DummyEntityA.class);
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() ->
                controller.create(TENANT_ID, "{\"" + ID_FIELD_NAME + "\": \"" + DUMMY_A_ID_1 + "\"}"));
    }

    @Test
    void create_payloadViolatesEntityConstraints_resourceConstraintViolationExceptionThrown() {
        var controller = getResourceApiController(DummyEntityD.class);
        Throwable thrown = catchThrowable(() -> controller.create(TENANT_ID, "{}"));

        assertThat(thrown).isInstanceOf(ResourceConstraintViolationException.class);
        var errorResponse = ((ResourceConstraintViolationException) thrown).getErrorResponse();

        StructuredError telephoneError = null;
        StructuredError descriptionError = null;
        for (StructuredError structuredError : errorResponse) {
            if (structuredError.getField().equals("telephone")) {
                telephoneError = structuredError;
            } else {
                descriptionError = structuredError;
            }
        }

        assertThat(telephoneError.getField()).isEqualTo("telephone");
        assertThat(telephoneError.getMessage()).isEqualTo("must not be empty");

        assertThat(descriptionError.getField()).isEqualTo("description");
        assertThat(descriptionError.getMessage()).isEqualTo("must not be empty");
    }

    @Test
    @Transactional
    void create_requestTenantIdMatchesPayloadTenantId_resourceIsCreatedWithTenantId() throws JsonProcessingException {
        String payload = PayloadCreator.createPayload(TENANT_ID_FIELD_NAME, TENANT_ID);

        var controller = getResourceApiController(DummyEntityA.class);
        var apiResponse = controller.create(TENANT_ID, payload);

        assertThat(apiResponse.getItems()).hasSize(1);
        assertThat(apiResponse.getItems().get(0).getTenantId()).isEqualTo(TENANT_ID);
    }

    @Test
    @Transactional
    void create_requestTenantIdDoesNotMatchPayloadTenantId_tenantIdMismatchExceptionThrown() {
        String payload = PayloadCreator.createPayload(Map.of(
                ID_FIELD_NAME, NON_EXISTENT_ID,
                TENANT_ID_FIELD_NAME, TENANT_ID));

        var controller = getResourceApiController(DummyEntityA.class);
        assertThatExceptionOfType(TenantIdMismatchException.class).isThrownBy(() -> controller.create(INVALID_TENANT_ID, payload));
    }

    @Test
    @Transactional
    void create_requestTenantIdIsPresent_payloadTenantIdIsNotPresent_tenantIdIsSavedWithResource() throws JsonProcessingException {

        String payload = PayloadCreator.createPayload(PROFILE_ID_FIELD_NAME, 1);

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

        String payload = PayloadCreator.createPayload(Map.of(DESCRIPTION_FIELD_NAME, "Dummy Entity C",
                INDEX_FIELD_NAME, 1));

        //create new resource
        var controller = getResourceApiController(DummyEntityC.class);
        var apiResponse = controller.create(TENANT_ID, payload);
        assertThat(apiResponse.getItems()).hasSize(1);
        var createdResource = apiResponse.getItems().get(0);

        //get the newly created resource
        var getResponse = controller.get(TENANT_ID, createdResource.getId());
        var getResource = getResponse.getItems().get(0);
        assertThat(getResource.getDescription()).isEqualTo("Dummy Entity C");

        String updatedPayload = PayloadCreator.createPayload(Map.of(ID_FIELD_NAME, createdResource.getId(),
                DESCRIPTION_FIELD_NAME, "Updated Dummy Entity C",
                INDEX_FIELD_NAME, 2));

        var updateResponse = controller.update(TENANT_ID, createdResource.getId(), updatedPayload);


        assertThat(updateResponse.getItems()).hasSize(1);
        var dummy = updateResponse.getItems().get(0);
        assertThat(dummy).isNotNull();
        assertThat(dummy.getId()).isEqualTo(createdResource.getId());
        assertThat(dummy.getIndex()).isEqualTo(2);
        assertThat(dummy.getDescription()).isEqualTo("Updated Dummy Entity C");

        var checkResponse = controller.get(TENANT_ID, createdResource.getId());
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
    void update_resourceDoesntExist_noActiveTransactionFound() {
        var controller = getResourceApiController(DummyEntityA.class);

        assertThatExceptionOfType(ResourceNotFoundException.class)
                        .isThrownBy(() -> controller.update(TENANT_ID, NON_EXISTENT_ID, "{}"));

        assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isFalse();
    }

    @Test
    @Transactional
    void update_payloadIdDoesNotMatchUrlPathId_throwsError() {
        var controller = getResourceApiController(DummyEntityA.class);

        String payload = PayloadCreator.createPayload(ID_FIELD_NAME, DUMMY_A_ID_2);
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
        Throwable thrown = catchThrowable(() -> controller.update(TENANT_ID, NON_EXISTENT_ID, "{}"));

        assertThat(thrown).isInstanceOf(ResourceConstraintViolationException.class);
        var errorResponse = ((ResourceConstraintViolationException) thrown).getErrorResponse();

        StructuredError telephoneError = null;
        for (StructuredError structuredError : errorResponse) {
            if (structuredError.getField().equals("telephone")) {
                telephoneError = structuredError;
            }
        }

        assertThat(telephoneError.getField()).isEqualTo("telephone");
        assertThat(telephoneError.getMessage()).isEqualTo("must not be empty");
    }

    @Test
    @Transactional
    void update_requestTenantIdMatchesResourceTenantId_noExceptionThrown() throws JsonProcessingException {

        String payload = PayloadCreator.createPayload(Map.of(DESCRIPTION_FIELD_NAME, "Dummy Entity C 100",
                INDEX_FIELD_NAME, 1));
        var controller = getResourceApiController(DummyEntityC.class);
        var resource = createResource(controller, payload, TENANT_ID);

        String updatedPayload = PayloadCreator.createPayload(Map.of(ID_FIELD_NAME, resource.getId(),
                DESCRIPTION_FIELD_NAME, "Updated Dummy Entity C 100",
                INDEX_FIELD_NAME, 2));

        assertThatNoException().isThrownBy(() -> controller.get(TENANT_ID, resource.getId()));
        assertThatNoException().isThrownBy(() -> controller.update(TENANT_ID, resource.getId(), updatedPayload));
    }


    @Test
    @Transactional
    void update_requestTenantIdDoesNotMatchResourceTenantId_resourceNotFoundExceptionThrown() throws JsonProcessingException {

        String payload = PayloadCreator.createPayload(Map.of(DESCRIPTION_FIELD_NAME, "Dummy Entity C 100",
                INDEX_FIELD_NAME, 1));

        var controller = getResourceApiController(DummyEntityC.class);
        var resource = createResource(controller, payload, TENANT_ID);
        UUID id = resource.getId();



        String updatedPayload = PayloadCreator.createPayload(Map.of(ID_FIELD_NAME, id,
                DESCRIPTION_FIELD_NAME, "Updated Dummy Entity C 100",
                INDEX_FIELD_NAME, 2));


        assertThatNoException().isThrownBy(() -> controller.create(TENANT_ID, payload));
        assertThatNoException().isThrownBy(() -> controller.get(TENANT_ID, id));
        assertThatExceptionOfType(ResourceNotFoundException.class).isThrownBy(() -> controller.update(INVALID_TENANT_ID, id, updatedPayload));
    }

    @Test
    void update_requestTenantIdDoesNotMatchResourceTenantId_noActiveTransactionFound() {
        var controller = getResourceApiController(DummyEntityA.class);

        assertThatExceptionOfType(ResourceNotFoundException.class).isThrownBy(() -> controller.update(INVALID_TENANT_ID, DUMMY_A_ID_1, "{}"));

        assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isFalse();
    }

    @Test
    @Transactional
    void update_requestTenantIdMatchesPayloadTenantId_noExceptionThrown() throws JsonProcessingException {

        String payload = PayloadCreator.createPayload(Map.of(TENANT_ID_FIELD_NAME, TENANT_ID,
                DESCRIPTION_FIELD_NAME, "Dummy Entity C 100",
                INDEX_FIELD_NAME, 1));

        var controller = getResourceApiController(DummyEntityC.class);
        var resource = createResource(controller, payload, TENANT_ID);



        String updatedPayload = PayloadCreator.createPayload(Map.of(ID_FIELD_NAME, resource.getId(),
                TENANT_ID_FIELD_NAME, TENANT_ID,
                DESCRIPTION_FIELD_NAME, "Updated Dummy Entity C 100",
                INDEX_FIELD_NAME, 2));

        assertThatNoException().isThrownBy(() -> controller.create(TENANT_ID, payload));
        assertThatNoException().isThrownBy(() -> controller.update(TENANT_ID, resource.getId(), updatedPayload));
    }

    @Test
    @Transactional
    void update_requestTenantIdDoesNotMatchPayloadTenantId_tenantIdMismatchExceptionThrown() {


        String payload = PayloadCreator.createPayload(Map.of(TENANT_ID_FIELD_NAME, TENANT_ID,
                DESCRIPTION_FIELD_NAME, "Dummy Entity C 100",
                INDEX_FIELD_NAME, 1));

        String updatedPayload = PayloadCreator.createPayload(Map.of(ID_FIELD_NAME, NEW_RESOURCE_ID,
                TENANT_ID_FIELD_NAME, TENANT_ID,
                DESCRIPTION_FIELD_NAME, "Updated Dummy Entity C 100",
                INDEX_FIELD_NAME, 2));

        var controller = getResourceApiController(DummyEntityC.class);

        assertThatNoException().isThrownBy(() -> controller.create(TENANT_ID, payload));
        assertThatExceptionOfType(TenantIdMismatchException.class).isThrownBy(() -> controller.update(INVALID_TENANT_ID, NEW_RESOURCE_ID, updatedPayload));
    }

    @Test
    @Transactional
    void update_requestTenantIdIsPresentAndPayloadTenantIdIsNotPresent_tenantIdIsSavedWithResource() throws JsonProcessingException {



        String payload = PayloadCreator.createPayload(Map.of(TENANT_ID_FIELD_NAME, TENANT_ID,
                DESCRIPTION_FIELD_NAME, "Dummy Entity C 100",
                INDEX_FIELD_NAME, 1));

        var controller = getResourceApiController(DummyEntityC.class);
        var resource = createResource(controller, payload, TENANT_ID);

        String updatedPayload = PayloadCreator.createPayload(Map.of(ID_FIELD_NAME, resource.getId(),
                DESCRIPTION_FIELD_NAME, "Updated Dummy Entity C 100",
                INDEX_FIELD_NAME, 2));

        assertThatNoException().isThrownBy(() -> controller.create(TENANT_ID, payload));
        var updateResponse = controller.update(TENANT_ID, resource.getId(), updatedPayload);

        var dummy = updateResponse.getItems().get(0);
        assertThat(updateResponse.getItems()).hasSize(1);
        assertThat(dummy).isNotNull();
        assertThat(dummy.getTenantId()).isEqualTo(TENANT_ID);
    }

    @Test
    @Transactional
    void update_idExistsOnRequestPathAndDoesNotExistOnBody_verifyPayloadIsValidatedWithIdPresent() throws JsonProcessingException {

        String payload = PayloadCreator.createPayload(Map.of(TENANT_ID_FIELD_NAME, TENANT_ID,
                DESCRIPTION_FIELD_NAME, "Dummy Entity C 100",
                INDEX_FIELD_NAME, 1));

        var entityUtils = new EntityUtils<>(DummyEntityC.class, baseEntityCheckerService);
        var mockedEntityValidator = Mockito.mock(EntityValidator.class);

        var resourceApiService = new ResourceApiService<>(
                entityUtils,
            new TenantRepositoryImpl<>(DummyEntityC.class, entityManager),
                mockedEntityValidator,
                new TransactionTemplate(transactionManager));

        var controller = new ResourceApiController<>(
                DummyEntityC.class,
                resourceApiService,
                objectMapper);

        var resource = createResource(controller, payload, TENANT_ID);
        controller.update(TENANT_ID, resource.getId(), payload);

        ArgumentCaptor<DummyEntityC> payloadCaptor = ArgumentCaptor.forClass(DummyEntityC.class);
        verify(mockedEntityValidator, times(2)).validateAndThrowIfErrorsExist(payloadCaptor.capture());
        assertThat(payloadCaptor.getAllValues()).hasSize(2);
        assertThat(payloadCaptor.getAllValues().get(1).getId()).isEqualTo(resource.getId());
    }

    // endregion

    // region patch
    @Test
    @Transactional
    void patch_resourcesExist_persistsChanges() throws JsonProcessingException {

        String payloadOne = PayloadCreator.createPayload(Map.of(DESCRIPTION_FIELD_NAME, "Dummy Entity C One",
            INDEX_FIELD_NAME, 1, TENANT_ID_FIELD_NAME, TENANT_ID));
        String payloadTwo = PayloadCreator.createPayload(Map.of(DESCRIPTION_FIELD_NAME, "Dummy Entity C Two",
            INDEX_FIELD_NAME, 2, TENANT_ID_FIELD_NAME, TENANT_ID));

        //create new resources
        var controller = getResourceApiController(DummyEntityC.class);
        var apiResponseOne = controller.create(TENANT_ID, payloadOne);
        assertThat(apiResponseOne.getItems()).hasSize(1);
        var createdResource = apiResponseOne.getItems().get(0);

        var apiResponseTwo = controller.create(TENANT_ID, payloadTwo);
        assertThat(apiResponseTwo.getItems()).hasSize(1);
        var createdResource2 = apiResponseTwo.getItems().get(0);

        //get the newly created resources
        var getResponseOne = controller.get(TENANT_ID, createdResource.getId());
        var getResourceOne = getResponseOne.getItems().get(0);
        assertThat(getResourceOne.getDescription()).isEqualTo("Dummy Entity C One");

        var getResponseTwo = controller.get(TENANT_ID, createdResource2.getId());
        var getResourceTwo = getResponseTwo.getItems().get(0);
        assertThat(getResourceTwo.getDescription()).isEqualTo("Dummy Entity C Two");

        //create update payload
        var updatedResourceOne = new DummyEntityC();
        updatedResourceOne.setDescription("Updated Dummy Entity C One");
        updatedResourceOne.setIndex(2L);
        updatedResourceOne.setId(getResourceOne.getId());

        var updatedResourceTwo = new DummyEntityC();
        updatedResourceTwo.setDescription("Updated Dummy Entity C Two");
        updatedResourceTwo.setIndex(3L);
        updatedResourceTwo.setId(getResourceTwo.getId());

        Object operationOne =
            new PatchOperation<>("replace", "/" + getResourceOne.getId().toString(), updatedResourceOne);
        Object operationTwo =
            new PatchOperation<>("replace", "/" + getResourceTwo.getId().toString(), updatedResourceTwo);

        var updatedPayload = Arrays.asList(operationOne, operationTwo);

        var updateResponse = controller.patch(TENANT_ID, updatedPayload);


        assertThat(updateResponse.getItems()).hasSize(2);

        var dummyOne = updateResponse.getItems().get(0);
        assertThat(dummyOne).isNotNull();
        assertThat(dummyOne.getId()).isEqualTo(createdResource.getId());
        assertThat(dummyOne.getIndex()).isEqualTo(2);
        assertThat(dummyOne.getDescription()).isEqualTo("Updated Dummy Entity C One");

        var dummyTwo = updateResponse.getItems().get(1);
        assertThat(dummyTwo).isNotNull();
        assertThat(dummyTwo.getId()).isEqualTo(createdResource2.getId());
        assertThat(dummyTwo.getIndex()).isEqualTo(3);
        assertThat(dummyTwo.getDescription()).isEqualTo("Updated Dummy Entity C Two");

        var checkResponse = controller.get(TENANT_ID, createdResource.getId());
        var checkResource = checkResponse.getItems().get(0);
        assertThat(checkResource).isEqualTo(dummyOne);

        var checkResponse2 = controller.get(TENANT_ID, createdResource2.getId());
        var checkResource2 = checkResponse2.getItems().get(0);
        assertThat(checkResource2).isEqualTo(dummyTwo);
    }

    @Test
    @Transactional
    void patch_invalidPayloadOperation_illegalArgumentExceptionThrown() {
        var controller = getResourceApiController(DummyEntityA.class);

        Object object = new DummyEntityA();

        var payload = List.of(object);
        var id = UUID.randomUUID();

        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> controller.patch(id, payload));
    }

    @Test
    @Transactional
    void patch_oneResourceDoesntExist_resourceNotFoundExceptionThrown()
        throws JsonProcessingException {

        String createPayload = PayloadCreator.createPayload(Map.of(DESCRIPTION_FIELD_NAME, "Dummy Entity C",
            INDEX_FIELD_NAME, 1, TENANT_ID_FIELD_NAME, TENANT_ID));

        //create new resource
        var controller = getResourceApiController(DummyEntityC.class);
        var apiResponse = controller.create(TENANT_ID, createPayload);
        assertThat(apiResponse.getItems()).hasSize(1);
        var createdResource = apiResponse.getItems().get(0);

        //get the newly created resource
        var getResponse = controller.get(TENANT_ID, createdResource.getId());
        var getResource = getResponse.getItems().get(0);
        assertThat(getResource.getDescription()).isEqualTo("Dummy Entity C");

        //create update payload
        var updatedResourceOne = new DummyEntityC();
        updatedResourceOne.setDescription("Updated Dummy Entity C One");
        updatedResourceOne.setIndex(2L);
        updatedResourceOne.setId(createdResource.getId());

        var notCreatedResource = new DummyEntityC();
        notCreatedResource.setId(NON_EXISTENT_ID);
        notCreatedResource.setDescription("Updated Dummy Entity C Two");
        notCreatedResource.setIndex(3L);

        Object operationOne =
            new PatchOperation<>("replace", "/" + createdResource.getId().toString(), updatedResourceOne);
        Object operationTwo =
            new PatchOperation<>("replace", "/" + notCreatedResource.getId().toString(), notCreatedResource);

        var updatedPayload = Arrays.asList(operationOne, operationTwo);

        assertThatExceptionOfType(ResourceNotFoundException.class).isThrownBy(() -> controller.patch(TENANT_ID, updatedPayload));
    }

    @Test
    @Transactional
    void patch_unsupportedOperation_operationNotSupportedExceptionThrown()
        throws JsonProcessingException {

        String createPayload = PayloadCreator.createPayload(Map.of(DESCRIPTION_FIELD_NAME, "Dummy Entity C",
            INDEX_FIELD_NAME, 1, TENANT_ID_FIELD_NAME, TENANT_ID));

        //create new resource
        var controller = getResourceApiController(DummyEntityC.class);
        var apiResponse = controller.create(TENANT_ID, createPayload);
        assertThat(apiResponse.getItems()).hasSize(1);
        var createdResource = apiResponse.getItems().get(0);

        //get the newly created resource
        var getResponse = controller.get(TENANT_ID, createdResource.getId());
        var getResource = getResponse.getItems().get(0);
        assertThat(getResource.getDescription()).isEqualTo("Dummy Entity C");

        //create update payload
        Object operationOne =
            new PatchOperation<>("foo", "/" + createdResource.getId().toString(), createdResource);

        var updatedPayload = List.of(operationOne);

        assertThatExceptionOfType(OperationNotSupportedException.class).isThrownBy(() -> controller.patch(TENANT_ID, updatedPayload));
    }

    @Test
    @Transactional
    void patch_pathIdDoesNotMatchValueId_illegalArgumentExceptionThrown()
        throws JsonProcessingException {

        String createPayload = PayloadCreator.createPayload(Map.of(DESCRIPTION_FIELD_NAME, "Dummy Entity C",
            INDEX_FIELD_NAME, 1, TENANT_ID_FIELD_NAME, TENANT_ID));

        //create new resource
        var controller = getResourceApiController(DummyEntityC.class);
        var apiResponse = controller.create(TENANT_ID, createPayload);
        assertThat(apiResponse.getItems()).hasSize(1);
        var createdResource = apiResponse.getItems().get(0);

        //get the newly created resource
        var getResponse = controller.get(TENANT_ID, createdResource.getId());
        var getResource = getResponse.getItems().get(0);
        assertThat(getResource.getDescription()).isEqualTo("Dummy Entity C");

        //create update payload
        Object operationOne =
            new PatchOperation<>("replace", "/" + NON_EXISTENT_ID, createdResource);

        var updatedPayload = List.of(operationOne);

        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> controller.patch(TENANT_ID, updatedPayload));
    }

    @Test
    @Transactional
    void patch_valueIdNotPresent_changePersisted()
        throws JsonProcessingException {

        String createPayload = PayloadCreator.createPayload(Map.of(DESCRIPTION_FIELD_NAME, "Dummy Entity C",
            INDEX_FIELD_NAME, 1, TENANT_ID_FIELD_NAME, TENANT_ID));

        //create new resource
        var controller = getResourceApiController(DummyEntityC.class);
        var apiResponse = controller.create(TENANT_ID, createPayload);
        assertThat(apiResponse.getItems()).hasSize(1);
        var createdResource = apiResponse.getItems().get(0);

        //get the newly created resource
        var getResponse = controller.get(TENANT_ID, createdResource.getId());
        var getResource = getResponse.getItems().get(0);
        assertThat(getResource.getDescription()).isEqualTo("Dummy Entity C");

        //create update payload
        var updatedResource = new DummyEntityC();
        updatedResource.setDescription("Updated Dummy Entity C");
        updatedResource.setIndex(2L);
        updatedResource.setId(null);
        updatedResource.setTenantId(TENANT_ID);

        Object operationOne =
            new PatchOperation<>("replace", "/" + getResource.getId(), updatedResource);

        var updatedPayload = List.of(operationOne);

        var updateResponse = controller.patch(TENANT_ID, updatedPayload);


        assertThat(updateResponse.getItems()).hasSize(1);

        var dummyOne = updateResponse.getItems().get(0);
        assertThat(dummyOne).isNotNull();
        assertThat(dummyOne.getId()).isEqualTo(createdResource.getId());
        assertThat(dummyOne.getIndex()).isEqualTo(2);
        assertThat(dummyOne.getDescription()).isEqualTo("Updated Dummy Entity C");

        var checkResponse = controller.get(TENANT_ID, createdResource.getId());
        var checkResource = checkResponse.getItems().get(0);
        assertThat(checkResource).isEqualTo(dummyOne);
    }

    @Test
    void patch_resourceDoesntExist_noActiveTransactionFound() {
        var controller = getResourceApiController(DummyEntityC.class);

        var notCreatedResource = new DummyEntityC();
        notCreatedResource.setId(NON_EXISTENT_ID);
        notCreatedResource.setDescription("Updated Dummy Entity C Two");
        notCreatedResource.setIndex(3L);

        Object operation =
            new PatchOperation<>("replace", "/" + notCreatedResource.getId().toString(), notCreatedResource);

        var updatedPayload = List.of(operation);

        assertThatExceptionOfType(ResourceNotFoundException.class)
            .isThrownBy(() -> controller.patch(TENANT_ID, updatedPayload));

        assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isFalse();
    }

    @Test
    @Transactional
    void patch_requestTenantIdDoesNotMatchOneResourceTenantId_tenantIdMismatchExceptionThrown()
        throws JsonProcessingException {

        String payloadOne = PayloadCreator.createPayload(Map.of(DESCRIPTION_FIELD_NAME, "Dummy Entity C One",
            INDEX_FIELD_NAME, 1, TENANT_ID_FIELD_NAME, TENANT_ID));
        String payloadTwo = PayloadCreator.createPayload(Map.of(DESCRIPTION_FIELD_NAME, "Dummy Entity C Two",
            INDEX_FIELD_NAME, 2, TENANT_ID_FIELD_NAME, TENANT_ID));

        //create new resources
        var controller = getResourceApiController(DummyEntityC.class);
        var apiResponseOne = controller.create(TENANT_ID, payloadOne);
        assertThat(apiResponseOne.getItems()).hasSize(1);
        var createdResource = apiResponseOne.getItems().get(0);

        var apiResponseTwo = controller.create(TENANT_ID, payloadTwo);
        assertThat(apiResponseTwo.getItems()).hasSize(1);
        var createdResource2 = apiResponseTwo.getItems().get(0);

        //get the newly created resources
        var getResponseOne = controller.get(TENANT_ID, createdResource.getId());
        var getResourceOne = getResponseOne.getItems().get(0);
        assertThat(getResourceOne.getDescription()).isEqualTo("Dummy Entity C One");

        var getResponseTwo = controller.get(TENANT_ID, createdResource2.getId());
        var getResourceTwo = getResponseTwo.getItems().get(0);
        assertThat(getResourceTwo.getDescription()).isEqualTo("Dummy Entity C Two");

        //create update payload
        var updatedResourceOne = new DummyEntityC();
        updatedResourceOne.setDescription("Updated Dummy Entity C One");
        updatedResourceOne.setIndex(2L);
        updatedResourceOne.setId(getResourceOne.getId());
        updatedResourceOne.setTenantId(TENANT_ID);

        var updatedResourceTwo = new DummyEntityC();
        updatedResourceTwo.setDescription("Updated Dummy Entity C Two");
        updatedResourceTwo.setIndex(3L);
        updatedResourceTwo.setId(getResourceTwo.getId());
        updatedResourceTwo.setTenantId(TENANT_ID);

        Object operationOne =
            new PatchOperation<>("replace", "/" + getResourceOne.getId().toString(), updatedResourceOne);
        Object operationTwo =
            new PatchOperation<>("replace", "/" + getResourceTwo.getId().toString(), updatedResourceTwo);

        var updatedPayload = Arrays.asList(operationOne, operationTwo);

        assertThatExceptionOfType(TenantIdMismatchException.class).isThrownBy(() -> controller.patch(INVALID_TENANT_ID, updatedPayload));
    }

    @Test
    @Transactional
    void patch_payloadTenantIdDoesNotMatchOneResourceTenantId_tenantIdMismatchExceptionThrown()
        throws JsonProcessingException {

        String payloadOne = PayloadCreator.createPayload(Map.of(DESCRIPTION_FIELD_NAME, "Dummy Entity C One",
            INDEX_FIELD_NAME, 1, TENANT_ID_FIELD_NAME, TENANT_ID));
        String payloadTwo = PayloadCreator.createPayload(Map.of(DESCRIPTION_FIELD_NAME, "Dummy Entity C Two",
            INDEX_FIELD_NAME, 2, TENANT_ID_FIELD_NAME, TENANT_ID));

        //create new resources
        var controller = getResourceApiController(DummyEntityC.class);
        var apiResponseOne = controller.create(TENANT_ID, payloadOne);
        assertThat(apiResponseOne.getItems()).hasSize(1);
        var createdResource = apiResponseOne.getItems().get(0);

        var apiResponseTwo = controller.create(TENANT_ID, payloadTwo);
        assertThat(apiResponseTwo.getItems()).hasSize(1);
        var createdResource2 = apiResponseTwo.getItems().get(0);

        //get the newly created resources
        var getResponseOne = controller.get(TENANT_ID, createdResource.getId());
        var getResourceOne = getResponseOne.getItems().get(0);
        assertThat(getResourceOne.getDescription()).isEqualTo("Dummy Entity C One");

        var getResponseTwo = controller.get(TENANT_ID, createdResource2.getId());
        var getResourceTwo = getResponseTwo.getItems().get(0);
        assertThat(getResourceTwo.getDescription()).isEqualTo("Dummy Entity C Two");

        //create update payload
        var updatedResourceOne = new DummyEntityC();
        updatedResourceOne.setDescription("Updated Dummy Entity C One");
        updatedResourceOne.setIndex(2L);
        updatedResourceOne.setId(getResourceOne.getId());
        updatedResourceOne.setTenantId(INVALID_TENANT_ID);

        var updatedResourceTwo = new DummyEntityC();
        updatedResourceTwo.setDescription("Updated Dummy Entity C Two");
        updatedResourceTwo.setIndex(3L);
        updatedResourceTwo.setId(getResourceTwo.getId());

        Object operationOne =
            new PatchOperation<>("replace", "/" + getResourceOne.getId().toString(), updatedResourceOne);
        Object operationTwo =
            new PatchOperation<>("replace", "/" + getResourceTwo.getId().toString(), updatedResourceTwo);

        var updatedPayload = Arrays.asList(operationOne, operationTwo);

        assertThatExceptionOfType(TenantIdMismatchException.class).isThrownBy(() -> controller.patch(TENANT_ID, updatedPayload));
    }

    @Test
    @Transactional
    void patch_tenantIdInRequestButNotPayload_persistsChanges()
        throws JsonProcessingException {

        String payloadOne = PayloadCreator.createPayload(Map.of(DESCRIPTION_FIELD_NAME, "Dummy Entity C One",
            INDEX_FIELD_NAME, 1, TENANT_ID_FIELD_NAME, TENANT_ID));
        String payloadTwo = PayloadCreator.createPayload(Map.of(DESCRIPTION_FIELD_NAME, "Dummy Entity C Two",
            INDEX_FIELD_NAME, 2, TENANT_ID_FIELD_NAME, TENANT_ID));

        //create new resources
        var controller = getResourceApiController(DummyEntityC.class);
        var apiResponseOne = controller.create(TENANT_ID, payloadOne);
        assertThat(apiResponseOne.getItems()).hasSize(1);
        var createdResource = apiResponseOne.getItems().get(0);

        var apiResponseTwo = controller.create(TENANT_ID, payloadTwo);
        assertThat(apiResponseTwo.getItems()).hasSize(1);
        var createdResource2 = apiResponseTwo.getItems().get(0);

        //get the newly created resources
        var getResponseOne = controller.get(TENANT_ID, createdResource.getId());
        var getResourceOne = getResponseOne.getItems().get(0);
        assertThat(getResourceOne.getDescription()).isEqualTo("Dummy Entity C One");

        var getResponseTwo = controller.get(TENANT_ID, createdResource2.getId());
        var getResourceTwo = getResponseTwo.getItems().get(0);
        assertThat(getResourceTwo.getDescription()).isEqualTo("Dummy Entity C Two");

        //create update payload
        var updatedResourceOne = new DummyEntityC();
        updatedResourceOne.setDescription("Updated Dummy Entity C One");
        updatedResourceOne.setIndex(3L);
        updatedResourceOne.setId(getResourceOne.getId());

        var updatedResourceTwo = new DummyEntityC();
        updatedResourceTwo.setDescription("Updated Dummy Entity C Two");
        updatedResourceTwo.setIndex(2L);
        updatedResourceTwo.setId(getResourceTwo.getId());

        Object operationOne =
            new PatchOperation<>("replace", "/" + getResourceOne.getId().toString(), updatedResourceOne);
        Object operationTwo =
            new PatchOperation<>("replace", "/" + getResourceTwo.getId().toString(), updatedResourceTwo);

        var updatedPayload = Arrays.asList(operationOne, operationTwo);

        var updateResponse = controller.patch(TENANT_ID, updatedPayload);

        assertThat(updateResponse.getItems()).hasSize(2);

        var dummyOne = updateResponse.getItems().get(0);
        assertThat(dummyOne).isNotNull();
        assertThat(dummyOne.getId()).isEqualTo(createdResource.getId());
        assertThat(dummyOne.getIndex()).isEqualTo(3);
        assertThat(dummyOne.getDescription()).isEqualTo("Updated Dummy Entity C One");

        var dummyTwo = updateResponse.getItems().get(1);
        assertThat(dummyTwo).isNotNull();
        assertThat(dummyTwo.getId()).isEqualTo(createdResource2.getId());
        assertThat(dummyTwo.getIndex()).isEqualTo(2);
        assertThat(dummyTwo.getDescription()).isEqualTo("Updated Dummy Entity C Two");

        var checkResponse = controller.get(TENANT_ID, createdResource.getId());
        var checkResource = checkResponse.getItems().get(0);
        assertThat(checkResource).isEqualTo(dummyOne);

        var checkResponse2 = controller.get(TENANT_ID, createdResource2.getId());
        var checkResource2 = checkResponse2.getItems().get(0);
        assertThat(checkResource2).isEqualTo(dummyTwo);
    }

    @Test
    @Transactional
    void patch_fieldIsSetToNull_fieldIsSetToNullInResponse()
        throws JsonProcessingException {

        String createPayload = PayloadCreator.createPayload(Map.of(DESCRIPTION_FIELD_NAME, "Dummy Entity C",
            INDEX_FIELD_NAME, 1, TENANT_ID_FIELD_NAME, TENANT_ID));

        //create new resource
        var controller = getResourceApiController(DummyEntityC.class);
        var apiResponse = controller.create(TENANT_ID, createPayload);
        assertThat(apiResponse.getItems()).hasSize(1);
        var createdResource = apiResponse.getItems().get(0);

        //get the newly created resource
        var getResponse = controller.get(TENANT_ID, createdResource.getId());
        var getResource = getResponse.getItems().get(0);
        assertThat(getResource.getDescription()).isEqualTo("Dummy Entity C");

        //create update payload
        var updatedResource = new DummyEntityC();
        updatedResource.setDescription(null);
        updatedResource.setIndex(3L);
        updatedResource.setId(getResource.getId());

        Object operationOne =
            new PatchOperation<>("replace", "/" + getResource.getId(), updatedResource);

        var updatedPayload = List.of(operationOne);

        var updateResponse = controller.patch(TENANT_ID, updatedPayload);

        assertThat(updateResponse.getItems()).hasSize(1);

        var dummyOne = updateResponse.getItems().get(0);
        assertThat(dummyOne).isNotNull();
        assertThat(dummyOne.getId()).isEqualTo(createdResource.getId());
        assertThat(dummyOne.getIndex()).isEqualTo(3);
        assertThat(dummyOne.getDescription()).isNull();

        var checkResponse = controller.get(TENANT_ID, createdResource.getId());
        var checkResource = checkResponse.getItems().get(0);
        assertThat(checkResource).isEqualTo(dummyOne);
    }

    // endregion

    // region delete

    @Test
    @Transactional
    void delete_resourceExists_resourceIsDeleted() throws JsonProcessingException {
        String payload = PayloadCreator.createPayload(Map.of(DESCRIPTION_FIELD_NAME, "Dummy Entity C 100",
                INDEX_FIELD_NAME, 1));

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
        String payload = PayloadCreator.createPayload(Map.of(DESCRIPTION_FIELD_NAME, "Dummy Entity C 100",
                INDEX_FIELD_NAME, 1));

        var controller = getResourceApiController(DummyEntityC.class);
        var resource = createResource(controller, payload, TENANT_ID);
        assertThatNoException().isThrownBy(() -> controller.get(TENANT_ID, resource.getId()));
        assertThatNoException().isThrownBy(() -> controller.delete(TENANT_ID, resource.getId()));
    }

    @Test
    @Transactional
    void delete_requestTenantIdDoesNotMatchResourceTenantId_resourceNotFoundExceptionThrown() throws JsonProcessingException {
        String payload = PayloadCreator.createPayload(Map.of(DESCRIPTION_FIELD_NAME, "Dummy Entity C 100",
                INDEX_FIELD_NAME, 1));

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
    void addRelated_allResourcesExist_addsRelatedItems() {

        var controller = getResourceApiController(DummyEntityA.class);

        var getRelatedResponse = controller.getRelated(TENANT_ID, DUMMY_A_ID_10, DUMMY_B_SET_FIELD_NAME, Pageable.ofSize(100), null);
        assertThat(getRelatedResponse.getItems()).isEmpty();

        assertThatNoException()
                .isThrownBy(() -> controller.addRelated(TENANT_ID, DUMMY_A_ID_10, DUMMY_B_SET_FIELD_NAME, List.of(DUMMY_B_ID_2)));

        getRelatedResponse = controller.getRelated(TENANT_ID, DUMMY_A_ID_10, DUMMY_B_SET_FIELD_NAME, Pageable.ofSize(100), null);
        assertThat(getRelatedResponse.getItems()).hasSize(1);
        var resource = (DummyEntityB) getRelatedResponse.getItems().get(0);
        assertThat(resource.getId()).isEqualTo(DUMMY_B_ID_2);

    }

    @Test
    @Transactional
    void addRelated_resourceDoesntExist_resourceNotFoundExceptionThrown() {

        var controller = getResourceApiController(DummyEntityA.class);
        var relatedIds = List.of(DUMMY_B_ID_2);

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> controller.addRelated(TENANT_ID, NON_EXISTENT_ID, DUMMY_B_SET_FIELD_NAME, relatedIds))
                .withMessage(String.format(RESOURCE_NOT_FOUND_ERROR_FORMAT, NON_EXISTENT_ID));
    }

    @Test
    @Transactional
    void addRelated_relatedResourceDoesntExist_resourceNotFoundExceptionThrown() {
        var controller = getResourceApiController(DummyEntityA.class);
        var relatedIds = List.of(NON_EXISTENT_ID);

        assertThatNoException().isThrownBy(() -> controller.get(TENANT_ID, DUMMY_A_ID_1));

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() ->  controller.addRelated(TENANT_ID, DUMMY_A_ID_1, DUMMY_B_SET_FIELD_NAME, relatedIds))
                .withMessageContaining(ResourceNotFoundExceptionMessageUtil.relatedResourcesMessage(List.of(NON_EXISTENT_ID)));
    }

    @Test
    @Transactional
    void addRelated_requestTenantIdMatchesParentAndRelatedResourcesTenantIds_noExceptionThrown() {

        var controller = getResourceApiController(DummyEntityA.class);
        var relatedIds = List.of(DUMMY_B_ID_2);

        assertThatNoException().isThrownBy(
                () -> controller.addRelated(TENANT_ID, DUMMY_A_ID_10, DUMMY_B_SET_FIELD_NAME, relatedIds));
    }

    @Test
    @Transactional
    void addRelated_requestTenantIdDoesNotMatchParentTenantId_resourceNotFoundExceptionThrown() {

        var controller = getResourceApiController(DummyEntityA.class);
        var relatedIds = List.of(DUMMY_A_ID_1);

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> controller.addRelated(INVALID_TENANT_ID, DUMMY_A_ID_10, DUMMY_B_SET_FIELD_NAME, relatedIds))
                .withMessageContaining(DUMMY_A_ID_10.toString());
    }

    @Test
    @Transactional
    void addRelated_requestTenantIdMatchesParentAndAllRelatedResourcesTenantIdsDoNotMatch_resourceNotFoundExceptionThrown() {

        var controller = getResourceApiController(DummyEntityA.class);
        var relatedIds = List.of(NON_EXISTENT_ID, NON_EXISTENT_ID_2);

        assertThatExceptionOfType(ResourceNotFoundException.class).isThrownBy(() ->
                controller.addRelated(TENANT_ID, DUMMY_A_ID_10, DUMMY_B_SET_FIELD_NAME, relatedIds))
                .withMessageContainingAll("Not all related resources", NON_EXISTENT_ID.toString(), NON_EXISTENT_ID_2.toString());
    }

    @Test
    @Transactional
    void addRelated_requestTenantIdMatchesParentAndSomeRelatedResourcesTenantIdsDoNotMatch_resourceNotFoundExceptionThrown() {

        var controller = getResourceApiController(DummyEntityA.class);
        var relatedIds = List.of(DUMMY_B_ID_3, NON_EXISTENT_ID, NON_EXISTENT_ID_2);

        assertThatExceptionOfType(ResourceNotFoundException.class).isThrownBy(() ->
                controller.addRelated(TENANT_ID,
                        DUMMY_A_ID_10,
                        DUMMY_B_SET_FIELD_NAME,
                        relatedIds))
                .withMessageContainingAll("Not all related resources", DUMMY_B_ID_3.toString(), NON_EXISTENT_ID.toString(), NON_EXISTENT_ID_2.toString());
    }


    // endregion

    // region getRelated

    @ParameterizedTest
    @MethodSource("relatedResourceFilters")
    void getRelated_filterExpressionProvided_returnsFilteredResources(UUID resourceId,
                                                                      SpelExpression expression,
                                                                      int expectedItems) {

        var controller = getResourceApiController(DummyEntityA.class);

        var apiResponse = controller.getRelated(TENANT_ID, resourceId, DUMMY_B_SET_FIELD_NAME, Pageable.ofSize(100), expression);

        assertThat(apiResponse).isNotNull();
        assertThat(apiResponse.getItems()).hasSize(expectedItems);

    }

    @Test
    void getRelated_relatedResourcesExists_requestTenantIdMatchesParentTenantId_resourcesReturned() {

        var controller = getResourceApiController(DummyEntityA.class);
        var response = controller.getRelated(TENANT_ID, DUMMY_A_ID_1, DUMMY_B_SET_FIELD_NAME, Pageable.ofSize(100), null);
        assertThat(response).isNotNull();
        assertThat(response.getItems()).isNotEmpty();
    }

    @Test
    void getRelated_requestTenantIdDoesNotMatchParentTenantId_noResourcesReturned() {

        var controller = getResourceApiController(DummyEntityA.class);
        var apiResponse = controller.getRelated(INVALID_TENANT_ID, DUMMY_A_ID_1, DUMMY_B_SET_FIELD_NAME, Pageable.ofSize(100), null);
        assertThat(apiResponse).isNotNull();
        assertThat(apiResponse.getItems()).isEmpty();
    }

    // endregion

    // region deleteRelated

    @Test
    @Transactional
    void deleteRelated_relationshipExists_deletesRelationshipButAllResourcesStillExist() {

        var controllerA = getResourceApiController(DummyEntityA.class);

        var getRelatedResponse = controllerA.getRelated(TENANT_ID, DUMMY_A_ID_2, DUMMY_B_SET_FIELD_NAME, Pageable.ofSize(100), null);
        @SuppressWarnings("unchecked")
        var items = (List<DummyEntityB>) getRelatedResponse.getItems();
        assertThat(items).isNotEmpty()
                .anyMatch((item) -> item.getId().equals(DUMMY_B_ID_2));


        assertThatNoException().isThrownBy(
                () -> controllerA.deleteRelated(TENANT_ID, DUMMY_A_ID_2, DUMMY_B_SET_FIELD_NAME, List.of(DUMMY_B_ID_2 )));


        getRelatedResponse = controllerA.getRelated(TENANT_ID, DUMMY_A_ID_2, DUMMY_B_SET_FIELD_NAME, Pageable.ofSize(100), null);
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

        var relatedIds = List.of(DUMMY_B_ID_2);
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() ->  controllerA.deleteRelated(TENANT_ID, NON_EXISTENT_ID, DUMMY_B_SET_FIELD_NAME, relatedIds))
                .withMessage(String.format(RESOURCE_NOT_FOUND_ERROR_FORMAT, NON_EXISTENT_ID));

    }

    @Test
    @Transactional
    void deleteRelated_resourceExistsButRelationshipDoesnt_resourceNotFoundExceptionThrown() {

        var controllerA = getResourceApiController(DummyEntityA.class);

        assertThatNoException().isThrownBy(() -> controllerA.get(TENANT_ID, DUMMY_A_ID_1));


        var getRelatedResponse = controllerA.getRelated(TENANT_ID, DUMMY_A_ID_1, DUMMY_B_SET_FIELD_NAME, Pageable.ofSize(100), null);
        @SuppressWarnings("unchecked")
        var checkItems = (List<DummyEntityB>) getRelatedResponse.getItems();
        assertThat(checkItems).noneMatch((item) -> item.getId().equals(NON_EXISTENT_ID));

        var relatedIds = List.of(NON_EXISTENT_ID);
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> controllerA.deleteRelated(TENANT_ID, DUMMY_A_ID_1, DUMMY_B_SET_FIELD_NAME, relatedIds))
                .withMessageContainingAll("related resources", NON_EXISTENT_ID.toString());

    }

    @Test
    @Transactional
    void deleteRelated_resourceExists_relatedIdsResultInFoundAndNotFound_exceptionIncludesNotFoundIdsInMessage() {

        var controllerA = getResourceApiController(DummyEntityA.class);

        assertThatNoException().isThrownBy(() -> controllerA.get(TENANT_ID, DUMMY_A_ID_1));


        var getRelatedResponse = controllerA.getRelated(TENANT_ID, DUMMY_A_ID_1, DUMMY_B_SET_FIELD_NAME, Pageable.ofSize(100), null);
        @SuppressWarnings("unchecked")
        var checkItems = (List<DummyEntityB>) getRelatedResponse.getItems();

        assertThat(checkItems)
                .noneMatch((item) -> item.getId().equals(DUMMY_B_ID_3))
                .noneMatch((item) -> item.getId().equals(DUMMY_B_ID_4))
                .isNotEmpty().anyMatch((item) -> item.getId().equals(DUMMY_B_ID_2));

        var relatedIds = List.of(DUMMY_B_ID_3, DUMMY_B_ID_4, DUMMY_B_ID_2);
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> controllerA.deleteRelated(TENANT_ID,
                        DUMMY_A_ID_1, DUMMY_B_SET_FIELD_NAME, relatedIds))
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

        var getRelatedResponse = controllerA.getRelated(TENANT_ID, DUMMY_A_ID_2, DUMMY_B_SET_FIELD_NAME, Pageable.ofSize(100), null);
        @SuppressWarnings("unchecked")
        var items = (List<DummyEntityB>) getRelatedResponse.getItems();
        assertThat(items).isNotEmpty()
                .anyMatch((item) -> item.getId().equals(DUMMY_B_ID_2));

        assertThatNoException().isThrownBy(
                () -> controllerA.deleteRelated(TENANT_ID, DUMMY_A_ID_2, DUMMY_B_SET_FIELD_NAME, List.of(DUMMY_B_ID_2)));

        getRelatedResponse = controllerA.getRelated(TENANT_ID, DUMMY_A_ID_2, DUMMY_B_SET_FIELD_NAME, Pageable.ofSize(100), null);
        items = (List<DummyEntityB>) getRelatedResponse.getItems();
        assertThat(items).noneMatch((item) -> item.getId().equals(DUMMY_B_ID_2));
    }

    @Test
    @Transactional
    void deleteRelated_requestTenantIdDoesNotMatchParentTenantId_resourceNotFoundExceptionThrown() {

        var controllerA = getResourceApiController(DummyEntityA.class);

        var getRelatedResponse = controllerA.getRelated(TENANT_ID, DUMMY_A_ID_2, DUMMY_B_SET_FIELD_NAME, Pageable.ofSize(100), null);
        @SuppressWarnings("unchecked")
        var items = (List<DummyEntityB>) getRelatedResponse.getItems();
        assertThat(items).isNotEmpty()
                .anyMatch((item) -> item.getId().equals(DUMMY_B_ID_2));

        var relatedIds = List.of(DUMMY_B_ID_2);
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> controllerA.deleteRelated(INVALID_TENANT_ID, DUMMY_A_ID_2, DUMMY_B_SET_FIELD_NAME, relatedIds))
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

    private static Stream<Arguments> invalidBatchProperty() {
        return Stream.of(
            Arguments.of(named("invalid property",  "[{\"someProp\": \"someValue\"}]")),
            Arguments.of(named("array not closed and property is invalid", "[{ \"someProp\": [ \"string\" }]")),
            Arguments.of(named("string not quoted and property is invalid", "[{ \"someProp\": [ unquoted string ] }]"))
        );
    }

    // endregion

    private <T extends BaseEntity, U> ResourceApiController<T> getResourceApiController(Class<T> clazz) {
        var entityUtils = new EntityUtils<>(clazz, baseEntityCheckerService);

        var resourceApiService = new ResourceApiService<>(
                entityUtils,
                new TenantRepositoryImpl<T>(clazz, entityManager),
                entityValidator,
                new TransactionTemplate(transactionManager));

        return new ResourceApiController<>(clazz, resourceApiService, objectMapper);
    }

    private <T extends BaseEntity> T createResource(ResourceApiController<T> controller,
                                                    String payload,
                                                    UUID tenantId) throws JsonProcessingException {

        var response = controller.create(tenantId, payload);
        assertThat(response).isNotNull();
        assertThat(response.getItems()).isNotEmpty();
        return response.getItems().get(0);
    }


}
