package uk.gov.companieshouse.disqualifiedofficersdataapi.transform;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.disqualification.CorporateDisqualificationApi;
import uk.gov.companieshouse.api.disqualification.InternalCorporateDisqualificationApi;
import uk.gov.companieshouse.api.disqualification.InternalDisqualificationApiInternalData;
import uk.gov.companieshouse.api.disqualification.InternalNaturalDisqualificationApi;
import uk.gov.companieshouse.api.disqualification.NaturalDisqualificationApi;
import uk.gov.companieshouse.disqualifiedofficersdataapi.model.CorporateDisqualificationDocument;
import uk.gov.companieshouse.disqualifiedofficersdataapi.model.NaturalDisqualificationDocument;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DisqualificationTransformerTest {

    private static final String OFFICER_ID = "officerId";
    private static final String OFFICER_DISQ_ID = "officerDisqId";
    private static final String OFFICER_DETAIL_ID = "officerDetailId";
    private static final String OFFICER_ID_RAW = "officerIdRaw";


    private DisqualificationTransformer transformer;

    @BeforeEach
    void setup() {
        transformer = new DisqualificationTransformer();
    }

    @Test
    void shouldTransformNaturalDisqualifiedOfficer() {
        InternalNaturalDisqualificationApi request = new InternalNaturalDisqualificationApi();
        NaturalDisqualificationApi external = new NaturalDisqualificationApi();
        request.setExternalData(external);
        InternalDisqualificationApiInternalData internal = new InternalDisqualificationApiInternalData();
        internal.setOfficerDisqId(OFFICER_DISQ_ID);
        internal.setOfficerDetailId(OFFICER_DETAIL_ID);
        internal.setOfficerIdRaw(OFFICER_ID_RAW);
        OffsetDateTime deltaAt = OffsetDateTime.of(2020, 1, 1, 1, 1, 1, 1000, ZoneOffset.MIN);
        internal.setDeltaAt(deltaAt);
        request.setInternalData(internal);

        NaturalDisqualificationDocument document = (NaturalDisqualificationDocument) transformer
                .transformNaturalDisqualifiedOfficer(OFFICER_ID, request);

        assertEquals(OFFICER_DETAIL_ID, document.getOfficerDetailId());
        assertEquals(OFFICER_DISQ_ID, document.getOfficerDisqId());
        assertEquals(OFFICER_ID_RAW, document.getOfficerIdRaw());
        assertEquals("20200101010101000001", document.getDeltaAt());
        assertEquals(OFFICER_ID, document.getId());
        assertFalse(document.isCorporateOfficer());
        assertEquals(external, document.getData());
        assertTrue(LocalDateTime.now().toEpochSecond(ZoneOffset.MIN)
                - document.getUpdated().getAt().toEpochSecond(ZoneOffset.MIN) < 2);
    }

    @Test
    void shouldTransformCorporateDisqualifiedOfficer() {
        InternalCorporateDisqualificationApi request = new InternalCorporateDisqualificationApi();
        CorporateDisqualificationApi external = new CorporateDisqualificationApi();
        request.setExternalData(external);
        InternalDisqualificationApiInternalData internal = new InternalDisqualificationApiInternalData();
        internal.setOfficerDisqId(OFFICER_DISQ_ID);
        internal.setOfficerDetailId(OFFICER_DETAIL_ID);
        internal.setOfficerIdRaw(OFFICER_ID_RAW);
        OffsetDateTime deltaAt = OffsetDateTime.of(2020, 1, 1, 1, 1, 1, 1000, ZoneOffset.MIN);
        internal.setDeltaAt(deltaAt);
        request.setInternalData(internal);

        CorporateDisqualificationDocument document = (CorporateDisqualificationDocument) transformer
                .transformCorporateDisqualifiedOfficer(OFFICER_ID, request);

        assertEquals(OFFICER_DETAIL_ID, document.getOfficerDetailId());
        assertEquals(OFFICER_DISQ_ID, document.getOfficerDisqId());
        assertEquals(OFFICER_ID_RAW, document.getOfficerIdRaw());
        assertEquals("20200101010101000001", document.getDeltaAt());
        assertEquals(OFFICER_ID, document.getId());
        assertTrue(document.isCorporateOfficer());
        assertEquals(external, document.getData());
        assertTrue(LocalDateTime.now().toEpochSecond(ZoneOffset.MIN)
                - document.getUpdated().getAt().toEpochSecond(ZoneOffset.MIN) < 2);
    }

}
