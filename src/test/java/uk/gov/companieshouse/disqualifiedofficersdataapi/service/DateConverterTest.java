package uk.gov.companieshouse.disqualifiedofficersdataapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.companieshouse.disqualifiedofficersdataapi.service.DateConverter.deltaAtToOffsetDateTime;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

class DateConverterTest {

    @Test
    void shouldConvertStringToOffsetDateTime() {
        // given
        final String input = "20240925171003950844";
        OffsetDateTime expected = OffsetDateTime.of(2024, 9, 25, 17, 10, 3, 950844000, ZoneOffset.UTC);

        // when
        OffsetDateTime actual = deltaAtToOffsetDateTime(input);

        // then
        assertEquals(expected, actual);
    }

    @Test
    void shouldFailToConvertInvalidFormat() {
        // given
        final String input = "20240925";

        // when
        Executable executable = () -> deltaAtToOffsetDateTime(input);

        // then
        assertThrows(DateTimeParseException.class, executable);
    }
}