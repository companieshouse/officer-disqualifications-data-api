package uk.gov.companieshouse.disqualifiedofficersdataapi.exceptions;

public class MethodNotAllowedException extends RuntimeException {
    public MethodNotAllowedException(String message) {
        super(message);
    }
}