package org.gaussian.vgraph.exception;

import java.util.Map;

public class EmptyLookupKeyException extends RuntimeException {

  private final String fetcherName;
  private final Map<String, Object> requestParameters;

  public EmptyLookupKeyException(String fetcherName, Map<String, Object> requestParameters) {
    super(String.format("Empty lookup key for fetcher %s with parameters %s", fetcherName, requestParameters));
    this.fetcherName = fetcherName;
    this.requestParameters = requestParameters;
  }

  public String getFetcherName() {
    return fetcherName;
  }

  public Map<String, Object> getRequestParameters() {
    return requestParameters;
  }
}
