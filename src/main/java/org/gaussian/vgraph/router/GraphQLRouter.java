package org.gaussian.vgraph.router;

import com.google.inject.Inject;
import graphql.schema.DataFetcher;
import io.vertx.core.Future;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.graphql.GraphiQLHandler;
import org.gaussian.vgraph.bootstrap.Mountable;
import org.gaussian.vgraph.datafetcher.NamespaceFetcher;
import org.gaussian.vgraph.domain.UpdateSchemaErrorResponse;
import org.gaussian.vgraph.domain.UpdateSchemaRequest;
import org.gaussian.vgraph.handler.GraphQLHotHandler;
import org.gaussian.vgraph.json.JsonCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static org.gaussian.vgraph.json.JsonCodec.decode;

public class GraphQLRouter implements Mountable {

    private final static Logger log = LoggerFactory.getLogger(GraphQLRouter.class);

    //private final Future<GraphQLHandler> graphQLHandler;
    private final GraphQLHotHandler graphQLHandler;
    private final GraphiQLHandler playgroundHandler;
    private Future<Map<String, DataFetcher>> namespaces;

    //    @Inject
//    public GraphQLRouter(Future<GraphQLHandler> graphQLHandler, GraphiQLHandler playgroundHandler, Future<Map<String, DataFetcher>> namespaces) {
//        this.graphQLHandler = graphQLHandler;
//        this.namespaces = namespaces;
//        this.playgroundHandler = playgroundHandler;
//    }
    @Inject
    public GraphQLRouter(GraphQLHotHandler graphQLHandler, GraphiQLHandler playgroundHandler, Future<Map<String, DataFetcher>> namespaces) {
        this.namespaces = namespaces;
        this.playgroundHandler = playgroundHandler;
        this.graphQLHandler = graphQLHandler;
    }

    @Override
    public void mount(Router router) {
        router.post("/graphql").handler(this::query);
        router.put("/schema").handler(this::updateSchema);
        router.route("/graphiql/*").handler(playgroundHandler);

    }

    private void query(RoutingContext context) {
        log.info("query received");
        graphQLHandler.get()
                .onSuccess(handler -> handler.handle(context))
                .onFailure(error -> log.error("GraphQL handler error", error));
    }

    private void updateSchema(RoutingContext context) {
        log.info("update schema");
        UpdateSchemaRequest request = decode(context.body().asString(), UpdateSchemaRequest.class);
        namespaces.onSuccess(fetchers -> {
            NamespaceFetcher namespaceFetcher = (NamespaceFetcher) fetchers.get(request.namespace());
            namespaceFetcher.updateSchema(request.variable(), request.type());
            graphQLHandler.updateSchema(context.vertx(), request)
                    .onSuccess(handler -> context.response().setStatusCode(200).end())
                    .onFailure(error -> context.response().setStatusCode(400).end(badRequestResponse(error)));

        });
    }

    private String badRequestResponse(Throwable throwable) {
        UpdateSchemaErrorResponse response = new UpdateSchemaErrorResponse(throwable.getMessage());
        return JsonCodec.encode(response);
    }

}
