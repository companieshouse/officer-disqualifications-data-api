Feature: Delete disqualification information

  Scenario: Delete disqualified officer successfully

    Given disqualified officers data api service is running
    And the natural disqualified officer information exists for "id_to_delete"
    When I send DELETE request with officer id "id_to_delete"
    Then I should receive 200 status code
    And the CHS Kafka API is invoked with "id_to_delete"

  Scenario: Delete disqualified officer information while database is down

    Given disqualified officers data api service is running
    And the natural disqualified officer information exists for "1234567890"
    And the disqualification database is down
    When I send DELETE request with officer id "1234567891"
    Then I should receive 503 status code
    And the CHS Kafka API is not invoked

  Scenario: Delete disqualified officer information not found

    Given disqualified officers data api service is running
    When I send DELETE request with officer id "does_not_exist"
    And officer id does not exists for "does_not_exist"
    Then I should receive 200 status code
    And the CHS Kafka API is invoked with "does_not_exist" with null data

  Scenario: Delete disqualified officer when kafka-api is not available

    Given disqualified officers data api service is running
    And the natural disqualified officer information exists for "id_to_delete"
    And CHS kafka API service is unavailable
    When I send DELETE request with officer id "id_to_delete"
    Then I should receive 503 status code
    And the disqualified officer with officer id "id_to_delete" does not exist in the database

  Scenario: Delete disqualified officer with stale delta at

    Given disqualified officers data api service is running
    And the natural disqualified officer information exists for "id_to_delete"
    When I send DELETE request with officer id "id_to_delete" with a stale delta at
    Then I should receive 409 status code
    And the CHS Kafka API is not invoked

  Scenario: Delete disqualified officer with invalid officer type

    Given disqualified officers data api service is running
    And the natural disqualified officer information exists for "id_to_delete"
    When I send DELETE request with an invalid officer_type and officer id "id_to_delete"
    Then I should receive 400 status code
    And the CHS Kafka API is not invoked
