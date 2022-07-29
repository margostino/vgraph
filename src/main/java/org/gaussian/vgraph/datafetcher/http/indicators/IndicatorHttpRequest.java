package org.gaussian.vgraph.datafetcher.http.indicators;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.util.List;
import java.util.Map;

@Getter
public class IndicatorHttpRequest {

  private final Payload payload;
  private final Map<String, String> headers;

  @Builder
  @JsonCreator
  public IndicatorHttpRequest(@JsonProperty("payload") Payload payload,
                              @JsonProperty("headers") @Singular Map<String, String> headers) {
    this.payload = payload;
    this.headers = headers;
  }

  @Getter
  public static class Payload {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String market;
    private final String namespace;
    private final List<String> indicators;
    private final Map<String, Object> arguments;

    @Builder
    @JsonCreator
    public Payload(@JsonProperty("namespace") String namespace,
                   @JsonProperty("market") String market,
                   @JsonProperty("indicators") @Singular List<String> indicators,
                   @JsonProperty("arguments") @Singular Map<String, Object> arguments) {
      this.market = market;
      this.namespace = namespace;
      this.indicators = indicators;
      this.arguments = arguments;
    }
  }
}
