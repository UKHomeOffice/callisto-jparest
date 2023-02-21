package uk.gov.homeoffice.digital.sas.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ComponentScan("uk.gov.homeoffice.digital.sas.kafka")
@EntityScan("uk.gov.homeoffice.digital.sas.model")
@EnableJpaRepositories("uk.gov.homeoffice.digital.sas.repository")
public class TestConfig {
}
