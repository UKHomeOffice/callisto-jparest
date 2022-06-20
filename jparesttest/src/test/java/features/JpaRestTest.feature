@JpaRestApi
Feature: Integration of JPA Rest Service - Get, Save, Update and Delete

  Scenario: Test Retrieve call
    When As a tester I call the "retrieve" "profiles" endpoint with query parameter key: "tenantId" and value: "b7e813a2-bb28-11ec-8422-0242ac120002"
    Then A 200 status code is returned

  Scenario: Test Save call
    Given A json file for "profiles" endpoint is created: "src/test/java/payloads/save/profiles-save.json"
    When As a tester I call the "save" "profiles" endpoint with query parameter key: "tenantId" and value: "b7e813a2-bb28-11ec-8422-0242ac120002"
    Then A 200 status code is returned
    And The "items.id" value from the "Json Array" response is saved

  Scenario: Test Update call
    Given A json file for "profiles" endpoint is created: "src/test/java/payloads/update/profiles-update.json"
    When As a tester I call the "update" "profiles" endpoint with query parameter key: "tenantId" and value: "b7e813a2-bb28-11ec-8422-0242ac120002" with URL parameter: "savedValue"
    Then A 200 status code is returned
    And The "items.id" value from the "Json Array" response is saved

  Scenario: Test Remove call
    When As a tester I call the "remove" "profiles" endpoint with query parameter key: "tenantId" and value: "b7e813a2-bb28-11ec-8422-0242ac120002" with URL parameter: "savedValue"
    Then A 200 status code is returned
