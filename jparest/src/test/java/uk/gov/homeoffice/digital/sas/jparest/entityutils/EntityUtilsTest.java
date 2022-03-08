package uk.gov.homeoffice.digital.sas.jparest.entityutils;


import org.hibernate.metamodel.internal.MetamodelImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.homeoffice.digital.sas.jparest.EntityUtils;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityA;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityB;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityC;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import javax.persistence.EntityManager;
import java.lang.reflect.Field;

@ExtendWith(MockitoExtension.class)
public class EntityUtilsTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(EntityUtilsTest.class);

    @Mock
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

        var metaModel = new MetamodelImpl(null, null);
        //metaModel.initialize();
        //TODO: Problem here, as mocking the meta model in order to get the related entity type is very difficult and impractical.
        when(entityManager.getMetamodel()).thenReturn(metaModel);


        var entityUtils = new EntityUtils<>(DummyEntityA.class, entityManager);
        assertThat(entityUtils.getRelatedResources()).contains("dummyEntityBSet");
    }


    @Test
    public void getRelatedEntities_relatedEntitiesExist_relatedEntitiesReturned() {

        var metaModel = new MetamodelImpl(null, null);
        //metaModel.initialize();
        when(entityManager.getMetamodel()).thenReturn(metaModel);
        //TODO:  Same problem, as we cant test that related entities are returned if we cant mock them to be stored first


        var entityUtils = new EntityUtils<>(DummyEntityA.class, entityManager);
        var actualRelatedEntities = entityUtils.getRelatedEntities(new DummyEntityA(), "dummyEntityBSet");
        assertThat(actualRelatedEntities).isNotEmpty();
    }


    @Test
    public void getEntityReference_relatedEntityExist_relatedEntityReferenceReturned() {

        var metaModel = new MetamodelImpl(null, null);
        //metaModel.initialize();
        when(entityManager.getMetamodel()).thenReturn(metaModel);
        //TODO:  Same problem, as we cant test the reference of a  related entity if we cant mock them to be stored first


        var entityUtils = new EntityUtils<>(DummyEntityA.class, entityManager);
        var actualReference = entityUtils.getEntityReference("dummyEntityBSet", "id");
        assertThat(actualReference).isInstanceOf(DummyEntityB.class);
    }

    @Test
    public void getEntityReference_entityReferenceReturned() {

        var entityUtils = new EntityUtils<>(DummyEntityC.class, entityManager);
        var actualReference = entityUtils.getEntityReference("id");
        assertThat(actualReference).isInstanceOf(DummyEntityC.class);
    }



}
