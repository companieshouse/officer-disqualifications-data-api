package uk.gov.companieshouse.disqualifiedofficersdataapi.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import uk.gov.companieshouse.api.disqualification.NaturalDisqualificationApi;
import uk.gov.companieshouse.disqualifiedofficersdataapi.model.DisqualificationDocument;
import uk.gov.companieshouse.disqualifiedofficersdataapi.model.NaturalDisqualificationDocument;
import uk.gov.companieshouse.logging.Logger;

@WritingConverter
public class DisqualifiedNaturalOfficerWriteConverter implements Converter<NaturalDisqualificationApi, BasicDBObject> {

    private final ObjectMapper objectMapper;

    @Autowired
    private Logger logger;

    public DisqualifiedNaturalOfficerWriteConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Write convertor.
     * @param source source Document.
     * @return charge BSON object.
     */
    @Override
    public BasicDBObject convert(NaturalDisqualificationApi source) {
        try {
            return BasicDBObject.parse(objectMapper.writeValueAsString(source));
        } catch (Exception ex) {
            throw new RuntimeException("Error is here: " + ex.getMessage());
        }
    }
}
