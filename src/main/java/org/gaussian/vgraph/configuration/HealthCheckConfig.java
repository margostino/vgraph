package org.gaussian.vgraph.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.gaussian.vgraph.configuration.circuitbreaker.errorrate.ErrorRateConfig;
import lombok.Builder;
import lombok.Getter;

@Getter
public class HealthCheckConfig {

  private final DataFreshnessConfig dataFreshness;
  private final ErrorRateConfig errorRate;

  @Builder
  @JsonCreator
  public HealthCheckConfig(@JsonProperty("dataFreshness") DataFreshnessConfig dataFreshness,
                           @JsonProperty("errorRate") ErrorRateConfig errorRate) {
    this.dataFreshness = dataFreshness;
    this.errorRate = errorRate;
  }
}
