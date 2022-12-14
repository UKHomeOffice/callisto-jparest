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
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityG;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityTestUtil;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;

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

    private final Predicate<Class<?>> baseEntitySubclassPredicate = DummyEntityTestUtil.getBaseEntitySubclassPredicate();


    //region Constructor

    @Test
    void entityUtils_nullArgs_throwsNullPointerException() {
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> new EntityUtils(null, baseEntitySubclassPredicate));
    } 

    @Test
    void entityUtils_entityTypeHasRelations_relatedEntitiesStored() {

        var entityUtils = new EntityUtils<>(DummyEntityA.class, baseEntitySubclassPredicate);
        assertThat(entityUtils.getRelatedResources()).contains("dummyEntityBSet");
    }


    @Test
    void entityUtils_relatedEntityTypeDoesNotExtendBaseEntity_relationIsNotStored() {

        var entityUtils = new EntityUtils<>(DummyEntityG.class, baseEntitySubclassPredicate);
        assertThat(entityUtils.getRelatedResources()).isEmpty();
    }


    @Test
    void entityUtils_oneIdField_noExceptionThrows(){
        assertThatNoException()
                .isThrownBy(() -> new EntityUtils<>(DummyEntityA.class, baseEntitySubclassPredicate));
    }

    // region getRelatedEntities

    @Test
    void getRelatedEntities_relatedEntitiesExist_relatedEntitiesReturned() {

        var entityUtils = new EntityUtils<>(DummyEntityA.class, baseEntitySubclassPredicate);
        var findA = entityManager.find(DummyEntityA.class, sampleId);
        var actualRelatedEntities = entityUtils.getRelatedEntities(findA, "dummyEntityBSet");
        assertThat(actualRelatedEntities).isNotEmpty();
    }

    @Test
    void getRelatedEntities_relationDoesntExist_throwsIllegalArgumentException() {
        var entityUtils = new EntityUtils<>(DummyEntityA.class, baseEntitySubclassPredicate);
        var findA = entityManager.getReference(DummyEntityA.class, sampleId);
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> entityUtils.getRelatedEntities(findA, "invalidValue"));
    }

    @ParameterizedTest
    @MethodSource("getRelatedEntitiesNullArgs")
    void getRelatedEntities_nullArgs_throwsNullPointerException(DummyEntityA entity, String relation) {
        var entityUtils = new EntityUtils<>(DummyEntityA.class, baseEntitySubclassPredicate);
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> entityUtils.getRelatedEntities(entity, relation));
    }

    //endregion

    //region getEntityReference

    @Test
    void getEntityReference_relatedEntityExist_relatedEntityReferenceReturned() {

        var entityUtils = new EntityUtils<>(DummyEntityA.class, baseEntitySubclassPredicate);
        UUID relatedEntityReference = UUID.fromString("b7e813a2-bb28-11ec-8422-0242ac120001");
        var actualReference = entityUtils.getEntityReference("dummyEntityBSet", relatedEntityReference);
        assertThat(actualReference).isInstanceOf(DummyEntityB.class);
        DummyEntityB typedActual = (DummyEntityB)actualReference;
        assertThat(typedActual.getId()).isEqualTo(relatedEntityReference);
    }

    //endregion

    //region Method sources
    private static Stream<Arguments> getRelatedEntitiesNullArgs() {
        return Stream.of(
            Arguments.of(null, "relation"),
            Arguments.of(new DummyEntityA(), null),
            Arguments.of(null, null)            
        );
    }

    //endregion

}
