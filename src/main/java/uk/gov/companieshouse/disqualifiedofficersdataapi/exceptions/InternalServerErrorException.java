package uk.gov.companieshouse.disqualifiedofficersdataapi.exceptions;

public class InternalServerErrorException extends RuntimeException {

    public InternalServerErrorException(String msg) {
        super(msg);
    }
}
