package org.gaussian.vgraph.exception;

public class FetcherExecutionException extends RuntimeException {

  public String fetcherError;

  public FetcherExecutionException() {
    super();
  }

  public FetcherExecutionException(String message) {
    super(message);
  }

  public FetcherExecutionException(String message, Throwable cause) {
    super(message, cause);
  }

  public FetcherExecutionException(String message, String fetcherError) {
    super(message);
    this.fetcherError = fetcherError;
  }
}
