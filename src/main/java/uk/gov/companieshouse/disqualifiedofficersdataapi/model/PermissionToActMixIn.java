package uk.gov.companieshouse.disqualifiedofficersdataapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(value = {"purpose"}, allowGetters = true)
public interface PermissionToActMixIn {
}
