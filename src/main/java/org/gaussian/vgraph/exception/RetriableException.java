package org.gaussian.vgraph.exception;

public class RetriableException extends FetcherException {
    private final Throwable throwable;

    public RetriableException(Throwable throwable) {
        this.throwable = throwable;
    }
}
