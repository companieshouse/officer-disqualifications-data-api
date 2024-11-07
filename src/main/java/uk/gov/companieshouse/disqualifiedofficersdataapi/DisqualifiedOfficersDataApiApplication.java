package uk.gov.companieshouse.disqualifiedofficersdataapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DisqualifiedOfficersDataApiApplication {

    public static final String NAMESPACE = "disqualified-officers-data-api";

    public static void main(String[] args) {
        SpringApplication.run(DisqualifiedOfficersDataApiApplication.class, args);
    }

}
