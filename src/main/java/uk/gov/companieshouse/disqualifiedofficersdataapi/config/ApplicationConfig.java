package uk.gov.companieshouse.disqualifiedofficersdataapi.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import uk.gov.companieshouse.disqualifiedofficersdataapi.converter.DisqualifiedCorporateOfficerReadConverter;
import uk.gov.companieshouse.disqualifiedofficersdataapi.converter.DisqualifiedCorporateOfficerWriteConverter;
import uk.gov.companieshouse.disqualifiedofficersdataapi.converter.DisqualifiedNaturalOfficerReadConverter;
import uk.gov.companieshouse.disqualifiedofficersdataapi.converter.DisqualifiedNaturalOfficerWriteConverter;
import uk.gov.companieshouse.disqualifiedofficersdataapi.serialization.LocalDateDeSerializer;
import uk.gov.companieshouse.disqualifiedofficersdataapi.serialization.LocalDateSerializer;

import java.time.LocalDate;
import java.util.List;

@Configuration
public class ApplicationConfig {

    /**
     * mongoCustomConversions.
     *
     * @return MongoCustomConversions.
     */
    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        ObjectMapper objectMapper = mongoDbObjectMapper();
        return new MongoCustomConversions(List.of(new DisqualifiedNaturalOfficerWriteConverter(objectMapper),
                new DisqualifiedCorporateOfficerWriteConverter(objectMapper),
                new DisqualifiedNaturalOfficerReadConverter(objectMapper),
                new DisqualifiedCorporateOfficerReadConverter(objectMapper)));
    }

    /**
     * Mongo DB Object Mapper.
     *
     * @return ObjectMapper.
     */
    private ObjectMapper mongoDbObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        SimpleModule module = new SimpleModule();
        module.addSerializer(LocalDate.class, new LocalDateSerializer());
        module.addDeserializer(LocalDate.class, new LocalDateDeSerializer());
        objectMapper.registerModule(module);
        return objectMapper;
    }
}
