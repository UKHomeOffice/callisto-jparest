package uk.gov.homeoffice.digital.sas.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Import(TestConfig.class)
@EnableJpaRepositories("uk.gov.homeoffice.digital.sas.repository")
public class TestConfigWithJpa {
}
