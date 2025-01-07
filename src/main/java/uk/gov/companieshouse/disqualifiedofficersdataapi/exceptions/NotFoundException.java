package uk.gov.companieshouse.disqualifiedofficersdataapi.exceptions;

public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
