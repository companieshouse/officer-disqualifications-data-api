package uk.gov.companieshouse.disqualifiedofficersdataapi.repository;

import java.time.OffsetDateTime;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.companieshouse.api.disqualification.NaturalDisqualificationApi;
import uk.gov.companieshouse.disqualifiedofficersdataapi.config.AbstractMongoConfig;
import uk.gov.companieshouse.disqualifiedofficersdataapi.model.NaturalDisqualificationDocument;

@Testcontainers
@DataMongoTest(excludeAutoConfiguration = EmbeddedMongoAutoConfiguration.class)
public class RepositoryITest extends AbstractMongoConfig {

  @Autowired
  private NaturalDisqualifiedOfficerRepository naturalRepository;

  @BeforeAll
  static void setup(){
    mongoDBContainer.start();
  }

  @Test
  void should_save_and_retrieve_disqualified_officer_data() {

    NaturalDisqualificationDocument disqualificationDocument = createDisqualificationDocument("1234567890");
    naturalRepository.save(disqualificationDocument);

    Assertions.assertThat(naturalRepository.findById("1234567890")).isNotEmpty();
  }

  private NaturalDisqualificationDocument createDisqualificationDocument(String officerId) {
    NaturalDisqualificationDocument disqualificationDocument = new NaturalDisqualificationDocument();

    NaturalDisqualificationApi data = new NaturalDisqualificationApi();
    disqualificationDocument.setDeltaAt(OffsetDateTime.now().toString());
    disqualificationDocument.setId(officerId);

    disqualificationDocument.setData(data);

    return disqualificationDocument;
  }

  @AfterAll
  static void tear(){
    mongoDBContainer.stop();
  }

}
