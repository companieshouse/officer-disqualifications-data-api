package uk.gov.companieshouse.disqualifiedofficersdataapi.api;

import java.util.Objects;
import uk.gov.companieshouse.disqualifiedofficersdataapi.model.DisqualificationResourceType;

public class ResourceChangedRequest {

    private final String contextId;
    private final String officerId;
    private final DisqualificationResourceType type;

    public ResourceChangedRequest(String contextId, String officerId, DisqualificationResourceType type) {
        this.contextId = contextId;
        this.officerId = officerId;
        this.type = type;
    }

    public String getContextId() {
        return contextId;
    }

    public String getOfficerId() {
        return officerId;
    }

    public DisqualificationResourceType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ResourceChangedRequest that = (ResourceChangedRequest) o;
        return Objects.equals(contextId, that.contextId) && Objects.equals(
                officerId, that.officerId) && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(contextId, officerId, type);
    }
}
