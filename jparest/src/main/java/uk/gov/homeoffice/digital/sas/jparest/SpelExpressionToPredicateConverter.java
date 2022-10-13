package uk.gov.homeoffice.digital.sas.jparest;

import org.springframework.expression.spel.SpelNode;
import org.springframework.expression.spel.ast.Literal;
import org.springframework.expression.spel.ast.MethodReference;
import org.springframework.expression.spel.ast.OpAnd;
import org.springframework.expression.spel.ast.OpEQ;
import org.springframework.expression.spel.ast.OpGE;
import org.springframework.expression.spel.ast.OpGT;
import org.springframework.expression.spel.ast.OpLE;
import org.springframework.expression.spel.ast.OpLT;
import org.springframework.expression.spel.ast.OpNE;
import org.springframework.expression.spel.ast.OpOr;
import org.springframework.expression.spel.ast.OperatorMatches;
import org.springframework.expression.spel.ast.OperatorNot;
import org.springframework.expression.spel.ast.PropertyOrFieldReference;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.util.Assert;
import org.springframework.web.bind.WebDataBinder;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.InvalidFilterException;
import uk.gov.homeoffice.digital.sas.jparest.utils.WebDataBinderFactory;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Used to convert SpelExpression into a JPA predicate
 */
public class SpelExpressionToPredicateConverter {

    private SpelExpressionToPredicateConverter(){
    }

    private static final Logger LOGGER = Logger.getLogger(SpelExpressionToPredicateConverter.class.getName());

    private static WebDataBinder binder = WebDataBinderFactory.getWebDataBinder();

    /**
     * Converts SpelExpression to a JPA predicate
     *
     * @param from    The source SpelExpression
     * @param builder The CriteriaBuilder to use to create the predicate, must not be {@literal null}.
     * @param root    must not be {@literal null}.
     * @return a {@link javax.persistence.criteria.Predicate}.
     */
    public static Predicate convert(SpelExpression from, CriteriaBuilder builder, From<?, ?> root) {
        if (from == null) {
            LOGGER.fine("Nothing to convert, SpelExpression is null");
            return null;
        }

        Assert.notNull(builder, "builder must not be null!");
        Assert.notNull(root, "root must not be null!");

        LOGGER.fine("Converting SpelExpression AST to predicate");
        return getPredicate(from.getAST(), builder, root);
    }

    /*  Builds predicate recursively.
     *  Allows AND/OR/NOT as logical operators
     *  Allows Field comparison with literals or other fields
     *  Allows methods where method is supported by {@link #getMethodPredicate(SpelNode, CriteriaBuilder, From<?, ?>)
     */
    private static Predicate getPredicate(SpelNode node, CriteriaBuilder builder, From<?, ?> root) {

        // Handle logical operators
        var logicalPredicate = getLogicalPredicate(node, builder, root);
        if (logicalPredicate != null) {
            return logicalPredicate;
        }

        // Handle method references 
        if (node instanceof MethodReference methodReference) {
            return getMethodPredicate(methodReference, builder, root);
        }

        // At this point we are looking for "property {operator} property/literal"
        // so we can only handle 2 children the left side and the right side
        // of the expression. Throw if there are not only 2 children
        if (node.getChildCount() != 2) {
            throw new InvalidFilterException("Unknown expression");
        }

        // Left side must be a field
        SpelNode leftNode = node.getChild(0);
        if (!PropertyOrFieldReference.class.isAssignableFrom(leftNode.getClass())) {
            LOGGER.severe("Left hand side was not assignable to PropertyOrFieldReference");
            throw new InvalidFilterException("Left hand side must be a field");
        }

        PropertyOrFieldReference fieldReference = (PropertyOrFieldReference) leftNode;
        Path<Comparable<Object>> field = root.get(fieldReference.getName());

        // Get the right side
        SpelNode rightNode = node.getChild(1);
        // handle field comparison
        if (rightNode instanceof PropertyOrFieldReference propertyOrFieldReference) {
            Path<Comparable<Object>> rightField = root.get(propertyOrFieldReference.getName());
            var predicate = getEqualityOrRelativeOperatorPredicate(node, builder, field, rightField);
            if (predicate != null) {
                return predicate;
            } 
            
            LOGGER.severe("Left hand side and right hand side where properties but the operator was not supported");
            throw new InvalidFilterException("Operator not valid. " + node.toStringAST());

        }
        
        // handle literal comparison
        if (Literal.class.isAssignableFrom(rightNode.getClass())) {
            Class<?> clazz = field.getJavaType();
            Object rightValue = convertTo(((Literal) rightNode).getLiteralValue().getValue(), clazz);
            @SuppressWarnings("unchecked")
            Comparable<Object> comparableValue = (Comparable<Object>) rightValue;
            var predicate = getEqualityOrRelativeOperatorPredicate(node, builder, field, comparableValue);
            if (predicate != null) {
                return predicate;
            }
            if (node instanceof OperatorMatches) {
                return builder.like(field.as(String.class), (String) rightValue);
            }
        
            LOGGER.severe("Left hand side and right hand side where properties but the operator was not supported");
            throw new InvalidFilterException("Operator not valid. " + node.toStringAST());
        
        }
        throw new InvalidFilterException("Right hand side must be a literal or a field");

    }

    private static Predicate getLogicalPredicate(SpelNode node, CriteriaBuilder builder, From<?, ?> root) {
        if (node instanceof OpOr) {
            var x = getPredicate(node.getChild(0), builder, root);
            var y = getPredicate(node.getChild(1), builder, root);
            return builder.or(x, y);
        } else if (node instanceof OpAnd) {
            var x = getPredicate(node.getChild(0), builder, root);
            var y = getPredicate(node.getChild(1), builder, root);
            return builder.and(x, y);
        } else if (node instanceof OperatorNot) {
            var x = getPredicate(node.getChild(0), builder, root);
            return builder.not(x);
        }
        return null;
    }

    private static Predicate getEqualityOrRelativeOperatorPredicate(SpelNode node, CriteriaBuilder builder,
            Path<Comparable<Object>> field, Comparable<Object> comparableValue) {
        if (node instanceof OpEQ) {
            return builder.equal(field, comparableValue);
        } else if (node instanceof OpNE) {
            return builder.notEqual(field, comparableValue);
        } else if (node instanceof OpGE) {
            return builder.greaterThanOrEqualTo(field, comparableValue);
        } else if (node instanceof OpGT) {
            return builder.greaterThan(field, comparableValue);
        } else if (node instanceof OpLE) {
            return builder.lessThanOrEqualTo(field, comparableValue);
        } else if (node instanceof OpLT) {
            return builder.lessThan(field, comparableValue);
        }
        return null;
    }

    private static Predicate getEqualityOrRelativeOperatorPredicate(SpelNode node, CriteriaBuilder builder,
            Path<Comparable<Object>> field, Path<Comparable<Object>> comparableField) {
        if (node instanceof OpEQ) {
            return builder.equal(field, comparableField);
        } else if (node instanceof OpNE) {
            return builder.notEqual(field, comparableField);
        } else if (node instanceof OpGE) {
            return builder.greaterThanOrEqualTo(field, comparableField);
        } else if (node instanceof OpGT) {
            return builder.greaterThan(field, comparableField);
        } else if (node instanceof OpLE) {
            return builder.lessThanOrEqualTo(field, comparableField);
        } else if (node instanceof OpLT) {
            return builder.lessThan(field, comparableField);
        }
        return null;
    }

    private enum Method {
        IN, BETWEEN
    }

    /**
     * Converts Spel Method reference to predicate
     * Possible methods are
     * <ul><li>In
     * <li>Between</ul>
     */
    private static Predicate getMethodPredicate(MethodReference node, CriteriaBuilder builder, From<?, ?> root) {
        Method method;
        try {
            method = Method.valueOf(node.getName().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new InvalidFilterException("Unrecognised method " + node.getName());
        }

        // To handle a method the first argument must be the field reference
        SpelNode firstArg = node.getChild(0);
        if (!PropertyOrFieldReference.class.isAssignableFrom(firstArg.getClass())) {
            throw new InvalidFilterException("First argument must be a field");
        }

        PropertyOrFieldReference fieldReference = (PropertyOrFieldReference) firstArg;
        Path<Comparable<Object>> field = root.get(fieldReference.getName());
        Class<?> clazz = field.getJavaType();

        Comparable<Object>[] args;

        // Create the appropriate predicate
        switch (method) {
            case BETWEEN:
                args = getLiteralValues(node, 1, clazz);
                return builder.between(root.get(fieldReference.getName()), args[0], args[1]);
            default:
            case IN:
                args = getLiteralValues(node, 1, clazz);
                return field.in((Object[]) args);
        }
    }

    /**
     * Gets literal values from the spel expression as the given type
     */
    @SuppressWarnings("unchecked")
    private static Comparable<Object>[] getLiteralValues(SpelNode node, int startPos, Class<?> clazz) {
        ArrayList<Comparable<Object>> items = new ArrayList<>();
        for (int i = startPos; i < node.getChildCount(); i++) {
            Comparable<Object> rightValue =
                (Comparable<Object>) convertTo(((Literal) node.getChild(i)).getLiteralValue().getValue(), clazz);
            items.add(rightValue);
        }
        var result = new Comparable[node.getChildCount() - 1];
        return items.toArray(result);
    }

    private static <Y> Y convertTo(Object value, Class<Y> clazz) {
        return binder.convertIfNecessary(value, clazz);
    }

}