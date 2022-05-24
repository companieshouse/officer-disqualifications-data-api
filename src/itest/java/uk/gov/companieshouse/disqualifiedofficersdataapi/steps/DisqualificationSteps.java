package uk.gov.companieshouse.disqualifiedofficersdataapi.steps;

import java.io.IOException;
import java.util.List;

import com.github.dockerjava.api.exception.InternalServerErrorException;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import uk.gov.companieshouse.disqualifiedofficersdataapi.api.DisqualifiedOfficerApiService;
import uk.gov.companieshouse.disqualifiedofficersdataapi.api.ResourceChangedRequest;
import uk.gov.companieshouse.disqualifiedofficersdataapi.config.CucumberContext;
import uk.gov.companieshouse.disqualifiedofficersdataapi.exceptions.ServiceUnavailableException;
import uk.gov.companieshouse.disqualifiedofficersdataapi.model.DisqualificationDocument;
import uk.gov.companieshouse.disqualifiedofficersdataapi.repository.DisqualifiedOfficerRepository;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static uk.gov.companieshouse.disqualifiedofficersdataapi.config.AbstractMongoConfig.mongoDBContainer;

public class DisqualificationSteps {

    @Autowired
    public DisqualifiedOfficerApiService disqualifiedApiService;

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    private DisqualifiedOfficerRepository repository;

    @Given("disqualified officers data api service is running")
    public void theApplicationRunning() {
        assertThat(restTemplate).isNotNull();
    }

    @Given("the disqualification database is down")
    public void the_disqualification_db_is_down() {
        mongoDBContainer.stop();
    }

    @When("CHS kafka API service is unavailable")
    public void chs_kafka_service_unavailable() throws IOException {
        doThrow(ServiceUnavailableException.class)
            .when(disqualifiedApiService).invokeChsKafkaApi(any(ResourceChangedRequest.class));
    }

    @When("the api throws an internal server error")
    public void api_throws_an_internal_service_error() throws IOException {
        doThrow(InternalServerErrorException.class)
            .when(disqualifiedApiService).invokeChsKafkaApi(any(ResourceChangedRequest.class));
    }

    @Then("the CHS Kafka API is not invoked")
    public void chs_kafka_api_not_invoked() throws IOException {
        verify(disqualifiedApiService, times(0)).invokeChsKafkaApi(any(ResourceChangedRequest.class));
    }

    @Then("the CHS Kafka API is invoked successfully with {string}")
    public void chs_kafka_api_invoked(String officerId) {
        verify(disqualifiedApiService).invokeChsKafkaApi(new ResourceChangedRequest(
            CucumberContext.CONTEXT.get("contextId"),
            officerId,
            CucumberContext.CONTEXT.get("officerType")
        ));
    }

    @Then("nothing is persisted in the database")
    public void nothing_persisted_to_database() {
        List<DisqualificationDocument> disqDoc = repository.findAll();
        Assertions.assertThat(disqDoc).hasSize(0);
    }

    @Then("I should receive {int} status code")
    public void i_should_receive_status_code(Integer statusCode) {
        int expectedStatusCode = CucumberContext.CONTEXT.get("statusCode");
        Assertions.assertThat(expectedStatusCode).isEqualTo(statusCode);
    }

}
