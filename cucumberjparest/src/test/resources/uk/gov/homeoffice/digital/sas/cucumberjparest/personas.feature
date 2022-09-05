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
    Then Trevor is not Paula


Scenario: Anonymous access

  Sometimes scenarios will need to be performed for unuthenticated 
  resources or to test authentication/authorisation. In order to 
  achieve this a single well known persona exists for making 
  annonymous requests. The persona is called `someone`

  When someone stores users from the file './features/data/wip/login/valid-user.json' in the demo service
  Then the last response should have a status code of 401
