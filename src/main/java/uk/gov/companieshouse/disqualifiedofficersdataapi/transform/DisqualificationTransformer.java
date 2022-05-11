package uk.gov.companieshouse.disqualifiedofficersdataapi.transform;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.GenerateEtagUtil;
import uk.gov.companieshouse.api.disqualification.InternalCorporateDisqualificationApi;
import uk.gov.companieshouse.api.disqualification.InternalDisqualificationApiInternalData;
import uk.gov.companieshouse.api.disqualification.InternalNaturalDisqualificationApi;
import uk.gov.companieshouse.disqualifiedofficersdataapi.model.CorporateDisqualificationDocument;
import uk.gov.companieshouse.disqualifiedofficersdataapi.model.DisqualificationDocument;
import uk.gov.companieshouse.disqualifiedofficersdataapi.model.NaturalDisqualificationDocument;
import uk.gov.companieshouse.disqualifiedofficersdataapi.model.Updated;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class DisqualificationTransformer {

    private final DateTimeFormatter dateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSSSSS");

    /**
     * Transform the internal data class to a mongo ready document
     * @param officerId     Mongo Id
     * @param requestBody   Internal data class
     * @return document     Mongo wrapper document
     */
    public DisqualificationDocument transformNaturalDisqualifiedOfficer(
            String officerId, InternalNaturalDisqualificationApi requestBody) {

        NaturalDisqualificationDocument document = new NaturalDisqualificationDocument();

        requestBody.getExternalData().setEtag(GenerateEtagUtil.generateEtag());

        document.setData(requestBody.getExternalData())
                .setId(officerId)
                .setCorporateOfficer(false);

        return transformDisqualifiedOfficer(document, requestBody.getInternalData());
    }

    /**
     * Transform the internal data class to a mongo ready document
     * @param officerId     Mongo Id
     * @param requestBody   Internal data class
     * @return document     Mongo wrapper document
     */
    public DisqualificationDocument transformCorporateDisqualifiedOfficer(
            String officerId, InternalCorporateDisqualificationApi requestBody) {

        CorporateDisqualificationDocument document = new CorporateDisqualificationDocument();

        requestBody.getExternalData().setEtag(GenerateEtagUtil.generateEtag());

        document.setData(requestBody.getExternalData())
                .setId(officerId)
                .setCorporateOfficer(true);

        return transformDisqualifiedOfficer(document, requestBody.getInternalData());
    }

    /**
     * Complete officer type inspecific mappings
     * @param document     Mongo wrapper document
     * @param internalData Internal data class
     * @return document
     */
    private DisqualificationDocument transformDisqualifiedOfficer(
            DisqualificationDocument document,
            InternalDisqualificationApiInternalData internalData) {

        OffsetDateTime deltaAt = internalData.getDeltaAt();

        document.setUpdated(new Updated().setAt(LocalDateTime.now()))
                .setOfficerIdRaw(internalData.getOfficerIdRaw())
                .setOfficerDetailId(internalData.getOfficerDetailId())
                .setOfficerDisqId(internalData.getOfficerDisqId())
                .setDeltaAt(dateTimeFormatter.format(deltaAt));
        return document;
    }
}
