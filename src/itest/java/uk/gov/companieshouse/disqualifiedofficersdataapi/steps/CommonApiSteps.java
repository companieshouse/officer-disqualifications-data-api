package uk.gov.companieshouse.disqualifiedofficersdataapi.steps;

import io.cucumber.java.en.Given;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

public class CommonApiSteps {

    private ResponseEntity<String> lastResponse;

    @Autowired
    protected TestRestTemplate restTemplate;

    @Given("disqualified officers data api service is running")
    public void theApplicationRunning() {
        assertThat(restTemplate).isNotNull();
        lastResponse = null;
    }

}
