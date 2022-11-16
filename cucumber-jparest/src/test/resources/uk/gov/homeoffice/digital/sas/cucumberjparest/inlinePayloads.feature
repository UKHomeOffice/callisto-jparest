Feature: Inline Payloads

  Inline payloads allows for the body of a subsequent request to
  be specified directly in the feature file.
  The specified body supports interpolation.

  Interpolation occurs last and allows for strings within the
  payload to be interpolated at runtime using the syntax
  `#{code}` where code can be either a variable or executable
  code that calls other methods.

  To use a payload after declaring it simple reference it by name.
  If interacting with a resource endpoint the name of the
  variable must match the plural name of the resource.

  Background:
    Given the tester is a user

  Scenario: Simple inline payload

    For a small payload you may wish to declare the payload within
    the scenario.

    Given the valid profiles are
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
    And the invalid profiles are
      """
      {
        "tenantId": "b7e813a2-bb28-11ec-8422-0242ac120002",
        "preferences": "Valid preference",
        "bio": "Valid bio",
        "phoneNumber": "0133 3245 392",
        "dob": "Invalid date",
        "firstRelease": "Invalid date"
      }
      """
    When the tester creates the valid profiles in the test service
    Then the last response should have a status code of 200
    When the tester creates the invalid profiles in the test service
    Then the last response should have a status code of 400

  Scenario: Kebab case resource type should be allowed

    If resource type is composed of more than one word, the convention is to use Kebab Case.

    Given the valid person-profiles are
      """
      {}
      """
