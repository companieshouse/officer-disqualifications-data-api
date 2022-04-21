package uk.gov.companieshouse.disqualifiedofficersdataapi.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.disqualification.CorporateDisqualificationApi;
import uk.gov.companieshouse.api.disqualification.NaturalDisqualificationApi;

import static org.junit.Assert.assertEquals;

public class DisqualifiedCorporateOfficerReadConverterTest {

    private static final String COMPANY_NUMBER = "123456";

    private DisqualifiedCorporateOfficerReadConverter converter;

    @BeforeEach
    public void setUp() {
        converter = new DisqualifiedCorporateOfficerReadConverter(new ObjectMapper());
    }

    @Test
    public void canConvertDocument() {
        Document document = new Document("company_number", COMPANY_NUMBER);
        CorporateDisqualificationApi disqualification = converter.convert(document);

        assertEquals(disqualification.getCompanyNumber(),COMPANY_NUMBER);
    }
}
