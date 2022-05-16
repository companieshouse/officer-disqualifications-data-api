package uk.gov.companieshouse.disqualifiedofficersdataapi.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpResponseException;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.chskafka.ChangedResource;
import uk.gov.companieshouse.api.chskafka.ChangedResourceEvent;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.chskafka.PrivateChangedResourceHandler;
import uk.gov.companieshouse.api.handler.chskafka.request.PrivateChangedResourcePost;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.disqualifiedofficersdataapi.exceptions.MethodNotAllowedException;
import uk.gov.companieshouse.disqualifiedofficersdataapi.exceptions.ServiceUnavailableException;
import uk.gov.companieshouse.disqualifiedofficersdataapi.model.DisqualificationResourceType;
import uk.gov.companieshouse.logging.Logger;

import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DisqualifiedOfficerApiClientServiceTest {

    private static final String EXPECTED_CONTEXT_ID = "35234234";

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

    @InjectMocks
    private DisqualifiedOfficerApiService disqualifiedOfficerApiService;

    @ParameterizedTest(name = "Create a new resource changed resource with type {0}")
    @MethodSource("resourceChangedScenarios")
    void should_invoke_chs_kafka_endpoint_successfully(ResourceChangeTestArgument type) throws ApiErrorResponseException {

        when(apiClientService.getInternalApiClient()).thenReturn(internalApiClient);
        when(internalApiClient.privateChangedResourceHandler()).thenReturn(privateChangedResourceHandler);
        when(privateChangedResourceHandler.postChangedResource(Mockito.any(), Mockito.any())).thenReturn(changedResourcePost);
        when(changedResourcePost.execute()).thenReturn(response);
        when(dateGenerator.get()).thenReturn("date");

        ApiResponse<?> apiResponse = disqualifiedOfficerApiService.invokeChsKafkaApi(EXPECTED_CONTEXT_ID,
                "CH4000056", type.getResourceType());

        Assertions.assertThat(apiResponse).isNotNull();

        verify(apiClientService, times(1)).getInternalApiClient();
        verify(internalApiClient, times(1)).privateChangedResourceHandler();
        verify(privateChangedResourceHandler, times(1)).postChangedResource("/resource-changed", type.getChangedResource());
        verify(changedResourcePost, times(1)).execute();
    }

    @Test
    void should_handle_exception_when_chs_kafka_endpoint_throws_exception() throws ApiErrorResponseException {

        when(apiClientService.getInternalApiClient()).thenReturn(internalApiClient);
        when(internalApiClient.privateChangedResourceHandler()).thenReturn(privateChangedResourceHandler);
        when(privateChangedResourceHandler.postChangedResource(Mockito.any(), Mockito.any())).thenReturn(changedResourcePost);
        when(changedResourcePost.execute()).thenThrow(RuntimeException.class);


        Assert.assertThrows(RuntimeException.class, () -> disqualifiedOfficerApiService.invokeChsKafkaApi
                ("3245435", "CH4000056", DisqualificationResourceType.NATURAL));

        verify(apiClientService, times(1)).getInternalApiClient();
        verify(internalApiClient, times(1)).privateChangedResourceHandler();
        verify(privateChangedResourceHandler, times(1)).postChangedResource(Mockito.any(),
                Mockito.any());
        verify(changedResourcePost, times(1)).execute();
    }

    @ParameterizedTest
    @MethodSource("provideExceptionParameters")
    void should_handle_exception_when_chs_kafka_endpoint_throws_appropriate_exception(int statusCode, String statusMessage, Class<Throwable> exception) throws ApiErrorResponseException {
        when(apiClientService.getInternalApiClient()).thenReturn(internalApiClient);
        when(internalApiClient.privateChangedResourceHandler()).thenReturn(privateChangedResourceHandler);
        when(privateChangedResourceHandler.postChangedResource(Mockito.any(), Mockito.any())).thenReturn(changedResourcePost);

        HttpResponseException.Builder builder = new HttpResponseException.Builder(statusCode,
                statusMessage, new HttpHeaders());
        ApiErrorResponseException apiErrorResponseException =
                new ApiErrorResponseException(builder);
        when(changedResourcePost.execute()).thenThrow(apiErrorResponseException);

        Assert.assertThrows(exception,
                () -> disqualifiedOfficerApiService.invokeChsKafkaApi
                        ("3245435", "CH4000056", DisqualificationResourceType.NATURAL));

        verify(apiClientService, times(1)).getInternalApiClient();
        verify(internalApiClient, times(1)).privateChangedResourceHandler();
        verify(privateChangedResourceHandler, times(1)).postChangedResource(Mockito.any(),
                Mockito.any());
        verify(changedResourcePost, times(1)).execute();
    }

    private static Stream<Arguments> provideExceptionParameters() {
        return Stream.of(
                Arguments.of(503, "Service Unavailable", ServiceUnavailableException.class),
                Arguments.of(405, "Method Not Allowed", MethodNotAllowedException.class),
                Arguments.of(500, "Internal Service Error", RuntimeException.class)
        );
    }

    static Stream<ResourceChangeTestArgument> resourceChangedScenarios() {
        return Stream.of(
                ResourceChangeTestArgument.builder()
                        .withResourceType(DisqualificationResourceType.NATURAL)
                        .withContextId(EXPECTED_CONTEXT_ID)
                        .withResourceUri("/disqualified-officers/natural/CH4000056")
                        .withResourceKind("disqualified-officer-natural")
                        .withEventType("changed")
                        .withEventPublishedAt("date")
                        .build(),
                ResourceChangeTestArgument.builder()
                        .withResourceType(DisqualificationResourceType.CORPORATE)
                        .withContextId(EXPECTED_CONTEXT_ID)
                        .withResourceUri("/disqualified-officers/corporate/CH4000056")
                        .withResourceKind("disqualified-officer-corporate")
                        .withEventType("changed")
                        .withEventPublishedAt("date")
                        .build()
        );
    }

    static class ResourceChangeTestArgument {
        private final DisqualificationResourceType resourceType;
        private final ChangedResource changedResource;

        public ResourceChangeTestArgument(DisqualificationResourceType resourceType, ChangedResource changedResource) {
            this.resourceType = resourceType;
            this.changedResource = changedResource;
        }

        public DisqualificationResourceType getResourceType() {
            return resourceType;
        }

        public ChangedResource getChangedResource() {
            return changedResource;
        }

        public static ResourceChangeTestArgumentBuilder builder() {
            return new ResourceChangeTestArgumentBuilder();
        }

        @Override
        public String toString() {
            return this.resourceType.toString();
        }
    }

    static class ResourceChangeTestArgumentBuilder {
        private DisqualificationResourceType resourceType;
        private String resourceUri;
        private String resourceKind;
        private String contextId;
        private String eventType;
        private String eventPublishedAt;

        public ResourceChangeTestArgumentBuilder withResourceType(DisqualificationResourceType resourceType) {
            this.resourceType = resourceType;
            return this;
        }

        public ResourceChangeTestArgumentBuilder withResourceUri(String resourceUri) {
            this.resourceUri = resourceUri;
            return this;
        }

        public ResourceChangeTestArgumentBuilder withResourceKind(String resourceKind) {
            this.resourceKind = resourceKind;
            return this;
        }

        public ResourceChangeTestArgumentBuilder withContextId(String contextId) {
            this.contextId = contextId;
            return this;
        }

        public ResourceChangeTestArgumentBuilder withEventType(String eventType) {
            this.eventType = eventType;
            return this;
        }

        public ResourceChangeTestArgumentBuilder withEventPublishedAt(String eventPublishedAt) {
            this.eventPublishedAt = eventPublishedAt;
            return this;
        }

        public ResourceChangeTestArgument build() {
            ChangedResource changedResource = new ChangedResource();
            changedResource.setResourceUri(this.resourceUri);
            changedResource.setResourceKind(this.resourceKind);
            changedResource.setContextId(this.contextId);
            ChangedResourceEvent event = new ChangedResourceEvent();
            event.setType(this.eventType);
            event.setPublishedAt(this.eventPublishedAt);
            changedResource.setEvent(event);
            return new ResourceChangeTestArgument(this.resourceType, changedResource);
        }
    }
}
