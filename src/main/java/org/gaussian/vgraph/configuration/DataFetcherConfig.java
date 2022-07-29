package org.gaussian.vgraph.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Getter;
import org.gaussian.vgraph.configuration.circuitbreaker.DataFetcherCircuitBreakerConfig;
import org.gaussian.vgraph.configuration.circuitbreaker.DataFetcherRetryConfig;
import org.gaussian.vgraph.configuration.circuitbreaker.DefaultCircuitBreakerConfig;
import org.gaussian.vgraph.json.DurationDeserializer;

import java.time.Duration;
import java.util.List;

@Getter
public class DataFetcherConfig {

    private static final Duration DEFAULT_ATTEMPT_TIMEOUT = Duration.ofMillis(351);
    private static final Integer DEFAULT_NUMBER_OF_ATTEMPTS = 2;

    private final String url;
    private final String name;
    private final String username;
    private final String password;
    private final String metadataUrl;
    @JsonDeserialize(using = DurationDeserializer.class)
    private final Duration callTimeout;
    @JsonDeserialize(using = DurationDeserializer.class)
    private final Duration executionBudget; // default milliseconds(700)
    private final Integer percentageEnabled; // default 100;
    private final List<ArgumentConfig> fields;
    private final ContactPointConfig contactPoint;
    private final HealthCheckConfig healthCheck;
    private final DataFetcherRetryConfig retry;
    private final DataFetcherCircuitBreakerConfig circuitBreaker = new DefaultCircuitBreakerConfig();

    @Builder
    @JsonCreator
    private DataFetcherConfig(@JsonProperty("url") String url,
                              @JsonProperty("name") String name,
                              @JsonProperty("username") String username,
                              @JsonProperty("password") String password,
                              @JsonProperty("metadataUrl") String metadataUrl,
                              @JsonProperty("callTimeout") Duration callTimeout,
                              @JsonProperty("executionBudget") Duration executionBudget,
                              @JsonProperty("percentageEnabled") Integer percentageEnabled,
                              @JsonProperty("fields") List<ArgumentConfig> fields,
                              @JsonProperty("contactPoint") ContactPointConfig contactPoint,
                              @JsonProperty("healthCheck") HealthCheckConfig healthCheck,
                              @JsonProperty("retry") DataFetcherRetryConfig retry) {
        this.url = url;
        this.name = name;
        this.username = username;
        this.password = password;
        this.metadataUrl = metadataUrl;
        this.callTimeout = callTimeout;
        this.executionBudget = executionBudget;
        this.percentageEnabled = percentageEnabled;
        this.fields = fields;
        this.contactPoint = contactPoint;
        this.healthCheck = healthCheck;
        this.retry = retry;
    }
}
