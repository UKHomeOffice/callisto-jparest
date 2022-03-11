package uk.gov.homeoffice.digital.sas.jparest.entityutils;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
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
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@Transactional
@ContextConfiguration(locations = "/test-context.xml")
public class EntityUtilsTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(EntityUtilsTest.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    public void entityUtils_entityTypeHasIdField_idFieldInformationStored() {

        var idFieldName = "id";
        var resourceClass = DummyEntityC.class;
        var entityUtils = new EntityUtils<>(resourceClass, entityManager);
        Field expectedField = null;
        try {
            expectedField = resourceClass.getDeclaredField(idFieldName);
        } catch (NoSuchFieldException e) {
            LOGGER.info("{} field not found on class {}", idFieldName, resourceClass.toString());
        }
        assertThat(entityUtils.getIdFieldName()).isEqualTo(expectedField.getName());
        assertThat(entityUtils.getIdFieldType()).isEqualTo(expectedField.getType());

    }


    @Test
    public void entityUtils_entityTypeHasRelations_relatedEntitiesStored() {

        var entityUtils = new EntityUtils<>(DummyEntityA.class, entityManager);
        assertThat(entityUtils.getRelatedResources()).contains("dummyEntityBSet");
    }

    @Test
    public void getRelatedEntities_relatedEntitiesExist_relatedEntitiesReturned() throws JsonMappingException, JsonProcessingException {
        ObjectMapper om = new ObjectMapper();
        DummyEntityA a = om.readValue("{ \"id\": 1, \"dummyEntityBSet\": [{ \"id\": 2}]}", DummyEntityA.class);
        entityManager.persist(a);
        
        var entityUtils = new EntityUtils<>(DummyEntityA.class, entityManager);
        Object findA = entityManager.getReference(DummyEntityA.class, a.getId());
        var actualRelatedEntities = entityUtils.getRelatedEntities(findA, "dummyEntityBSet");
        assertThat(actualRelatedEntities).isNotEmpty();
    }


    @Test
    public void getEntityReference_relatedEntityExist_relatedEntityReferenceReturned() {

        var entityUtils = new EntityUtils<>(DummyEntityA.class, entityManager);
        long relatedEntityReference = 123;
        var actualReference = entityUtils.getEntityReference("dummyEntityBSet", relatedEntityReference);
        assertThat(actualReference).isInstanceOf(DummyEntityB.class);
        DummyEntityB typedActual = (DummyEntityB)actualReference;
        assertThat(typedActual.getId()).isEqualTo(relatedEntityReference);
    }


    
    @ParameterizedTest
    @MethodSource("invalidReferenceValues")
    public void getEntityReference_relatedEntityExist_referenceTypeIsInvalid_throws_illegalArgumentException(Serializable reference) {

        var entityUtils = new EntityUtils<>(DummyEntityA.class, entityManager);

        assertThrows(
            IllegalArgumentException.class,
            () -> entityUtils.getEntityReference("dummyEntityBSet", reference));
    }

    @Test
    public void getEntityReference_entityReferenceReturned() {
        long expectedEntityId = 1;
        var entityUtils = new EntityUtils<DummyEntityC>(DummyEntityC.class, entityManager);
        var actualReference = entityUtils.getEntityReference(expectedEntityId);
        assertThat(actualReference).isInstanceOf(DummyEntityC.class);
        DummyEntityC typedActual = (DummyEntityC)actualReference;
        assertThat(typedActual.getId()).isEqualTo(expectedEntityId);

    }

    @ParameterizedTest
    @MethodSource("invalidReferenceValues")
    public void getEntityReference_when_referenceTypeIsInvalid_throws_illegalArgumentException(Serializable reference) {
        // int expectedEntityId = 1;
        var entityUtils = new EntityUtils<DummyEntityC>(DummyEntityC.class, entityManager);
        assertThrows(
            IllegalArgumentException.class,
            () -> entityUtils.getEntityReference(reference));

    }
    private static Stream<Arguments> invalidReferenceValues() {
        return Stream.of(
          Arguments.of(1),
          Arguments.of("123")
        );
    }

}
