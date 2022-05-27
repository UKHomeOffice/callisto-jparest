package uk.gov.homeoffice.digital.sas.jparest;

import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityA;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityC;
import uk.gov.homeoffice.digital.sas.jparest.models.BaseEntity;

import javax.persistence.Id;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.junit.jupiter.api.Named.named;

class BaseEntityTest {

    public static final UUID SAMPLE_ID = UUID.fromString("7a7c7da4-bb29-11ec-8422-0242ac120000");
    public static final UUID SAMPLE_TENANT_ID = UUID.fromString("7a7c7da4-bb29-11ec-8422-0242ac120001");

    @Test
    void getIdField_classContainsSingleIdAnnotatedField_noExceptionThrown() {
        assertThatNoException().isThrownBy(DummyEntityA::new);
    }

    @Test
    void hashCode_calledOnEqualObjects_returnTheSameValue() {
        var obj1 = new DummyEntityC();
        obj1.setId(SAMPLE_ID);
        obj1.setTenantId(SAMPLE_TENANT_ID);
        obj1.setDescription("something");

        var obj2 = new DummyEntityC();
        obj2.setId(SAMPLE_ID);
        obj2.setTenantId(SAMPLE_TENANT_ID);
        obj2.setDescription("different");

        assertThat(obj1).isEqualTo(obj2).hasSameHashCodeAs(obj2);
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
        @Getter
        @Setter
        private UUID identifier;
    }

    static Stream<Arguments> equalObjects() {
        // Objects the the same ID annotated field
        var g1 = new GuidIdEntity();
        g1.setId(UUID.randomUUID());
        g1.setTenantId(SAMPLE_TENANT_ID);

        var g2 = new GuidIdEntity();
        g2.setTenantId(SAMPLE_TENANT_ID);
        g2.setId(g1.getId());
        
        // Objects the same ID annotated field
        var a1 = new DummyEntityA();
        a1.setId(SAMPLE_ID);
        a1.setTenantId(SAMPLE_TENANT_ID);
        var a2 = new DummyEntityA();
        a2.setTenantId(SAMPLE_TENANT_ID);
        a2.setId(SAMPLE_ID);

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
        g1.setId(UUID.randomUUID());
        g1.setTenantId(SAMPLE_TENANT_ID);
        var g2 = new GuidIdEntity();
        g2.setId(UUID.randomUUID());
        g2.setTenantId(SAMPLE_TENANT_ID);

        // Same value in id field with null ID annotated field
        var g3 = new GuidIdEntity();
        g3.setIdentifier(SAMPLE_ID);
        g3.setTenantId(SAMPLE_TENANT_ID);
        var g4 = new GuidIdEntity();
        g4.setIdentifier(SAMPLE_ID);
        g4.setTenantId(SAMPLE_TENANT_ID);

        // Same value in id field with non null ID annotated field
        var g5 = new GuidIdEntity();
        g5.setIdentifier(UUID.randomUUID());
        g5.setId(SAMPLE_ID);
        g5.setTenantId(SAMPLE_TENANT_ID);
        
        // Same value in description field but null ID annotated field
        var c1 = new DummyEntityC();
        c1.setDescription("something");
        var c2 = new DummyEntityC();
        c2.setDescription("something");

        // Same ID annotated field value but different types
        var c3 = new DummyEntityC();
        c3.setId(SAMPLE_ID);
        c3.setTenantId(SAMPLE_TENANT_ID);
        var a1 = new DummyEntityA();
        a1.setId(SAMPLE_ID);
        a1.setTenantId(SAMPLE_TENANT_ID);

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