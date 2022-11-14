package uk.gov.homeoffice.digital.sas.cucumberjparest.config;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.homeoffice.digital.sas.jparest.config.ObjectMapperConfig;

@CucumberContextConfiguration
@ContextConfiguration(classes = {JpaTestContext.class, ObjectMapperConfig.class})
public class CucumberConfig {
}
