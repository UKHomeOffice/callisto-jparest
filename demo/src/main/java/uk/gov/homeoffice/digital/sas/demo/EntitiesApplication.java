package uk.gov.homeoffice.digital.sas.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import uk.gov.homeoffice.digital.sas.jparest.config.ApplicationConfig;

@SpringBootApplication
@Import(ApplicationConfig.class)
public class EntitiesApplication {
  public static void main(String[] args) {
    Class<?>[] primarySources = {EntitiesApplication.class};
    SpringApplication.run(primarySources, args);
  }

}
