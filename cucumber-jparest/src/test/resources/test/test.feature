Feature: Test the test library

  Scenario: Kebab case resource type should be allowed

    If resource type is composed of more than one word, the convention is to use kebab-case.

    Given the valid person-profiles are
      """
      {}
      """
    # The following step definition is only accessible to internal tests of cucumber-jparest
    Then parse payload successfully
