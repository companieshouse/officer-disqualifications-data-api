package uk.gov.companieshouse.disqualifiedofficersdataapi.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.disqualification.NaturalDisqualificationApi;

import static org.junit.Assert.assertEquals;

public class DisqualifiedNaturalOfficerReadConverterTest {

    private static final String OFFICER_NAME = "officer";

    private DisqualifiedNaturalOfficerReadConverter converter;

    @BeforeEach
    public void setUp() {
        converter = new DisqualifiedNaturalOfficerReadConverter(new ObjectMapper());
    }

    @Test
    public void canConvertDocument() {
        Document document = new Document("forename", OFFICER_NAME);
        NaturalDisqualificationApi disqualification = converter.convert(document);

        assertEquals(disqualification.getForename(),OFFICER_NAME);
    }
}
