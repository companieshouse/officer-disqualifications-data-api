package uk.gov.companieshouse.disqualifiedofficersdataapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DeltaAtProcessorTest {

    private final DeltaAtProcessor deltaAtProcessor = new DeltaAtProcessor();

    @ParameterizedTest
    @CsvSource(value = {
            "20220925171003950844 , 20230925171003950844 , true",
            "20240925171003950844 , 20230925171003950844 , false",
            "20240925171003950844 , null , false",
            "20240925171003950844 , '' , false",
            "20230925171003950844 , 20230925171003950844 , false",
    }, nullValues = {"null"})
    void shouldReturnTrueWhenRequestIsStale(final String requestDeltaAt, final String documentDeltaAt, final boolean result) {
        assertEquals(deltaAtProcessor.isRequestStale(requestDeltaAt, documentDeltaAt), result);
    }
}
