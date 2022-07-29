package org.gaussian.vgraph.router;

import org.gaussian.vgraph.bootstrap.Mountable;
import org.gaussian.vgraph.metrics.TransactionTag;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Router implementing a simple health check endpoint.
 * <p>
 * Clients can verify the endpoint is alive by sending a GET request to ./ping.
 */
public class HealthCheckRouter implements Mountable {

  private final static Logger log = LoggerFactory.getLogger(HealthCheckRouter.class);

  @Override
  public void mount(Router router) {
    router.get("/ping").handler(this::pong);
  }

  private void pong(RoutingContext context) {
    log.info("ping received");
    trackTransaction(context);
    context.response()
           .putHeader(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_PLAIN)
           .end("pong");
  }

  private void trackTransaction(RoutingContext context) {
    TransactionTag.put(context.request(), "health_check");
  }

}
