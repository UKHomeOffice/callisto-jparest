package uk.gov.homeoffice.digital.sas.jparest;

import org.junit.jupiter.api.Test;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityA;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.ResourceException;
import uk.gov.homeoffice.digital.sas.jparest.models.BaseEntity;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;


class BaseEntityTest {


    @Test
    void getIdField_classDoesContainIdAnnotatedField_noExceptionThrown() {

       assertDoesNotThrow(DummyEntityA::new);
    }

    @Test
    void getIdField_classDoesNotContainIdAnnotatedField_exceptionThrown() {

        assertThatExceptionOfType(ResourceException.class)
                .isThrownBy(DummyEntity::new).withMessageContaining(
                        "should not be extended by subclasses that do not have the id annotation");
    }



    static class DummyEntity extends BaseEntity {
        private Long id;
    }

}