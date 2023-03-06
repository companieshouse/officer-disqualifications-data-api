package uk.gov.companieshouse.disqualifiedofficersdataapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.disqualification.*;
import uk.gov.companieshouse.disqualifiedofficersdataapi.api.DisqualifiedOfficerApiService;
import uk.gov.companieshouse.disqualifiedofficersdataapi.api.ResourceChangedRequest;
import uk.gov.companieshouse.disqualifiedofficersdataapi.model.*;
import uk.gov.companieshouse.disqualifiedofficersdataapi.repository.CorporateDisqualifiedOfficerRepository;
import uk.gov.companieshouse.disqualifiedofficersdataapi.repository.DisqualifiedOfficerRepository;
import uk.gov.companieshouse.disqualifiedofficersdataapi.repository.NaturalDisqualifiedOfficerRepository;
import uk.gov.companieshouse.disqualifiedofficersdataapi.transform.DisqualificationTransformer;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DisqualifiedOfficerServiceTest {

    private static final String OFFICER_ID = "officerId";
    private static final String PAST_DATE = "20220121133129395348";
    private static final String FUTURE_DATE = "30220121133129395348";

    @Mock
    private DisqualifiedOfficerRepository repository;

    @Mock
    private NaturalDisqualifiedOfficerRepository naturalRepository;

    @Mock
    private CorporateDisqualifiedOfficerRepository corporateRepository;

    @Mock
    private DisqualificationTransformer transformer;

    @Mock
    private DisqualifiedOfficerApiService disqualifiedOfficerApiService;

    @InjectMocks
    private DisqualifiedOfficerService service;

    private InternalNaturalDisqualificationApi request;
    private InternalCorporateDisqualificationApi corpRequest;
    private DisqualificationDocument document;

    @BeforeEach
    public void setUp() {
        OffsetDateTime date = OffsetDateTime.now();
        request = new InternalNaturalDisqualificationApi();
        corpRequest = new InternalCorporateDisqualificationApi();
        InternalDisqualificationApiInternalData internal = new InternalDisqualificationApiInternalData();
        internal.setDeltaAt(date);
        request.setInternalData(internal);
        corpRequest.setInternalData(internal);
        document = new DisqualificationDocument();
        document.setUpdated(new Updated().setAt(LocalDateTime.now()));
    }

    @Test
    public void processNaturalDisqualificationSavesDisqualificationIfNoExistingDocument() {
        when(repository.findById(OFFICER_ID)).thenReturn(Optional.empty());
        when(transformer.transformNaturalDisqualifiedOfficer(OFFICER_ID, request)).thenReturn(document);

        service.processNaturalDisqualification("", OFFICER_ID, request);

        verify(repository).save(document);
        verify(disqualifiedOfficerApiService).invokeChsKafkaApi(new ResourceChangedRequest("", "officerId",
                DisqualificationResourceType.NATURAL, null, false));
    }

    @Test
    public void processNaturalDisqualificationSavesDisqualificationIfExistingDocumentHasEmptyDeltaAt() {
        document.setDeltaAt("");
        when(repository.findById(OFFICER_ID)).thenReturn(Optional.of(document));
        when(transformer.transformNaturalDisqualifiedOfficer(OFFICER_ID, request)).thenReturn(document);

        service.processNaturalDisqualification("", OFFICER_ID, request);

        verify(repository).save(document);
        verify(disqualifiedOfficerApiService).invokeChsKafkaApi(new ResourceChangedRequest("", "officerId",
                DisqualificationResourceType.NATURAL, null, false));
    }

    @Test
    public void processNaturalDisqualificationSavesDisqualificationIfExistingDocumentHasValidDeltaAt() {
        document.setCreated(new Created().setAt(LocalDateTime.now()));
        document.setDeltaAt(PAST_DATE);
        when(repository.findById(OFFICER_ID)).thenReturn(Optional.of(document));
        when(transformer.transformNaturalDisqualifiedOfficer(OFFICER_ID, request)).thenReturn(document);

        service.processNaturalDisqualification("", OFFICER_ID, request);

        verify(repository).save(document);
        verify(disqualifiedOfficerApiService).invokeChsKafkaApi(new ResourceChangedRequest("", "officerId",
                DisqualificationResourceType.NATURAL, null, false));
    }

    @Test
    public void processNaturalDisqualificationSavesDisqualificationIfExistingDocumentHasValidDeltaAtAfterNow() {
        document.setDeltaAt(FUTURE_DATE);
        when(repository.findById(OFFICER_ID)).thenReturn(Optional.of(document));

        service.processNaturalDisqualification("", OFFICER_ID, request);

        verifyNoMoreInteractions(repository);
        verifyNoInteractions(transformer);
        verifyNoInteractions(disqualifiedOfficerApiService);
    }

    @Test
    public void processCorporateDisqualificationSavesDisqualificationIfNoExistingDocument() {
        when(repository.findById(OFFICER_ID)).thenReturn(Optional.empty());
        when(transformer.transformCorporateDisqualifiedOfficer(OFFICER_ID, corpRequest)).thenReturn(document);

        service.processCorporateDisqualification("", OFFICER_ID, corpRequest);

        verify(repository).save(document);
        verify(disqualifiedOfficerApiService).invokeChsKafkaApi(new ResourceChangedRequest("", "officerId",
                DisqualificationResourceType.CORPORATE, null, false));
    }

    @Test
    public void processCorporateDisqualificationSavesDisqualificationIfExistingDocumentHasEmptyDeltaAt() {
        document.setDeltaAt("");
        when(repository.findById(OFFICER_ID)).thenReturn(Optional.of(document));
        when(transformer.transformCorporateDisqualifiedOfficer(OFFICER_ID, corpRequest)).thenReturn(document);

        service.processCorporateDisqualification("", OFFICER_ID, corpRequest);

        verify(repository).save(document);
        verify(disqualifiedOfficerApiService).invokeChsKafkaApi(new ResourceChangedRequest("", "officerId",
                DisqualificationResourceType.CORPORATE, null, false));
    }

    @Test
    public void processCorporateDisqualificationSavesDisqualificationIfExistingDocumentHasValidDeltaAt() {
        document.setCreated(new Created().setAt(LocalDateTime.now()));
        document.setDeltaAt(PAST_DATE);
        when(repository.findById(OFFICER_ID)).thenReturn(Optional.of(document));
        when(transformer.transformCorporateDisqualifiedOfficer(OFFICER_ID, corpRequest)).thenReturn(document);

        service.processCorporateDisqualification("", OFFICER_ID, corpRequest);

        verify(repository).save(document);
        verify(disqualifiedOfficerApiService).invokeChsKafkaApi(new ResourceChangedRequest("", "officerId",
                DisqualificationResourceType.CORPORATE, null, false));
    }

    @Test
    public void processCoporateDisqualificationSavesDisqualificationIfExistingDocumentHasValidDeltaAtAfterNow() {
        document.setDeltaAt(FUTURE_DATE);
        when(repository.findById(OFFICER_ID)).thenReturn(Optional.of(document));

        service.processCorporateDisqualification("", OFFICER_ID, corpRequest);

        verifyNoMoreInteractions(repository);
        verifyNoInteractions(transformer);
        verifyNoInteractions(disqualifiedOfficerApiService);
    }

    @Test
    void correctOfficerIdIsGivenReturnsDisqualification() {
        NaturalDisqualificationDocument naturalDocument = new NaturalDisqualificationDocument();
        naturalDocument.setData(new NaturalDisqualificationApi());
        naturalDocument.setId(OFFICER_ID);
        when(naturalRepository.findById(OFFICER_ID)).thenReturn(Optional.of(naturalDocument));

        NaturalDisqualificationDocument disqualification = service.retrieveNaturalDisqualification(OFFICER_ID);

        assertNotNull(disqualification);
        verify(naturalRepository, times(1)).findById(any());
    }

    @Test
    void correctOfficerIdIsGivenReturnsCorporateDisqualification() {
        CorporateDisqualificationDocument corporateDocument = new CorporateDisqualificationDocument();
        corporateDocument.setData(new CorporateDisqualificationApi());
        corporateDocument.setCorporateOfficer(true);
        corporateDocument.setId(OFFICER_ID);
        when(corporateRepository.findById(OFFICER_ID)).thenReturn(Optional.of(corporateDocument));

        CorporateDisqualificationDocument disqualification = service.retrieveCorporateDisqualification(OFFICER_ID);

        assertNotNull(disqualification);
        verify(corporateRepository, times(1)).findById(any());
    }

    @Test
    void throwsExceptionWhenCorporateIndIsTrueButNaturalOfficerCalled() {
        NaturalDisqualificationDocument naturalDocument = new NaturalDisqualificationDocument();
        naturalDocument.setData(new NaturalDisqualificationApi());
        naturalDocument.setCorporateOfficer(true);
        naturalDocument.setId(OFFICER_ID);
        when(naturalRepository.findById(OFFICER_ID)).thenReturn(Optional.of(naturalDocument));

        assertThrows(RuntimeException.class, () -> service.retrieveNaturalDisqualification
                (OFFICER_ID));
        verify(naturalRepository, times(1)).findById(any());

    }

    @Test
    void throwsExceptionWhenCorporateIndIsFalseButCorporateOfficerCalled() {
        CorporateDisqualificationDocument corporateDocument = new CorporateDisqualificationDocument();
        corporateDocument.setData(new CorporateDisqualificationApi());
        corporateDocument.setCorporateOfficer(false);
        corporateDocument.setId(OFFICER_ID);
        when(corporateRepository.findById(OFFICER_ID)).thenReturn(Optional.of(corporateDocument));

        assertThrows(RuntimeException.class, () -> service.retrieveCorporateDisqualification
                (OFFICER_ID));
        verify(corporateRepository, times(1)).findById(any());

    }

    @Test
    void throwsExceptionWhenInvalidIdGiven() {

        assertThrows(RuntimeException.class, () -> service.retrieveNaturalDisqualification
                ("asdfasdfasdf"));
        verify(naturalRepository, times(1)).findById(any());

    }

    @Test
    public void deleteNaturalDisqualificationDeletesDisqualification() {
        when(repository.findById(OFFICER_ID)).thenReturn(Optional.of(document));
        NaturalDisqualificationDocument doc = new NaturalDisqualificationDocument();
        doc.setData(new NaturalDisqualificationApi());
        when(naturalRepository.findById(OFFICER_ID)).thenReturn(Optional.of(doc));

        service.deleteDisqualification("", OFFICER_ID);

        verify(repository).delete(doc);
        verify(disqualifiedOfficerApiService).invokeChsKafkaApi(new ResourceChangedRequest("", "officerId",
                DisqualificationResourceType.NATURAL, doc.getData(), true));
        assertEquals(doc.getData().getKind().toString(), "natural-disqualification");
    }

    @Test
    public void deleteCorporateDisqualificationDeletesDisqualification() {
        document.setCorporateOfficer(true);
        when(repository.findById(OFFICER_ID)).thenReturn(Optional.of(document));
        CorporateDisqualificationDocument doc = new CorporateDisqualificationDocument();
        doc.setCorporateOfficer(true);
        doc.setData(new CorporateDisqualificationApi());
        when(corporateRepository.findById(OFFICER_ID)).thenReturn(Optional.of(doc));

        service.deleteDisqualification("", OFFICER_ID);

        verify(repository).delete(doc);
        verify(disqualifiedOfficerApiService).invokeChsKafkaApi(new ResourceChangedRequest("", "officerId",
                DisqualificationResourceType.CORPORATE, doc.getData(), true));
        assertEquals(doc.getData().getKind().toString(), "corporate-disqualification");
    }

    @Test
    public void deleteCorporateDisqualificationThrowsErrorWhenNatural() {
        when(repository.findById(OFFICER_ID)).thenReturn(Optional.of(document));

        assertThrows(IllegalArgumentException.class, () -> service.deleteDisqualification
                ("",OFFICER_ID));
        verify(naturalRepository, times(1)).findById(any());

    }

    @Test
    public void deleteNaturalDisqualificationThrowsErrorWhenCorporate() {
        document.setCorporateOfficer(true);
        when(repository.findById(OFFICER_ID)).thenReturn(Optional.of(document));

        assertThrows(IllegalArgumentException.class, () -> service.deleteDisqualification
                ("",OFFICER_ID));
        verify(corporateRepository, times(1)).findById(any());
    }
}