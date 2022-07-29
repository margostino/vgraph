package org.gaussian.vgraph.configuration.circuitbreaker;

import lombok.Builder;
import lombok.Getter;

import java.time.Duration;

@Getter
public class CircuitBreakerConfig {

  private final int failureRateThreshold;
  private final Duration slidingWindowSize;
  private final Duration waitDurationInOpenState;
  private final int minimumNumberOfCalls;

  @Builder
  public CircuitBreakerConfig(int failureRateThreshold, Duration slidingWindowSize, Duration waitDurationInOpenState, int minimumNumberOfCalls) {
    this.failureRateThreshold = failureRateThreshold;
    this.slidingWindowSize = slidingWindowSize;
    this.waitDurationInOpenState = waitDurationInOpenState;
    this.minimumNumberOfCalls = minimumNumberOfCalls;
  }
}
