package uk.gov.companieshouse.disqualifiedofficersdataapi.service;

import static uk.gov.companieshouse.api.disqualification.CorporateDisqualificationApi.KindEnum.CORPORATE_DISQUALIFICATION;
import static uk.gov.companieshouse.api.disqualification.NaturalDisqualificationApi.KindEnum.NATURAL_DISQUALIFICATION;
import static uk.gov.companieshouse.disqualifiedofficersdataapi.DisqualifiedOfficersDataApiApplication.NAMESPACE;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.disqualifiedofficersdataapi.exceptions.ConflictException;
import uk.gov.companieshouse.disqualifiedofficersdataapi.repository.CorporateDisqualifiedOfficerRepository;
import uk.gov.companieshouse.disqualifiedofficersdataapi.repository.NaturalDisqualifiedOfficerRepository;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
public class DeletionDataFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);
    private static final String STALE_DELTA_AT_MESSAGE = "[delta_at] field on request is stale";
    private static final String NULL_DATA_MESSAGE = "Document not found in database with officer id: [%s] - invoking CHS Kafka API with null data";

    private final NaturalDisqualifiedOfficerRepository naturalRepository;
    private final CorporateDisqualifiedOfficerRepository corporateRepository;
    private final DeltaAtProcessor deltaAtProcessor;

    public DeletionDataFactory(NaturalDisqualifiedOfficerRepository naturalRepository,
            CorporateDisqualifiedOfficerRepository corporateRepository, DeltaAtProcessor deltaAtProcessor) {
        this.naturalRepository = naturalRepository;
        this.corporateRepository = corporateRepository;
        this.deltaAtProcessor = deltaAtProcessor;
    }

    public Object processNaturalDisqualificationData(final String officerId, final String requestDeltaAt) {
        return naturalRepository.findById(officerId)
                .map(document -> {
                    if (deltaAtProcessor.isRequestStale(requestDeltaAt, document.getDeltaAt())) {
                        LOGGER.error(STALE_DELTA_AT_MESSAGE);
                        throw new ConflictException(STALE_DELTA_AT_MESSAGE);
                    }
                    document.getData().setKind(NATURAL_DISQUALIFICATION);
                    return document.getData();
                }).orElseGet(() -> {
                    LOGGER.info(NULL_DATA_MESSAGE.formatted(officerId));
                    return null;
                });
    }

    public Object processCorporateDisqualificationData(final String officerId, final String requestDeltaAt) {
        return corporateRepository.findById(officerId)
                .map(document -> {
                    if (deltaAtProcessor.isRequestStale(requestDeltaAt, document.getDeltaAt())) {
                        LOGGER.error(STALE_DELTA_AT_MESSAGE);
                        throw new ConflictException(STALE_DELTA_AT_MESSAGE);
                    }
                    document.getData().setKind(CORPORATE_DISQUALIFICATION);
                    return document.getData();
                }).orElseGet(() -> {
                    LOGGER.info(NULL_DATA_MESSAGE.formatted(officerId));
                    return null;
                });
    }
}
