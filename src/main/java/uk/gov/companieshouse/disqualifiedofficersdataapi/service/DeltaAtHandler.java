package uk.gov.companieshouse.disqualifiedofficersdataapi.service;

import static uk.gov.companieshouse.disqualifiedofficersdataapi.service.DateConverter.deltaAtToOffsetDateTime;

import java.time.OffsetDateTime;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class DeltaAtHandler {

    public boolean isRequestStale(final String requestDeltaAt, final String docDeltaAt) {
        return StringUtils.isNotBlank(docDeltaAt)
                && deltaAtToOffsetDateTime(requestDeltaAt).isBefore(deltaAtToOffsetDateTime(docDeltaAt));
    }

    public boolean isRequestStale(OffsetDateTime requestDeltaAt, final String docDeltaAt) {
        return StringUtils.isNotBlank(docDeltaAt)
                && requestDeltaAt.isBefore(deltaAtToOffsetDateTime(docDeltaAt));
    }
}
