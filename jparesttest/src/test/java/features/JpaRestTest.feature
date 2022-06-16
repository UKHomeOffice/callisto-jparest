@JpaRestApi
Feature: Integration of JPA Rest Service - Get, Save, Update and Delete

  Scenario: Test GET call
    When as a tester I call the "RETRIEVE" "records" endpoint with "tenantId" value "b7e813a2-bb28-11ec-8422-0242ac120002" and the parameter "records"
    Then I should get 200 back

#  Scenario: Test DELETE call
#    When as a tester I call the "REMOVE" "jparestapi" endpoint with "tenantId" value "b7e813a2-bb28-11ec-8422-0242ac120002" and the parameter "recordId-dynamic"
#    Then I should get 200 back

    #    And The "items.id" value from the response "Array" is saved

