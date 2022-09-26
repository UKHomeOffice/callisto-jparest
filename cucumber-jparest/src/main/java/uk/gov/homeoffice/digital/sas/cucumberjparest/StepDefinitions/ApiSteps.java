package uk.gov.homeoffice.digital.sas.cucumberjparest.StepDefinitions;

import org.springframework.beans.factory.annotation.Autowired;

import io.cucumber.java.en.When;
import lombok.NonNull;
import uk.gov.homeoffice.digital.sas.cucumberjparest.HttpResponseManager;
import uk.gov.homeoffice.digital.sas.cucumberjparest.JpaRestApiClient;
import uk.gov.homeoffice.digital.sas.cucumberjparest.JpaRestApiResourceResponse;
import uk.gov.homeoffice.digital.sas.cucumberjparest.JpaRestApiResponse;
import uk.gov.homeoffice.digital.sas.cucumberjparest.Persona;

public class ApiSteps {

    private final HttpResponseManager httpResponseManager;
    private final JpaRestApiClient jpaRestApiClient;

    @Autowired
    public ApiSteps(@NonNull HttpResponseManager httpResponseManager,
            @NonNull JpaRestApiClient jpaRestApiClient) {
        this.httpResponseManager = httpResponseManager;
        this.jpaRestApiClient = jpaRestApiClient;

    }

    /**
     * 
     * Creates a payload from the specified file and posts it to
     * the the endpoint exposed for the given resource.
     * 
     * @param persona      The persona to use for auth context
     * @param resource     The type of resource to be created
     * @param fileContents The file contents of the specified file
     * @param service      There service to use
     */
    @When("{persona} creates {word} from the {filecontents}{service}")
    public void persona_creates_resource_from_the_file_in_the_service(Persona persona, String resource,
            String fileContents, String service) {

        JpaRestApiResourceResponse apiResponse = this.jpaRestApiClient.Create(persona, service, resource, fileContents);

        this.httpResponseManager.addResponse(apiResponse.getBaseResourceURL(), apiResponse.getResponse());

    }

    /**
     * 
     * Retrieves the specified resource type from the given service.
     * 
     * @param persona  The persona to use for auth context
     * @param resource The type of resources to be retrieved
     * @param service  There service to use
     */
    @When("{persona} retrieves {word}{service}")
    public void persona_retrieves_resources_from_the_service(Persona persona, String resource, String service) {
        JpaRestApiResourceResponse apiResponse = this.jpaRestApiClient.Retrieve(persona, service, resource, null);

        this.httpResponseManager.addResponse(apiResponse.getBaseResourceURL(), apiResponse.getResponse());
    }

    /**
     * 
     * Makes a GET request to the service with the given path
     * 
     * @param persona The persona to use for auth context
     * @param path    The URL to request
     * @param service There service to use
     */
    @When("{persona} successfully GETs {string}{service}")
    public void someone_successfully_gets_url_from_service(Persona persona, String path, String service) {
        JpaRestApiResponse apiResponse = this.jpaRestApiClient.Get(persona, service, path);

        this.httpResponseManager.addResponse(apiResponse.getUrl(), apiResponse.getResponse());
    }

}
