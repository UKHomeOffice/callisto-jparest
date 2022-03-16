package uk.gov.homeoffice.digital.sas.jparest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.expression.spel.standard.SpelExpression;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;


class SpelExpressionToPredicateConverterTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

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


}