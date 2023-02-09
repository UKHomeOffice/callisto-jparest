package uk.gov.homeoffice.digital.sas.jparest;

import org.hibernate.query.sqm.ComparisonOperator;
import org.hibernate.query.sqm.tree.expression.ValueBindJpaCriteriaParameter;
import org.hibernate.query.sqm.tree.predicate.SqmComparisonPredicate;
import org.hibernate.query.sqm.tree.predicate.SqmJunctionPredicate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.homeoffice.digital.sas.jparest.config.BaseEntityCheckerServiceTestConfig;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityA;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityC;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityTestUtil;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.InvalidFilterException;
import uk.gov.homeoffice.digital.sas.jparest.service.BaseEntityCheckerService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;

import static org.hibernate.query.sqm.ComparisonOperator.*;
import static jakarta.persistence.criteria.Predicate.BooleanOperator.*;

@SpringBootTest
@ContextConfiguration(locations = "/test-context.xml", classes = BaseEntityCheckerServiceTestConfig.class)
class SpelExpressionToPredicateConverterTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private BaseEntityCheckerService baseEntityCheckerService;

    SpelExpressionParser expressionParser = new SpelExpressionParser();

    CriteriaBuilder builder=null;
    Root<DummyEntityA> root=null;
    private final java.util.function.Predicate<Class<?>> baseEntitySubclassPredicate = DummyEntityTestUtil.getBaseEntitySubclassPredicate();

    @BeforeEach
    public void setUpBeforeEachTestCase(){
        builder = entityManager.getCriteriaBuilder();
        root = builder.createQuery(DummyEntityA.class).from(DummyEntityA.class);
    }

    @Test
    void convert_test_the_expression_parsing_with_null_from_data() {
        SpelExpression from = null;
        Predicate result = SpelExpressionToPredicateConverter.convert(from,null,null);
        Assertions.assertNull(result);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "index == 1",
        "index != 2",
        "index >= 2",
        "index <= 2",
        "index > 2",
        "index < 2",
        "description == \"Ricardo\"",
        "description matches \"%the%\"",
        "index != 2 && index != 3 || index != 4",
        "!(index != 2 && index != 3)",
        "in(index, 1, 2, 3, 4)",
        "between(index, 1, 4)",
        "not (index == 1)",
        "dob == \"1901-05-21\"",
        "instant == \"1901-05-21T00:00:00.000+00:00\""
    })
    void convert_when_expressionIsValid_shouldNotThrow(String expressionString){
        SpelExpression spelExpression = (SpelExpression)expressionParser.parseExpression(expressionString);
        var entityUtils = new EntityUtils<>(DummyEntityC.class, baseEntityCheckerService);
        CriteriaQuery<DummyEntityC> query = builder.createQuery(entityUtils.getEntityType());
        Root<DummyEntityC> root = query.from(entityUtils.getEntityType());
        assertThatNoException().isThrownBy(() -> SpelExpressionToPredicateConverter.convert(spelExpression, builder, root) );
    }

    private static Stream<Arguments> invalidFilterValues() {
        return Stream.of(
          Arguments.of("1 == 1", "Left hand side must be a field"),
          Arguments.of("description matches index", "Operator not valid."),
          Arguments.of("index % 2", "Operator not valid"),
          Arguments.of("description == doSomething()", "Right hand side must be a literal or a field"),
          Arguments.of("index ? 1 : 3", "Unknown expression"),
          Arguments.of("In(1,2,3)", "First argument must be a field"),
          Arguments.of("DoSomething(index)", "Unrecognised method"),
          Arguments.of("DoSomething()", "Unrecognised method")
        );
    }

    @ParameterizedTest
    @MethodSource("invalidFilterValues")
    void convert_when_expressionIsInvalid_throws_invalidFilterException(String expressionString, String errorMessage){
        SpelExpression spelExpression = (SpelExpression)expressionParser.parseExpression(expressionString);
        var entityUtils = new EntityUtils<>(DummyEntityC.class, baseEntityCheckerService);
        CriteriaQuery<DummyEntityC> query = builder.createQuery(entityUtils.getEntityType());
        Root<DummyEntityC> root = query.from(entityUtils.getEntityType());

        assertThatExceptionOfType(InvalidFilterException.class)
            .isThrownBy(() -> SpelExpressionToPredicateConverter.convert(spelExpression, builder, root))
            .withMessageStartingWith(errorMessage);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "between(index, 1, 4)",
        "Between(index, 1, 4)",
        "BETWEEN(index, 1, 4)",
        "BetWeen(index, 1, 4)",
        "in(index, 1, 4)",
        "In(index, 1, 4)",
        "IN(index, 1, 4)"
    })
    void convert_when_methodNameCaseIsDifferentCase_shouldNotThrow(String expressionString){
        SpelExpression spelExpression = (SpelExpression)expressionParser.parseExpression(expressionString);
        var entityUtils = new EntityUtils<>(DummyEntityC.class, baseEntityCheckerService);
        CriteriaQuery<DummyEntityC> query = builder.createQuery(entityUtils.getEntityType());
        Root<DummyEntityC> root = query.from(entityUtils.getEntityType());
        assertThatNoException().isThrownBy( () -> SpelExpressionToPredicateConverter.convert(spelExpression, builder, root) );
    }

    @Test
    void test_convert_with_or_operation_in_filter() {
        SpelExpression expression = expressionParser.parseRaw("index==1L or index>2L");
        Predicate predicate = SpelExpressionToPredicateConverter.convert(expression, builder, root);
        assertThat(predicate.getOperator()).isEqualTo(OR);
        assertThat(predicate.getExpressions()).hasSize(2);
        assertThat(((SqmComparisonPredicate)predicate.getExpressions().get(0)).getSqmOperator()).isEqualTo(EQUAL);
        assertThat(((SqmComparisonPredicate)predicate.getExpressions().get(1)).getSqmOperator()).isEqualTo(GREATER_THAN);
    }

    @Test
    void test_convert_with_and_operation_in_filter() {
        SpelExpression expression = expressionParser.parseRaw("index==1L and index>2L");
        Predicate predicate = SpelExpressionToPredicateConverter.convert(expression, builder, root);
        assertThat(predicate.getOperator()).isEqualTo(AND);
        assertThat(predicate.getExpressions()).hasSize(2);
        assertThat(((SqmComparisonPredicate)predicate.getExpressions().get(0)).getSqmOperator()).isEqualTo(EQUAL);
        assertThat(((SqmComparisonPredicate)predicate.getExpressions().get(1)).getSqmOperator()).isEqualTo(GREATER_THAN);
    }

    @Test
    void test_convert_with_equal_operation_in_filter() {
        SpelExpression expression = expressionParser.parseRaw("index==1L");
        SqmComparisonPredicate predicate = (SqmComparisonPredicate) SpelExpressionToPredicateConverter.convert(expression, builder, root);
        assertThat(predicate.getSqmOperator()).isEqualTo(EQUAL);
        assertThat(((ValueBindJpaCriteriaParameter<Long>)predicate.getRightHandExpression()).getValue()).isEqualTo(1L);
    }

    @Test
    void test_convert_with_greaterThan_operation_in_filter() {
        SpelExpression expression = expressionParser.parseRaw("index>1L");
        SqmComparisonPredicate predicate = (SqmComparisonPredicate) SpelExpressionToPredicateConverter.convert(expression, builder, root);
        assertThat(predicate.getSqmOperator()).isEqualTo(GREATER_THAN);
        assertThat(((ValueBindJpaCriteriaParameter<Long>)predicate.getRightHandExpression()).getValue()).isEqualTo(1L);
    }

    @Test
    void test_convert_with_greaterThanOrEqual_operation_in_filter() {
        SpelExpression expression = expressionParser.parseRaw("index>=1L");
        SqmComparisonPredicate predicate = (SqmComparisonPredicate) SpelExpressionToPredicateConverter.convert(expression, builder, root);
        assertThat(predicate.getSqmOperator()).isEqualTo(GREATER_THAN_OR_EQUAL);
        assertThat(((ValueBindJpaCriteriaParameter<Long>)predicate.getRightHandExpression()).getValue()).isEqualTo(1L);
    }

    @Test
    void test_convert_with_lessthan_operation_in_filter() {
        SpelExpression expression = expressionParser.parseRaw("index<1L");
        SqmComparisonPredicate predicate = (SqmComparisonPredicate) SpelExpressionToPredicateConverter.convert(expression, builder, root);
        assertThat(predicate.getSqmOperator()).isEqualTo(LESS_THAN);
        assertThat(((ValueBindJpaCriteriaParameter<Long>)predicate.getRightHandExpression()).getValue()).isEqualTo(1L);
    }

    @Test
    void test_convert_with_lessThanOrEqual_operation_in_filter() {
        SpelExpression expression = expressionParser.parseRaw("index<=1L");
        SqmComparisonPredicate predicate = (SqmComparisonPredicate) SpelExpressionToPredicateConverter.convert(expression, builder, root);
        assertThat(predicate.getSqmOperator()).isEqualTo(LESS_THAN_OR_EQUAL);
        assertThat(((ValueBindJpaCriteriaParameter<Long>)predicate.getRightHandExpression()).getValue()).isEqualTo(1L);
    }

    @Test
    void test_convert_with_field_operation_in_filter() {
        SpelExpression expression = expressionParser.parseRaw("id == dummyEntityBSet");
        SqmComparisonPredicate predicate = (SqmComparisonPredicate) SpelExpressionToPredicateConverter.convert(expression, builder, root);
        assertThat(predicate.getSqmOperator()).isEqualTo(EQUAL);
    }

    @Test
    void test_convert_with_field_or_operation_in_filter() {
        SpelExpression expression = expressionParser.parseRaw("id == dummyEntityBSet or index>2");
        SqmJunctionPredicate predicate = (SqmJunctionPredicate)SpelExpressionToPredicateConverter.convert(expression, builder, root);
        assertThat(predicate.getOperator()).isEqualTo(OR);
        assertThat(((SqmComparisonPredicate)predicate.getExpressions().get(0)).getSqmOperator()).isEqualTo(EQUAL);
        assertThat(((SqmComparisonPredicate)predicate.getExpressions().get(1)).getSqmOperator()).isEqualTo(GREATER_THAN);
    }

    private static Stream<Arguments> fieldFilterTestData() {
        return Stream.of(
                Arguments.of("id<=dummyEntityBSet", LESS_THAN_OR_EQUAL),
                Arguments.of("id<dummyEntityBSet", LESS_THAN),
                Arguments.of("id==dummyEntityBSet", EQUAL),
                Arguments.of("id>=dummyEntityBSet", GREATER_THAN_OR_EQUAL),
                Arguments.of("id>dummyEntityBSet", GREATER_THAN)
        );
    }
    @ParameterizedTest
    @MethodSource("fieldFilterTestData")
    void test_convert_with_field_used_in_filter(String expressionString, ComparisonOperator operator){
        SpelExpression expression = parseExpression(expressionString);
        SqmComparisonPredicate predicate = (SqmComparisonPredicate) SpelExpressionToPredicateConverter.convert(expression, builder, root);
        assertThat(predicate.getSqmOperator()).isEqualTo(operator);
    }

    @Test
    void test_convert_throws_InvalidFilterException_with_describeError_in_filter() {
        SpelExpression expression = expressionParser.parseRaw("1==index");

        assertThatExceptionOfType(InvalidFilterException.class)
            .isThrownBy(() -> SpelExpressionToPredicateConverter.convert(expression, builder, root))
            .withMessage("Left hand side must be a field");
    }

    private SpelExpression parseExpression(String expression) {
        return expressionParser.parseRaw(String.format(expression, 1L));
    }
}