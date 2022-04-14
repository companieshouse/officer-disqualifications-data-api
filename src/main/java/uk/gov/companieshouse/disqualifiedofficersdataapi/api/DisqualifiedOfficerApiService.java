package uk.gov.companieshouse.disqualifiedofficersdataapi.api;

import java.time.OffsetDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.chskafka.ChangedResource;
import uk.gov.companieshouse.api.chskafka.ChangedResourceEvent;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.chskafka.request.PrivateChangedResourcePost;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.disqualifiedofficersdataapi.exceptions.MethodNotAllowedException;
import uk.gov.companieshouse.disqualifiedofficersdataapi.exceptions.ServiceUnavailableException;
import uk.gov.companieshouse.logging.Logger;

@Service
public class DisqualifiedOfficerApiService {

    private static final String CHANGED_RESOURCE_URI = "/resource-changed";
    private final Logger logger;
    private final String chsKafkaUrl;
    private final ApiClientService apiClientService;

    /**
     * Invoke Insolvency API.
     */
    public DisqualifiedOfficerApiService(@Value("${chs.kafka.api.endpoint}") String chsKafkaUrl,
                                ApiClientService apiClientService, Logger logger) {
        this.chsKafkaUrl = chsKafkaUrl;
        this.apiClientService = apiClientService;
        this.logger = logger;
    }

    /**
     * Calls the CHS Kafka api.
     * @param contextId the kafka context id
     * @param officerId the officer id for the record in question
     * @param type the type of officer, corporate or natural disqualified
     * @return the respons from the kafka api
     */
    public ApiResponse<Void> invokeChsKafkaApi(String contextId, String officerId, String type) {
        InternalApiClient internalApiClient = apiClientService.getInternalApiClient();
        internalApiClient.setBasePath(chsKafkaUrl);

        PrivateChangedResourcePost changedResourcePost =
                internalApiClient.privateChangedResourceHandler().postChangedResource(
                        CHANGED_RESOURCE_URI, mapChangedResource(contextId, officerId, type));

        try {
            return changedResourcePost.execute();
        } catch (ApiErrorResponseException exp) {
            HttpStatus statusCode = HttpStatus.valueOf(exp.getStatusCode());
            if (!statusCode.is2xxSuccessful() && statusCode != HttpStatus.SERVICE_UNAVAILABLE) {
                logger.error("Unsuccessful call to /resource-changed endpoint", exp);
                throw new MethodNotAllowedException(exp.getMessage());
            } else if (statusCode == HttpStatus.SERVICE_UNAVAILABLE) {
                logger.error("Service unavailable while calling /resource-changed endpoint", exp);
                throw new ServiceUnavailableException(exp.getMessage());
            } else {
                logger.error("Error occurred while calling /resource-changed endpoint", exp);
                throw new RuntimeException(exp);
            }
        }
    }

    private ChangedResource mapChangedResource(String contextId, String officerId, String officerType) {
        String resourceUri = "/disqualified-officers/" + officerType + "/" + officerId;

        ChangedResourceEvent event = new ChangedResourceEvent();
        event.setType("changed");
        event.publishedAt(String.valueOf(OffsetDateTime.now()));

        ChangedResource changedResource = new ChangedResource();
        changedResource.setResourceUri(resourceUri);
        changedResource.event(event);
        changedResource.setResourceKind("disqualified-officers");
        changedResource.setContextId(contextId);

        return changedResource;
    }

}
