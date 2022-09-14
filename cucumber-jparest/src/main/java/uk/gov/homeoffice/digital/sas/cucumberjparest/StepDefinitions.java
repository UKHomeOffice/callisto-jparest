package uk.gov.homeoffice.digital.sas.cucumberjparest;

import java.io.File;
import java.util.Objects;

import org.assertj.core.util.Files;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import io.cucumber.java.ParameterType;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.spring.CucumberContextConfiguration;
import io.restassured.response.Response;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 
 * Step definitions to be included in the published cucumber-jparest package
 * 
 */
@CucumberContextConfiguration
@ContextConfiguration(classes = JpaTestContext.class) 
public class StepDefinitions {

    private final PersonaManager personaManager;
    private final HttpResponseManager httpResponseManager;
    private final JpaRestApiClient jpaRestApiClient;

    @Autowired
    public StepDefinitions(PersonaManager personaManager, HttpResponseManager httpResponseManager, JpaRestApiClient jpaRestApiClient) {
        this.personaManager = Objects.requireNonNull( personaManager, "personaManager must not be null" );
        this.httpResponseManager = Objects.requireNonNull( httpResponseManager, "httpResponseManager must not be null" );
        this.jpaRestApiClient = Objects.requireNonNull( jpaRestApiClient, "jpaRestApiClient must not be null" );
    }
    
    
    /** 
     * 
     * Creates a new persona for the given name
     * 
     * @param name The name of the persona to create
     */
    @Given("^(\\S*) is a user$")
    public void persona_is_a_user(String name) {
        personaManager.createPersona(name);
    }

    
    /** 
     * 
     * Creates a payload from the specified file and posts it to
     * the the endpoint exposed for the given resource.
     * 
     * @param persona The persona to use for auth context
     * @param resource The type of resource to be created
     * @param fileContents The file contents of the specified file
     * @param service There service to use
     */
    @When("{persona} creates {word} from the {filecontents}{service}")
    public void persona_creates_resource_from_the_file_in_the_service(Persona persona, String resource, String fileContents, String service) {

        Response response = this.jpaRestApiClient.Create(service, resource, fileContents);

        this.httpResponseManager.addResponse("/resources/" + resource, response);
        
    }

    
    /** 
     * 
     * Retrieves the specified resource type from the given service.
     * 
     * @param persona The persona to use for auth context
     * @param resource The type of resources to be retrieved
     * @param service There service to use
     */
    @When("{persona} retrieves {word}{service}")
    public void persona_retrieves_resources_from_the_service(Persona persona, String resource, String service) {
        Response response = this.jpaRestApiClient.Retrieve(service, resource, null);

        this.httpResponseManager.addResponse("/resources/" + resource, response);
    }

    
    /** 
     * 
     * Validates the last responses status code against the given value
     * 
     * @param expectedCode The expected status code of the last response
     */
    @Then("the last response should have a status code of {int}")
    public void the_last_response_should_have_a_status_code_of(Integer expectedCode) {
        assertThat(this.httpResponseManager.getLastResponse().statusCode()).isEqualTo(expectedCode);
    }

    
    /** 
     * 
     * Matchs when a file is specified and returns the contents
     * of the specified file
     * 
     * @param path The path of the file
     * @return String The contents of the file
     */
    @ParameterType("file '([^']*)'")
    public String filecontents(String path){
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource(path).getFile());
            String data = Files.contentOf(file, "UTF-8");
            return data;
        }
        catch(RuntimeException ex) {
            throw new IllegalArgumentException("File doesn't exist " + path);
        }        
    }

    
    /** 
     * 
     * Gets the persona for the given name
     * 
     * @param name The name of the persona
     * @return Persona The persona associated with the given name
     */
    @ParameterType("(\\S*)")
    public Persona persona(String name){
        return this.personaManager.getPersona(name);        
    }

    
    /** 
     * 
     * Retrieves the location of the given service.
     * 
     * @param name The name of the service
     * @return String The URL of the service
     */
    @ParameterType("(?: (?:from|in) the (\\S*) service)?")
    public String service(String name){
        return name;        
    }

}
