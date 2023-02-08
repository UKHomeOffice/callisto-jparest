package uk.gov.homeoffice.digital.sas.cucumberjparest.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate5.jakarta.Hibernate5JakartaModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * A configuration distinct from
 * {@link uk.gov.homeoffice.digital.sas.jparest.config.ObjectMapperConfig} to avoid dependency on
 * jparest module
 *
 */
@Configuration
public class ObjectMapperConfig {

  @Bean
  public ObjectMapper objectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new Hibernate5JakartaModule());
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }
}
