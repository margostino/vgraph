package org.gaussian.vgraph.metrics;

import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.NonBlockingStatsDClientBuilder;
import com.timgroup.statsd.StatsDClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements {@link MetricCollector} for tracking in DataDog in Fury environment.
 */
public class DataDogMetricCollector implements MetricCollector {

  private static final Logger log = LoggerFactory.getLogger(MetricCollector.class);

  protected final StatsDClient client;

  public DataDogMetricCollector(StatsDClient client) {
    super();
    this.client = client;
  }

  public DataDogMetricCollector() {
    this(new NonBlockingStatsDClient(new NonBlockingStatsDClientBuilder().port(8125)
                                                                         .constantTags(new String[]{})
                                                                         .hostname("datadog")
                                                                         .errorHandler((Exception e) -> log.error("Metric Collector error", e))));
  }

  public void stop() {
    log.info("Stopping StatsD client");
    client.stop();
  }

  @Override
  public void count(String aspect, long delta, String... tags) {
    client.count(aspect, delta, tags);
  }

  @Override
  public void incrementCounter(String aspect, String... tags) {
    client.incrementCounter(aspect, tags);
  }

  @Override
  public void decrementCounter(String aspect, String... tags) {
    client.decrementCounter(aspect, tags);
  }

  @Override
  public void recordGaugeValue(String aspect, double value, String... tags) {
    client.recordGaugeValue(aspect, value, tags);
  }

  @Override
  public void recordGaugeValue(String aspect, long value, String... tags) {
    client.recordGaugeValue(aspect, value, tags);
  }

  @Override
  public void recordExecutionTime(String aspect, long timeInMs, String... tags) {
    client.recordExecutionTime(aspect, timeInMs, tags);
  }

  @Override
  public void recordHistogramValue(String aspect, double value, String... tags) {
    client.recordHistogramValue(aspect, value, tags);
  }

  @Override
  public void recordHistogramValue(String aspect, long value, String... tags) {
    client.recordHistogramValue(aspect, value, tags);
  }

}
