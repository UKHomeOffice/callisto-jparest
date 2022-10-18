Feature: General features

  General features of the step definitions provided

  Scenario: Omit service name

    Any step that make a reference to a service can
    have the service name omitted once the service 
    has already been named.

    This can improve the readability of a scenario
    as the steps do not need to be as verbose

    When someone retrieves profiles from the test service with
      | tenantId | b7e813a2-bb28-11ec-8422-0242ac120002 |
    And someone retrieves profiles from the test service with
      | tenantId | b7e813a2-bb28-11ec-8422-0242ac120002 |
    Then the 1st of the profiles in the 1st response from the test service should equal the 1st of the profiles in the last response from the test service
    And the 1st of the profiles in the 1st response should equal the 1st of the profiles in the last response
