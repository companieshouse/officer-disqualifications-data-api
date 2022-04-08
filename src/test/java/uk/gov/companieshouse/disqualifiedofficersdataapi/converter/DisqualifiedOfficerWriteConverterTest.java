package uk.gov.companieshouse.disqualifiedofficersdataapi.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.disqualifiedofficersdataapi.model.DisqualificationDocument;

import static org.junit.Assert.assertTrue;

public class DisqualifiedOfficerWriteConverterTest {

    private static final String OFFICER_ID = "officerId";

    private DisqualifiedOfficerWriteConverter converter;

    @BeforeEach
    public void setUp() {
        converter = new DisqualifiedOfficerWriteConverter(new ObjectMapper());
    }

    @Test
    public void canConvertDocument() {
        DisqualificationDocument document = new DisqualificationDocument();
        document.setId("OFFICER_ID");

        BasicDBObject object = converter.convert(document);

        String json = object.toJson();
        assertTrue(json.contains(OFFICER_ID));
    }
}
