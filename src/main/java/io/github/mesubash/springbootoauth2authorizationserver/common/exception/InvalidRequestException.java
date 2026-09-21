package io.github.mesubash.springbootoauth2authorizationserver.common.exception;

public class InvalidRequestException extends RuntimeException {

    public InvalidRequestException(String message) {
        super(message);
    }
}