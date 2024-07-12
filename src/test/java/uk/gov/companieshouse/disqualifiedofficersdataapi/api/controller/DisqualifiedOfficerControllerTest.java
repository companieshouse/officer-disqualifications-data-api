package uk.gov.companieshouse.disqualifiedofficersdataapi.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.dockerjava.api.exception.InternalServerErrorException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.companieshouse.api.disqualification.InternalDisqualificationApiInternalData;
import uk.gov.companieshouse.api.disqualification.InternalNaturalDisqualificationApi;
import uk.gov.companieshouse.api.disqualification.NaturalDisqualificationApi;
import uk.gov.companieshouse.disqualifiedofficersdataapi.config.ExceptionHandlerConfig;
import uk.gov.companieshouse.disqualifiedofficersdataapi.config.WebSecurityConfig;
import uk.gov.companieshouse.disqualifiedofficersdataapi.controller.DisqualifiedOfficerController;
import uk.gov.companieshouse.disqualifiedofficersdataapi.exceptions.BadRequestException;
import uk.gov.companieshouse.disqualifiedofficersdataapi.exceptions.MethodNotAllowedException;
import uk.gov.companieshouse.disqualifiedofficersdataapi.exceptions.ServiceUnavailableException;
import uk.gov.companieshouse.disqualifiedofficersdataapi.model.NaturalDisqualificationDocument;
import uk.gov.companieshouse.disqualifiedofficersdataapi.service.DisqualifiedOfficerService;
import uk.gov.companieshouse.logging.Logger;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = DisqualifiedOfficerController.class)
@ContextConfiguration(classes = {DisqualifiedOfficerController.class, ExceptionHandlerConfig.class})
@Import({WebSecurityConfig.class})
class DisqualifiedOfficerControllerTest {
    private static final String OFFICER_ID = "02588581";
    private static final String NATURAL = "natural";
    private static final String DELETE = "delete";
    private static final String NATURAL_URL = String.format("/disqualified-officers/%s/%s/internal", NATURAL, OFFICER_ID);
    private static final String NATURAL_GET_URL = String.format("/disqualified-officers/%s/%s", NATURAL, OFFICER_ID);
    private static final String DELETE_URL = String.format("/disqualified-officers/%s/%s/internal", DELETE, OFFICER_ID);

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private Logger logger;

    @MockBean
    private DisqualifiedOfficerService disqualifiedOfficerService;

    @Autowired
    private ObjectMapper objectMapper;

    private ObjectMapper mapper = new ObjectMapper();

    private final Gson gson = new GsonBuilder().setPrettyPrinting()
            .excludeFieldsWithoutExposeAnnotation()
            .create();

    @BeforeEach
    void setUp() {
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @Test
    @DisplayName("Disqualified Officer PUT request")
    void callDisqualifiedOfficerPutRequest() throws Exception {
        InternalNaturalDisqualificationApi request = new InternalNaturalDisqualificationApi();
        request.setInternalData(new InternalDisqualificationApiInternalData());
        request.setExternalData(new NaturalDisqualificationApi());

        doNothing().when(disqualifiedOfficerService).processNaturalDisqualification(anyString(), anyString(),
                isA(InternalNaturalDisqualificationApi.class));

        mockMvc.perform(put(NATURAL_URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "5342342")
                        .header("ERIC-Identity", "Test-Identity")
                        .header("ERIC-Identity-Type", "Key")
                        .header("ERIC-Authorised-Key-Privileges", "internal-app")
                        .content(gson.toJson(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Disqualified Officer PUT request fails with missing ERIC-Authorised-Key-Privileges")
    void callDisqualifiedOfficerPutRequestMissingAuthorisation() throws Exception {
        InternalNaturalDisqualificationApi request = new InternalNaturalDisqualificationApi();
        request.setInternalData(new InternalDisqualificationApiInternalData());
        request.setExternalData(new NaturalDisqualificationApi());

        doNothing().when(disqualifiedOfficerService).processNaturalDisqualification(anyString(), anyString(),
                isA(InternalNaturalDisqualificationApi.class));

        mockMvc.perform(put(NATURAL_URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "5342342")
                        .header("ERIC-Identity", "SOME_IDENTITY")
                        .header("ERIC-Identity-Type", "key")
                        .content(gson.toJson(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Disqualified Officer PUT request fails with wrong privileges")
    void callDisqualifiedOfficerPutRequestWrongPrivileges() throws Exception {
        InternalNaturalDisqualificationApi request = new InternalNaturalDisqualificationApi();
        request.setInternalData(new InternalDisqualificationApiInternalData());
        request.setExternalData(new NaturalDisqualificationApi());

        doNothing().when(disqualifiedOfficerService).processNaturalDisqualification(anyString(), anyString(),
                isA(InternalNaturalDisqualificationApi.class));

        mockMvc.perform(put(NATURAL_URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "5342342")
                        .header("ERIC-Identity", "SOME_IDENTITY")
                        .header("ERIC-Identity-Type", "key")
                        .header("ERIC-Authorised-Key-Privileges", "privilege")
                        .content(gson.toJson(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Disqualified Officer PUT request fails with oauth2 authorisation")
    void callDisqualifiedOfficerPutRequestWrongAuthorisation() throws Exception {
        InternalNaturalDisqualificationApi request = new InternalNaturalDisqualificationApi();
        request.setInternalData(new InternalDisqualificationApiInternalData());
        request.setExternalData(new NaturalDisqualificationApi());

        doNothing().when(disqualifiedOfficerService).processNaturalDisqualification(anyString(), anyString(),
                isA(InternalNaturalDisqualificationApi.class));

        mockMvc.perform(put(NATURAL_URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "5342342")
                        .header("ERIC-Identity", "SOME_IDENTITY")
                        .header("ERIC-Identity-Type", "oauth2")
                        .content(gson.toJson(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Disqualified Officer PUT request fails with oauth2 authorisation and internal app privileges")
    void callDisqualifiedOfficerPutRequestWrongAuthorisationWithPrivileges() throws Exception {
        InternalNaturalDisqualificationApi request = new InternalNaturalDisqualificationApi();
        request.setInternalData(new InternalDisqualificationApiInternalData());
        request.setExternalData(new NaturalDisqualificationApi());

        doNothing().when(disqualifiedOfficerService).processNaturalDisqualification(anyString(), anyString(),
                isA(InternalNaturalDisqualificationApi.class));

        mockMvc.perform(put(NATURAL_URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "5342342")
                        .header("ERIC-Identity", "SOME_IDENTITY")
                        .header("ERIC-Identity-Type", "oauth2")
                        .header("ERIC-Authorised-Key-Privileges", "internal-app")
                        .content(gson.toJson(request)))
                .andExpect(status().isForbidden());
    }


    @Test
    @DisplayName("Disqualified Officer PUT request - IllegalArgumentException status code 404 not found")
    void callDisqualifiedOfficerPutRequestIllegalArgument() throws Exception {
        InternalNaturalDisqualificationApi request = new InternalNaturalDisqualificationApi();
        request.setInternalData(new InternalDisqualificationApiInternalData());
        request.setExternalData(new NaturalDisqualificationApi());

        doThrow(new IllegalArgumentException())
                .when(disqualifiedOfficerService).processNaturalDisqualification(anyString(), anyString(),
                        isA(InternalNaturalDisqualificationApi.class));

        mockMvc.perform(put(NATURAL_URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "5342342")
                        .header("ERIC-Identity", "Test-Identity")
                        .header("ERIC-Identity-Type", "Key")
                        .header("ERIC-Authorised-Key-Privileges", "internal-app")
                        .content(gson.toJson(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Disqualified Officer PUT request - BadRequestException status code 400")
    void callDisqualifiedOfficerPutRequestBadRequest() throws Exception {
        InternalNaturalDisqualificationApi request = new InternalNaturalDisqualificationApi();
        request.setInternalData(new InternalDisqualificationApiInternalData());
        request.setExternalData(new NaturalDisqualificationApi());

        doThrow(new BadRequestException("Bad request - data in wrong format"))
                .when(disqualifiedOfficerService).processNaturalDisqualification(anyString(), anyString(),
                        isA(InternalNaturalDisqualificationApi.class));

        mockMvc.perform(put(NATURAL_URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "5342342")
                        .header("ERIC-Identity", "Test-Identity")
                        .header("ERIC-Identity-Type", "Key")
                        .header("ERIC-Authorised-Key-Privileges", "internal-app")
                        .content(gson.toJson(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Disqualified Officer PUT request - MethodNotAllowed status code 405")
    void callDisqualifiedOfficerPutRequestMethodNotAllowed() throws Exception {
        InternalNaturalDisqualificationApi request = new InternalNaturalDisqualificationApi();
        request.setInternalData(new InternalDisqualificationApiInternalData());
        request.setExternalData(new NaturalDisqualificationApi());

        doThrow(new MethodNotAllowedException(String.format("Method Not Allowed - unsuccessful call to %s endpoint", NATURAL_URL)))
                .when(disqualifiedOfficerService).processNaturalDisqualification(anyString(), anyString(),
                        isA(InternalNaturalDisqualificationApi.class));

        mockMvc.perform(put(NATURAL_URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "5342342")
                        .header("ERIC-Identity", "Test-Identity")
                        .header("ERIC-Identity-Type", "Key")
                        .header("ERIC-Authorised-Key-Privileges", "internal-app")
                        .content(gson.toJson(request)))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @DisplayName("Disqualified Officer PUT request - InternalServerError status code 500")
    void callDisqualifiedOfficerPutRequestInternalServerError() throws Exception {
        InternalNaturalDisqualificationApi request = new InternalNaturalDisqualificationApi();
        request.setInternalData(new InternalDisqualificationApiInternalData());
        request.setExternalData(new NaturalDisqualificationApi());

        doThrow(new InternalServerErrorException("Internal Server Error - unexpected error"))
                .when(disqualifiedOfficerService).processNaturalDisqualification(anyString(), anyString(),
                        isA(InternalNaturalDisqualificationApi.class));

        mockMvc.perform(put(NATURAL_URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "5342342")
                        .header("ERIC-Identity", "Test-Identity")
                        .header("ERIC-Identity-Type", "Key")
                        .header("ERIC-Authorised-Key-Privileges", "internal-app")
                        .content(gson.toJson(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Disqualified Officer PUT request - ServiceUnavailable status code 503")
    void callDisqualifiedOfficerPutRequestServiceUnavailable() throws Exception {
        InternalNaturalDisqualificationApi request = new InternalNaturalDisqualificationApi();
        request.setInternalData(new InternalDisqualificationApiInternalData());
        request.setExternalData(new NaturalDisqualificationApi());

        doThrow(new ServiceUnavailableException("Service Unavailable - connection issues"))
                .when(disqualifiedOfficerService).processNaturalDisqualification(anyString(), anyString(),
                        isA(InternalNaturalDisqualificationApi.class));

        mockMvc.perform(put(NATURAL_URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "5342342")
                        .header("ERIC-Identity", "Test-Identity")
                        .header("ERIC-Identity-Type", "Key")
                        .header("ERIC-Authorised-Key-Privileges", "internal-app")
                        .content(gson.toJson(request)))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    @DisplayName("Disqualified Officer PUT request - Forbidden status code 403")
    void callDisqualifiedOfficerPutRequestForbidden() throws Exception {
        InternalNaturalDisqualificationApi request = new InternalNaturalDisqualificationApi();
        request.setInternalData(new InternalDisqualificationApiInternalData());
        request.setExternalData(new NaturalDisqualificationApi());

        doNothing().when(disqualifiedOfficerService).processNaturalDisqualification(anyString(), anyString(),
                isA(InternalNaturalDisqualificationApi.class));

        mockMvc.perform(put(NATURAL_URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "5342342")
                        .content(gson.toJson(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Disqualified Officer DELETE request")
    void callDisqualifiedOfficerDeleteRequest() throws Exception {

        doNothing()
                .when(disqualifiedOfficerService).deleteDisqualification(anyString(), anyString());

        mockMvc.perform(delete(DELETE_URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "5342342")
                        .header("ERIC-Identity", "Test-Identity")
                        .header("ERIC-Identity-Type", "Key")
                        .header("ERIC-Authorised-Key-Privileges", "internal-app"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Disqualified Officer DELETE request - ServiceUnavailable status code 503")
    void callDisqualifiedOfficerDeleteRequestServiceUnavailable() throws Exception {

        doThrow(new ServiceUnavailableException("Service Unavailable - connection issues"))
                .when(disqualifiedOfficerService).deleteDisqualification(anyString(), anyString());

        mockMvc.perform(delete(DELETE_URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "5342342")
                        .header("ERIC-Identity", "Test-Identity")
                        .header("ERIC-Identity-Type", "Key")
                        .header("ERIC-Authorised-Key-Privileges", "internal-app"))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    @DisplayName("Disqualified Officer GET request")
    void callDisqualifiedOfficerGetRequest() throws Exception {
        NaturalDisqualificationDocument naturalDisqualification = new NaturalDisqualificationDocument();
        NaturalDisqualificationApi data = new NaturalDisqualificationApi();
        naturalDisqualification.setData(data);

        doReturn(naturalDisqualification)
                .when(disqualifiedOfficerService).retrieveNaturalDisqualification(anyString());

        MvcResult result = mockMvc.perform(get(NATURAL_GET_URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "5342342")
                        .header("ERIC-Identity", "SOME_IDENTITY")
                        .header("ERIC-Identity-Type", "key"))
                .andExpect(status().isOk())
                .andReturn();

        assertEquals(data, objectMapper.readValue(result.getResponse().getContentAsString(), NaturalDisqualificationApi.class));
    }

    @Test
    @DisplayName("Disqualified Officer GET request with oauth2 success")
    void callDisqualifiedOfficerGetRequestOauth2() throws Exception {
        NaturalDisqualificationDocument naturalDisqualification = new NaturalDisqualificationDocument();
        NaturalDisqualificationApi data = new NaturalDisqualificationApi();
        naturalDisqualification.setData(data);

        doReturn(naturalDisqualification)
                .when(disqualifiedOfficerService).retrieveNaturalDisqualification(anyString());

        mockMvc.perform(get(NATURAL_GET_URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "5342342")
                        .header("ERIC-Identity", "SOME_IDENTITY")
                        .header("ERIC-Identity-Type", "oauth2"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DisqualifiedOfficer GET request - DocumentNotFoundException status code 404 resource not found")
    void callDisqualifiedOfficerGetRequestDocumentnotFound() throws Exception {
        doThrow(new IllegalArgumentException("Document not found"))
                .when(disqualifiedOfficerService).retrieveNaturalDisqualification(anyString());

        mockMvc.perform(get(NATURAL_GET_URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "5342342")
                        .header("ERIC-Identity", "SOME_IDENTITY")
                        .header("ERIC-Identity-Type", "key"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Disqualified Officer OPTIONS request - CORS")
    void callDisqualifiedOfficerOptionsRequestCORS() throws Exception {

        MvcResult result = mockMvc.perform(options(NATURAL_GET_URL)
                .contentType(APPLICATION_JSON)
                .header("Origin", "")
                )
            .andExpect(status().isNoContent())
            .andExpect(header().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN))
            .andExpect(header().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS))
            .andExpect(header().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS))
            .andExpect(header().exists(HttpHeaders.ACCESS_CONTROL_MAX_AGE))
            .andReturn();
    }

    @Test
    @DisplayName("Disqualified Officer GET request - CORS")
    void callDisqualifiedOfficerGetRequestCORS() throws Exception {
        NaturalDisqualificationDocument naturalDisqualification = new NaturalDisqualificationDocument();
        NaturalDisqualificationApi data = new NaturalDisqualificationApi();
        naturalDisqualification.setData(data);

        doReturn(naturalDisqualification)
                .when(disqualifiedOfficerService).retrieveNaturalDisqualification(anyString());

        MvcResult result = mockMvc.perform(get(NATURAL_GET_URL)
                        .contentType(APPLICATION_JSON)
                        .header("Origin", "")
                        .header("ERIC-Allowed-Origin", "some-origin")
                        .header("x-request-id", "5342342")
                        .header("ERIC-Identity", "SOME_IDENTITY")
                        .header("ERIC-Identity-Type", "key")
                        )
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, containsString("GET")))
                .andReturn();

        assertEquals(data, objectMapper.readValue(result.getResponse().getContentAsString(), NaturalDisqualificationApi.class));
    }

    @Test
    @DisplayName("Forbidden Disqualified Officer GET request - CORS")
    void getCompanyExemptionsForbiddenCORS() throws Exception {

        MvcResult result = mockMvc.perform(get(NATURAL_GET_URL)
                .contentType(APPLICATION_JSON)
                .header("Origin", "")
                .header("ERIC-Allowed-Origin", "")
                .header("x-request-id", "5342342")
                .header("ERIC-Identity", "Test-Identity")
                .header("ERIC-Identity-Type", "oauth2"))
            .andExpect(status().isForbidden())
            .andExpect(header().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS))
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, containsString("GET")))
            .andExpect(content().string(""))
            .andReturn();
    }

    @Test
    @DisplayName("Disqualified Officer PUT request - CORS")
    void callDisqualifiedOfficerPutRequestCORS() throws Exception {
        InternalNaturalDisqualificationApi request = new InternalNaturalDisqualificationApi();
        request.setInternalData(new InternalDisqualificationApiInternalData());
        request.setExternalData(new NaturalDisqualificationApi());

        doNothing().when(disqualifiedOfficerService).processNaturalDisqualification(anyString(), anyString(),
                isA(InternalNaturalDisqualificationApi.class));

        mockMvc.perform(put(NATURAL_URL)
                        .contentType(APPLICATION_JSON)
                        .header("Origin", "")
                        .header("ERIC-Allowed-Origin", "some-origin")
                        .header("x-request-id", "5342342")
                        .header("ERIC-Identity", "Test-Identity")
                        .header("ERIC-Identity-Type", "Key")
                        .header("ERIC-Authorised-Key-Privileges", "internal-app")
                        .content(gson.toJson(request)))
                .andExpect(status().isForbidden())
                .andExpect(header().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, containsString("GET")))
                .andExpect(content().string(""))
                .andReturn();
    }

}
