package uk.gov.companieshouse.disqualifiedofficersdataapi.model;

public enum DisqualificationResourceType {
    NATURAL("natural", "disqualified-officer-natural"),
    CORPORATE("corporate", "disqualified-officer-corporate");

    private final String pathIdentification;
    private final String resourceKindIdentification;

    DisqualificationResourceType(String pathIdentification, String resourceKindIdentification) {
        this.pathIdentification = pathIdentification;
        this.resourceKindIdentification = resourceKindIdentification;
    }

    public String getPathIdentification() {
        return pathIdentification;
    }

    public String getResourceKindIdentification() {
        return resourceKindIdentification;
    }
}

