package uk.gov.homeoffice.digital.sas.cucumberjparest;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.OBJECT_FACTORY_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("uk/gov/homeoffice/digital/sas/cucumberjparest")
@SelectClasspathResource("test")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "uk.gov.homeoffice.digital.sas.cucumberjparest,uk.gov.homeoffice.digital.sas.cucumberjparest.testapi")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty,html:target/cucumber-report.html")
@ConfigurationParameter(key = OBJECT_FACTORY_PROPERTY_NAME, value = "io.cucumber.spring.SpringFactory")
@SuppressWarnings("squid:S2187")//Actual tests are BDD features under resources folder
class RunCucumberIntegrationTest {
}
