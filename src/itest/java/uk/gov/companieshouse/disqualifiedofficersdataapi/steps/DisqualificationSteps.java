package uk.gov.companieshouse.disqualifiedofficersdataapi.steps;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.assertj.core.api.Assertions;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.*;
import org.springframework.util.FileCopyUtils;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.shaded.org.apache.commons.io.FileUtils;
import uk.gov.companieshouse.api.disqualification.NaturalDisqualificationApi;
import uk.gov.companieshouse.disqualifiedofficersdataapi.api.DisqualifiedOfficerApiService;
import uk.gov.companieshouse.disqualifiedofficersdataapi.config.CucumberContext;
import uk.gov.companieshouse.disqualifiedofficersdataapi.model.CorporateDisqualificationDocument;
import uk.gov.companieshouse.disqualifiedofficersdataapi.model.NaturalDisqualificationDocument;
import uk.gov.companieshouse.disqualifiedofficersdataapi.repository.CorporateDisqualifiedOfficerRepository;
import uk.gov.companieshouse.disqualifiedofficersdataapi.repository.DisqualifiedOfficerRepository;
import uk.gov.companieshouse.disqualifiedofficersdataapi.repository.NaturalDisqualifiedOfficerRepository;
import uk.gov.companieshouse.disqualifiedofficersdataapi.service.DisqualifiedOfficerService;
import uk.gov.companieshouse.disqualifiedofficersdataapi.transform.DisqualificationTransformer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static uk.gov.companieshouse.disqualifiedofficersdataapi.config.AbstractMongoConfig.mongoDBContainer;

public class DisqualificationSteps {

    private String officerId;
    private String contextId;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private NaturalDisqualifiedOfficerRepository naturalRepository;

    @Autowired
    private CorporateDisqualifiedOfficerRepository corporateRepository;

    @Autowired
    private DisqualifiedOfficerRepository repository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    public DisqualifiedOfficerApiService disqualifiedApiService;

    @Before
    public void dbCleanUp(){
        if (!mongoDBContainer.isRunning()) {
            mongoDBContainer.start();
        }
        repository.deleteAll();
        naturalRepository.deleteAll();
        corporateRepository.deleteAll();
    }

    @Given("the disqualified officer information exists for {string}")
    public void the_disqualification_information_exists_for(String officerId) throws IOException {
        File file = new ClassPathResource("/json/output/retrieve_natural_disqualified_officer.json").getFile();
        NaturalDisqualificationApi data = objectMapper.readValue(file, NaturalDisqualificationApi.class);
        NaturalDisqualificationDocument naturalDisqualification = new NaturalDisqualificationDocument();
        naturalDisqualification.setData(data);
        naturalDisqualification.setId(officerId);

        mongoTemplate.save(naturalDisqualification);
    }

    @When("I send natural GET request with officer Id {string}")
    public void i_send_natural_get_request_with_officer_id(String officerId) throws IOException {
        String uri = "/disqualified-officers/natural/{officerId}";
        ResponseEntity<NaturalDisqualificationApi> response = restTemplate.exchange(uri, HttpMethod.GET, null,
                NaturalDisqualificationApi.class, officerId);

        CucumberContext.CONTEXT.set("statusCode", response.getStatusCodeValue());
        CucumberContext.CONTEXT.set("getResponseBody", response.getBody());
    }


    @When("I send natural PUT request with payload {string} file")
    public void i_send_natural_put_request_with_payload(String dataFile) throws IOException {
        File file = new ClassPathResource("/json/input/" + dataFile + ".json").getFile();
        NaturalDisqualificationApi disqualification = objectMapper.readValue(file, NaturalDisqualificationApi.class);
        String data = readFile("src/itest/resources/json/input/natural_disqualified_officer.json");
        System.out.println("\n\nOver here " + data + "\n\n");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        this.contextId = "5234234234";
        headers.set("x-request-id", this.contextId);

        HttpEntity request = new HttpEntity(data, headers);
        String uri = "/disqualified-officers/natural/{officerId}/internal";
        String officerId = "1234567890";
        ResponseEntity<Void> response = restTemplate.exchange(uri, HttpMethod.PUT, request, Void.class, officerId);

        this.officerId = officerId;
        CucumberContext.CONTEXT.set("statusCode", response.getStatusCodeValue());
    }

    @Then("I should receive {int} status code")
    public void i_should_receive_status_code(Integer statusCode) {
        int expectedStatusCode = CucumberContext.CONTEXT.get("statusCode");
        Assertions.assertThat(expectedStatusCode).isEqualTo(statusCode);
    }

    @Then("the expected result should match {string} file")
    public void the_expected_result_should_match(String string) throws IOException {
        File file = new ClassPathResource("/json/output/" + string + ".json").getFile();

        List<NaturalDisqualificationDocument> naturalDocuments = naturalRepository.findAll();

        Assertions.assertThat(naturalDocuments).hasSize(1);

        Optional<NaturalDisqualificationDocument> actual = naturalRepository.findById(this.officerId);

        assertThat(actual.isPresent()).isTrue();

        NaturalDisqualificationDocument expected = objectMapper.readValue(file, NaturalDisqualificationDocument.class);

        NaturalDisqualificationDocument actualDocument = actual.get();

        // Verify that the time inserted is after the input
        assertThat(actualDocument.getUpdated().getAt()).isAfter(expected.getUpdated().getAt());

        // Matching both updatedAt since it will never match the output (Uses now time)
        LocalDateTime replacedLocalDateTime = LocalDateTime.now();
        expected.getUpdated().setAt(replacedLocalDateTime);
        actualDocument.getUpdated().setAt(replacedLocalDateTime);
        verifyPutData(actual.get(), expected);
    }

    @Then("the Get call response body should match {string} file")
    public void the_get_call_response_body_should_match(String dataFile) throws IOException {
        File file = new ClassPathResource("/json/output/" + dataFile + ".json").getFile();
        NaturalDisqualificationApi expected = objectMapper.readValue(file, NaturalDisqualificationApi.class);

        NaturalDisqualificationApi actual = CucumberContext.CONTEXT.get("getResponseBody");

        assertThat(expected.getSurname()).isEqualTo(actual.getSurname());
        assertThat(expected.getDisqualifications()).isEqualTo(actual.getDisqualifications());
    }

    @Then("the CHS Kafka API is invoked successfully")
    public void chs_kafka_api_invoked() throws IOException {
        verify(disqualifiedApiService).invokeChsKafkaApi(eq(this.contextId), eq(officerId),any());
    }

    private void verifyPutData(NaturalDisqualificationDocument actual, NaturalDisqualificationDocument expected) {
        NaturalDisqualificationApi actualDisqualification = actual.getData();
        NaturalDisqualificationApi expectedDisqualification = expected.getData();

        assertThat(actualDisqualification.getDisqualifications()).isEqualTo(expectedDisqualification.getDisqualifications());
        assertThat(actual.getId()).isEqualTo(expected.getId());
        assertThat(actual.getUpdated().getAt()).isEqualTo(expected.getUpdated().getAt());
        assertThat(actual.getDeltaAt()).isEqualTo(expected.getDeltaAt());
    }

    private static String readFile(String path) {
        String data;
        try {
            data = FileCopyUtils.copyToString(new InputStreamReader(new FileInputStream(new File(path))));
        } catch (IOException e) {
            System.out.println("exception thrown: " + e.getMessage());
            data = null;
        }
        return data;
    }

}
