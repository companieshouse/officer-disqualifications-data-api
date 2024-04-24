package uk.gov.companieshouse.disqualifiedofficersdataapi.api;

import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpResponseException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.chskafka.ChangedResource;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.chskafka.PrivateChangedResourceHandler;
import uk.gov.companieshouse.api.handler.chskafka.request.PrivateChangedResourcePost;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.disqualifiedofficersdataapi.exceptions.ServiceUnavailableException;
import uk.gov.companieshouse.logging.Logger;

import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DisqualifiedOfficerApiClientServiceTest {

    @Mock
    private ApiClientService apiClientService;

    @Mock
    private InternalApiClient internalApiClient;

    @Mock
    private PrivateChangedResourceHandler privateChangedResourceHandler;

    @Mock
    private PrivateChangedResourcePost changedResourcePost;

    @Mock
    private ApiResponse<Void> response;

    @Mock
    private Logger logger;

    @Mock
    private Supplier<String> dateGenerator;

    @Mock
    private Function<ResourceChangedRequest, ChangedResource> mapper;

    @Mock
    private ResourceChangedRequest resourceChangedRequest;

    @Mock
    private ChangedResource changedResource;

    @InjectMocks
    private DisqualifiedOfficerApiService disqualifiedOfficerApiService;

    @Test
    void should_invoke_chs_kafka_endpoint_successfully() throws ApiErrorResponseException {
        when(apiClientService.getInternalApiClient()).thenReturn(internalApiClient);
        when(internalApiClient.privateChangedResourceHandler()).thenReturn(privateChangedResourceHandler);
        when(privateChangedResourceHandler.postChangedResource(Mockito.any(), Mockito.any())).thenReturn(changedResourcePost);
        when(changedResourcePost.execute()).thenReturn(response);
        when(mapper.apply(resourceChangedRequest)).thenReturn(changedResource);

        ApiResponse<?> apiResponse =
                disqualifiedOfficerApiService.invokeChsKafkaApi(resourceChangedRequest);

        Assertions.assertThat(apiResponse).isNotNull();

        verify(apiClientService, times(1)).getInternalApiClient();
        verify(internalApiClient, times(1)).privateChangedResourceHandler();
        verify(privateChangedResourceHandler, times(1)).postChangedResource("/private/resource-changed", changedResource);
        verify(changedResourcePost, times(1)).execute();
    }

    @Test
    void should_handle_exception_when_chs_kafka_endpoint_throws_503() throws ApiErrorResponseException {

        setupExceptionScenario(503, "Service Unavailable");

        assertThrows(ServiceUnavailableException.class, () -> disqualifiedOfficerApiService.invokeChsKafkaApi(resourceChangedRequest));

        verifyExceptionScenario();
    }

    @Test
    void should_handle_exception_when_chs_kafka_endpoint_throws_500() throws ApiErrorResponseException {

        setupExceptionScenario(500, "Internal Service Error");

        assertThrows(ServiceUnavailableException.class, () -> disqualifiedOfficerApiService.invokeChsKafkaApi(resourceChangedRequest));

        verifyExceptionScenario();
    }

    @Test
    void should_handle_exception_when_chs_kafka_endpoint_throws_error_with_200() throws ApiErrorResponseException {

        setupExceptionScenario(200, "");

        assertThrows(RuntimeException.class, () -> disqualifiedOfficerApiService.invokeChsKafkaApi(resourceChangedRequest));

        verifyExceptionScenario();
    }

    private void setupExceptionScenario(int statusCode, String statusMessage) throws ApiErrorResponseException {
        when(apiClientService.getInternalApiClient()).thenReturn(internalApiClient);
        when(internalApiClient.privateChangedResourceHandler()).thenReturn(privateChangedResourceHandler);
        when(privateChangedResourceHandler.postChangedResource(Mockito.any(), Mockito.any())).thenReturn(changedResourcePost);
        when(mapper.apply(resourceChangedRequest)).thenReturn(changedResource);

        HttpResponseException.Builder builder = new HttpResponseException.Builder(statusCode,
                statusMessage, new HttpHeaders());
        ApiErrorResponseException apiErrorResponseException =
                new ApiErrorResponseException(builder);
        when(changedResourcePost.execute()).thenThrow(apiErrorResponseException);
    }

    private void verifyExceptionScenario() throws ApiErrorResponseException {
        verify(apiClientService, times(1)).getInternalApiClient();
        verify(internalApiClient, times(1)).privateChangedResourceHandler();
        verify(privateChangedResourceHandler, times(1)).postChangedResource("/private/resource-changed",
                changedResource);
        verify(changedResourcePost, times(1)).execute();
    }
}
