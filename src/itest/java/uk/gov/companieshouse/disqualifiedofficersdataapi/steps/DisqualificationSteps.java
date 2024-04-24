package uk.gov.companieshouse.disqualifiedofficersdataapi.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import uk.gov.companieshouse.disqualifiedofficersdataapi.api.DisqualifiedOfficerApiService;
import uk.gov.companieshouse.disqualifiedofficersdataapi.api.ResourceChangedRequest;
import uk.gov.companieshouse.disqualifiedofficersdataapi.config.CucumberContext;
import uk.gov.companieshouse.disqualifiedofficersdataapi.exceptions.ServiceUnavailableException;
import uk.gov.companieshouse.disqualifiedofficersdataapi.model.DisqualificationDocument;
import uk.gov.companieshouse.disqualifiedofficersdataapi.model.DisqualificationResourceType;
import uk.gov.companieshouse.disqualifiedofficersdataapi.repository.DisqualifiedOfficerRepository;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.companieshouse.disqualifiedofficersdataapi.config.AbstractMongoConfig.mongoDBContainer;

public class DisqualificationSteps {

    private String officerId;

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

    @When("I send DELETE request with officer id {string}")
    public void send_delete_request_for_officer(String officerId) throws IOException {
        String uri = "/disqualified-officers/delete/{officer_id}/internal";

        HttpHeaders headers = new HttpHeaders();
        CucumberContext.CONTEXT.set("contextId", "5234234234");
        this.officerId = officerId;
        CucumberContext.CONTEXT.set("officerType", DisqualificationResourceType.NATURAL);
        headers.set("x-request-id", CucumberContext.CONTEXT.get("contextId"));
        headers.set("ERIC-Identity", "TEST-IDENTITY");
        headers.set("ERIC-Identity-Type", "KEY");
        headers.set("ERIC-Authorised-Key-Privileges", "internal-app");

        HttpEntity<String> request = new HttpEntity<String>(null, headers);

        ResponseEntity<Void> response = restTemplate.exchange(uri, HttpMethod.DELETE, request, Void.class, officerId);

        CucumberContext.CONTEXT.set("statusCode", response.getStatusCode().value());
    }

    @When("officer id does not exists for {string}")
    public void officer_does_not_exists(String officerId) {
        Assertions.assertThat(repository.existsById(officerId)).isFalse();
    }

    @Then("the CHS Kafka API is not invoked")
    public void chs_kafka_api_not_invoked() throws IOException {
        verify(disqualifiedApiService, times(0)).invokeChsKafkaApi(any(ResourceChangedRequest.class));
    }

    @Then("the CHS Kafka API is invoked with {string}")
    public void chs_kafka_api_invoked(String officerId) {
        boolean isDelete = officerId.equals("id_to_delete") ? true : false;

        verify(disqualifiedApiService).invokeChsKafkaApi(new ResourceChangedRequest(
            CucumberContext.CONTEXT.get("contextId"),
            officerId,
            CucumberContext.CONTEXT.get("officerType"),
            isDelete ? CucumberContext.CONTEXT.get("disqualificationData") : null,
            isDelete
        ));
    }

    @Then("nothing is persisted in the database")
    public void nothing_persisted_to_database() {
        List<DisqualificationDocument> disqDoc = repository.findAll();
        Assertions.assertThat(disqDoc).isEmpty();
    }

    @Then("I should receive {int} status code")
    public void i_should_receive_status_code(Integer statusCode) {
        int expectedStatusCode = CucumberContext.CONTEXT.get("statusCode");
        Assertions.assertThat(expectedStatusCode).isEqualTo(statusCode);
    }

    @Then("the disqualified officer with officer id {string} still exists in the database")
    public void disqualified_officer_exists(String officerId) {
        Assertions.assertThat(repository.existsById(officerId)).isTrue();
    }

}
