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
    Then the last response should contain fields
      | items |
      | meta  |

  Scenario: Response should not contain fields
    When the tester creates users from the file './features/data/wip/login/valid-user.json' in the test service
    Then the last response should not contain fields
      | field   |
      | made_up |
      | missing |

  Scenario: Object should contain fields

    When the admin successfully retrieves tenants in the test service with
      | filter | value |
      | owner  | root  |
    Then the first of the tenants in the last response should contain the fields
      | field       |
      | featureRefs |
      | title       |
      | description |

  Scenario: Object should not contain fields
    When the admin successfully retrieves tenants in the test service with
      | filter | value |
      | owner  | root  |
    Then the first of the tenants in the last response should not contain the fields
      | field   |
      | help_me |
      | missing |

  Scenario: Resources should be equal

    Resources are positional element of a given property in
    a specific response.
    This step enables two objects to be directly compared.

    When the admin successfully GETs /pauth/tenants from the test service
    And the admin successfully GETs /pauth/tenants from the test service
    Then the first of the tenants in the first /pauth/tenants response should equal the first of the tenants in the last /pauth/tenants response

  Scenario: Property expectations

    Objects can be sepcified as either a last response or an indexed response
    to a business/resourceful endpoint.

    It can also be specified as a specific resource in a response

    When the admin successfully GETs /pauth/tenants from the test service
    Then the last response should contain
      | field   | type  | expect | match                  |
      | tenants | Array | to     | have_at_least(1).items |
    And the first of the tenants in the last /pauth/tenants response should contain
      | field       | type   | expect | match                  |
      | featureRefs | Array  | to     | have_at_least(1).items |
      | title       | String | to not | be_empty               |
      | description | String | to not | be_empty               |

  Scenario: Property expectations for each object

    When the admin successfully retrieves tenants in the test service with
      | filter | value |
      | owner  | root  |
    Then the last response should contain
      | field   | type  | expect | match                  |
      | tenants | Array | to     | have_at_least(1).items |
    And each of the tenants in the last response should contain
      | field       | type   | expect | match                  |
      | featureRefs | Array  | to     | have_at_least(1).items |
      | title       | String | to not | be_empty               |

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


  Scenario: Persona substitution for underlying user's ref for userRef and profileRef fields
    When the admin creates an assignment for the tester
    And eventually data becomes consistent
    When the admin successfully retrieves assignments in the test service with
      | filter      | value  |
      | withUserRef | tester |
    Then the last response should contain
      | field       | type  | expect | match         |
      | assignments | Array | to     | have(2).items |
    And the first of the assignments in the last response should contain
      | field   | type   | expect | match                         |
      | userRef | String | to     | eq users['tester']['userRef'] |
    And the second of the assignments in the last response should contain
      | field   | type   | expect | match                         |
      | userRef | String | to     | eq users['tester']['userRef'] |

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
