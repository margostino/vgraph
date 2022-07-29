package org.gaussian.vgraph.exception;

public class FetcherTimeoutException extends FetcherException {

    public FetcherTimeoutException() {
        super();
    }

    public FetcherTimeoutException(String message) {
        super(message);
    }

    public FetcherTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
