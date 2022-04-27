Feature: Process corporate disqualified officer information

  Scenario Outline: Processing corporate disqualified officer information successfully

    Given disqualified officers data api service is running
    When I send corporate PUT request with payload "<data>" file
    Then I should receive 200 status code
    And the CHS Kafka API is invoked successfully with "<officerId>"

    Examples:
      | data                                  | officerId     |
      | corporate_disqualified_officer        | 1234567891    |

  Scenario Outline: Retrieve corporate disqualified officer information successfully

    Given disqualified officers data api service is running
    And the corporate disqualified officer information exists for "<officerId>"
    When I send corporate GET request with officer Id "<officerId>"
    Then I should receive 200 status code
    And the corporate Get call response body should match "<result>" file

    Examples:
      | officerId     | result                                    |
      | 1234567891    | retrieve_corporate_disqualified_officer   |
