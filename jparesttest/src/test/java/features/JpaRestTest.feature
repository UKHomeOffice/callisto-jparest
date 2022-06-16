@JpaRestApi
Feature: Integration of JPA Rest Service - Get, Save, Update and Delete

  Scenario: Test GET call
    When I call the "GET" "jparestapi" endpoint with "b7e813a2-bb28-11ec-8422-0242ac120002" and parameter "records"
    Then I should get 200 back

