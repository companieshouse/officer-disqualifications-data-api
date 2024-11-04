package uk.gov.companieshouse.disqualifiedofficersdataapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.api.disqualification.CorporateDisqualificationApi.KindEnum.CORPORATE_DISQUALIFICATION;
import static uk.gov.companieshouse.api.disqualification.NaturalDisqualificationApi.KindEnum.NATURAL_DISQUALIFICATION;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.disqualification.CorporateDisqualificationApi;
import uk.gov.companieshouse.api.disqualification.NaturalDisqualificationApi;
import uk.gov.companieshouse.disqualifiedofficersdataapi.exceptions.BadRequestException;
import uk.gov.companieshouse.disqualifiedofficersdataapi.exceptions.ConflictException;
import uk.gov.companieshouse.disqualifiedofficersdataapi.model.CorporateDisqualificationDocument;
import uk.gov.companieshouse.disqualifiedofficersdataapi.model.NaturalDisqualificationDocument;
import uk.gov.companieshouse.disqualifiedofficersdataapi.repository.CorporateDisqualifiedOfficerRepository;
import uk.gov.companieshouse.disqualifiedofficersdataapi.repository.NaturalDisqualifiedOfficerRepository;

@ExtendWith(MockitoExtension.class)
class DeletionDataServiceTest {

    private static final String OFFICER_ID = "officer_id";
    private static final String REQUEST_DELTA_AT = "20240925171003950844";
    private static final String EXISTING_DELTA_AT = "20230925171003950844";
    private static final String STALE_DELTA_AT = "20220925171003950844";

    @InjectMocks
    private DeletionDataService deletionDataService;

    @Mock
    private NaturalDisqualifiedOfficerRepository naturalRepository;
    @Mock
    private CorporateDisqualifiedOfficerRepository corporateRepository;
    @Mock
    private DeltaAtHandler deltaAtHandler;

    @Mock
    private NaturalDisqualificationDocument naturalDisqualificationDocument;
    @Mock
    private CorporateDisqualificationDocument corporateDisqualificationDocument;
    @Mock
    private NaturalDisqualificationApi naturalData;
    @Mock
    private CorporateDisqualificationApi corporateData;

    @Test
    void shouldReturnNaturalDisqualificationData() {
        // given
        when(naturalRepository.findById(anyString())).thenReturn(Optional.of(naturalDisqualificationDocument));
        when(naturalDisqualificationDocument.getDeltaAt()).thenReturn(EXISTING_DELTA_AT);
        when(deltaAtHandler.isRequestStale(anyString(), anyString())).thenReturn(false);
        when(naturalDisqualificationDocument.getData()).thenReturn(naturalData);

        // when
        Object actual = deletionDataService.processNaturalDisqualificationData(OFFICER_ID, REQUEST_DELTA_AT);

        // then
        assertEquals(naturalData, actual);
        verify(naturalRepository).findById(OFFICER_ID);
        verify(deltaAtHandler).isRequestStale(REQUEST_DELTA_AT, EXISTING_DELTA_AT);
        verify(naturalData).setKind(NATURAL_DISQUALIFICATION);
        verifyNoInteractions(corporateRepository);
    }

    @Test
    void shouldReturnNullDataWhenNoNaturalDocumentFound() {
        // given
        when(naturalRepository.findById(anyString())).thenReturn(Optional.empty());

        // when
        Object actual = deletionDataService.processNaturalDisqualificationData(OFFICER_ID, REQUEST_DELTA_AT);

        // then
        assertNull(actual);
        verifyNoInteractions(corporateRepository);
    }

    @Test
    void shouldThrowConflictExceptionWhenRequestIsStaleOnNaturalDelete() {
        // given
        when(naturalRepository.findById(anyString())).thenReturn(Optional.of(naturalDisqualificationDocument));
        when(naturalDisqualificationDocument.getDeltaAt()).thenReturn(EXISTING_DELTA_AT);
        when(deltaAtHandler.isRequestStale(anyString(), anyString())).thenReturn(true);

        // when
        Executable ex = () -> deletionDataService.processNaturalDisqualificationData(OFFICER_ID, STALE_DELTA_AT);

        // then
        assertThrows(ConflictException.class, ex);
        verify(deltaAtHandler).isRequestStale(STALE_DELTA_AT, EXISTING_DELTA_AT);
        verify(naturalDisqualificationDocument, times(0)).getData();
        verifyNoInteractions(corporateRepository);
    }

    @Test
    void shouldThrowBadRequestExceptionWhenRequestTypeIsNaturalButMongoDocumentIsCorporate() {
        // given
        when(naturalRepository.findById(anyString())).thenReturn(Optional.of(naturalDisqualificationDocument));
        when(naturalDisqualificationDocument.isCorporateOfficer()).thenReturn(true);

        // when
        Executable ex = () -> deletionDataService.processNaturalDisqualificationData(OFFICER_ID, STALE_DELTA_AT);

        // then
        assertThrows(BadRequestException.class, ex);
        verifyNoInteractions(deltaAtHandler);
        verify(naturalDisqualificationDocument, times(0)).getData();
        verifyNoInteractions(corporateRepository);
    }

    @Test
    void shouldReturnCorporateDisqualificationData() {
        // given
        when(corporateRepository.findById(anyString())).thenReturn(Optional.of(corporateDisqualificationDocument));
        when(corporateDisqualificationDocument.isCorporateOfficer()).thenReturn(true);
        when(corporateDisqualificationDocument.getDeltaAt()).thenReturn(EXISTING_DELTA_AT);
        when(deltaAtHandler.isRequestStale(anyString(), anyString())).thenReturn(false);
        when(corporateDisqualificationDocument.getData()).thenReturn(corporateData);

        // when
        Object actual = deletionDataService.processCorporateDisqualificationData(OFFICER_ID, REQUEST_DELTA_AT);

        // then
        assertEquals(corporateData, actual);
        verify(corporateRepository).findById(OFFICER_ID);
        verify(deltaAtHandler).isRequestStale(REQUEST_DELTA_AT, EXISTING_DELTA_AT);
        verify(corporateData).setKind(CORPORATE_DISQUALIFICATION);
        verifyNoInteractions(naturalRepository);
    }

    @Test
    void shouldReturnNullDataWhenNoCorporateDocumentFound() {
        // given
        when(corporateRepository.findById(anyString())).thenReturn(Optional.empty());

        // when
        Object actual = deletionDataService.processCorporateDisqualificationData(OFFICER_ID, REQUEST_DELTA_AT);

        // then
        assertNull(actual);
        verifyNoInteractions(naturalRepository);
    }

    @Test
    void shouldThrowConflictExceptionWhenRequestIsStaleOnCorporateDelete() {
        // given
        when(corporateRepository.findById(anyString())).thenReturn(Optional.of(corporateDisqualificationDocument));
        when(corporateDisqualificationDocument.isCorporateOfficer()).thenReturn(true);
        when(corporateDisqualificationDocument.getDeltaAt()).thenReturn(EXISTING_DELTA_AT);
        when(deltaAtHandler.isRequestStale(anyString(), anyString())).thenReturn(true);

        // when
        Executable ex = () -> deletionDataService.processCorporateDisqualificationData(OFFICER_ID, STALE_DELTA_AT);

        // then
        assertThrows(ConflictException.class, ex);
        verify(deltaAtHandler).isRequestStale(STALE_DELTA_AT, EXISTING_DELTA_AT);
        verify(corporateDisqualificationDocument, times(0)).getData();
        verifyNoInteractions(naturalRepository);
    }

    @Test
    void shouldThrowBadRequestExceptionWhenRequestTypeIsCorporateButMongoDocumentIsNatural() {
        // given
        when(corporateRepository.findById(anyString())).thenReturn(Optional.of(corporateDisqualificationDocument));

        // when
        Executable ex = () -> deletionDataService.processCorporateDisqualificationData(OFFICER_ID, STALE_DELTA_AT);

        // then
        assertThrows(BadRequestException.class, ex);
        verifyNoInteractions(deltaAtHandler);
        verify(corporateDisqualificationDocument, times(0)).getData();
        verifyNoInteractions(naturalRepository);
    }
}