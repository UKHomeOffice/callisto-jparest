package uk.gov.homeoffice.digital.sas.jparest;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.homeoffice.digital.sas.demo.EntitiesApplication;
import uk.gov.homeoffice.digital.sas.demo.models.Artist;
import uk.gov.homeoffice.digital.sas.demo.models.Concert;
import uk.gov.homeoffice.digital.sas.demo.models.Profile;

import javax.persistence.EntityManager;
import java.lang.reflect.Field;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes= EntitiesApplication.class)
public class EntityUtilsTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(EntityUtilsTest.class);

    @Autowired
    private EntityManager entityManager;



    @Test
    public void entityUtils_entityTypeHasIdField_idFieldInformationStored() {

        var idFieldName = "profile_id";
        var resourceClass = Profile.class;
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

        var entityUtils = new EntityUtils<>(Concert.class, entityManager);
        assertThat(entityUtils.getRelatedResources()).contains("artists");
    }


    @Test
    public void getRelatedEntities_relatedEntitiesExist_relatedEntitiesReturned() {

        var entityUtils = new EntityUtils<>(Concert.class, entityManager);

        var concert = new Concert();
        concert.setArtists(Set.of(new Artist()));
        var actualRelatedEntities = entityUtils.getRelatedEntities(concert, "artists");
        assertThat(actualRelatedEntities).hasSize(concert.getArtists().size());
    }


    @Test
    public void getEntityReference_relatedEntityExist_relatedEntityReferenceReturned() {

        var entityUtils = new EntityUtils<>(Concert.class, entityManager);
        var actualReference = entityUtils.getEntityReference("artists", "id");
        assertThat(actualReference).isInstanceOf(Artist.class);
    }

    @Test
    public void getEntityReference_entityReferenceReturned() {

        var entityUtils = new EntityUtils<>(Profile.class, entityManager);
        var actualReference = entityUtils.getEntityReference("id");
        assertThat(actualReference).isInstanceOf(Profile.class);
    }



}
