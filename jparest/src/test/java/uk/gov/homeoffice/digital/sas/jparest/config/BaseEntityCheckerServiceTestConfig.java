package uk.gov.homeoffice.digital.sas.jparest.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.homeoffice.digital.sas.jparest.service.BaseEntityCheckerService;

@Configuration
public class BaseEntityCheckerServiceTestConfig {

    @PersistenceContext
    EntityManager entityManager;

    @Bean
    public BaseEntityCheckerService baseEntityCheckerService() {
        return new BaseEntityCheckerService(entityManager);
    }

}
