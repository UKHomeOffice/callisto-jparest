Feature: Interpolated payloads

  When payloads are loaded from files they are interpolated.
  This enables payloads to reference resources created
  during a scenario and even personas.

  For example, if I define a persona called Trevor and then
  create a time entry for George within a scenario using
  the predefined steps I can use the following payload

  {
    "personId": "#{users['George']['personId']}",
    "tenantId": "#{CallistoClient.owner}:tenantId",
    "startTime": "2022-01-01T09:00Z",
    "endTime": "2022-01-01T17:00Z"
  }

Background:
  Given the tester is a user
  Given the viewer is a user

Scenario: Reference a persona

  In this scenario we will create a resource and
  populate one of the fields with the identifier from
  the viewer.

  When the tester successfully stores profiles from the file './features/data/wip/userProfile/userProfileForViewer.json' in the test service
  Then the first of the profiles in the last response should contain
    | field       | type    | expect | match                          |
    | description | String  | to     | eq users['viewer']['personId'] |

