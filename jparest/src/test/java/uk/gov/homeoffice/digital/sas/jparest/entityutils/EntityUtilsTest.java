package uk.gov.homeoffice.digital.sas.jparest.entityutils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.homeoffice.digital.sas.jparest.EntityUtils;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityA;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityB;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityC;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@Transactional
@ContextConfiguration(locations = "/test-context.xml")
@TestInstance(Lifecycle.PER_CLASS)
class EntityUtilsTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(EntityUtilsTest.class);
    public static final UUID sampleId = UUID.fromString("b7e813a2-bb28-11ec-8422-0242ac110001");

    @PersistenceContext
    private EntityManager entityManager;

    //region Constructor

    @ParameterizedTest
    @MethodSource("nullConstructorArgs")
    void entityUtils_nullArgs_throwsNullPointerException(Class<?> clazz, EntityManager manager) {
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> new EntityUtils<>(clazz, manager));
    } 

    //endregion

    @Test
    void entityUtils_entityTypeHasIdField_idFieldInformationStored() {

        var idFieldName = "id";
        var resourceClass = DummyEntityC.class;
        var entityUtils = new EntityUtils<>(resourceClass, entityManager);
        Field expectedField = null;
        try {
            expectedField = resourceClass.getSuperclass().getDeclaredField(idFieldName);
        } catch (NoSuchFieldException e) {
            LOGGER.info("{} field not found on class {}", idFieldName, resourceClass.toString());
        }
        assertThat(entityUtils.getIdFieldName()).isEqualTo(expectedField.getName());
        assertThat(entityUtils.getIdFieldType()).isEqualTo(expectedField.getType());

    }


    @Test
    void entityUtils_entityTypeHasRelations_relatedEntitiesStored() {

        var entityUtils = new EntityUtils<>(DummyEntityA.class, entityManager);
        assertThat(entityUtils.getRelatedResources()).contains("dummyEntityBSet");
    }

    // region getRelatedEntities

    @Test
    void getRelatedEntities_relatedEntitiesExist_relatedEntitiesReturned() {

        var entityUtils = new EntityUtils<>(DummyEntityA.class, entityManager);
        var findA = entityManager.find(DummyEntityA.class, sampleId);
        var actualRelatedEntities = entityUtils.getRelatedEntities(findA, "dummyEntityBSet");
        assertThat(actualRelatedEntities).isNotEmpty();
    }

    @Test
    void getRelatedEntities_relationDoesntExist_throwsIllegalArgumentException() {
        var entityUtils = new EntityUtils<>(DummyEntityA.class, entityManager);
        var findA = entityManager.getReference(DummyEntityA.class, sampleId);
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> entityUtils.getRelatedEntities(findA, "invalidValue"));
    }

    @ParameterizedTest
    @MethodSource("getRelatedEntitiesNullArgs")
    void getRelatedEntities_nullArgs_throwsNullPointerException(DummyEntityA entity, String relation) {
        var entityUtils = new EntityUtils<>(DummyEntityA.class, entityManager);
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> entityUtils.getRelatedEntities(entity, relation));
    }

    //endregion

    //region getEntityReference

    @Test
    void getEntityReference_relatedEntityExist_relatedEntityReferenceReturned() {

        var entityUtils = new EntityUtils<>(DummyEntityA.class, entityManager);
        UUID relatedEntityReference = UUID.fromString("b7e813a2-bb28-11ec-8422-0242ac120001");
        var actualReference = entityUtils.getEntityReference("dummyEntityBSet", relatedEntityReference);
        assertThat(actualReference).isInstanceOf(DummyEntityB.class);
        DummyEntityB typedActual = (DummyEntityB)actualReference;
        assertThat(typedActual.getId()).isEqualTo(relatedEntityReference);
    }


    
    @ParameterizedTest
    @MethodSource("invalidReferenceValues")
    void getEntityReference_relatedEntityExist_referenceTypeIsInvalid_throws_illegalArgumentException(Serializable reference) {

        var entityUtils = new EntityUtils<>(DummyEntityA.class, entityManager);

        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> entityUtils.getEntityReference("dummyEntityBSet", reference));
    }

    @Test
    void getEntityReference_entityReferenceReturned() {
        var entityUtils = new EntityUtils<DummyEntityC>(DummyEntityC.class, entityManager);
        var actualReference = entityUtils.getEntityReference(sampleId);
        assertThat(actualReference.getId()).isEqualTo(sampleId);

    }

    @ParameterizedTest
    @MethodSource("invalidReferenceValues")
    void getEntityReference_referenceTypeIsInvalid_throwsIllegalArgumentException(Serializable reference) {
        var entityUtils = new EntityUtils<>(DummyEntityC.class, entityManager);
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> entityUtils.getEntityReference(reference));

    }

    //endregion

    //region Method sources
    
    private static Stream<Arguments> invalidReferenceValues() {
        return Stream.of(
          Arguments.of(1),
          Arguments.of("123"),
          Arguments.of("invalid")
        );
    }

    private Stream<Arguments> nullConstructorArgs() {
        return Stream.of(
            Arguments.of(null, this.entityManager),
            Arguments.of(DummyEntityC.class, null),
            Arguments.of(null, null)            
        );
    }

    private static Stream<Arguments> getRelatedEntitiesNullArgs() {
        return Stream.of(
            Arguments.of(null, "relation"),
            Arguments.of(new DummyEntityA(), null),
            Arguments.of(null, null)            
        );
    }

    //endregion

}
