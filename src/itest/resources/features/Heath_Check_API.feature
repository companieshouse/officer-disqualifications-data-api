Feature: Health check API endpoint

  Scenario: Client invokes GET /healthcheck endpoint
    Given disqualified officers data api service is running

    When the client invokes '/healthcheck' endpoint

    Then the client receives status code of 200