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
    When someone successfully GETs "/swagger-ui/index.html" from the test service
    Then the last response should have a status code of 200
    And the last response body should not be empty

  Scenario: Assert response body should be empty
    When someone successfully GETs "/tests/empty.html" from the test service
    Then the last response body should be empty

  Scenario: Response should contain fields
    When someone retrieves profiles from the test service
    Then the last response should contain the fields
      | items |
      | meta  |

  Scenario: Response should not contain fields
    When someone retrieves profiles from the test service
    Then the last response should not contain the fields
      | made_up |
      | missing |

  Scenario: Object should contain fields
    When the admin retrieves profiles from the test service
    Then the last of the profiles in the last response from the test service should contain the fields
      | preferences |
      | bio         |

  Scenario: Object should contain fields
    When someone successfully GETs "/resources/profiles" from the test service
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
    Then the last response should contain
      | field | type | expectation           |
      | items | List | hasSizeGreaterThan(1) |
    Then the 1st of the profiles in the last response from the test service should contain
      | field       | type    | expectation |
      | dob         | Instant | isNotNull() |
      | preferences | String  | isNotNull() |

  Scenario: Property expectations for each object

    When someone retrieves profiles from the test service
    Then the last response should contain
      | field | type | expectation           |
      | items | List | hasSizeGreaterThan(1) |
    And each of the profiles in the last response from the test service should contain
      | field        | type    | expectation                                        |
      | dob          | Instant | isNotNull()                                        |
      | preferences  | String  | isNotNull()                                        |
      | firstRelease | Instant | isBeforeOrEqualTo("1979-01-01T00:00:00.000+00:00") |

  Scenario: Automatic prefixing of the generated owner for ref fields
    Given the admin creates users from the file './features/data/wip/login/valid-user.json' in the test service
    And eventually data becomes consistent
    When the admin successfully retrieves users in the test service with
      | filter  | value           |
      | withRef | test-admin-user |
    Then the last response should contain
      | field | type  | expect | match         |
      | users | Array | to     | have(1).items |
    And the first of the users in the last response should contain
      | field | type   | expect | match             |
      | title | String | to     | eq 'Example user' |

  Scenario: Asserting against nested properties in a response

    Assertions against an object can test nested properties
    This is done by specifying the path to the nested property

    And the admin creates roles from the file './features/data/wip/login/valid-role.json' in the test service
    And eventually data becomes consistent
    When the admin successfully retrieves roles in the test service with
      | filter  | value      |
      | withRef | valid-role |
    Then the last response should contain
      | field | type  | expect | match         |
      | roles | Array | to     | have(1).items |
    Then the last response should contain
      | field          | type   | expect | match                                |
      | roles[0].title | String | to     | eq 'role created by test automation' |
    And the first of the roles in the last response should contain
      | field      | type   | expect | match                               |
      | actions[0] | String | to     | eq 'service:resource:action'        |
      | actions[1] | String | to     | eq 'anotherservice:resource:write'  |
      | actions[2] | String | to     | eq 'differentservice:resource:read' |
      | actions[3] | String | to     | eq 'sameservice:resource:write'     |
      | actions[4] | String | to     | eq 'whichservice:resource:read'     |

  Scenario: Asserting against properties of raw HTTP response

    Assertions can test against properties of the response object.
    The expression for 'field' will be evaluated against the response body first
    if the response body can be parsed as a json object. In case of ambiguity
    e.g. for a field named `code` you can explicitly specify `response.code`
    to check for the property on the response object itself.

    When someone GETs /MockedEndpoint2 from the dcr
    Then the response should contain
      | field                | type    | expect | match                      |
      | code                 | Integer | to     | eq(300)                    |
      | parsed_response      | Hash    | to     | include({"code"=>300})     |
      | parsed_response.code | Integer | to     | eq(300)                    |
      | response.code        | String  | to     | eq("200")                  |
      | headers.content-type | String  | to     | eq("application/json")     |
      | headers.vary         | String  | to     | include("User-Agent")      |
      | headers.vary         | String  | to     | include("Accept-Encoding") |
