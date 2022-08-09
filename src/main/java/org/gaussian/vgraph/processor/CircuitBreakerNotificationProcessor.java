package org.gaussian.vgraph.processor;

import com.google.inject.Singleton;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Tags;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.micrometer.backends.BackendRegistries;
import org.gaussian.vgraph.metrics.CircuitBreakerMetrics;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.String.format;
import static org.gaussian.vgraph.metrics.CircuitBreakerMetrics.closed;
import static org.gaussian.vgraph.metrics.CircuitBreakerMetrics.halfOpen;
import static org.gaussian.vgraph.metrics.CircuitBreakerMetrics.open;

/**
 * Handles scoring events using provided {@link CircuitBreakerNotificationProcessor}.
 */
@Singleton
public class CircuitBreakerNotificationProcessor implements Handler<Message<Object>> {

    public static final String OPEN = "open";
    public static final String CLOSED = "closed";
    public static final String HALF_OPEN = "half-open";
    public static final String CIRCUIT_BREAKER_NOTIFICATIONS = "circuit-breaker.notifications";

    private final Map<String, Counter> counters;

    public CircuitBreakerNotificationProcessor() {
        this.counters = new HashMap<>();
        registerMetrics("http_circuit_breaker_state", Tags.of("state", "closed"), CircuitBreakerMetrics.CLOSED);
        registerMetrics("http_circuit_breaker_state", Tags.of("state", "open"), CircuitBreakerMetrics.OPEN);
        registerMetrics("http_circuit_breaker_state", Tags.of("state", "half_open"), CircuitBreakerMetrics.HALF_OPEN);
        registerMetrics("lambda_circuit_breaker_state", Tags.of("state", "closed"), CircuitBreakerMetrics.CLOSED);
        registerMetrics("lambda_circuit_breaker_state", Tags.of("state", "open"), CircuitBreakerMetrics.OPEN);
        registerMetrics("lambda_circuit_breaker_state", Tags.of("state", "half_open"), CircuitBreakerMetrics.HALF_OPEN);
    }

    public void handle(Message message) {
        final JsonObject metrics = (JsonObject) message.body();
        final String name = metrics.getString("name");

        metrics.stream()
               .filter(metric -> metric.getKey().startsWith("total") || metric.getKey().equalsIgnoreCase("failures"))
               .forEach(metric -> {
                   if (!counters.containsKey(metric.getKey())) {
                       final Counter counter = registerMetrics(name, metric.getKey());
                       counters.put(metric.getKey(), counter);
                   }

                   if (shouldCount(metric)) {
                       counters.get(metric.getKey()).increment();
                   }

               });

        if (metrics.getString("state").equalsIgnoreCase(CLOSED)) closed();
        else if (metrics.getString("state").equalsIgnoreCase(OPEN)) {
            open();
        } else if (metrics.getString("state").equalsIgnoreCase(HALF_OPEN)) {
            halfOpen();
        }

    }

    private void registerMetrics(String name, Tags tags, AtomicInteger value) {
        Gauge.builder(format("%s.state", name), () -> value)
             .tags(tags)
             .strongReference(true)
             .register(BackendRegistries.getDefaultNow());
    }

    private Counter registerMetrics(String prefix, String suffix) {
        return Counter.builder(format("%s.%s", prefix, suffix))
                      .register(BackendRegistries.getDefaultNow());
    }

    // TODO: improve this
    private boolean shouldCount(Map.Entry<String, Object> metric) {
        return counters.containsKey(metric.getKey()) && (metric.getValue() instanceof Long && (Long) metric.getValue() > 0) ||
                (metric.getValue() instanceof Integer && (Integer) metric.getValue() > 0);
    }

}
