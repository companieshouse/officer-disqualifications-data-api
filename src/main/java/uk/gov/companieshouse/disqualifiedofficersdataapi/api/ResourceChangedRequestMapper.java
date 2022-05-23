package uk.gov.companieshouse.disqualifiedofficersdataapi.api;

import java.util.function.Supplier;

import uk.gov.companieshouse.api.chskafka.ChangedResource;
import uk.gov.companieshouse.api.chskafka.ChangedResourceEvent;
import uk.gov.companieshouse.disqualifiedofficersdataapi.model.CorporateDisqualificationDocument;
import uk.gov.companieshouse.disqualifiedofficersdataapi.model.DisqualificationResourceType;
import uk.gov.companieshouse.disqualifiedofficersdataapi.model.NaturalDisqualificationDocument;


public class ResourceChangedRequestMapper {

    private final Supplier<String> timestampGenerator;

    public ResourceChangedRequestMapper(Supplier<String> timestampGenerator) {
        this.timestampGenerator = timestampGenerator;
    }

    public ChangedResource mapChangedResource(ResourceChangedRequest request) {
        ChangedResource changedResource = new ChangedResource();

        if (request.getType() == DisqualificationResourceType.NATURAL) {
            changedResource.setResourceUri("/disqualified-officers/natural/" + request.getOfficerId());
            changedResource.setResourceKind("disqualified-officer-natural");
        } else if (request.getType() == DisqualificationResourceType.CORPORATE) {
            changedResource.setResourceUri("/disqualified-officers/corporate/" + request.getOfficerId());
            changedResource.setResourceKind("disqualified-officer-corporate");
        } else {
            throw new IllegalStateException("Unknown disqualification type");
        }

        ChangedResourceEvent event = new ChangedResourceEvent();
        event.setType("changed");
        event.publishedAt(this.timestampGenerator.get());

        changedResource.event(event);
        changedResource.setContextId(request.getContextId());

        return changedResource;
    }

    public ChangedResource mapChangedResource(ResourceChangedRequest request, CorporateDisqualificationDocument document) {
        ChangedResource changedResource = new ChangedResource();

        changedResource.setResourceUri("/disqualified-officers/corporate/" + request.getOfficerId());
        changedResource.setResourceKind("disqualified-officer-corporate");

        ChangedResourceEvent event = new ChangedResourceEvent();
        event.setType("deleted");
        changedResource.setDeletedData(document.getData());
        event.publishedAt(this.timestampGenerator.get());

        changedResource.event(event);
        changedResource.setContextId(request.getContextId());

        return changedResource;
    }

    public ChangedResource mapChangedResource(ResourceChangedRequest request, NaturalDisqualificationDocument document) {
        ChangedResource changedResource = new ChangedResource();
        changedResource.setResourceUri("/disqualified-officers/natural/" + request.getOfficerId());
        changedResource.setResourceKind("disqualified-officer-natural");

        ChangedResourceEvent event = new ChangedResourceEvent();
        event.setType("deleted");
        changedResource.setDeletedData(document.getData());
        event.publishedAt(this.timestampGenerator.get());

        changedResource.event(event);
        changedResource.setContextId(request.getContextId());

        return changedResource;
    }
}
