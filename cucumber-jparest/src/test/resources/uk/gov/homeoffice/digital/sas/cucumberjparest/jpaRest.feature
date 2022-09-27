Feature: Make requests to endpoints created by JpaRest

  Steps to enable requests to be made to an endpoint generated by
  the JpaRest package.
  The steps understand the protocol exposed by the JpaRest package
  and understands how filters are applied to GET requests and how
  the tenantId must be provided as parameter in the URL.

  Background:
    Given Trevor is a user

  Scenario: Get specific resources by reference

    Get a single resource from the endpoint using
    its identifier
    e.g. "/resources/profiles/17e813a2-bb28-11ec-8422-0242ac120001"

    When Trevor gets the profiles '17e813a2-bb28-11ec-8422-0242ac120001' from the test service
    Then the last response body should contain
      | field | type | expectation |
      | items | List | hasSize(1)  |
    And the 1st of the profiles in the last response should contain
      | field | type   | expectation                                       |
      | id    | String | isEqualTo("17e813a2-bb28-11ec-8422-0242ac120001") |

  Scenario: Retrieve all resources

    Get all resources. The response is paged.
    Additional scenarios might be required to
    demonstrate how to page results

    When Trevor retrieves profiles from the test service

  Scenario Outline: Retrieve filtered resources

    Get all resources matching the given filter.

    Given the additional profiles are
      """
      {
        "tenantId": "b7e813a2-bb28-11ec-8422-0242ac120002",
        "preferences": "<Preference>",
        "bio": "Valid bio",
        "phoneNumber": "0133 3245 392",
        "dob": "<Date of birth>",
        "firstRelease": "1989-05-21T00:00:00.000+00:00"
      }
      """
    And Trevor creates the additional profiles in the test service
    When Trevor retrieves profiles from the test service
    Then the last response body should contain
      | field | type | expectation           |
      | items | List | hasSizeGreaterThan(3) |
    When Trevor retrieves profiles from the test service with
      | filter | <Filter> |
    Then the last response body should contain
      | field | type | expectation   |
      | items | List | <Expectation> |
    Examples:
      | Preference | Date of birth                 | Filter                                 | Expectation           |
      | Pref 1     | 1901-05-21T00:00:00.000+00:00 | dob == "1901-05-21T00:00:00.000+00:00" | hasSize(1)            |
      | Pref 2     | 1902-05-21T00:00:00.000+00:00 | preferences == "Pref 2"                | hasSize(1)            |
      | Pref 3     | 1903-05-21T00:00:00.000+00:00 | preferences != "Pref 3"                | hasSizeGreaterThan(3) |

  Scenario: Paged resources

    Resource responses can be paged

    When Trevor retrieves profiles from the test service
    Then the last response body should contain
      | field | type | expectation           |
      | items | List | hasSizeGreaterThan(3) |
    When Trevor retrieves profiles from the test service with
      | size | 2 |
    Then the last response body should contain
      | field | type | expectation |
      | items | List | hasSize(2)  |
    When Trevor retrieves profiles from the test service with
      | size | 2 |
      | page | 1 |
    Then the 1st of the profiles in the 2nd response should not be equal to the 1st of the profiles in the 3rd response

  Scenario: Ordered resources

    Resource responses can be ordered

    When Trevor retrieves profiles from the test service with
      | sort | dob,asc |
    When Trevor retrieves profiles from the test service with
      | sort | dob,desc |
    Then the 1st of the profiles in the 1st response should not be equal to the 1st of the profiles in the 2nd response

  Scenario: Create resource

    Create a new resource from the payload in the file.

    When Trevor creates profiles from the file 'data/profiles2.json' in the test service

  Scenario: Update a resource

    Update the specific resource with the given identifier

    Given the initial profiles are
      """
      {
        "tenantId": "b7e813a2-bb28-11ec-8422-0242ac120002",
        "preferences": "Valid preference",
        "bio": "Valid bio",
        "phoneNumber": "0133 3245 392",
        "dob": "1975-02-28T00:00:00.000+00:00",
        "firstRelease": "1989-05-21T00:00:00.000+00:00"
      }
      """
    And Trevor creates the initial profiles in the test service
    When the updated profiles are
      """
      {
        "tenantId": "b7e813a2-bb28-11ec-8422-0242ac120002",
        "preferences": "Valid preference",
        "bio": "Updated bio",
        "phoneNumber": "0133 3245 392",
        "dob": "1975-02-28T00:00:00.000+00:00",
        "firstRelease": "1989-05-21T00:00:00.000+00:00"
      }
      """
    And Trevor updates the 1st of the profiles in the last response with the updated profiles
    Then the 1st of the profiles in the last response should contain
      | field        | type    | expectation                                       |
      | tenantId     | String  | isEqualTo("b7e813a2-bb28-11ec-8422-0242ac120002") |
      | preferences  | String  | isEqualTo("Valid preference")                     |
      | bio          | String  | isEqualTo("Updated bio")                          |
      | phoneNumber  | String  | isEqualTo("0133 3245 392")                        |
      | dob          | Instant | isEqualTo("1975-02-28T00:00:00.000+00:00")        |
      | firstRelease | Instant | isEqualTo("1989-05-21T00:00:00.000+00:00")        |


  Scenario: Delete a resource

    Delete one resource using its identifier

    Given Trevor creates profiles from the file 'data/profiles2.json' in the test service
    When Trevor deletes the 1st of the profiles in the last response from the test service
    Then the last response should have a status code of 200

# Need to think about related resources too
