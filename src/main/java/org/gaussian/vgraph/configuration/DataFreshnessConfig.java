package org.gaussian.vgraph.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.gaussian.vgraph.json.DurationDeserializer;

import java.time.Duration;
import java.util.Optional;

import static java.util.Optional.ofNullable;

public record DataFreshnessConfig(
  @JsonDeserialize(using = DurationDeserializer.class) Duration warningThreshold,
  @JsonDeserialize(using = DurationDeserializer.class) Duration criticalThreshold) {

  @JsonCreator
  public DataFreshnessConfig(@JsonProperty("warningThreshold") Duration warningThreshold,
                             @JsonProperty("criticalThreshold") Duration criticalThreshold) {
    this.warningThreshold = warningThreshold;
    this.criticalThreshold = criticalThreshold;
  }

  @Override
  public Duration warningThreshold() {
    return toJavaDuration(this.warningThreshold).orElse(null);
  }

  @Override
  public Duration criticalThreshold() {
    return toJavaDuration(this.criticalThreshold).orElse(null);
  }

  private static Optional<Duration> toJavaDuration(Duration dropwizardDuration) {
    return ofNullable(dropwizardDuration).map(duration -> Duration.ofMillis(duration.toMillis()));
  }
}
