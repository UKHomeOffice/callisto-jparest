@JpaRestApi
Feature: Integration of JPA Rest Service - Get, Save, Update and Delete

  Scenario: Test GET call
    When As a tester I call the "RETRIEVE" "profiles" endpoint with "tenantId" value "b7e813a2-bb28-11ec-8422-0242ac120002"
    Then A 200 status code is returned

  Scenario: Test POST call
    And A json file for "profiles" endpoint is created: "src/test/java/payloads/save/profiles-save.json"
    When As a tester I call the "SAVE" "profiles" endpoint with "tenantId" value "b7e813a2-bb28-11ec-8422-0242ac120002"
    Then A 200 status code is returned
    And The "items.id" value from the "Json Array" response is saved

  Scenario: Test PUT call
    And A json file for "profiles" endpoint is created: "src/test/java/payloads/update/profiles-update.json"
    When As a tester I call the "UPDATE" "profiles" endpoint with "tenantId" value "b7e813a2-bb28-11ec-8422-0242ac120002" and the parameter "savedValue"
    Then A 200 status code is returned
    And The "items.id" value from the "Json Array" response is saved

  Scenario: Test DELETE call
    When As a tester I call the "REMOVE" "profiles" endpoint with "tenantId" value "b7e813a2-bb28-11ec-8422-0242ac120002" and the parameter "savedValue"
    Then A 200 status code is returned
