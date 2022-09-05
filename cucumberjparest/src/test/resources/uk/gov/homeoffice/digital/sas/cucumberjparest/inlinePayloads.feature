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

    When time-entries are created as
      """
      {
        "personId": "#{users['George']['personId']}",
        "tenantId": "#{CallistoClient.owner}:tenantId",
        "startTime": "2022-01-01T09:00Z",
        "endTime": "2022-01-01T17:00Z"
      }
      """
    Then the time-entries payload should contain
      | field     | type   | expect | match         |
      | bookmarks | Array  | to     | have(1).items |
    And the tester creates the time-entries in the demo service
