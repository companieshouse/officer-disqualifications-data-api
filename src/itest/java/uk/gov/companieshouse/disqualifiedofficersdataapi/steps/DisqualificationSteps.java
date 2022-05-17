package uk.gov.companieshouse.disqualifiedofficersdataapi.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import uk.gov.companieshouse.disqualifiedofficersdataapi.api.DisqualifiedOfficerApiService;
import uk.gov.companieshouse.disqualifiedofficersdataapi.api.ResourceChangedRequest;
import uk.gov.companieshouse.disqualifiedofficersdataapi.config.CucumberContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

public class DisqualificationSteps {

    @Autowired
    public DisqualifiedOfficerApiService disqualifiedApiService;

    @Autowired
    protected TestRestTemplate restTemplate;

    @Given("disqualified officers data api service is running")
    public void theApplicationRunning() {
        assertThat(restTemplate).isNotNull();
    }

    @Then("the CHS Kafka API is invoked successfully with {string}")
    public void chs_kafka_api_invoked(String officerId) {
        verify(disqualifiedApiService).invokeChsKafkaApi(new ResourceChangedRequest(
                CucumberContext.CONTEXT.get("contextId"),
                officerId,
                CucumberContext.CONTEXT.get("officerType")
        ));
    }

    @Then("I should receive {int} status code")
    public void i_should_receive_status_code(Integer statusCode) {
        int expectedStatusCode = CucumberContext.CONTEXT.get("statusCode");
        Assertions.assertThat(expectedStatusCode).isEqualTo(statusCode);
    }

}
