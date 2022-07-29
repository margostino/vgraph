package org.gaussian.vgraph.exception;

public class MissingRequestParameterException extends RuntimeException {

  private final String requestParameter;
  private final String fetcherName;

  public MissingRequestParameterException(String fetcherName, String requestParameter) {
    super(getErrorMessage(fetcherName, requestParameter));
    this.fetcherName = fetcherName;
    this.requestParameter = requestParameter;
  }

  private static String getErrorMessage(String fetcherName, String requestParameter) {
    return String.format("Missing request parameter: [%s] for fetcher: %s", requestParameter, fetcherName);
  }

  public String getFetcherName() {
    return fetcherName;
  }

  public String getRequestParameter() {
    return requestParameter;
  }
}
