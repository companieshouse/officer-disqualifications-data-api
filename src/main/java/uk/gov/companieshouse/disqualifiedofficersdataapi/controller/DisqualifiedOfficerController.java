package uk.gov.companieshouse.disqualifiedofficersdataapi.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import uk.gov.companieshouse.api.disqualification.CorporateDisqualificationApi;
import uk.gov.companieshouse.api.disqualification.InternalCorporateDisqualificationApi;
import uk.gov.companieshouse.api.disqualification.InternalNaturalDisqualificationApi;
import uk.gov.companieshouse.api.disqualification.NaturalDisqualificationApi;
import uk.gov.companieshouse.api.disqualification.PermissionToAct;
import uk.gov.companieshouse.disqualifiedofficersdataapi.model.*;
import uk.gov.companieshouse.disqualifiedofficersdataapi.service.DisqualifiedOfficerService;

import uk.gov.companieshouse.logging.Logger;

@RestController
public class DisqualifiedOfficerController {

    @Autowired
    private Logger logger;
    @Autowired
    private DisqualifiedOfficerService service;


    /**
     * PUT request to save or update a Natural Disqualified Officer.
     *
     * @param  officerId  the id for the disqualified officer
     * @param  requestBody  the request body containing disqualified officer data
     * @return  no response
     */
    @PutMapping("/disqualified-officers/natural/{officer_id}/internal")
    public ResponseEntity<Void> naturalDisqualifiedOfficer(
            @RequestHeader("x-request-id") String contextId,
            @PathVariable("officer_id") String officerId,
            @RequestBody InternalNaturalDisqualificationApi requestBody
    ) throws JsonProcessingException {
        logger.info(String.format(
                "Processing disqualified officer information for officer id %s",
                officerId));

        service.processNaturalDisqualification(contextId, officerId, requestBody);

        return ResponseEntity.status(HttpStatus.OK).build();
    }



    /**
     * PUT request to save or update a Corporate Disqualified Officers.
     *
     * @param  officerId  the id for the disqualified officer
     * @param  requestBody  the request body containing disqualified officer data
     * @return  no response
     */
    @PutMapping("/disqualified-officers/corporate/{officer_id}/internal")
    public ResponseEntity<Void> corporateDisqualifiedOfficer(
            @RequestHeader("x-request-id") String contextId,
            @PathVariable("officer_id") String officerId,
            @RequestBody InternalCorporateDisqualificationApi requestBody
    ) throws JsonProcessingException {
        logger.info(String.format(
                "Processing disqualified officer information for officer id %s",
                officerId));

        service.processCorporateDisqualification(contextId, officerId, requestBody);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Retrieve natural disqualified officer information for a officer ID.
     *
     * @param  officerId  the officer ID for the disqualification
     * @return NaturalDisqualificationDocument return natural disqualified officer information
     */
    @GetMapping("/disqualified-officers/natural/{officer_id}")
    public ResponseEntity<NaturalDisqualificationApi> naturalDisqualification(
            @PathVariable("officer_id") final String officerId) {
        logger.info(String.format(
                "Retrieving natural officer disqualification information for officer ID %s",
                officerId));

        NaturalDisqualificationDocument disqualification = service.retrieveNaturalDisqualification(officerId);
        NaturalDisqualificationApi data = disqualification.getData();
        for (PermissionToAct permissionToAct : data.getPermissionsToAct()) {
            permissionToAct.setPurpose(null);
        }
        data.setPersonNumber(null);

        return ResponseEntity.status(HttpStatus.OK).body(disqualification.getData());
    }

    /**
     * Retrieve corporate disqualified officer information for a officer ID.
     *
     * @param  officerId  the officer ID for the disqualification
     * @return CorporateDisqualificationDocument return corporate disqualified officer information
     */
    @GetMapping("/disqualified-officers/corporate/{officer_id}")
    public ResponseEntity<CorporateDisqualificationApi> corporateDisqualification(
            @PathVariable("officer_id") String officerId) {
        logger.info(String.format(
                "Retrieving corporate officer disqualification information for officer ID %s",
                officerId));

        CorporateDisqualificationDocument disqualification = service.retrieveCorporateDisqualification(
                officerId);
        CorporateDisqualificationApi data = disqualification.getData();
        for (PermissionToAct permissionToAct : data.getPermissionsToAct()) {
            permissionToAct.setPurpose(null);
        }
        data.setPersonNumber(null);

        return ResponseEntity.status(HttpStatus.OK).body(disqualification.getData());
    }
}
