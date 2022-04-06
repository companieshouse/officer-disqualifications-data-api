package uk.gov.companieshouse.disqualifiedofficersdataapi.model;

import uk.gov.companieshouse.api.disqualification.NaturalDisqualificationApi;

public class NaturalDisqualificationDocument extends DisqualificationDocument {

    private NaturalDisqualificationApi data;

    public NaturalDisqualificationApi getData() {
        return data;
    }

    public NaturalDisqualificationDocument setData(NaturalDisqualificationApi data) {
        this.data = data;
        return this;
    }
}
