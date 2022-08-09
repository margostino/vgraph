package org.gaussian.vgraph.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.gaussian.vgraph.configuration.circuitbreaker.DataFetcherCircuitBreakerConfig;
import org.gaussian.vgraph.configuration.circuitbreaker.DataFetcherRetryConfig;
import org.gaussian.vgraph.configuration.circuitbreaker.DefaultCircuitBreakerConfig;
import org.gaussian.vgraph.datafetcher.DataFetcher;
import org.gaussian.vgraph.datafetcher.http.HttpDataFetcher;
import org.gaussian.vgraph.json.DurationDeserializer;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.netty.handler.codec.http.HttpHeaders.Names.AUTHORIZATION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaders.Values.APPLICATION_JSON;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableMap;
import static org.gaussian.vgraph.processor.CircuitBreakerNotificationProcessor.CIRCUIT_BREAKER_NOTIFICATIONS;

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
        this.retry = retry;
        this.fields = fields;
        this.healthCheck = healthCheck;
        this.contactPoint = contactPoint;
        this.username = username;
        this.password = password;
        this.metadataUrl = metadataUrl;
        this.callTimeout = callTimeout;
        this.executionBudget = executionBudget;
        this.percentageEnabled = percentageEnabled;
    }

    public DataFetcher buildDataFetcher(Vertx vertx, String namespace) {
        try {
            final URL endpoint = new URL(url);
            final String path = endpoint.getPath();
            final CircuitBreaker circuitBreaker = buildHttpCircuitBreaker(vertx);
            final WebClient webClient = buildWebClient(vertx, endpoint);
            final Map<String, String> headers = unmodifiableMap(buildBasicHeaders(username, password));
            return new HttpDataFetcher(path, namespace, name, webClient, circuitBreaker, headers, fields == null ? emptyList() : fields);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null; // TODO
        }
    }

    // TODO: inject from config
    private static WebClient buildWebClient(Vertx vertx, URL url) {
        final WebClientOptions options = new WebClientOptions().setDefaultHost(url.getHost())
                                                               .setDefaultPort(url.getPort())
                                                               .setTryUseCompression(true)
                                                               .setMaxPoolSize(10)
                                                               .setKeepAlive(true)
                                                               .setIdleTimeout(60)
                                                               .setKeepAliveTimeout(60)
                                                               .setConnectTimeout(300)
                                                               //.setMaxWaitQueueSize(100)
                                                               .setSsl(false);
        return WebClient.create(vertx, options);
    }

    protected static CircuitBreaker buildHttpCircuitBreaker(Vertx vertx) {
        return CircuitBreaker.create("http-circuit-breaker", vertx,
                                     new CircuitBreakerOptions()
                                             .setMaxFailures(50)
                                             .setTimeout(500)
                                             .setNotificationAddress(CIRCUIT_BREAKER_NOTIFICATIONS)
                                             .setFallbackOnFailure(true)
                                             .setResetTimeout(10000)
        );
    }

    private static Map<String, String> buildBasicHeaders(final String username, final String password) {
        Map<String, String> map = new HashMap<>();
        map.put(CONTENT_TYPE, APPLICATION_JSON);

        if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)) {
            map.put(AUTHORIZATION, createAuthorizationHeader(username, password));
        }
        return map;
    }

    private static String createAuthorizationHeader(final String username, final String password) {
        return "Basic " + Base64.getEncoder().encodeToString((username + ':' + password).getBytes(StandardCharsets.UTF_8));
    }
}
