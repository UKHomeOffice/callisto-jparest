Feature: Personas

  This feature enables authors to write automated tests in the context
  of the agile personas used in their stories.

  It will create completely new users for total scenario isolation.
  Personas only live for the context of a single scenario, so when using
  scenario outlines the context of the persona will be unique for each
  example row.

  Scenario: Defining a persona

    Personas can be created with any name and will be unique

    Given Trevor is a user
    And Paula is a user
    Then Trevor is a different persona to Paula

  Scenario: Defining a persona as a noun

    Usually personas are proper nouns but the step also allows the persona
    name to be prefixed with "the" in order to use a noun instead of a
    proper noun

    Given the tester is a user
    And the admin is a user


  Scenario: Anonymous access

    Sometimes scenarios will need to be performed for unuthenticated
    resources or to test authentication/authorisation. In order to
    achieve this a single well known persona exists for making
    annonymous requests. The persona is called `someone`

    Given the valid profiles are loaded from the file 'data/profiles.json'
    When someone creates the valid profiles in the test service
    Then the last response should have a status code of 200
