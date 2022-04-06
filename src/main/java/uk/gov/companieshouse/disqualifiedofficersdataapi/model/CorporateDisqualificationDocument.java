package uk.gov.companieshouse.disqualifiedofficersdataapi.model;

import uk.gov.companieshouse.api.disqualification.CorporateDisqualificationApi;

public class CorporateDisqualificationDocument extends DisqualificationDocument {

    private CorporateDisqualificationApi data;

    public CorporateDisqualificationApi getData() {
        return data;
    }

    public CorporateDisqualificationDocument setData(CorporateDisqualificationApi data) {
        this.data = data;
        return this;
    }
}
