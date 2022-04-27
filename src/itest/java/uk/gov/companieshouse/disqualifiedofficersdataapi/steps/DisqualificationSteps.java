package uk.gov.companieshouse.disqualifiedofficersdataapi.steps;

import io.cucumber.java.en.Then;
import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.companieshouse.disqualifiedofficersdataapi.api.DisqualifiedOfficerApiService;
import uk.gov.companieshouse.disqualifiedofficersdataapi.config.CucumberContext;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

public class DisqualificationSteps {

    @Autowired
    public DisqualifiedOfficerApiService disqualifiedApiService;

    @Then("the CHS Kafka API is invoked successfully with {string}")
    public void chs_kafka_api_invoked(String officerId) throws IOException {
        verify(disqualifiedApiService).invokeChsKafkaApi(any(), eq(officerId),any());
    }

    @Then("I should receive {int} status code")
    public void i_should_receive_status_code(Integer statusCode) {
        int expectedStatusCode = CucumberContext.CONTEXT.get("statusCode");
        Assertions.assertThat(expectedStatusCode).isEqualTo(statusCode);
    }

}
