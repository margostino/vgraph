package org.gaussian.vgraph.verticle;

import com.google.inject.Inject;
import io.vertx.core.AbstractVerticle;
import org.gaussian.vgraph.processor.CircuitBreakerNotificationProcessor;

import static org.gaussian.vgraph.processor.CircuitBreakerNotificationProcessor.CIRCUIT_BREAKER_NOTIFICATIONS;

/**
 * Defines REST API endpoints and starts a HTTP server on port {@value}.
 */
public class TrackingVerticle extends AbstractVerticle {

    private final CircuitBreakerNotificationProcessor breakerNotificationProcessor;

    @Inject
    public TrackingVerticle(CircuitBreakerNotificationProcessor breakerNotificationProcessor) {
        super();
        this.breakerNotificationProcessor = breakerNotificationProcessor;
    }

    @Override
    public void start() throws Exception {
        vertx.eventBus().consumer(CIRCUIT_BREAKER_NOTIFICATIONS, breakerNotificationProcessor);
    }

}
