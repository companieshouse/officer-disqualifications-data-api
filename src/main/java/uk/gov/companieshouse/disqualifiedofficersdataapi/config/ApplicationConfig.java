package uk.gov.companieshouse.disqualifiedofficersdataapi.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.environment.impl.EnvironmentReaderImpl;

import javax.annotation.PostConstruct;

@Configuration
public class ApplicationConfig {

    @Bean
    EnvironmentReader environmentReader() {
        return new EnvironmentReaderImpl();
    }

    @Autowired
    private MappingMongoConverter mappingMongoConverter;

    /**
     * Converter to remove _class from the mongo record during saving
     */
    @PostConstruct
    public void createConverterToRemoveClassName() {
        mappingMongoConverter.setTypeMapper(new DefaultMongoTypeMapper(null));
    }
}
