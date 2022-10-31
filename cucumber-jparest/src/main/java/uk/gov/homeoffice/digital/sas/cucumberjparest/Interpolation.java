package uk.gov.homeoffice.digital.sas.cucumberjparest;

import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.expression.BeanExpressionContextAccessor;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

/**
 * Supports interpolation with a {@See StandardEvaluationContext} and provides access to bean
 * expressions
 */
@Component
public class Interpolation {

  private final ExpressionParser parser = new SpelExpressionParser();
  private final StandardEvaluationContext context;
  private final BeanExpressionContext rootObject;
  private final Pattern interpolationPattern = Pattern.compile("#\\{([^\\}]*)\\}");

  @Autowired
  public Interpolation(ConfigurableBeanFactory beanFactory) {
    this.context = createContext(beanFactory);
    this.rootObject = new BeanExpressionContext(beanFactory, null);
  }

  /**
   * Creates a StandardEvaluationContext with a configured bean resolver and property accessor.
   *
   * @param beanFactory The bean factory to configure the resolver with
   * @return StandardEvaluationContext
   */
  private static StandardEvaluationContext createContext(ConfigurableBeanFactory beanFactory) {
    var context = new StandardEvaluationContext();
    context.setBeanResolver(new BeanFactoryResolver(beanFactory));
    context.addPropertyAccessor(new BeanExpressionContextAccessor());
    return context;
  }

  /**
   * Evaluates a string and interpolates any values enclosed in #{...}
   *
   * @param content The content to process
   * @return String
   */
  public String evaluate(String content) {

    var value = interpolationPattern
        .matcher(content)
        .replaceAll((mr) -> {
          Expression expression = parser.parseExpression(mr.group(1));
          return expression.getValue(context, rootObject, String.class);
        });

    return value;
  }
}
