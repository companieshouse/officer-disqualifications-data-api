package uk.gov.companieshouse.disqualifiedofficersdataapi.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.dockerjava.api.exception.InternalServerErrorException;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import uk.gov.companieshouse.api.disqualification.InternalDisqualificationApiInternalData;
import uk.gov.companieshouse.api.disqualification.InternalNaturalDisqualificationApi;
import uk.gov.companieshouse.api.disqualification.NaturalDisqualificationApi;
import uk.gov.companieshouse.disqualifiedofficersdataapi.config.ExceptionHandlerConfig;
import uk.gov.companieshouse.disqualifiedofficersdataapi.controller.DisqualifiedOfficerController;
import uk.gov.companieshouse.disqualifiedofficersdataapi.exceptions.BadRequestException;
import uk.gov.companieshouse.disqualifiedofficersdataapi.exceptions.MethodNotAllowedException;
import uk.gov.companieshouse.disqualifiedofficersdataapi.exceptions.ServiceUnavailableException;
import uk.gov.companieshouse.disqualifiedofficersdataapi.service.DisqualifiedOfficerService;
import uk.gov.companieshouse.logging.Logger;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = DisqualifiedOfficerController.class)
@ContextConfiguration(classes = {DisqualifiedOfficerController.class, ExceptionHandlerConfig.class})
class DisqualifiedOfficerControllerTest {
    private static final String OFFICER_ID = "02588581";
    private static final String NATURAL = "natural";
    private static final String NATURAL_URL = String.format("/disqualified-officers/%s/%s/internal", NATURAL, OFFICER_ID);

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private Logger logger;

    @MockBean
    private DisqualifiedOfficerService disqualifiedOfficerService;

    private ObjectMapper mapper = new ObjectMapper();

    private Gson gson = new Gson();

    @BeforeEach
    void setUp() {
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @Test
    @DisplayName("Disqualified Officer PUT request")
    public void callDisqualifiedOfficerPutRequest() throws Exception {
        InternalNaturalDisqualificationApi request = new InternalNaturalDisqualificationApi();
        request.setInternalData(new InternalDisqualificationApiInternalData());
        request.setExternalData(new NaturalDisqualificationApi());

        doNothing().when(disqualifiedOfficerService).processNaturalDisqualification(anyString(), anyString(),
                isA(InternalNaturalDisqualificationApi.class));

        mockMvc.perform(put(NATURAL_URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "5342342")
                        .content(gson.toJson(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Disqualified Officer PUT request - IllegalArgumentException status code 404 not found")
    public void callDisqualifiedOfficerPutRequestIllegalArgument() throws Exception {
        InternalNaturalDisqualificationApi request = new InternalNaturalDisqualificationApi();
        request.setInternalData(new InternalDisqualificationApiInternalData());
        request.setExternalData(new NaturalDisqualificationApi());

        doThrow(new IllegalArgumentException())
                .when(disqualifiedOfficerService).processNaturalDisqualification(anyString(), anyString(),
                        isA(InternalNaturalDisqualificationApi.class));

        mockMvc.perform(put(NATURAL_URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "5342342")
                        .content(gson.toJson(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Disqualified Officer PUT request - BadRequestException status code 400")
    public void callDisqualifiedOfficerPutRequestBadRequest() throws Exception {
        InternalNaturalDisqualificationApi request = new InternalNaturalDisqualificationApi();
        request.setInternalData(new InternalDisqualificationApiInternalData());
        request.setExternalData(new NaturalDisqualificationApi());

        doThrow(new BadRequestException("Bad request - data in wrong format"))
                        .when(disqualifiedOfficerService).processNaturalDisqualification(anyString(), anyString(),
                                        isA(InternalNaturalDisqualificationApi.class));

        mockMvc.perform(put(NATURAL_URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "5342342")
                        .content(gson.toJson(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Disqualified Officer PUT request - MethodNotAllowed status code 405")
    public void callDisqualifiedOfficerPutRequestMethodNotAllowed() throws Exception {
        InternalNaturalDisqualificationApi request = new InternalNaturalDisqualificationApi();
        request.setInternalData(new InternalDisqualificationApiInternalData());
        request.setExternalData(new NaturalDisqualificationApi());

        doThrow(new MethodNotAllowedException(String.format("Method Not Allowed - unsuccessful call to %s endpoint", NATURAL_URL)))
                .when(disqualifiedOfficerService).processNaturalDisqualification(anyString(), anyString(),
                        isA(InternalNaturalDisqualificationApi.class));

        mockMvc.perform(put(NATURAL_URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "5342342")
                        .content(gson.toJson(request)))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @DisplayName("Disqualified Officer PUT request - InternalServerError status code 500")
    public void callDisqualifiedOfficerPutRequestInternalServerError() throws Exception {
        InternalNaturalDisqualificationApi request = new InternalNaturalDisqualificationApi();
        request.setInternalData(new InternalDisqualificationApiInternalData());
        request.setExternalData(new NaturalDisqualificationApi());

        doThrow(new InternalServerErrorException("Internal Server Error - unexpected error"))
                .when(disqualifiedOfficerService).processNaturalDisqualification(anyString(), anyString(),
                        isA(InternalNaturalDisqualificationApi.class));

        mockMvc.perform(put(NATURAL_URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "5342342")
                        .content(gson.toJson(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Disqualified Officer PUT request - ServiceUnavailable status code 503")
    public void callDisqualifiedOfficerPutRequestServiceUnavailable() throws Exception {
        InternalNaturalDisqualificationApi request = new InternalNaturalDisqualificationApi();
        request.setInternalData(new InternalDisqualificationApiInternalData());
        request.setExternalData(new NaturalDisqualificationApi());

        doThrow(new ServiceUnavailableException("Service Unavailable - connection issues"))
                .when(disqualifiedOfficerService).processNaturalDisqualification(anyString(), anyString(),
                        isA(InternalNaturalDisqualificationApi.class));

        mockMvc.perform(put(NATURAL_URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "5342342")
                        .content(gson.toJson(request)))
                .andExpect(status().isServiceUnavailable());
    }
}
