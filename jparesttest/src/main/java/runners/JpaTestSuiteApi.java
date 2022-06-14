package runners;

import io.cucumber.junit.CucumberOptions;
import net.serenitybdd.cucumber.CucumberWithSerenity;
import org.junit.runner.RunWith;

@RunWith(CucumberWithSerenity.class)
@CucumberOptions(
        tags = "@JpaRestApi",
        plugin = {"pretty"},
        features = "classpath:features",
        glue = "steps"
)
public class JpaTestSuiteApi {

}
