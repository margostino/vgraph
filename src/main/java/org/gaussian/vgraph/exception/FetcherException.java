package org.gaussian.vgraph.exception;

public class FetcherException extends RuntimeException {

    public FetcherException() {
        super();
    }

    public FetcherException(String message) {
        super(message);
    }

    public FetcherException(String message, Throwable cause) {
        super(message, cause);
    }
}
