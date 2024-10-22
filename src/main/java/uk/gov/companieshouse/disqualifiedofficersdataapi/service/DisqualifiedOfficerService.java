package uk.gov.companieshouse.disqualifiedofficersdataapi.service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.disqualification.CorporateDisqualificationApi.KindEnum;
import uk.gov.companieshouse.api.disqualification.InternalCorporateDisqualificationApi;
import uk.gov.companieshouse.api.disqualification.InternalNaturalDisqualificationApi;
import uk.gov.companieshouse.disqualifiedofficersdataapi.api.DisqualifiedOfficerApiService;
import uk.gov.companieshouse.disqualifiedofficersdataapi.api.ResourceChangedRequest;
import uk.gov.companieshouse.disqualifiedofficersdataapi.exceptions.BadRequestException;
import uk.gov.companieshouse.disqualifiedofficersdataapi.model.CorporateDisqualificationDocument;
import uk.gov.companieshouse.disqualifiedofficersdataapi.model.Created;
import uk.gov.companieshouse.disqualifiedofficersdataapi.model.DisqualificationDocument;
import uk.gov.companieshouse.disqualifiedofficersdataapi.model.DisqualificationResourceType;
import uk.gov.companieshouse.disqualifiedofficersdataapi.model.NaturalDisqualificationDocument;
import uk.gov.companieshouse.disqualifiedofficersdataapi.repository.CorporateDisqualifiedOfficerRepository;
import uk.gov.companieshouse.disqualifiedofficersdataapi.repository.DisqualifiedOfficerRepository;
import uk.gov.companieshouse.disqualifiedofficersdataapi.repository.NaturalDisqualifiedOfficerRepository;
import uk.gov.companieshouse.disqualifiedofficersdataapi.transform.DisqualificationTransformer;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Service
public class DisqualifiedOfficerService {

    public static final String APPLICATION_NAME_SPACE = "disqualified-officers-data-api";
    private static final Logger logger = LoggerFactory.getLogger(APPLICATION_NAME_SPACE);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSSSSS")
            .withZone(ZoneOffset.UTC);
    private static final String RESOURCE_NOT_FOUND_FOR_OFFICER_ID = "Resource not found for officer ID: %s";
    private static final String STALE_DELTA_AT_MESSAGE = "Delta at field on request is stale";

    private final DisqualifiedOfficerRepository repository;

    private final NaturalDisqualifiedOfficerRepository naturalRepository;

    private final CorporateDisqualifiedOfficerRepository corporateRepository;

    private final DisqualificationTransformer transformer;

    private final DisqualifiedOfficerApiService disqualifiedOfficerApiService;

    public DisqualifiedOfficerService(DisqualifiedOfficerRepository repository,
            NaturalDisqualifiedOfficerRepository naturalRepository,
            CorporateDisqualifiedOfficerRepository corporateRepository, DisqualificationTransformer transformer,
            DisqualifiedOfficerApiService disqualifiedOfficerApiService) {
        this.repository = repository;
        this.naturalRepository = naturalRepository;
        this.corporateRepository = corporateRepository;
        this.transformer = transformer;
        this.disqualifiedOfficerApiService = disqualifiedOfficerApiService;
    }

    /**
     * Save or update a natural disqualification
     *
     * @param contextId   Id used for chsKafkaCall
     * @param officerId   Id used for mongo record
     * @param requestBody Data to be saved
     */
    public void processNaturalDisqualification(String contextId, String officerId,
            InternalNaturalDisqualificationApi requestBody) {
        Optional<DisqualificationDocument> existingDocument = repository.findById(officerId);

        if (existingDocument.isEmpty() ||
                isLatestRecord(requestBody.getInternalData().getDeltaAt(), existingDocument.get())) {
            DisqualificationDocument document = transformer.transformNaturalDisqualifiedOfficer(officerId, requestBody);
            saveAndCallChsKafka(contextId, officerId, document, DisqualificationResourceType.NATURAL,
                    existingDocument.orElse(null));
        } else {
            logger.info(STALE_DELTA_AT_MESSAGE);
        }
    }

    /**
     * Save or update a corporate disqualification
     *
     * @param contextId   Id used for chsKafkaCall
     * @param officerId   Id used for mongo record
     * @param requestBody Data to be saved
     */
    public void processCorporateDisqualification(String contextId, String officerId,
            InternalCorporateDisqualificationApi requestBody) {
        Optional<DisqualificationDocument> existingDocument = repository.findById(officerId);

        if (existingDocument.isEmpty() ||
                isLatestRecord(requestBody.getInternalData().getDeltaAt(), existingDocument.get())) {
            DisqualificationDocument document = transformer.transformCorporateDisqualifiedOfficer(officerId,
                    requestBody);
            saveAndCallChsKafka(contextId, officerId, document, DisqualificationResourceType.CORPORATE,
                    existingDocument.orElse(null));
        } else {
            logger.info(STALE_DELTA_AT_MESSAGE);
        }
    }

    /**
     * Find and delete a disqualification record.
     *
     * @param contextId passed into the call to changed-resource
     * @param officerId used to find the document to delete
     */
    public void deleteDisqualification(String contextId, String officerId) {
        DisqualificationDocument document = retrieveDeleteDisqualification(officerId);
        if (document.isCorporateOfficer()) {
            deleteCorporateDisqualification(contextId, officerId);
        } else {
            deleteNaturalDisqualification(contextId, officerId);
        }
    }

    /**
     * Delete a corporate disqualification record.
     *
     * @param contextId passed into the call to changed-resource
     * @param officerId used to find the document to delete
     */
    private void deleteCorporateDisqualification(String contextId, String officerId) {
        CorporateDisqualificationDocument document = retrieveCorporateDisqualification(officerId);

        document.getData().setKind(KindEnum.CORPORATE_DISQUALIFICATION);

        repository.delete(document);
        logger.info(
                String.format("Corporate disqualification is deleted in MongoDb for context id: %s and officer id: %s",
                        contextId,
                        officerId));

        disqualifiedOfficerApiService.invokeChsKafkaApi(
                new ResourceChangedRequest(contextId, officerId,
                        DisqualificationResourceType.CORPORATE, document.getData(), true));
        logger.info(
                String.format("ChsKafka api DELETED invoked updated successfully for context id: %s and officer id: %s",
                        contextId,
                        officerId));
    }

    /**
     * Delete a natural disqualification record.
     *
     * @param contextId passed into the call to changed-resource
     * @param officerId used to find the document to delete
     */
    private void deleteNaturalDisqualification(String contextId, String officerId) {
        NaturalDisqualificationDocument document = retrieveNaturalDisqualification(officerId);

        document.getData().setKind(uk.gov.companieshouse.api.disqualification.NaturalDisqualificationApi.
                KindEnum.NATURAL_DISQUALIFICATION);

        repository.delete(document);
        logger.info(
                String.format("Natural disqualification is deleted in MongoDb for context id: %s and officer id: %s",
                        contextId,
                        officerId));

        disqualifiedOfficerApiService.invokeChsKafkaApi(
                new ResourceChangedRequest(contextId, officerId,
                        DisqualificationResourceType.NATURAL, document.getData(), true));
        logger.info(
                String.format("ChsKafka api DELETED invoked updated successfully for context id: %s and officer id: %s",
                        contextId,
                        officerId));
    }

    private boolean isLatestRecord(OffsetDateTime deltaAt, DisqualificationDocument existingDocument) {
        return StringUtils.isBlank(existingDocument.getDeltaAt()) ||
                !deltaAt.isBefore(ZonedDateTime.parse(existingDocument.getDeltaAt(), FORMATTER)
                        .toOffsetDateTime());
    }

    /**
     * Save or update the mongo record
     *
     * @param contextId Chs kafka id
     * @param officerId Mongo id
     * @param document  Transformed Data
     */
    private void saveAndCallChsKafka(
            String contextId, String officerId,
            DisqualificationDocument document, DisqualificationResourceType type,
            DisqualificationDocument existingDocument) {

        Optional.ofNullable(existingDocument)
                .map(DisqualificationDocument::getCreated)
                .ifPresentOrElse(document::setCreated,
                        () -> document.setCreated(new Created().setAt(document.getUpdated().getAt())));

        try {
            repository.save(document);
            logger.info(String.format("Disqualification is updated in MongoDb for context id: %s and officer id: %s",
                    contextId,
                    officerId));
        } catch (IllegalArgumentException illegalArgumentEx) {
            throw new BadRequestException(illegalArgumentEx.getMessage());
        }

        disqualifiedOfficerApiService.invokeChsKafkaApi(
                new ResourceChangedRequest(contextId, officerId,
                        type, null, false));
        logger.info(
                String.format("ChsKafka api CHANGED invoked updated successfully for context id: %s and officer id: %s",
                        contextId,
                        officerId));
    }

    public DisqualificationDocument retrieveDeleteDisqualification(String officerId) {
        Optional<DisqualificationDocument> disqualificationDocumentOptional =
                repository.findById(officerId);
        return disqualificationDocumentOptional.orElseThrow(
                () -> new IllegalArgumentException(String.format(
                        RESOURCE_NOT_FOUND_FOR_OFFICER_ID, officerId)));
    }

    public NaturalDisqualificationDocument retrieveNaturalDisqualification(String officerId) {
        Optional<NaturalDisqualificationDocument> disqualificationDocumentOptional =
                naturalRepository.findById(officerId);
        NaturalDisqualificationDocument disqualificationDocument = disqualificationDocumentOptional.orElseThrow(
                () -> new IllegalArgumentException(String.format(
                        RESOURCE_NOT_FOUND_FOR_OFFICER_ID, officerId)));
        if (disqualificationDocument.isCorporateOfficer()) {
            throw new IllegalArgumentException(String.format(
                    "Natural resource not found for officer ID: %s", officerId));
        }
        return disqualificationDocument;
    }

    public CorporateDisqualificationDocument retrieveCorporateDisqualification(String officerId) {
        Optional<CorporateDisqualificationDocument> disqualificationDocumentOptional =
                corporateRepository.findById(officerId);
        CorporateDisqualificationDocument disqualificationDocument = disqualificationDocumentOptional.orElseThrow(
                () -> new IllegalArgumentException(String.format(
                        RESOURCE_NOT_FOUND_FOR_OFFICER_ID, officerId)));
        if (!disqualificationDocument.isCorporateOfficer()) {
            throw new IllegalArgumentException(String.format(
                    "Corporate resource not found for officer ID: %s", officerId));
        }
        return disqualificationDocument;
    }

}
