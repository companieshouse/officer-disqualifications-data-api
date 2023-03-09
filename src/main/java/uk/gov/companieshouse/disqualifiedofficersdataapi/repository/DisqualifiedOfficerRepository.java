package uk.gov.companieshouse.disqualifiedofficersdataapi.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.gov.companieshouse.disqualifiedofficersdataapi.model.DisqualificationDocument;

@Repository
public interface DisqualifiedOfficerRepository extends MongoRepository<DisqualificationDocument, String> {
}
