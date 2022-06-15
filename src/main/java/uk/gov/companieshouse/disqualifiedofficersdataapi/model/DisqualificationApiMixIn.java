package uk.gov.companieshouse.disqualifiedofficersdataapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(value = {"person_number"}, allowGetters = true)
public interface DisqualificationApiMixIn {
}
