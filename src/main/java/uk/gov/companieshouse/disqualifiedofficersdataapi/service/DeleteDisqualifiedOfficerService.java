package uk.gov.companieshouse.disqualifiedofficersdataapi.service;

import static uk.gov.companieshouse.disqualifiedofficersdataapi.DisqualifiedOfficersDataApiApplication.NAMESPACE;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.disqualifiedofficersdataapi.api.DisqualifiedOfficerApiService;
import uk.gov.companieshouse.disqualifiedofficersdataapi.api.ResourceChangedRequest;
import uk.gov.companieshouse.disqualifiedofficersdataapi.exceptions.BadRequestException;
import uk.gov.companieshouse.disqualifiedofficersdataapi.model.DisqualificationResourceType;
import uk.gov.companieshouse.disqualifiedofficersdataapi.repository.DisqualifiedOfficerRepository;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
public class DeleteDisqualifiedOfficerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);

    private final DisqualifiedOfficerRepository repository;
    private final DisqualifiedOfficerApiService disqualifiedOfficerApiService;
    private final DeletionDataService deletionDataService;

    public DeleteDisqualifiedOfficerService(DisqualifiedOfficerRepository repository,
            DisqualifiedOfficerApiService disqualifiedOfficerApiService, DeletionDataService deletionDataService) {
        this.repository = repository;
        this.disqualifiedOfficerApiService = disqualifiedOfficerApiService;
        this.deletionDataService = deletionDataService;
    }

    public void deleteDisqualification(String contextId, String officerId, String requestDeltaAt, String officerType) {
        final DisqualificationResourceType type;
        try {
            type = DisqualificationResourceType.valueOfOfficerType(officerType);
        } catch (IllegalArgumentException ex) {
            final String msg = "Invalid officer type used in URI: [%s]".formatted(officerType);
            LOGGER.error(msg, ex);
            throw new BadRequestException(msg, ex);
        }

        Object data;
        if (type == DisqualificationResourceType.CORPORATE) {
            data = deletionDataService.processCorporateDisqualificationData(officerId, requestDeltaAt);
        } else {
            data = deletionDataService.processNaturalDisqualificationData(officerId, requestDeltaAt);
        }
        repository.deleteById(officerId);
        LOGGER.info(
                String.format(
                        "Disqualification deleted in MongoDb for context id: %s and officer id: %s",
                        contextId,
                        officerId));

        disqualifiedOfficerApiService.invokeChsKafkaApi(new ResourceChangedRequest(
                contextId, officerId, type, data, true));
        LOGGER.info(
                String.format("ChsKafka api DELETED invoked updated successfully for context id: %s and officer id: %s",
                        contextId,
                        officerId));
    }
}
