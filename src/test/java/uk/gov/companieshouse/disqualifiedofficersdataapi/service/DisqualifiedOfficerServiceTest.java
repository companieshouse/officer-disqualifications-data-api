package uk.gov.companieshouse.disqualifiedofficersdataapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.disqualification.InternalCorporateDisqualificationApi;
import uk.gov.companieshouse.api.disqualification.InternalDisqualificationApiInternalData;
import uk.gov.companieshouse.api.disqualification.InternalNaturalDisqualificationApi;
import uk.gov.companieshouse.disqualifiedofficersdataapi.api.DisqualifiedOfficerApiService;
import uk.gov.companieshouse.disqualifiedofficersdataapi.model.Created;
import uk.gov.companieshouse.disqualifiedofficersdataapi.model.DisqualificationDocument;
import uk.gov.companieshouse.disqualifiedofficersdataapi.model.Updated;
import uk.gov.companieshouse.disqualifiedofficersdataapi.repository.DisqualifiedOfficerRepository;
import uk.gov.companieshouse.disqualifiedofficersdataapi.transform.DisqualificationTransformer;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DisqualifiedOfficerServiceTest {

    private static final String OFFICER_ID = "officerId";

    @Mock
    private DisqualifiedOfficerRepository repository;

    @Mock
    private DisqualificationTransformer transformer;

    @Mock
    private DisqualifiedOfficerApiService disqualifiedOfficerApiService;

    @Captor
    private ArgumentCaptor<String> dateCaptor;

    @InjectMocks
    private DisqualifiedOfficerService service;

    private InternalNaturalDisqualificationApi request;
    private InternalCorporateDisqualificationApi corpRequest;
    private DisqualificationDocument document;
    private String dateString;

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
        final DateTimeFormatter dateTimeFormatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        dateString = date.format(dateTimeFormatter);
    }

    @Test
    public void processNaturalDisqualificationSavesDisqualification() {
        when(repository.findUpdatedDisqualification(eq(OFFICER_ID), dateCaptor.capture())).thenReturn(new ArrayList<>());
        when(repository.findById(OFFICER_ID)).thenReturn(Optional.empty());
        when(transformer.transformNaturalDisqualifiedOfficer(OFFICER_ID, request)).thenReturn(document);

        service.processNaturalDisqualification("", OFFICER_ID, request);

        verify(repository).save(document);
        verify(disqualifiedOfficerApiService).invokeChsKafkaApi("", "officerId", "natural");
        assertEquals(dateString, dateCaptor.getValue());
        assertNotNull(document.getCreated().getAt());
    }

    @Test
    public void processNaturalDisqualificationUpdatesDisqualification() {
        document.setCreated(new Created().setAt(LocalDateTime.now()));
        when(repository.findUpdatedDisqualification(eq(OFFICER_ID), dateCaptor.capture())).thenReturn(new ArrayList<>());
        when(repository.findById(OFFICER_ID)).thenReturn(Optional.of(document));
        when(transformer.transformNaturalDisqualifiedOfficer(OFFICER_ID, request)).thenReturn(document);

        service.processNaturalDisqualification("", OFFICER_ID, request);

        verify(repository).save(document);
        verify(disqualifiedOfficerApiService).invokeChsKafkaApi("", "officerId", "natural");
        assertEquals(dateString, dateCaptor.getValue());
        assertNotNull(document.getCreated());
    }

    @Test
    public void processNaturalDisqualificationDoesNotSavesDisqualificationWhenUpdateAlreadyMade() {

        List<DisqualificationDocument> documents = new ArrayList<>();
        documents.add(new DisqualificationDocument());
        when(repository.findUpdatedDisqualification(eq(OFFICER_ID), dateCaptor.capture())).thenReturn(documents);

        service.processNaturalDisqualification("", OFFICER_ID, request);

        verify(repository, times(0)).save(document);
        verify(disqualifiedOfficerApiService, times(0)).invokeChsKafkaApi("", "officerId", "natural");
        assertEquals(dateString, dateCaptor.getValue());
    }

    @Test
    public void processCorporateDisqualificationSavesDisqualification() {
        when(repository.findUpdatedDisqualification(eq(OFFICER_ID), dateCaptor.capture())).thenReturn(new ArrayList<>());
        when(repository.findById(OFFICER_ID)).thenReturn(Optional.empty());
        when(transformer.transformCorporateDisqualifiedOfficer(OFFICER_ID, corpRequest)).thenReturn(document);

        service.processCorporateDisqualification("", OFFICER_ID, corpRequest);

        verify(repository).save(document);
        verify(disqualifiedOfficerApiService).invokeChsKafkaApi("", "officerId", "corporate");
        assertEquals(dateString, dateCaptor.getValue());
        assertNotNull(document.getCreated().getAt());
    }
}
