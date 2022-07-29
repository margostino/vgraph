package org.gaussian.vgraph.configuration.circuitbreaker.errorrate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("custom")
public class CustomErrorRateConfig extends AbstractErrorRateConfig {
    @JsonCreator
    public CustomErrorRateConfig(
            @JsonProperty("warningThreshold") Float warningThreshold,
            @JsonProperty("criticalThreshold") Float criticalThreshold
    ) {
        super(warningThreshold, criticalThreshold);
    }
}
