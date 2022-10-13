package uk.gov.homeoffice.digital.sas.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EntitiesApplication {
    public static void main(String[] args) {
        Class<?>[] primarySources = {EntitiesApplication.class};
        SpringApplication.run(primarySources, args);
    }

}
