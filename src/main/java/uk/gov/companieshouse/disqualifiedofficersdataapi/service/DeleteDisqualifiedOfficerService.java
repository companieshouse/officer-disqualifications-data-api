package uk.gov.companieshouse.disqualifiedofficersdataapi.service;

import static uk.gov.companieshouse.disqualifiedofficersdataapi.DisqualifiedOfficersDataApiApplication.NAMESPACE;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.disqualifiedofficersdataapi.api.DisqualifiedOfficerApiService;
import uk.gov.companieshouse.disqualifiedofficersdataapi.api.ResourceChangedRequest;
import uk.gov.companieshouse.disqualifiedofficersdataapi.exceptions.BadGatewayException;
import uk.gov.companieshouse.disqualifiedofficersdataapi.exceptions.BadRequestException;
import uk.gov.companieshouse.disqualifiedofficersdataapi.logging.DataMapHolder;
import uk.gov.companieshouse.disqualifiedofficersdataapi.model.DeleteRequestParameters;
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

    public void deleteDisqualification(DeleteRequestParameters deleteRequestParameters) {
        final String officerType = deleteRequestParameters.officerType();
        final String officerId = deleteRequestParameters.officerId();
        final String requestDeltaAt = deleteRequestParameters.requestDeltaAt();
        final String contextId = deleteRequestParameters.contextId();

        final DisqualificationResourceType type;
        try {
            type = DisqualificationResourceType.valueOfOfficerType(officerType);
        } catch (IllegalArgumentException ex) {
            final String msg = "Invalid officer type used in URI";
            LOGGER.error(msg, ex, DataMapHolder.getLogMap());
            throw new BadRequestException(msg, ex);
        }

        Object data;
        if (type == DisqualificationResourceType.CORPORATE) {
            data = deletionDataService.processCorporateDisqualificationData(officerId, requestDeltaAt);
        } else {
            data = deletionDataService.processNaturalDisqualificationData(officerId, requestDeltaAt);
        }
        if (data != null) {
            LOGGER.info("Attempting to delete disqualification", DataMapHolder.getLogMap());
            try {
                repository.deleteById(officerId);
            } catch (TransientDataAccessException ex) {
                LOGGER.info("Recoverable MongoDB error when deleting document", DataMapHolder.getLogMap());
                throw new BadGatewayException("Recoverable MongoDB error when deleting document", ex);
            } catch (DataAccessException ex) {
                LOGGER.error("MongoDB error when deleting document", ex, DataMapHolder.getLogMap());
                throw new BadGatewayException("MongoDB error when deleting document", ex);
            }
            disqualifiedOfficerApiService.invokeChsKafkaApi(new ResourceChangedRequest(
                    contextId, officerId, type, data, true));
        } else {
            disqualifiedOfficerApiService.invokeChsKafkaApi(new ResourceChangedRequest(
                    contextId, officerId, type, null, true));
        }
    }
}
