package uk.gov.companieshouse.disqualifiedofficersdataapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FeatureFlags {

    private final boolean streamHookEnabled;

    public FeatureFlags(@Value("${chs.kafka.api.enabled}") boolean streamHookEnabled) {
        this.streamHookEnabled = streamHookEnabled;
    }

    public boolean isStreamHookEnabled() {
        return streamHookEnabled;
    }
}
