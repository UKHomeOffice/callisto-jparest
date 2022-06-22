@JpaRestApi
Feature: Integration of JPA Rest Service - Get, Save, Update and Delete

  Scenario Outline: Test retrieve call - with no url parameters
    When As a tester I call the "retrieve" "<Entity>" endpoint with query parameter key: "<QueryParameterKey>" and value: "<QueryParamValue>"
    Then A 200 status code is returned
    And A json file is created: "<ExpectedResponse>"
    And I check that the return response is correct

    Examples:
      | Entity   | QueryParameterKey | QueryParamValue                      | ExpectedResponse                                           |
      | records  | tenantId          | b7e813a2-bb28-11ec-8422-0242ac120002 | src/test/java/payloads/response/retrieve/records-all.json  |
      | profiles | tenantId          | b7e813a2-bb28-11ec-8422-0242ac120002 | src/test/java/payloads/response/retrieve/profiles-all.json |
      | concerts | tenantId          | b7e813a2-bb28-11ec-8422-0242ac120002 | src/test/java/payloads/response/retrieve/concerts-all.json |
      | artists  | tenantId          | b7e813a2-bb28-11ec-8422-0242ac120002 | src/test/java/payloads/response/retrieve/artists-all.json  |

  Scenario Outline: Test retrieve call - with url parameters
    When As a tester I call the "retrieve" "<Entity>" endpoint with query parameter key: "<QueryParameterKey>" and value: "<QueryParamValue>" with URL parameter: "<ParamValue>"
    Then A 200 status code is returned
    And A json file is created: "<ExpectedResponse>"
    And I check that the return response is correct

    Examples:
      | Entity   | QueryParameterKey | QueryParamValue                      | ExpectedResponse                                                                | ParamValue                                         |
      | artists  | tenantId          | b7e813a2-bb28-11ec-8422-0242ac120002 | src/test/java/payloads/response/retrieve/artistsById.json                       | /2a7c7da4-bb29-11ec-8422-0242ac120006              |
      | profiles | tenantId          | b7e813a2-bb28-11ec-8422-0242ac120002 | src/test/java/payloads/response/retrieve/profilesById.json                      | /1a7c7da4-bb29-11ec-8422-0242ac120004              |
      | concerts | tenantId          | b7e813a2-bb28-11ec-8422-0242ac120002 | src/test/java/payloads/response/retrieve/concertsById.json                      | /3a7c7da4-bb29-11ec-8422-0242ac120002              |
      | records  | tenantId          | b7e813a2-bb28-11ec-8422-0242ac120002 | src/test/java/payloads/response/retrieve/recordsById.json                       | /47e813a2-bb28-11ec-8422-0242ac120001              |
      | concerts | tenantId          | b7e813a2-bb28-11ec-8422-0242ac120002 | src/test/java/payloads/response/retrieve/concerts-associated-artists.json       | /37e813a2-bb28-11ec-8422-0242ac120003/artists      |
      | artists  | tenantId          | b7e813a2-bb28-11ec-8422-0242ac120002 | src/test/java/payloads/response/retrieve/artists-filterById.json                | ?filter=id=='2a7c7da4-bb29-11ec-8422-0242ac120005' |
      | records  | tenantId          | b7e813a2-bb28-11ec-8422-0242ac120002 | src/test/java/payloads/response/retrieve/records-order-by-record-name-desc.json | ?sort=recordName,DESC                              |
      | records  | tenantId          | b7e813a2-bb28-11ec-8422-0242ac120002 | src/test/java/payloads/response/retrieve/records-get-first-5.json               | ?page=0&size=5                                     |
      | concerts | tenantId          | b7e813a2-bb28-11ec-8422-0242ac120002 | src/test/java/payloads/response/retrieve/concerts-filter-with-concert_name.json | ?filter=concertName=='Live Aid 2021'               |

  Scenario Outline: Test Save call
    Given A json file is created: "<RequestPayload>"
    When As a tester I call the "save" "<Entity>" endpoint with query parameter key: "<QueryParameterKey>" and value: "<QueryParameterValue>"
    Then A 200 status code is returned
    And The "items.id" value from the "Json Array" response is saved as: "<IdValue>"
    And "<IdValue>" has been assigned
    And A json file is created: "<ExpectedResponse>"
    And I check that the return response is correct

    Examples:
      | Entity   | RequestPayload                                         | IdValue      | QueryParameterKey | QueryParameterValue                  | ExpectedResponse                                            |
      | profiles | src/test/java/payloads/request/save/profiles-save.json | idValueOne   | tenantId          | b7e813a2-bb28-11ec-8422-0242ac120002 | src/test/java/payloads/response/save/profiles-save.json     |
      | concerts | src/test/java/payloads/request/save/concerts-save.json | idValueTwo   | tenantId          | b7e813a2-bb28-11ec-8422-0242ac120002 | src/test/java/payloads/response/save/concerts-save.json     |
      | artists  | src/test/java/payloads/request/save/artists-save.json  | idValueThree | tenantId          | b7e813a2-bb28-11ec-8422-0242ac120002 | src/test/java/payloads/response/save/artists-save.json      |
      | records  | src/test/java/payloads/request/save/records-save.json  | idValueFour  | tenantId          | b7e813a2-bb28-11ec-8422-0242ac120002 | src/test/java/payloads/response/save/records-save.json      |

#  Scenario: Test Update call
#    Given A json file is created: "src/test/java/payloads/request/update/profiles-update.json"
#    When As a tester I call the "update" "profiles" endpoint with query parameter key: "tenantId" and value: "b7e813a2-bb28-11ec-8422-0242ac120002" with URL parameter: "savedValue"
#    Then A 200 status code is returned
#    And The "items.id" value from the "Json Array" response is saved
#    And A json file is created: "src/test/java/payloads/response/update/profiles-update.json"
#    And I check that the return response is correct
#
#  Scenario: Test Remove call
#    When As a tester I call the "remove" "profiles" endpoint with query parameter key: "tenantId" and value: "b7e813a2-bb28-11ec-8422-0242ac120002" with URL parameter: "savedValue"
#    Then A 200 status code is returned

