package uk.gov.companieshouse.disqualifiedofficersdataapi.api;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.disqualifiedofficersdataapi.config.ApplicationConfig;

class TimestampGeneratorTest {

    private final Supplier<String> offsetDateTimeGenerator = new ApplicationConfig().timestampGenerator();

    @Test
    void shouldReturnFormattedTimestampString() {
        String timestamp = offsetDateTimeGenerator.get();
        assertTrue(timestamp.matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}$"));
    }
}
