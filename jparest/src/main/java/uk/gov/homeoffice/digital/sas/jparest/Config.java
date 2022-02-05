package uk.gov.homeoffice.digital.sas.jparest;

import java.util.logging.Logger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

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
	
}
