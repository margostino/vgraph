package org.gaussian.vgraph.verticle;

import com.google.inject.Inject;
import org.gaussian.vgraph.bootstrap.Mountable;
import org.gaussian.vgraph.bootstrap.Mounter;
import org.gaussian.vgraph.bootstrap.RoutingConfigurator;
import org.gaussian.vgraph.exception.HttpFailureHandler;
import org.gaussian.vgraph.router.HealthCheckRouter;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_JSON;

/**
 * Defines REST API endpoints and starts a HTTP server on port {@value DEFAULT_PORT}.
 */
public class GraphQLServerVerticle extends AbstractVerticle implements Mounter {

  public static final int DEFAULT_PORT = 8080;

  private static final int MAX_INITIAL_LINE_LENGTH = 11 * 1024;
  private static final int MAX_HEADER_SIZE = (11 + 8 + 1) * 1024; // allowed in ML + X-Request-Uri + other headers

  private final RoutingConfigurator configurator;
  private Router root;

  @Inject
  public GraphQLServerVerticle(RoutingConfigurator configurator) {
    super();
    this.configurator = configurator;
  }

  @Override
  public void start() throws Exception {
    root = Router.router(vertx);
    initRoutes();
    httpServer().requestHandler(root).listen();
  }

  private HttpServer httpServer() {
    final HttpServerOptions options =
      new HttpServerOptions()
        .setMaxInitialLineLength(MAX_INITIAL_LINE_LENGTH)
        .setMaxHeaderSize(MAX_HEADER_SIZE)
        .setCompressionSupported(true)
        .setPort(DEFAULT_PORT);
    return vertx.createHttpServer(options);
  }

  private void initRoutes() {
    initDefaultHandlers();
    mount("/", new HealthCheckRouter());
    configurator.mountRoutes(this);
    root.route().failureHandler(HttpFailureHandler.create());
  }

  private void initDefaultHandlers() {
    root.route().handler(BodyHandler.create());
    root.route().handler(this::contentTypeHandler);
  }

  @Override
  public void mount(String path, Mountable unit) {
    final Router sub = Router.router(vertx);
    unit.mount(sub);
    root.mountSubRouter(path, sub);
  }

  private void contentTypeHandler(RoutingContext routingContext) {
    routingContext.response().putHeader(CONTENT_TYPE, APPLICATION_JSON);
    routingContext.next();
  }

}
