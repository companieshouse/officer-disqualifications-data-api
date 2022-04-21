package uk.gov.companieshouse.disqualifiedofficersdataapi.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.gov.companieshouse.disqualifiedofficersdataapi.model.CorporateDisqualificationDocument;


@Repository
public interface CorporateDisqualifiedOfficerRepository extends MongoRepository<CorporateDisqualificationDocument, String> {

}
