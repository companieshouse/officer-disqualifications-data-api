package uk.gov.companieshouse.disqualifiedofficersdataapi.api;

import java.util.Objects;
import uk.gov.companieshouse.disqualifiedofficersdataapi.model.DisqualificationResourceType;

public class ResourceChangedRequest {

    private final String contextId;
    private final String officerId;
    private final DisqualificationResourceType type;
    private final Object disqualificationData;
    private final Boolean isDelete;

    public ResourceChangedRequest(String contextId, String officerId, DisqualificationResourceType type, 
            Object disqualificationData, Boolean isDelete) {
        this.contextId = contextId;
        this.officerId = officerId;
        this.type = type;
        this.disqualificationData = disqualificationData;
        this.isDelete = isDelete;
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

    public Object getDisqualificationData() {
        return disqualificationData;
    }

    public Boolean getIsDelete() {
        return isDelete;
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
                officerId, that.officerId) && type == that.type &&
                disqualificationData == that.disqualificationData &&
                isDelete == that.isDelete;
    }

    @Override
    public int hashCode() {
        return Objects.hash(contextId, officerId, type, disqualificationData, isDelete);
    }
}
