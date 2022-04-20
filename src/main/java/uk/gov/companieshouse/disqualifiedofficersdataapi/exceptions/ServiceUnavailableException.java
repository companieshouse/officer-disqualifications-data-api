package uk.gov.companieshouse.disqualifiedofficersdataapi.exceptions;

public class ServiceUnavailableException extends RuntimeException {
    public ServiceUnavailableException(String message) {
        super(message);
    }
}