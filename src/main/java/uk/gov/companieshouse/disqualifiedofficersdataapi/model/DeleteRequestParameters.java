package uk.gov.companieshouse.disqualifiedofficersdataapi.model;

public record DeleteRequestParameters(String contextId, String officerId, String requestDeltaAt, String officerType) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String contextId;
        private String officerId;
        private String requestDeltaAt;
        private String officerType;

        private Builder() {}

        public Builder officerType(String officerType) {
            this.officerType = officerType;
            return this;
        }

        public Builder requestDeltaAt(String requestDeltaAt) {
            this.requestDeltaAt = requestDeltaAt;
            return this;
        }

        public Builder officerId(String officerId) {
            this.officerId = officerId;
            return this;
        }

        public Builder contextId(String contextId) {
            this.contextId = contextId;
            return this;
        }

        public DeleteRequestParameters build() {
            return new DeleteRequestParameters(contextId, officerId, requestDeltaAt, officerType);
        }
    }
}
