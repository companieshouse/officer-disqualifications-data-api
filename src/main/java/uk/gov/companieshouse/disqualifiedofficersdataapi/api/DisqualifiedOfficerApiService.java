package uk.gov.companieshouse.disqualifiedofficersdataapi.api;

import static uk.gov.companieshouse.disqualifiedofficersdataapi.DisqualifiedOfficersDataApiApplication.NAMESPACE;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.chskafka.ChangedResource;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.chskafka.request.PrivateChangedResourcePost;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.disqualifiedofficersdataapi.exceptions.BadGatewayException;
import uk.gov.companieshouse.disqualifiedofficersdataapi.logging.DataMapHolder;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import java.util.function.Function;

@Service
public class DisqualifiedOfficerApiService {

    private static final String CHANGED_RESOURCE_URI = "/private/resource-changed";
    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);
    private final String chsKafkaUrl;
    private final ApiClientService apiClientService;
    private final Function<ResourceChangedRequest, ChangedResource> mapper;

    /**
     * Invoke API.
     */
    public DisqualifiedOfficerApiService(@Value("${chs.kafka.api.endpoint}") String chsKafkaUrl,
            ApiClientService apiClientService,
            Function<ResourceChangedRequest, ChangedResource> mapper) {
        this.chsKafkaUrl = chsKafkaUrl;
        this.apiClientService = apiClientService;
        this.mapper = mapper;
    }


    /**
     * Calls the CHS Kafka api.
     * @param resourceChangedRequest encapsulates details relating to the updated or deleted officer
     * @return the response from the kafka api
     */
    public ApiResponse<Void> invokeChsKafkaApi(ResourceChangedRequest resourceChangedRequest) {
        InternalApiClient internalApiClient = apiClientService.getInternalApiClient();
        internalApiClient.setBasePath(chsKafkaUrl);
        internalApiClient.getHttpClient().setRequestId(DataMapHolder.getRequestId());

        PrivateChangedResourcePost changedResourcePost =
                internalApiClient.privateChangedResourceHandler().postChangedResource(
                        CHANGED_RESOURCE_URI, mapper.apply(resourceChangedRequest));

        return handleApiCall(changedResourcePost);
    }

    private ApiResponse<Void> handleApiCall(PrivateChangedResourcePost changedResourcePost) {
        try {
            return changedResourcePost.execute();
        } catch (ApiErrorResponseException ex) {
            DataMapHolder.get().status(Integer.toString(ex.getStatusCode()));
            LOGGER.info("Resource changed call failed: %s".formatted(ex.getStatusCode()), DataMapHolder.getLogMap());
            throw new BadGatewayException("Error calling resource changed endpoint");
            }
        }
}
