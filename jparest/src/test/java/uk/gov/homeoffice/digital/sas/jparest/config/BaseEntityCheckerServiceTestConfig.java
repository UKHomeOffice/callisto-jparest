package uk.gov.homeoffice.digital.sas.jparest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.homeoffice.digital.sas.jparest.service.BaseEntityCheckerService;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Configuration
public class BaseEntityCheckerServiceTestConfig {

    @PersistenceContext
    EntityManager entityManager;

    @Bean
    public BaseEntityCheckerService baseEntityCheckerService() {
        return new BaseEntityCheckerService(entityManager);
    }

}
