@JpaRestApi
Feature: Integration of JPA Rest Service - Get, Save, Update and Delete

  Scenario: Test Retrieve call
    When As a tester I call the "retrieve" "profiles" endpoint with query parameter key: "tenantId" and value: "b7e813a2-bb28-11ec-8422-0242ac120002"
    Then A 200 status code is returned
    And The "items.id" value from the "Json Array" response is saved
    And A json file is created: "src/test/java/payloads/response/retrieve/profiles-all.json"
    And I check that the return response is correct

  Scenario Outline: Test Save call
    Given A json file is created: "<RequestPayload>"
    When As a tester I call the "save" "<Entity>" endpoint with query parameter key: "<QueryParameterKey>" and value: "<QueryParameterValue>"
    Then A 200 status code is returned
    And The "items.id" value from the "Json Array" response is saved
    And A json file is created: "<ExpectedResponse>"
    And I check that the return response is correct

    Examples:
      | Entity   | RequestPayload                                         | QueryParameterKey | QueryParameterValue                  | ExpectedResponse                                           |
      | profiles | src/test/java/payloads/request/save/profiles-save.json | tenantId          | b7e813a2-bb28-11ec-8422-0242ac120002 | src/test/java/payloads/response/save/profiles-save.json     |
#      | concerts | src/test/java/payloads/request/save/concerts-save.json | tenantId          | b7e813a2-bb28-11ec-8422-0242ac120002 | src/test/java/payloads/response/save/concerts-save.json     |
#      | artists  | src/test/java/payloads/request/save/artists-save.json  | tenantId          | b7e813a2-bb28-11ec-8422-0242ac120002 | src/test/java/payloads/response/save/post/artists-save.json |
#      | records  | src/test/java/payloads/request/save/records-save.json  | tenantId          | b7e813a2-bb28-11ec-8422-0242ac120002 | src/test/java/payloads/response/save/records-save.json      |

  Scenario: Test Update call
    Given A json file is created: "src/test/java/payloads/request/update/profiles-update.json"
    When As a tester I call the "update" "profiles" endpoint with query parameter key: "tenantId" and value: "b7e813a2-bb28-11ec-8422-0242ac120002" with URL parameter: "savedValue"
    Then A 200 status code is returned
    And The "items.id" value from the "Json Array" response is saved
    And A json file is created: "src/test/java/payloads/response/update/profiles-update.json"
    And I check that the return response is correct

  Scenario: Test Remove call
    When As a tester I call the "remove" "profiles" endpoint with query parameter key: "tenantId" and value: "b7e813a2-bb28-11ec-8422-0242ac120002" with URL parameter: "savedValue"
    Then A 200 status code is returned

