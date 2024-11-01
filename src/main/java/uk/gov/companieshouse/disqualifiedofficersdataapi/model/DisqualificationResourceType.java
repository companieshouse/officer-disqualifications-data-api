package uk.gov.companieshouse.disqualifiedofficersdataapi.model;

import java.util.HashMap;
import java.util.Map;

public enum DisqualificationResourceType {

    NATURAL("natural"),
    CORPORATE("corporate");

    private static final Map<String, DisqualificationResourceType> BY_OFFICER_TYPE = new HashMap<>();

    static {
        for (DisqualificationResourceType type : values()) {
            BY_OFFICER_TYPE.put(type.officerType, type);
        }
    }

    private final String officerType;

    DisqualificationResourceType(String officerType) {
        this.officerType = officerType;
    }

    public static DisqualificationResourceType valueOfOfficerType(final String input) {
        return BY_OFFICER_TYPE.computeIfAbsent(input, (k) -> {
            throw new IllegalArgumentException();
        });
    }
}

