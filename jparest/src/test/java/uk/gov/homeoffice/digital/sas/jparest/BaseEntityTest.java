package uk.gov.homeoffice.digital.sas.jparest;

import org.junit.jupiter.api.Test;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityA;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.ResourceException;
import uk.gov.homeoffice.digital.sas.jparest.models.BaseEntity;

import javax.persistence.Id;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;


class BaseEntityTest {

    private static final String NO_ID_ANNOTATION_ERROR_MSG =
            "should not be extended by subclasses that do not have the id annotation";
    private static final String MULTIPLE_ID_ANNOTATIONS_ERROR_MSG =
            "should not be extended by a subclass with multiple id annotations";


    @Test
    void getIdField_classContainsSingleIdAnnotatedField_noExceptionThrown() {
       assertDoesNotThrow(DummyEntityA::new);
    }

    @Test
    void getIdField_fieldsExist_noIdAnnotatedFieldExists_exceptionThrown() {

        assertThatExceptionOfType(ResourceException.class)
                .isThrownBy(BadEntityA::new).withMessageContaining(NO_ID_ANNOTATION_ERROR_MSG);
    }

    @Test
    void getIdField_noFieldsExist_exceptionThrown() {

        assertThatExceptionOfType(ResourceException.class)
                .isThrownBy(BadEntityB::new).withMessageContaining(NO_ID_ANNOTATION_ERROR_MSG);
    }

    @Test
    void getIdField_multipleIdAnnotatedFieldExists_exceptionThrown() {

        assertThatExceptionOfType(ResourceException.class)
                .isThrownBy(BadEntityC::new).withMessageContaining(MULTIPLE_ID_ANNOTATIONS_ERROR_MSG);
    }



    static class BadEntityA extends BaseEntity {
        private Long id;
    }

    static class BadEntityB extends BaseEntity {}

    static class BadEntityC extends BaseEntity {
        @Id
        private Long id;

        @Id
        private Long id2;
    }

}