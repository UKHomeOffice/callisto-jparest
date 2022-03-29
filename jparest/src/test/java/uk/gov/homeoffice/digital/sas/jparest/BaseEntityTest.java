package uk.gov.homeoffice.digital.sas.jparest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import lombok.Getter;
import lombok.Setter;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityA;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityC;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.ResourceException;
import uk.gov.homeoffice.digital.sas.jparest.models.BaseEntity;

import javax.persistence.Id;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Named.named;

import java.util.UUID;
import java.util.stream.Stream;

class BaseEntityTest {

    private static final String NO_ID_ANNOTATION_ERROR_MSG = "should not be extended by subclasses that do not have the id annotation";
    private static final String MULTIPLE_ID_ANNOTATIONS_ERROR_MSG = "should not be extended by a subclass with multiple id annotations";

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

    @Test
    void hashCode_calledOnEqualObjects_returnTheSameValue() {
        var obj1 = new DummyEntityC();
        obj1.setId(100L);
        obj1.setDescription("something");
        var obj2 = new DummyEntityC();
        obj2.setId(100L);
        obj2.setDescription("different");
        assertThat(obj1).isEqualTo(obj2);
        assertThat(obj1).hasSameHashCodeAs(obj2);
    }

    @ParameterizedTest(name="{0}")
    @MethodSource("equalObjects")
    void equals_calledOnDifferentInstancesOfSameTypeWithMatchingIdAnnotatedFields_returnsTrue(Object object1, Object object2) {
        assertThat(object1).isEqualTo(object2);
    }

    @ParameterizedTest(name="{0}")
    @MethodSource("unequalObjects")
    void equals_calledOnUnequalObjects_returnsFalse(Object object1, Object object2) {
        assertThat(object1).isNotEqualTo(object2);
    }

    static class BadEntityA extends BaseEntity {
        @SuppressWarnings("unused") 
        private Long id;
    }

    static class BadEntityB extends BaseEntity {
    }

    static class BadEntityC extends BaseEntity {
        @Id
        private Long id;

        @Id
        private Long id2;
    }

    static class GuidIdEntity extends BaseEntity {

        @Id
        @Getter
        @Setter
        private UUID identifier;

        @Getter
        @Setter
        private Long id;
    }

    static Stream<Arguments> equalObjects() {
        // Objects the the same ID annotated field
        var g1 = new GuidIdEntity();
        g1.setIdentifier(UUID.randomUUID());
        var g2 = new GuidIdEntity();
        g2.setIdentifier(g1.getIdentifier());
        
        // Objects the the same ID annotated field
        var a1 = new DummyEntityA();
        a1.setId(100L);
        var a2 = new DummyEntityA();
        a2.setId(100L);

        return Stream.of(
            Arguments.of(named("same id annotated field value", g1), g2),
            Arguments.of(named("same instance", g1), g1),
            Arguments.of(named("same instance", g2), g2),
            Arguments.of(named("same id annotated field value", a1), a2),
            Arguments.of(named("same instance", a1), a1),
            Arguments.of(named("same instance", a2), a2)
        );
    }

    static Stream<Arguments> unequalObjects() {
        // Different values in id annotated field
        var g1 = new GuidIdEntity();
        g1.setIdentifier(UUID.randomUUID());
        var g2 = new GuidIdEntity();
        g2.setIdentifier(UUID.randomUUID());

        // Same value in id field with null ID annotated field
        var g3 = new GuidIdEntity();
        g3.setId(100L);
        var g4 = new GuidIdEntity();
        g4.setId(100L);

        // Same value in id field with non null ID annotated field
        var g5 = new GuidIdEntity();
        g5.setIdentifier(UUID.randomUUID());
        g5.setId(100L);
        
        // Same value in description field but null ID annotated field
        var c1 = new DummyEntityC();
        c1.setDescription("something");
        var c2 = new DummyEntityC();
        c2.setDescription("something");

        // Same ID annotated field value but different types
        var c3 = new DummyEntityC();
        c3.setId(100L);
        var a1 = new DummyEntityA();
        a1.setId(100L);

        return Stream.of(
            Arguments.of(named("different ids", g1), g2),
            Arguments.of(named("null id annotated field", g3), g4),
            Arguments.of(named("null id annotated field != non null id annotated field", g4), g5),
            Arguments.of(named("non null id annotated field != null id annotated field", g4), g5),
            Arguments.of(named("null id annotated field", c1), c2),
            Arguments.of(named("same id annotated field but different types", c3), a1),
            Arguments.of(named("right side null object", c3), null),
            Arguments.of(named("left side null object", null), c3)
        );
    }

}