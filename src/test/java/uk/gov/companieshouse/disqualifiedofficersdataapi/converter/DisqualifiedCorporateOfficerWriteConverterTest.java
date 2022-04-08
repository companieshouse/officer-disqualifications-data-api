package uk.gov.companieshouse.disqualifiedofficersdataapi.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.disqualification.CorporateDisqualificationApi;

import static org.junit.Assert.assertTrue;

public class DisqualifiedCorporateOfficerWriteConverterTest {

    private static final String OFFICER_ID = "officerId";

    private DisqualifiedCorporateOfficerWriteConverter converter;

    @BeforeEach
    public void setUp() {
        converter = new DisqualifiedCorporateOfficerWriteConverter(new ObjectMapper());
    }

    @Test
    public void canConvertDocument() {
        CorporateDisqualificationApi api = new CorporateDisqualificationApi();
        api.setCompanyNumber(OFFICER_ID);

        BasicDBObject object = converter.convert(api);

        String json = object.toJson();
        assertTrue(json.contains(OFFICER_ID));
    }
}
