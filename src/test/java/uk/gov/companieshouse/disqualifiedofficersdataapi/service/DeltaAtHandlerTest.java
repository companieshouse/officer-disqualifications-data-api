package uk.gov.companieshouse.disqualifiedofficersdataapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class DeltaAtHandlerTest {

    private static final String DOCUMENT_DELTA_AT = "20230925171003950844";
    private static final OffsetDateTime REQUEST_DELTA_AT = OffsetDateTime.of(2024, 9, 25, 17, 10, 3, 950844000,
            ZoneOffset.UTC);
    private static final OffsetDateTime STALE_DELTA_AT = OffsetDateTime.of(2022, 9, 25, 17, 10, 3, 950844000,
            ZoneOffset.UTC);
    private static final OffsetDateTime SAME_DELTA_AT = OffsetDateTime.of(2023, 9, 25, 17, 10, 3, 950844000,
            ZoneOffset.UTC);

    private final DeltaAtHandler deltaAtHandler = new DeltaAtHandler();

    @ParameterizedTest
    @CsvSource(value = {
            "20220925171003950844 , 20230925171003950844 , true",
            "20240925171003950844 , 20230925171003950844 , false",
            "20240925171003950844 , null , false",
            "20240925171003950844 , '' , false",
            "20230925171003950844 , 20230925171003950844 , false",
    }, nullValues = {"null"})
    void shouldReturnTrueWhenRequestIsStale(final String requestDeltaAt, final String documentDeltaAt,
            final boolean result) {
        assertEquals(deltaAtHandler.isRequestStale(requestDeltaAt, documentDeltaAt), result);
    }

    @ParameterizedTest
    @MethodSource("deltaAtArgs")
    void shouldReturnTrueWhenRequestIsStale(OffsetDateTime requestDeltaAt, final String documentDeltaAt,
            final boolean result) {
        assertEquals(deltaAtHandler.isRequestStale(requestDeltaAt, documentDeltaAt), result);
    }

    private static Stream<Arguments> deltaAtArgs() {
        return Stream.of(
                Arguments.of(STALE_DELTA_AT, DOCUMENT_DELTA_AT, true),
                Arguments.of(REQUEST_DELTA_AT, DOCUMENT_DELTA_AT, false),
                Arguments.of(SAME_DELTA_AT, DOCUMENT_DELTA_AT, false),
                Arguments.of(REQUEST_DELTA_AT, null, false),
                Arguments.of(REQUEST_DELTA_AT, "", false)
        );
    }
}
