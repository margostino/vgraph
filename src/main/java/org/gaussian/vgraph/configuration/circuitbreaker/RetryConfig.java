package org.gaussian.vgraph.configuration.circuitbreaker;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.time.Duration;
import java.util.List;
import java.util.function.Predicate;

@Getter
public class RetryConfig {

  private final Duration attemptTimeout;
  private final Integer numberOfAttempts;
  private final List<Class<? extends Throwable>> retryExceptions;
  private final List<Predicate<Throwable>> retryExceptionPredicates;

  @Builder
  public RetryConfig(Duration attemptTimeout, Integer numberOfAttempts, @Singular List<Class<? extends Throwable>> retryExceptions, @Singular List<Predicate<Throwable>> retryExceptionPredicates) {
    this.attemptTimeout = attemptTimeout;
    this.numberOfAttempts = numberOfAttempts;
    this.retryExceptions = retryExceptions;
    this.retryExceptionPredicates = retryExceptionPredicates;
  }

  public boolean isRetryEnabled() {
    return null != numberOfAttempts;
  }

  public Predicate<Throwable> exceptionPredicate() {
    return this.retryExceptionPredicates.size() == 0 ? null : (throwable -> this.retryExceptionPredicates.stream().anyMatch(p -> p.test(throwable)));
  }
}
