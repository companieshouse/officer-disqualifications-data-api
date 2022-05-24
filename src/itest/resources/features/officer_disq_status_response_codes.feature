Feature: Respone codes scenarios for disqualification officer

  Scenario Outline: Processing disqualified officers information unsuccessfully

    Given disqualified officers data api service is running
    When I send natural PUT request with payload "<data>" file
    And the CHS Kafka API is not invoked
    Then I should receive <response_code> status code
    And nothing is persisted in the database

    Examples:
        | data                | response_code |
        | bad_request_natural | 400           |

  Scenario: Processing disqualified officers information unsuccessfully after internal server error

    Given disqualified officers data api service is running
    When the api throws an internal server error
    And I send natural PUT request with payload "natural_disqualified_officer" file
    Then I should receive 500 status code

  Scenario Outline: Processing disqualified officers information unsuccessfully but saves to database

    Given disqualified officers data api service is running
    When CHS kafka API service is unavailable
    And I send natural PUT request with payload "<data>" file
    Then I should receive 503 status code
    And the natural Get call response body should match "<result>" file
    And the CHS Kafka API is invoked successfully with "<officerId>"

    Examples:
        | data                         | result                                | officerId  |
        | natural_disqualified_officer | retrieve_natural_disqualified_officer | 1234567890 |

  Scenario: Processing disqualified officers information while database is down

    Given disqualified officers data api service is running
    And the disqualification database is down
    When I send natural PUT request with payload "natural_disqualified_officer" file
    Then I should receive 503 status code
    And the CHS Kafka API is not invoked
