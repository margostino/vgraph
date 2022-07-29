package org.gaussian.vgraph.exception;

public class NonNormalizableRequestParameterException extends RuntimeException {

  private final String requestParameter;
  private final String fetcherName;
  private final Object value;

  public NonNormalizableRequestParameterException(String fetcherName, String requestParameter, Object value) {
    super(getErrorMessage(fetcherName, requestParameter, value));
    this.fetcherName = fetcherName;
    this.requestParameter = requestParameter;
    this.value = value;
  }

  private static String getErrorMessage(String fetcherName, String requestParameter, Object value) {
    return String.format("Input parameter [%s: '%s'] could not be normalized for fetcher: %s", requestParameter, value, fetcherName);
  }

  public String getFetcherName() {
    return fetcherName;
  }

  public String getRequestParameter() {
    return requestParameter;
  }

  public Object getValue() {
    return value;
  }
}
