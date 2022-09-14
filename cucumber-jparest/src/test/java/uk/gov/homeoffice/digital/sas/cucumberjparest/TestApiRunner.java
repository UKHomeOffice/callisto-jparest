package uk.gov.homeoffice.digital.sas.cucumberjparest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import io.cucumber.plugin.EventListener;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.plugin.event.TestRunStarted;

/**
 * Cucumber event listener that registers
 * handlers for TestRunStarted and TestRunFinished
 * to spin up a test API and tear it down when
 * the test run completes.
 */
@SpringBootConfiguration
@EnableAutoConfiguration
public class TestApiRunner implements EventListener {
  
  /**
   * Holds a reference to the application context
   * so that it can be stopped when the test run
   * completes
   */
  private ConfigurableApplicationContext context = null;

  @Override
  public void setEventPublisher(EventPublisher publisher) {

    //Register handler for test run start
    publisher.registerHandlerFor(TestRunStarted.class, event -> {

      // Running the Sprin Boot application with the JpaRest module
      // application properties etc. wil create a Test API service  
      Class<?>[] primarySources = {TestApiRunner.class};
      String[] args = new String[0];
      this.context = SpringApplication.run(primarySources, args);

      // The port is set to be ephemeral so we need to retrieve the
      // port number and then set the address of the service
      // in the service registry.
      int port = ((ServletWebServerApplicationContext)this.context).getWebServer().getPort();

      JpaTestContext.serviceRegistry.addService("test", "http://localhost:" + port);

    });
    
    // register handler for test run finished
    publisher.registerHandlerFor(TestRunFinished.class, event -> {

      // If a context exists the Test API was successfully started
      // shut it down
      if (this.context != null) {
        SpringApplication.exit(this.context);
      }
    });
  }
}