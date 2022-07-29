package org.gaussian.vgraph.configuration.circuitbreaker;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.time.Duration;

@JsonTypeName("custom")
public class CustomCircuitBreakerConfig extends AbstractFetcherCircuitBreakerConfig {
  @JsonCreator
  public CustomCircuitBreakerConfig(
    @JsonProperty("failureRateThreshold") int failureRateThreshold,
    @JsonProperty("slidingWindowSize") Duration slidingWindowSize,
    @JsonProperty("waitDurationInOpenState") Duration waitDurationInOpenState,
    @JsonProperty("minimumNumberOfCalls") int minimumNumberOfCalls
  ) {
    super(failureRateThreshold, slidingWindowSize, waitDurationInOpenState, minimumNumberOfCalls);
  }
}
