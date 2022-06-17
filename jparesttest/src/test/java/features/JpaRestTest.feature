@JpaRestApi
Feature: Integration of JPA Rest Service - Get, Save, Update and Delete

  Scenario: Test GET call
    When As a tester I call the "RETRIEVE" "profiles" endpoint with "tenantId" value "b7e813a2-bb28-11ec-8422-0242ac120002" and the parameter "profiles"
    Then I should get 200 back

  Scenario: Test POST call
    And A json file for "profiles" endpoint is created: "src/test/java/payloads/save/profiles-save.json"
    When As a tester I call the "SAVE" "profiles" endpoint with "tenantId" value "b7e813a2-bb28-11ec-8422-0242ac120002" and the parameter "profiles"
    Then I should get 200 back
    And The "items.id" value from the response "Array" is saved

  Scenario: Test PUT call
    And I define the "profiles" request json with values "src/test/java/payloads/update/profiles-update.json"
    When As a tester I call the "UPDATE" "profiles" endpoint with "tenantId" value "b7e813a2-bb28-11ec-8422-0242ac120002" and the parameter "profiles"
    Then I should get 200 back
    And The "items.id" value from the response "Array" is saved

#  Scenario: Test DELETE call
#    When As a tester I call the "REMOVE" "profiles" endpoint with "tenantId" value "b7e813a2-bb28-11ec-8422-0242ac120002" and the parameter "profilesId-dynamic"
#    Then I should get 200 back
