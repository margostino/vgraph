package org.gaussian.vgraph.verticle;

import com.google.inject.Inject;
import org.gaussian.vgraph.bootstrap.EventBusConsumersConfigurator;
import io.vertx.core.AbstractVerticle;

/**
 * Defines REST API endpoints and starts a HTTP server on port {@value}.
 */
public class HttpServerVerticle extends AbstractVerticle {

  private final EventBusConsumersConfigurator configurator;

  @Inject
  public HttpServerVerticle(EventBusConsumersConfigurator configurator) {
    super();
    this.configurator = configurator;
  }

  @Override
  public void start() throws Exception {
    configurator.registerConsumers(vertx.eventBus());
  }

}
