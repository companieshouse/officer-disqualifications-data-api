package uk.gov.companieshouse.disqualifiedofficersdataapi.service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class DateConverter {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSSSSS")
            .withZone(ZoneOffset.UTC);

    private DateConverter() {
    }

    static OffsetDateTime deltaAtToOffsetDateTime(final String input) {
        return OffsetDateTime.parse(input, FORMATTER);
    }

}
