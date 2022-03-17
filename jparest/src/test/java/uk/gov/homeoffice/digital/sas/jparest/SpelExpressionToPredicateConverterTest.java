package uk.gov.homeoffice.digital.sas.jparest;

import org.hibernate.query.criteria.internal.expression.LiteralExpression;
import org.hibernate.query.criteria.internal.predicate.ComparisonPredicate;
import org.hibernate.query.criteria.internal.predicate.CompoundPredicate;
import org.junit.jupiter.api.Assertions;
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
import org.springframework.transaction.PlatformTransactionManager;
import uk.gov.homeoffice.digital.sas.jparest.controller.ResourceApiController;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityA;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityC;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.InvalidFilterException;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ContextConfiguration(locations = "/test-context.xml")
class SpelExpressionToPredicateConverterTest {

    @PersistenceContext
    private EntityManager entityManager;

    SpelExpressionParser expressionParser = new SpelExpressionParser();

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    void convert_test_the_expression_parsing_with_null_from_data() {
        SpelExpression from = null;
        Predicate result = SpelExpressionToPredicateConverter.convert(
                from,
                null,
                null
        );
        Assertions.assertNull(result);
    }

    @Test
    void spelExpressionToPredicateConverter_constructorIsPrivate_exceptionThrownWhenAccessed() {
       var constructor = assertDoesNotThrow(() ->
               SpelExpressionToPredicateConverter.class.getDeclaredConstructor());
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);

        var actualException = assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertThat(actualException.getTargetException()).isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "id == 1",
        "id != 2",
        "id >= 2",
        "id <= 2",
        "id > 2",
        "id < 2",
        "description == \"Ricardo\"",
        "description matches \"%the%\"",
        "id == index",
        "id != index",
        "id >= index",
        "id <= index",
        "id > index",
        "id < index",
        "id != 2 && id != 3 || id != 4",
        "!(id != 2 && id != 3)",
        "in(id, 1, 2, 3, 4)",
        "between(id, 1, 4)"
    })
    void convert_when_expressionIsValid_shouldNotThrow(String expressionString){
        SpelExpression spelExpression = (SpelExpression)expressionParser.parseExpression(expressionString);
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        var resourceClass = DummyEntityC.class;
        var entityUtils = new EntityUtils<>(resourceClass, entityManager);
        CriteriaQuery<DummyEntityC> query = builder.createQuery(entityUtils.getEntityType());
        Root<DummyEntityC> root = query.from(entityUtils.getEntityType());

        assertDoesNotThrow(
             () -> SpelExpressionToPredicateConverter.convert(spelExpression, builder, root));
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
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        var resourceClass = DummyEntityC.class;
        var entityUtils = new EntityUtils<>(resourceClass, entityManager);
        CriteriaQuery<DummyEntityC> query = builder.createQuery(entityUtils.getEntityType());
        Root<DummyEntityC> root = query.from(entityUtils.getEntityType());

        var thrown = assertThrows(
                InvalidFilterException.class,
                () -> SpelExpressionToPredicateConverter.convert(spelExpression, builder, root));

        assertTrue(thrown.getMessage().startsWith(errorMessage));
        ;
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "between(id, 1, 4)",
        "Between(id, 1, 4)",
        "BETWEEN(id, 1, 4)",
        "BetWeen(id, 1, 4)",
        "in(id, 1, 4)",
        "In(id, 1, 4)",
        "IN(id, 1, 4)"
    })
    void convert_when_methodNameCaseIsDifferentCase_shouldNotThrow(String expressionString){
        SpelExpression spelExpression = (SpelExpression)expressionParser.parseExpression(expressionString);
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        var resourceClass = DummyEntityC.class;
        var entityUtils = new EntityUtils<>(resourceClass, entityManager);
        CriteriaQuery<DummyEntityC> query = builder.createQuery(entityUtils.getEntityType());
        Root<DummyEntityC> root = query.from(entityUtils.getEntityType());

        assertDoesNotThrow(
             () -> SpelExpressionToPredicateConverter.convert(spelExpression, builder, root));
    }

    @Test
    void test_convert_with_or_operation_in_filter() {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        Root<DummyEntityA> root = builder.createQuery(DummyEntityA.class).from(DummyEntityA.class);
        SpelExpression expression = expressionParser.parseRaw(String.format("id==1L or id==2L", 1L));

        Predicate predicate = SpelExpressionToPredicateConverter.convert(expression, builder, root);
        assertThat(predicate).isNotNull();
        assertThat(predicate.getOperator().name()).isEqualTo("OR");
        assertThat(predicate.getExpressions().size()).isEqualTo(2);
        assertThat(predicate.getExpressions().get(0).in(expression)).isNotNull();
        assertThat(predicate.getExpressions().get(1).in(expression)).isNotNull();
    }

    @Test
    void test_convert_with_and_operation_in_filter() {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        Root<DummyEntityA> root = builder.createQuery(DummyEntityA.class).from(DummyEntityA.class);
        SpelExpression expression = expressionParser.parseRaw(String.format("id==1L and id==2L", 1L));

        Predicate predicate = SpelExpressionToPredicateConverter.convert(expression, builder, root);
        System.out.println(1);
        assertThat(predicate).isNotNull();
        assertThat(predicate.getOperator().name()).isEqualTo("AND");
        assertThat(predicate.getExpressions().size()).isEqualTo(2);
        assertThat(predicate.getExpressions().get(0).in(expression)).isNotNull();
        assertThat(predicate.getExpressions().get(1).in(expression)).isNotNull();
    }

    @Test
    void test_convert_with_equal_operation_in_filter() {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        Root<DummyEntityA> root = builder.createQuery(DummyEntityA.class).from(DummyEntityA.class);
        SpelExpression expression = expressionParser.parseRaw(String.format("id==1L", 1L));

        ComparisonPredicate predicate = (ComparisonPredicate) SpelExpressionToPredicateConverter.convert(expression, builder, root);
        assertThat(predicate).isNotNull();
        assertThat(predicate.getComparisonOperator()).isEqualTo(ComparisonPredicate.ComparisonOperator.EQUAL);
        assertThat(predicate.getRightHandOperand()).isNotNull();
        assertThat(((LiteralExpression)predicate.getRightHandOperand()).getLiteral()).isEqualTo(1L);
    }

    @Test
    void test_convert_with_greaterThan_operation_in_filter() {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        Root<DummyEntityA> root = builder.createQuery(DummyEntityA.class).from(DummyEntityA.class);
        SpelExpression expression = expressionParser.parseRaw(String.format("id>1L", 1L));

        ComparisonPredicate predicate = (ComparisonPredicate) SpelExpressionToPredicateConverter.convert(expression, builder, root);
        assertThat(predicate).isNotNull();
        assertThat(predicate.getComparisonOperator()).isEqualTo(ComparisonPredicate.ComparisonOperator.GREATER_THAN);
        assertThat(predicate.getRightHandOperand()).isNotNull();
        assertThat(((LiteralExpression)predicate.getRightHandOperand()).getLiteral()).isEqualTo(1L);
    }

    @Test
    void test_convert_with_greaterThanOrEqual_operation_in_filter() {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        Root<DummyEntityA> root = builder.createQuery(DummyEntityA.class).from(DummyEntityA.class);
        SpelExpression expression = expressionParser.parseRaw(String.format("id>=1L", 1L));

        ComparisonPredicate predicate = (ComparisonPredicate) SpelExpressionToPredicateConverter.convert(expression, builder, root);
        assertThat(predicate).isNotNull();
        assertThat(predicate.getComparisonOperator()).isEqualTo(ComparisonPredicate.ComparisonOperator.GREATER_THAN_OR_EQUAL);
        assertThat(predicate.getRightHandOperand()).isNotNull();
        assertThat(((LiteralExpression)predicate.getRightHandOperand()).getLiteral()).isEqualTo(1L);
    }

    @Test
    void test_convert_with_lessthan_operation_in_filter() {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        Root<DummyEntityA> root = builder.createQuery(DummyEntityA.class).from(DummyEntityA.class);
        SpelExpression expression = expressionParser.parseRaw(String.format("id<1L", 1L));

        ComparisonPredicate predicate = (ComparisonPredicate) SpelExpressionToPredicateConverter.convert(expression, builder, root);
        assertThat(predicate).isNotNull();
        assertThat(predicate.getComparisonOperator()).isEqualTo(ComparisonPredicate.ComparisonOperator.LESS_THAN);
        assertThat(predicate.getRightHandOperand()).isNotNull();
        assertThat(((LiteralExpression)predicate.getRightHandOperand()).getLiteral()).isEqualTo(1L);
    }

    @Test
    void test_convert_with_lessThanOrEqual_operation_in_filter() {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        Root<DummyEntityA> root = builder.createQuery(DummyEntityA.class).from(DummyEntityA.class);
        SpelExpression expression = expressionParser.parseRaw(String.format("id<=1L", 1L));

        ComparisonPredicate predicate = (ComparisonPredicate) SpelExpressionToPredicateConverter.convert(expression, builder, root);
        assertThat(predicate).isNotNull();
        assertThat(predicate.getComparisonOperator()).isEqualTo(ComparisonPredicate.ComparisonOperator.LESS_THAN_OR_EQUAL);
        assertThat(predicate.getRightHandOperand()).isNotNull();
        assertThat(((LiteralExpression)predicate.getRightHandOperand()).getLiteral()).isEqualTo(1L);
    }

    @Test
    void test_convert_with_field_operation_in_filter() {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        Root<DummyEntityA> root = builder.createQuery(DummyEntityA.class).from(DummyEntityA.class);
        SpelExpression expression = expressionParser.parseRaw(String.format("id == dummyEntityBSet", 1L));

        ComparisonPredicate predicate = (ComparisonPredicate) SpelExpressionToPredicateConverter.convert(expression, builder, root);
        assertThat(predicate).isNotNull();
        assertThat(predicate.getComparisonOperator()).isEqualTo(ComparisonPredicate.ComparisonOperator.EQUAL);
        assertThat(predicate.getRightHandOperand()).isNotNull();
    }

    @Test
    void test_convert_with_field_or_operation_in_filter() {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        Root<DummyEntityA> root = builder.createQuery(DummyEntityA.class).from(DummyEntityA.class);
        SpelExpression expression = expressionParser.parseRaw(String.format("id == dummyEntityBSet or id ==2", 1L));

        CompoundPredicate predicate = (CompoundPredicate) SpelExpressionToPredicateConverter.convert(expression, builder, root);
        assertThat(predicate).isNotNull();
        assertThat(predicate.getOperator().name()).isEqualTo("OR");
    }

    @Test
    void test_convert_with_field_used_in_filter() {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        Root<DummyEntityA> root = builder.createQuery(DummyEntityA.class).from(DummyEntityA.class);

        SpelExpression expression = parseExpression("id<=dummyEntityBSet");
        ComparisonPredicate predicate = (ComparisonPredicate) SpelExpressionToPredicateConverter.convert(expression, builder, root);
        assertThat(predicate).isNotNull();
        assertThat(predicate.getComparisonOperator()).isEqualTo(ComparisonPredicate.ComparisonOperator.LESS_THAN_OR_EQUAL);

        predicate = (ComparisonPredicate) SpelExpressionToPredicateConverter.convert(parseExpression("id<dummyEntityBSet"), builder, root);
        assertThat(predicate).isNotNull();
        assertThat(predicate.getComparisonOperator()).isEqualTo(ComparisonPredicate.ComparisonOperator.LESS_THAN);

        predicate = (ComparisonPredicate) SpelExpressionToPredicateConverter.convert(parseExpression("id==dummyEntityBSet"), builder, root);
        assertThat(predicate).isNotNull();
        assertThat(predicate.getComparisonOperator()).isEqualTo(ComparisonPredicate.ComparisonOperator.EQUAL);

        predicate = (ComparisonPredicate) SpelExpressionToPredicateConverter.convert(parseExpression("id>=dummyEntityBSet"), builder, root);
        assertThat(predicate).isNotNull();
        assertThat(predicate.getComparisonOperator()).isEqualTo(ComparisonPredicate.ComparisonOperator.GREATER_THAN_OR_EQUAL);

        predicate = (ComparisonPredicate) SpelExpressionToPredicateConverter.convert(parseExpression("id>dummyEntityBSet"), builder, root);
        assertThat(predicate).isNotNull();
        assertThat(predicate.getComparisonOperator()).isEqualTo(ComparisonPredicate.ComparisonOperator.GREATER_THAN);
    }

    @Test
    void test_convert_throws_InvalidFilterException_with_describeError_in_filter() {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        Root<DummyEntityA> root = builder.createQuery(DummyEntityA.class).from(DummyEntityA.class);
        SpelExpression expression = expressionParser.parseRaw(String.format("1==id", 1L));

        InvalidFilterException exception = assertThrows(
                InvalidFilterException.class,
                () -> SpelExpressionToPredicateConverter.convert(expression, builder, root),
                "Left hand side must be a field"
        );
    }

    private <T, U> ResourceApiController<T, U> getResourceApiController(Class<T> clazz) {
        EntityUtils<T> entityUtils = new EntityUtils<T>(clazz, entityManager);
        return new ResourceApiController<T, U>(clazz, entityManager, transactionManager, entityUtils);
    }

    private SpelExpression parseExpression(String expression) {
        return expressionParser.parseRaw(String.format(expression, 1L));
    }
}