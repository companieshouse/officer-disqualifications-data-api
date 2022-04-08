package uk.gov.companieshouse.disqualifiedofficersdataapi.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import uk.gov.companieshouse.api.disqualification.CorporateDisqualificationApi;

@WritingConverter
public class DisqualifiedCorporateOfficerWriteConverter implements Converter<CorporateDisqualificationApi, BasicDBObject> {

    private final ObjectMapper objectMapper;

    public DisqualifiedCorporateOfficerWriteConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Write convertor.
     * @param source source Document.
     * @return charge BSON object.
     */
    @Override
    public BasicDBObject convert(CorporateDisqualificationApi source) {
        try {
            return BasicDBObject.parse(objectMapper.writeValueAsString(source));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
