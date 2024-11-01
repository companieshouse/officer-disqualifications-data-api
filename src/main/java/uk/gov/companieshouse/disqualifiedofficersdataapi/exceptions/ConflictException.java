package uk.gov.companieshouse.disqualifiedofficersdataapi.exceptions;

public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
