# Cucumber JPA REST

The goal of the cucumber-jparest project is to provide cucumber steps
that enable the testing of service that has utilised the JpaRest
library to expose resources (JPA entities) over HTTP.

This project is developed as a module alongside the JpaRest project
so that it can keep up to date with any enhancements made to the
JpaRest library.

## Features

The tests for this library are themselves written in gherkin and
executed by cucumber.

Therefore the living documentation of what features the cucumber-jparest
supports are the [feature files](src/test/resources/uk/gov/homeoffice/digital/sas/cucumberjparest/)
themselves.

## Getting Started

It is important to consider that the purpose of this project is to create
a package that will be used downstream in the test scope. Dependencies
that are typically depended on in the test scope here may be required
in the compile dependency scope.

However, we also need to test this project so there are a mixture of
step definitions in the src path and the test path. Step definitions
in the src path are included in the package but step definitions in the
test path exist to allow the execution of the cucumber-jparest
specification.

### What service are the tests running against

Cucumber-jparest provides steps definitions for specifying the
endpoints a service has exposed with the JpaRest project. In order to
test that the packaged step definitions work as expected, we require
a service to be running so that we can execute our specifications
against it.

Therefore cucumber-jparest project includes the JpaRest dependency as
a test dependency and defines a JPA entity in the test path. When the
test run starts @BeforeAll hook runs a Spring application which creates a
web application with a H2 database and runs it on an ephemeral port.
The configuration for the application can be found in the test resources
path. Running the application on an ephemeral port avoids ports conflict
that a developer may be running locally.

This approach could be used when testing services that use the JpaRest
library or the service could be deployed and then the test run against the
deployed endpoint.

#### How to specify the service URL(s) against which the tests will be executed

Cucumber-jparest expects the target service URL(s) to be provided via a system property named: 
`cucumber.jparest.serviceRegistry`. It is up to the project using the library to supply that 
system property.

For example:
- If you want to execute tests against a `timecard` service running on `http://localhost:9090`,
you will need to provide this JVM option: `-Dcucumber.jparest.serviceRegistry=timecard=http://localhost:9090`
- If you have a scenario which requires two services: `timecard` running on `http://localhost:9090` and `scheduler` running on `http://localhost:9091`
  you will need to provide this JVM option: `-Dcucumber.jparest.serviceRegistry=timecard=http://localhost:9090,scheduler=http://localhost:9091`

#### The tenantId request parameter temporary workaround

Although the long term view is that `tenantId` should be extracted from the authenticated user
details, the current implementation expects `tenantId` as a request parameter. To avoid the
overhead of having to specify `tenantId` as a request parameter for each BDD test scenario, you 
only have to specify it once, as a JVM option: `-Dcucumber.jparest.tenantId=b7e813a2-bb28-11ec-8422-0242ac120002`

### If using VSCode

Please note that calling lifecycle commands on projects doesn't work correctly for a multi module project.
Instead try calling mvnw with the `-am` option and specify the target module with the `-pl` option.

```bash
$ ./mvnw test -pl cucumber-jparest
```

#### VS Code extensions

- Extension Pack for Java
- Lombok Annotations Support
- Cucumber (Gherkin) Full Support
