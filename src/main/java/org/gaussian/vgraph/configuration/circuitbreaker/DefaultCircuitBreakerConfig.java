package org.gaussian.vgraph.configuration.circuitbreaker;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.time.Duration;

@JsonTypeName("default")
public class DefaultCircuitBreakerConfig extends AbstractFetcherCircuitBreakerConfig {
    public DefaultCircuitBreakerConfig() {
        super(50, Duration.ofMinutes(1), Duration.ofSeconds(30), 100);
    }
}
