Feature: Delete disqualification information

  Scenario: Delete disqualified officer successfully

    Given disqualified officers data api service is running
    And the natural disqualified officer information exists for "id_to_delete"
    When I send DELETE request with officer id "id_to_delete"
    Then I should receive 200 status code
    And the CHS Kafka API is invoked successfully with "id_to_delete"

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
    Then I should receive 404 status code
    And the CHS Kafka API is not invoked

  Scenario: Processing delete disqualified officer when kafka-api is not available

    Given disqualified officers data api service is running
    And the natural disqualified officer information exists for "id_to_delete"
    And CHS kafka API service is unavailable
    When I send DELETE request with officer id "id_to_delete"
    Then I should receive 503 status code
    And the CHS Kafka API is invoked successfully with "id_to_delete"
    And the disqualified officer with officer id "id_to_delete" still exists in the database

