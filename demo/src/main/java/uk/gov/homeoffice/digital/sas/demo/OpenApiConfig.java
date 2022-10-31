package uk.gov.homeoffice.digital.sas.demo;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI customOpenApi(@Value("1.0.0.0") String appVersion) {
    return new OpenAPI()
      .info(new Info()
        .title("Example API")
        .version(appVersion)
        .description(
          "This is a sample API server created using springdocs "
            + "- a library for OpenAPI 3 with spring boot.")
        .termsOfService("http://swagger.io/terms/")
        .license(new License().name("Apache 2.0")
          .url("http://springdoc.org")));
  }

}
