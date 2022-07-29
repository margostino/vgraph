package org.gaussian.vgraph.configuration.circuitbreaker;

import org.gaussian.vgraph.exception.RetriableException;

import java.io.IOException;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCause;

public class DataFetcherRetryConfig {

  public Integer numberOfAttempts;
  public Duration attemptTimeout;

  protected RetryConfig getRetryConfig() {
    return RetryConfig.builder()
                      .numberOfAttempts(numberOfAttempts)
                      .attemptTimeout(Optional.ofNullable(attemptTimeout).map(Duration::toMillis).map(Duration::ofMillis).orElse(null))
                      .retryException(TimeoutException.class)
                      .retryException(RetriableException.class)
                      .retryExceptionPredicate(throwable -> Optional.ofNullable(getRootCause(throwable)).map(t -> t instanceof IOException).orElse(false))
                      .build();
  }
}
