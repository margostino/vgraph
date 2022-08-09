package org.gaussian.vgraph.metrics;

import com.google.inject.Singleton;

import java.util.concurrent.atomic.AtomicInteger;

@Singleton
public class CircuitBreakerMetrics {

  public static final AtomicInteger OPEN = new AtomicInteger(0);
  public static final AtomicInteger CLOSED = new AtomicInteger(1);
  public static final AtomicInteger HALF_OPEN = new AtomicInteger(0);

  public static void open() {
    OPEN.set(1);
    CLOSED.set(0);
    HALF_OPEN.set(0);
  }

  public static void closed() {
    CLOSED.set(1);
    OPEN.set(0);
    HALF_OPEN.set(0);
  }

  public static void halfOpen() {
    HALF_OPEN.set(1);
    CLOSED.set(0);
    OPEN.set(0);
  }

}
