@JpaRestApi
Feature: Integration of JPA Rest Service - Get, Save, Update and Delete

  Scenario: Test GET call
    When as a tester I call the "RETRIEVE" "jparestapi" endpoint with "b7e813a2-bb28-11ec-8422-0242ac120002" and the parameter "records"
    Then I should get 200 back

  Scenario: Test DELETE call
    When as a tester I call the "REMOVE" "<Entity>" endpoint with "b7e813a2-bb28-11ec-8422-0242ac120002" and the parameter "jparestapi-records"
    Then I should get 200 back

