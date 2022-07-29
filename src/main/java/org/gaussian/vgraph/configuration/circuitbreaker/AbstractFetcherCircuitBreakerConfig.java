package org.gaussian.vgraph.configuration.circuitbreaker;

import java.time.Duration;

public abstract class AbstractFetcherCircuitBreakerConfig implements DataFetcherCircuitBreakerConfig {
  private final int failureRateThreshold;
  private final Duration slidingWindowSize;
  private final Duration waitDurationInOpenState;
  private final int minimumNumberOfCalls;

  public AbstractFetcherCircuitBreakerConfig(int failureRateThreshold, Duration slidingWindowSize, Duration waitDurationInOpenState, int minimumNumberOfCalls) {
    this.failureRateThreshold = failureRateThreshold;
    this.slidingWindowSize = slidingWindowSize;
    this.waitDurationInOpenState = waitDurationInOpenState;
    this.minimumNumberOfCalls = minimumNumberOfCalls;
  }

  public CircuitBreakerConfig getCircuitBreakerConfig() {
    return CircuitBreakerConfig.builder()
                               .failureRateThreshold(failureRateThreshold)
                               .slidingWindowSize(Duration.ofMillis(slidingWindowSize.toMillis()))
                               .waitDurationInOpenState(Duration.ofMillis(waitDurationInOpenState.toMillis()))
                               .minimumNumberOfCalls(minimumNumberOfCalls)
                               .build();
  }
}
