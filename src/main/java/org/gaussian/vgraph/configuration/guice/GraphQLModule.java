package org.gaussian.vgraph.configuration.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.gaussian.vgraph.configuration.Configuration;
import org.gaussian.vgraph.configuration.SchemaConfig;
import org.gaussian.vgraph.configuration.qualifier.Schema;
import graphql.GraphQL;
import graphql.schema.DataFetcher;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.web.handler.graphql.GraphQLHandler;
import io.vertx.ext.web.handler.graphql.GraphQLHandlerOptions;
import io.vertx.ext.web.handler.graphql.GraphiQLHandler;
import io.vertx.ext.web.handler.graphql.GraphiQLHandlerOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static org.gaussian.vgraph.configuration.guice.GraphQLBuilders.buildGraphQLApi;
import static org.gaussian.vgraph.configuration.guice.GraphQLBuilders.buildRuntimeWiring;
import static io.vertx.core.CompositeFuture.join;

public class GraphQLModule extends AbstractModule {

  private static final Logger log = LoggerFactory.getLogger(GraphQLModule.class);

  @Override
  protected void configure() {
  }

  @Inject
  @Provides
  @Singleton
  private Future<GraphQLHandler> getGraphQLHandler(Vertx vertx, Future<GraphQL> graphQL) {
    return graphQL.onFailure(error -> failAndClose(vertx, error))
                  .map(api -> {
                    GraphQLHandlerOptions graphQLHandlerOptions = new GraphQLHandlerOptions();
                    return GraphQLHandler.create(api, graphQLHandlerOptions);
                  });
  }

  @Inject
  @Provides
  @Singleton
  private Future<GraphQL> getGraphQLApi(Vertx vertx, Future<TypeDefinitionRegistry> typeDefRegistryFut, Future<RuntimeWiring> runtimeWiringFut) {
    return join(typeDefRegistryFut, runtimeWiringFut)
      .onFailure(error -> failAndClose(vertx, error))
      .map(joined -> buildGraphQLApi(joined).orElseThrow(() -> new RuntimeException("cannot build GraphQL API")));
  }

  @Inject
  @Provides
  @Singleton
  private Future<TypeDefinitionRegistry> getSchemaRegistry(@Schema Future<String> schemaAsString) {
    return schemaAsString.map(GraphQLBuilders::buildSchemaRegistry);
  }

  @Inject
  @Provides
  @Singleton
  private Future<RuntimeWiring> getRuntimeWiring(Vertx vertx, Future<Configuration> configAsync, Future<SchemaConfig> schemaAsync) {
    return join(configAsync, schemaAsync)
      .map(joined -> buildRuntimeWiring(vertx, joined).orElseThrow(() -> new RuntimeException("cannot build GraphQL runtime wiring")));
  }

  @Inject
  @Provides
  @Singleton
  private Future<Map<String, DataFetcher>> getIndicatorDataFetchers(Vertx vertx, Future<RuntimeWiring> runtimeWiring) {
    return runtimeWiring
      .onFailure(error -> failAndClose(vertx, error))
      .map(wiring -> wiring.getDataFetchers().get("Query"));
  }

  @Inject
  @Provides
  @Singleton
  private GraphiQLHandler getPlaygroundHandler() {
    GraphiQLHandlerOptions options = new GraphiQLHandlerOptions().setEnabled(true);
    return GraphiQLHandler.create(options);
  }

  private void failAndClose(Vertx vertx, Throwable error) {
    log.error("GraphQL configuration failed: {}", error.getMessage());
    vertx.close();
  }

}
