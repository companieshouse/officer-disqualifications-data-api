package uk.gov.companieshouse.disqualifiedofficersdataapi.exceptions;

public class BadGatewayException extends RuntimeException {

    public BadGatewayException(String message) {
        super(message);
    }

    public BadGatewayException(String message, Throwable ex) {
        super(message, ex);
    }
}
