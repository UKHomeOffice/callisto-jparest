package runners;

import io.cucumber.junit.CucumberOptions;
import net.serenitybdd.cucumber.CucumberWithSerenity;
import org.junit.runner.RunWith;

@RunWith(CucumberWithSerenity.class)
@CucumberOptions(
        tags = "@JpaRestApi",
        plugin = {"pretty"},
        features = "src/test/java/features",
        glue = "uk.gov.homeoffice.digital.sas.jparesttest.steps"
)
public class RunTest {

}
