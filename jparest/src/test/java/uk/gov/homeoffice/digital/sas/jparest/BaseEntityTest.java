//package uk.gov.homeoffice.digital.sas.jparest;
//
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.params.ParameterizedTest;
//import org.junit.jupiter.params.provider.Arguments;
//import org.junit.jupiter.params.provider.MethodSource;
//import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityA;
//import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityC;
//import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityD;
//import uk.gov.homeoffice.digital.sas.jparest.models.BaseEntity;
//
//import javax.persistence.Id;
//import java.util.UUID;
//import java.util.stream.Stream;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatNoException;
//import static org.junit.jupiter.api.Named.named;
//
//class BaseEntityTest {
//
//    public static final UUID SAMPLE_ID = UUID.fromString("7a7c7da4-bb29-11ec-8422-0242ac120000");
//    public static final UUID SAMPLE_TENANT_ID = UUID.fromString("7a7c7da4-bb29-11ec-8422-0242ac120001");
//    public static final UUID SAMPLE2_TENANT_ID = UUID.fromString("7a7c7da4-bb29-11ec-8422-0242ac120002");
//    public static final UUID UUID1 = UUID.fromString("7a7c7da4-bb29-11ec-1000-0242ac120001");
//    public static final UUID UUID2 = UUID.fromString("7a7c7da4-bb29-11ec-1001-0242ac120002");
//    public static final UUID UUID3 = UUID.fromString("7a7c7da4-bb29-11ec-1002-0242ac120003");
//
//
//    @Test
//    void getIdField_classContainsSingleIdField_noExceptionThrown() {
//        assertThatNoException().isThrownBy(DummyEntityA::new);
//    }
//
//    @ParameterizedTest(name="{0}")
//    @MethodSource("equalObjects")
//    void hashCode_calledOnEqualObjects_returnTheSameValue(Object object1, Object object2) {
//        assertThat(object1).isEqualTo(object2).hasSameHashCodeAs(object2);
//    }
//
//
//    @ParameterizedTest(name="{0}")
//    @MethodSource("equalObjects")
//    void equals_calledOnEqualObjects_returnsTrue(Object object1, Object object2) {
//        assertThat(object1).isEqualTo(object2).hasSameHashCodeAs(object2);;
//    }
//
//    @ParameterizedTest(name="{0}")
//    @MethodSource("unequalObjects")
//    void equals_calledOnUnequalObjects_returnsFalse(Object object1, Object object2) {
//        assertThat(object1).isNotEqualTo(object2);
//    }
//
//    static Stream<Arguments> equalObjects() {
//        // Objects with the same ID field
//        var d1 = new DummyEntityD();
//        d1.setId(SAMPLE_ID);
//        d1.setTenantId(SAMPLE_TENANT_ID);
//
//        var d2 = new DummyEntityD();
//        d2.setId(SAMPLE_ID);
//        d2.setTenantId(SAMPLE_TENANT_ID);
//
//        // Objects with the same ID field & different tenantId value
//        var dt2 = new DummyEntityD();
//        dt2.setId(SAMPLE_ID);
//        dt2.setTenantId(SAMPLE2_TENANT_ID);
//
//        // Objects the same ID field
//        var a1 = new DummyEntityA();
//        a1.setId(SAMPLE_ID);
//        a1.setTenantId(SAMPLE_TENANT_ID);
//        var a2 = new DummyEntityA();
//        a2.setTenantId(SAMPLE_TENANT_ID);
//        a2.setId(SAMPLE_ID);
//        var at2 = new DummyEntityA();
//        at2.setTenantId(SAMPLE2_TENANT_ID);
//        at2.setId(SAMPLE_ID);
//
//        return Stream.of(
//            Arguments.of(named("DummyEntityD same id & tenantid value", d1), d2),
//            Arguments.of(named("DummyEntityD same id & different tenantid value", d1), dt2),
//            Arguments.of(named("DummyEntityD same instance", d1), d1),
//            Arguments.of(named("DummyEntityA same id & tenantid value", a1), a2),
//            Arguments.of(named("DummyEntityA same id & different tenantid value", a1), at2),
//            Arguments.of(named("DummyEntityA same instance", a1), a1)
//        );
//    }
//
//    static Stream<Arguments> unequalObjects() {
//        // Different values in id field
//        var d1 = new DummyEntityD();
//        d1.setId(UUID1);
//        d1.setTenantId(SAMPLE_TENANT_ID);
//        var d2 = new DummyEntityD();
//        d2.setId(UUID2);
//        d2.setTenantId(SAMPLE_TENANT_ID);
//
//        // with null id field
//        var d3 = new DummyEntityD();
//        d3.setId(null);
//        d3.setTenantId(SAMPLE_TENANT_ID);
//        var d4 = new DummyEntityD();
//        d4.setId(null);
//        d4.setTenantId(SAMPLE_TENANT_ID);
//
//        // Same value in description field but null ID field
//        var c1 = new DummyEntityC();
//        c1.setDescription("something");
//        var c2 = new DummyEntityC();
//        c2.setDescription("something");
//
//        // Same id field value but different types
//        var c3 = new DummyEntityC();
//        c3.setId(SAMPLE_ID);
//        c3.setTenantId(SAMPLE_TENANT_ID);
//        var a1 = new DummyEntityA();
//        a1.setId(SAMPLE_ID);
//        a1.setTenantId(SAMPLE_TENANT_ID);
//
//        return Stream.of(
//            Arguments.of(named("different ids", d1), d2),
//            Arguments.of(named("DummyEntityD null id field", d3), d4),
//            Arguments.of(named("null id  field != non null id  field", d4), d1),
//            Arguments.of(named("non null id  field != null id  field", d1), d4),
//            Arguments.of(named("DummyEntityC null id field", c1), c2),
//            Arguments.of(named("same id field but different entity types", c3), a1),
//            Arguments.of(named("right side null object", c3), null),
//            Arguments.of(named("left side null object", null), c3)
//        );
//    }
//
//}