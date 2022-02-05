package uk.gov.homeoffice.digital.sas.demo;

import javax.annotation.PostConstruct;

import org.springdoc.core.GroupedOpenApi;
import org.springdoc.core.SpringDocUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.tags.Tag;
import uk.gov.homeoffice.digital.sas.jparest.ResourceApiController;

@Configuration
public class test {
    
    @PostConstruct
    public void init() {
        // GroupedOpenApi.builder().group("concerts").pathsToMatch("/resources/concerts/**").build();
        SpringDocUtils.getConfig().addRestControllers(ResourceApiController.class);
    }
    // @Bean
    // public GroupedOpenApi storeOpenApi() {
    //    String paths[] = {"/resources/concerts/**"};
    //    return GroupedOpenApi.builder().group("stores").pathsToMatch(paths)
    //          .build();
    // }

    @Bean
    public OpenAPI customOpenAPI(@Value("1.0.0.0") String appVersion) {
        PathItem pi = new PathItem();
        ApiResponse response = new ApiResponse();
        ApiResponses responses = new ApiResponses().addApiResponse("200", response);
        Operation put = new Operation();
        put.setResponses(responses);
        pi.put(put);
        put.addTagsItem("Stephen");
        return new OpenAPI()
        		.info(new Info()
        				.title("Foobar API")
        				.version(appVersion)
        				.description("This is a sample Foobar server created using springdocs - a library for OpenAPI 3 with spring boot.")
        				.termsOfService("http://swagger.io/terms/")
        				.license(new License().name("Apache 2.0")
        						.url("http://springdoc.org")))
                    .path("/resources/stephen", pi);
    }
}
