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

  Scenario Outline: Test Update call
    Given "<IdValue>" has been assigned
    And A json file is created: "<RequestPayload>"
    When As a tester I call the "update" "<Entity>" endpoint with query parameter key: "<QueryParameterKey>" and value: "<QueryParameterValue>" with URL parameter: "savedValue"
    Then A 200 status code is returned
    And The "items.id" value from the "Json Array" response is saved as: "<IdValue>"
    And A json file is created: "<ExpectedResponse>"
    And I check that the return response is correct

    Examples:
      | Entity    | RequestPayload                                               | IdValue      | ExpectedResponse                                              | QueryParameterKey | QueryParameterValue                  |
      | profiles  | src/test/java/payloads/request/update/profiles-update.json   | idValueOne   | src/test/java/payloads/response/update/profiles-update.json   | tenantId          | b7e813a2-bb28-11ec-8422-0242ac120002 |
      | concerts  | src/test/java/payloads/request/update/concerts-update.json   | idValueTwo   | src/test/java/payloads/response/update/concerts-update.json   | tenantId          | b7e813a2-bb28-11ec-8422-0242ac120002 |
      | artists   | src/test/java/payloads/request/update/artists-update.json    | idValueThree | src/test/java/payloads/response/update/artists-update.json    | tenantId          | b7e813a2-bb28-11ec-8422-0242ac120002 |
      | records   | src/test/java/payloads/request/update/records-update.json    | idValueFour  | src/test/java/payloads/response/update/records-update.json    | tenantId          | b7e813a2-bb28-11ec-8422-0242ac120002 |


  Scenario Outline: Test Remove call
    Given "<IdValue>" has been assigned
    When As a tester I call the "remove" "<Entity>" endpoint with query parameter key: "<QueryParameterKey>" and value: "<QueryParameterValue>" with URL parameter: "savedValue"
    Then A 200 status code is returned

    Examples:
      | Entity    | IdValue      | QueryParameterKey | QueryParameterValue                  |
      | profiles  | idValueOne   | tenantId         | b7e813a2-bb28-11ec-8422-0242ac120002 |
      | concerts  | idValueTwo   | tenantId         | b7e813a2-bb28-11ec-8422-0242ac120002 |
      | artists   | idValueThree | tenantId         | b7e813a2-bb28-11ec-8422-0242ac120002 |
      | records   | idValueFour  | tenantId         | b7e813a2-bb28-11ec-8422-0242ac120002 |
