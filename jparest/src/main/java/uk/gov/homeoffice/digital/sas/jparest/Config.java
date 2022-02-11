package uk.gov.homeoffice.digital.sas.jparest;

import java.util.logging.Logger;

import javax.annotation.PostConstruct;

import org.springdoc.core.SpringDocUtils;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import uk.gov.homeoffice.digital.sas.jparest.controller.ResourceApiController;
import uk.gov.homeoffice.digital.sas.jparest.swagger.ResourceOpenApiCustomiser;

@Configuration
public class Config {

	private final static Logger LOGGER = Logger.getLogger(Config.class.getName());

	@Bean
	@Lazy(false)
	JpaRestMvcConfigurer jpaRestMvcConfigurer() {
		LOGGER.info(("auto configure"));
		return new JpaRestMvcConfigurer();
	}

	@Bean
	@Lazy(false)
	HandlerMappingConfigurer handlerMappingConfigurer() {
		LOGGER.info(("auto configure handler mapping"));
		return new HandlerMappingConfigurer();
	}

	@Bean
	public OpenApiCustomiser resourceOpenApiCustomiser() {
		return new ResourceOpenApiCustomiser();
	}

	@Bean
	public ResourceEndpoint singletonBean() {
		return new ResourceEndpoint();
	}

	@PostConstruct
	public void init() {
		// Add the ResourceApiController to OpenApi so that all the endpoints
		// are exposed. The customiser will then customise the
		// specification to accomodate the generic controller and type erasure.
		SpringDocUtils.getConfig().addRestControllers(ResourceApiController.class);
	}

}
