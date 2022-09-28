Feature: Interpolated payloads

    When payloads are created they are interpolated.
  This enables payloads to reference resources created
  during a scenario and even personas.

  The value to interpolate are enclosed in #{...}

  For example, if I define a persona called Trevor I
  can reference the persona with

  "#{personaManager.getPersona('Trevor').id}"

  For Example

  {
  "personId": "#{personaManager.getPersona('Trevor').id}",
  "startTime": "2022-01-01T09:00Z",
  "endTime": "2022-01-01T17:00Z"
  }

  Interpolation uses SpEL and evaluates them with a BeanFactoryResolver
  https://docs.spring.io/spring-framework/docs/3.2.x/spring-framework-reference/html/expressions.html

  Scenario: Reference a persona in an inline payload

    In this scenario we will create a resource and
    populate one of the fields with the identifier from
    Trevor.

    Given Trevor is a user
    And the valid profiles are
      """
      {
        "tenantId": "b7e813a2-bb28-11ec-8422-0242ac120002",
        "preferences": "Valid preference",
        "bio": "#{personaManager.getPersona('Trevor').id}",
        "phoneNumber": "0133 3245 392",
        "dob": "1975-02-28T00:00:00.000+00:00",
        "firstRelease": "#{T(java.time.Instant).now().toString()}"
      }
      """
    When the Trevor creates the valid profiles in the test service
    Then the last response should have a status code of 200
    Then the 1st of the profiles in the last response should contain
      | field        | type    | expectation                                               |
      | bio          | String  | isNotNull()                                               |
      | bio          | String  | isNotEqualTo("#{personaManager.getPersona('Trevor').id}") |
      | firstRelease | Instant | isNotNull()                                               |

  Scenario: Reference a persona in an file payload

    In this scenario we will read a resource from a file and
    populate one of the fields with the identifier from
    Trevor.

    Given Trevor is a user
    When Trevor creates profiles from the file 'data/interpolatedProfile.json' in the test service
    Then the last response should have a status code of 200
    Then the 1st of the profiles in the last response should contain
      | field        | type    | expectation                                               |
      | bio          | String  | isNotNull()                                               |
      | bio          | String  | isNotEqualTo("#{personaManager.getPersona('Trevor').id}") |
      | firstRelease | Instant | isNotNull()                                               |
