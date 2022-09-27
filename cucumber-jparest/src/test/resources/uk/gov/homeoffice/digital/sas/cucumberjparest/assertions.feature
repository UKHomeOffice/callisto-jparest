Feature: Assertions

    When a request is issued assertions will be made against the returned
  response to ensure the correct result is returned.

  Several steps are provided for basic checking of the returned response
  but the most interesting steps are the ones that allow for several
  expectations to be assert for a specified object.

  The object to assert expectations against can be specified for
  resourceful and business endpoints alike and back referencing
  to previous requests in the same scenario is supported.

  Once the object to assert against is specified a table can be provided
  to specify the field to assert against, it's type and an expectation.

  For more details on expectations and how they can be specified and
  customised see the Expectations supplement in the documentation.

  Background:
    Given the tester is a user
    And the admin is a user

  Scenario: Assert response body should not be empty
    When someone GETs "/swagger-ui/index.html" from the test service
    Then the last response should have a status code of 200
    And the last response body should not be empty

  Scenario: Assert response body should be empty
    When someone GETs "/tests/empty.html" from the test service
    Then the last response body should be empty

  Scenario: Response should contain fields
    When someone retrieves profiles from the test service
    Then the last response body should contain the fields
      | items |
      | meta  |

  Scenario: Response should not contain fields
    When someone retrieves profiles from the test service
    Then the last response body should not contain the fields
      | made_up |
      | missing |

  Scenario: Object should contain fields
    When the admin retrieves profiles from the test service
    Then the last of the profiles in the last response from the test service should contain the fields
      | preferences |
      | bio         |

  Scenario: Object should contain fields
    When someone GETs "/resources/profiles" from the test service
    Then the last of the profiles in the last "/resources/profiles" response from the test service should contain the fields
      | preferences |
      | bio         |

  Scenario: Object should not contain fields
    When the admin retrieves profiles from the test service
    Then the 1st of the profiles in the last response from the test service should not contain the fields
      | field   |
      | help_me |
      | missing |

  Scenario: Resources should be equal

    Resources are positional element of a given property in
    a specific response.
    This step enables two objects to be directly compared.

    When someone retrieves profiles from the test service
    And someone retrieves profiles from the test service
    Then the 1st of the profiles in the 1st response from the test service should equal the 1st of the profiles in the last response from the test service

  Scenario: Property expectations

    Objects can be sepcified as either a last response or an indexed response
    to a business/resourceful endpoint.

    It can also be specified as a specific resource in a response

    When someone retrieves profiles from the test service
    Then the last response body should contain
      | field | type | expectation           |
      | items | List | hasSizeGreaterThan(1) |
    Then the 1st of the profiles in the last response from the test service should contain
      | field       | type    | expectation |
      | dob         | Instant | isNotNull() |
      | preferences | String  | isNotNull() |

  Scenario: Property expectations for each object

    When someone retrieves profiles from the test service
    Then the last response body should contain
      | field | type | expectation           |
      | items | List | hasSizeGreaterThan(1) |
    And each of the profiles in the last response from the test service should contain
      | field        | type    | expectation                                        |
      | dob          | Instant | isNotNull()                                        |
      | preferences  | String  | isNotNull()                                        |
      | firstRelease | Instant | isBeforeOrEqualTo("1979-01-01T00:00:00.000+00:00") |

  Scenario: Asserting against nested properties in a response

    Assertions against an object can test nested properties
    This is done by specifying the path to the nested property

    When someone retrieves profiles from the test service
    Then the last response body should contain
      | field | type | expectation           |
      | items | List | hasSizeGreaterThan(1) |
    And the last response body should contain
      | field        | type   | expectation               |
      | items[2]     | Object | isNotNull                 |
      | items[1].bio | String | isEqualTo("My Bio for 2") |
    And the 1st of the profiles in the last response should contain
      | field                  | type   | expectation           |
      | props.subItems         | List   | hasSize(2)            |
      | props.subItems[0].make | String | isEqualTo("Vauxhall") |
      | props.subItems[1].make | String | isEqualTo("Austin")   |
    And the 2nd of the profiles in the last response should contain
      | field | type | expectation |
      | props | Map  | isNull      |

  Scenario: Asserting against headers of HTTP response

    Assertions can be made against headers of the response object.

    When someone retrieves profiles from the test service
    Then the last response should contain the headers
      | content-type | isEqualTo("application/json") |
