package org.gaussian.vgraph.router;

import com.google.inject.Inject;
import org.gaussian.vgraph.bootstrap.Mountable;
import io.vertx.core.Future;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.graphql.GraphQLHandler;
import io.vertx.ext.web.handler.graphql.GraphiQLHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphQLRouter implements Mountable {

  private final static Logger log = LoggerFactory.getLogger(GraphQLRouter.class);

  private final Future<GraphQLHandler> graphQLHandler;
  private final GraphiQLHandler playgroundHandler;

  @Inject
  public GraphQLRouter(Future<GraphQLHandler> graphQLHandler, GraphiQLHandler playgroundHandler) {
    this.graphQLHandler = graphQLHandler;
    this.playgroundHandler = playgroundHandler;
  }

  @Override
  public void mount(Router router) {
    router.post("/graphql").handler(this::query);
    router.route("/graphiql/*").handler(playgroundHandler);

  }

  private void query(RoutingContext context) {
    log.info("query received");
    graphQLHandler.onSuccess(handler -> handler.handle(context))
                  .onFailure(error -> log.error("GraphQL handler error", error));
  }

}
