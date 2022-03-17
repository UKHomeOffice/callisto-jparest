package uk.gov.homeoffice.digital.sas.jparest;

import org.hibernate.query.criteria.internal.predicate.ComparisonPredicate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.expression.spel.SpelParseException;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.PlatformTransactionManager;
import uk.gov.homeoffice.digital.sas.jparest.controller.ResourceApiController;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityA;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@Transactional
@ContextConfiguration(locations = "/test-context.xml")
class SpelExpressionToPredicateConverterTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private PlatformTransactionManager transactionManager;

    SpelExpressionParser expressionParser = new SpelExpressionParser();

    @Test
    void convert_test_the_expression_parsing_with_null_from_data() {
        SpelExpression from = null;
        CriteriaBuilder builder = Mockito.mock(CriteriaBuilder.class);
        From<String, String> root =  Mockito.mock(From.class);;
        Predicate result = SpelExpressionToPredicateConverter.convert(
                from,
                builder,
                root
        );
        Assertions.assertNull(result);
    }

    @Test
    public void spelExpressionToPredicateConverter_constructorIsPrivate_exceptionThrownWhenAccessed() {
       var constructor = assertDoesNotThrow(() ->
               SpelExpressionToPredicateConverter.class.getDeclaredConstructor());
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);

        var actualException = assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertThat(actualException.getTargetException()).isInstanceOf(IllegalStateException.class);
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
        assertThat(predicate.getExpressions().get(0).in(expression));
        assertThat(predicate.getExpressions().get(1).in(expression));
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
        assertThat(predicate.getExpressions().get(0).in(expression));
        assertThat(predicate.getExpressions().get(1).in(expression));
    }

    @Test
    void test_convert_with_equal_operation_in_filter() {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        Root<DummyEntityA> root = builder.createQuery(DummyEntityA.class).from(DummyEntityA.class);
        SpelExpression expression = expressionParser.parseRaw(String.format("id==1L", 1L));

        ComparisonPredicate predicate = (ComparisonPredicate) SpelExpressionToPredicateConverter.convert(expression, builder, root);
        assertThat(predicate).isNotNull();
        assertThat(predicate.getComparisonOperator()).isEqualTo(ComparisonPredicate.ComparisonOperator.EQUAL);
        assertThat(predicate.getRightHandOperand().isNotNull());
        assertThat(predicate.getRightHandOperand().in(1L));
    }

    @Test
    void test_convert_with_greaterThan_operation_in_filter() {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        Root<DummyEntityA> root = builder.createQuery(DummyEntityA.class).from(DummyEntityA.class);
        SpelExpression expression = expressionParser.parseRaw(String.format("id>1L", 1L));

        ComparisonPredicate predicate = (ComparisonPredicate) SpelExpressionToPredicateConverter.convert(expression, builder, root);
        assertThat(predicate).isNotNull();
        assertThat(predicate.getComparisonOperator()).isEqualTo(ComparisonPredicate.ComparisonOperator.GREATER_THAN);
        assertThat(predicate.getRightHandOperand().isNotNull());
        assertThat(predicate.getRightHandOperand().in(1L));
    }

    @Test
    void test_convert_with_greaterThanOrEqual_operation_in_filter() {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        Root<DummyEntityA> root = builder.createQuery(DummyEntityA.class).from(DummyEntityA.class);
        SpelExpression expression = expressionParser.parseRaw(String.format("id>=1L", 1L));

        ComparisonPredicate predicate = (ComparisonPredicate) SpelExpressionToPredicateConverter.convert(expression, builder, root);
        assertThat(predicate).isNotNull();
        assertThat(predicate.getComparisonOperator()).isEqualTo(ComparisonPredicate.ComparisonOperator.GREATER_THAN_OR_EQUAL);
        assertThat(predicate.getRightHandOperand().isNotNull());
        assertThat(predicate.getRightHandOperand().in(1L));
    }

    @Test
    void test_convert_with_lessthan_operation_in_filter() {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        Root<DummyEntityA> root = builder.createQuery(DummyEntityA.class).from(DummyEntityA.class);
        SpelExpression expression = expressionParser.parseRaw(String.format("id<1L", 1L));

        ComparisonPredicate predicate = (ComparisonPredicate) SpelExpressionToPredicateConverter.convert(expression, builder, root);
        assertThat(predicate).isNotNull();
        assertThat(predicate.getComparisonOperator()).isEqualTo(ComparisonPredicate.ComparisonOperator.LESS_THAN);
        assertThat(predicate.getRightHandOperand().isNotNull());
        assertThat(predicate.getRightHandOperand().in(1L));
    }

    @Test
    void test_convert_with_lessThanOrEqual_operation_in_filter() {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        Root<DummyEntityA> root = builder.createQuery(DummyEntityA.class).from(DummyEntityA.class);
        SpelExpression expression = expressionParser.parseRaw(String.format("id<=1L", 1L));

        ComparisonPredicate predicate = (ComparisonPredicate) SpelExpressionToPredicateConverter.convert(expression, builder, root);
        assertThat(predicate).isNotNull();
        assertThat(predicate.getComparisonOperator()).isEqualTo(ComparisonPredicate.ComparisonOperator.LESS_THAN_OR_EQUAL);
        assertThat(predicate.getRightHandOperand().isNotNull());
        assertThat(predicate.getRightHandOperand().in(1L));
    }

    @Test
    void test_convert_with_field_operation_in_filter() {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        Root<DummyEntityA> root = builder.createQuery(DummyEntityA.class).from(DummyEntityA.class);
        SpelExpression expression = expressionParser.parseRaw(String.format("id == dummyEntityBSet", 1L));

        ComparisonPredicate predicate = (ComparisonPredicate) SpelExpressionToPredicateConverter.convert(expression, builder, root);
        assertThat(predicate).isNotNull();
        assertThat(predicate.getComparisonOperator()).isEqualTo(ComparisonPredicate.ComparisonOperator.EQUAL);
        assertThat(predicate.getRightHandOperand().isNotNull());
        assertThat(predicate.getRightHandOperand().in(1L));
    }

    @Test
    void test_convert_throws_InvalidFilterException_with_describeError_in_filter() {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        Root<DummyEntityA> root = builder.createQuery(DummyEntityA.class).from(DummyEntityA.class);
        SpelExpression expression = expressionParser.parseRaw(String.format("id!=1", 1L));

        InvalidFilterException exception = assertThrows(
                InvalidFilterException.class,
                () -> SpelExpressionToPredicateConverter.convert(expression, builder, root),
                "TODO: Describe error"
        );
        assertTrue(exception.getMessage().contains("TODO: Describe error"));
    }

    private <T, U> ResourceApiController<T, U> getResourceApiController(Class<T> clazz) {
        EntityUtils<T> entityUtils = new EntityUtils<T>(clazz, entityManager);
        return new ResourceApiController<T, U>(clazz, entityManager, transactionManager, entityUtils);
    }

}