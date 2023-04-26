package org.gaussian.vgraph.configuration.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import graphql.GraphQL;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.web.handler.graphql.GraphQLHandler;
import io.vertx.ext.web.handler.graphql.GraphiQLHandler;
import io.vertx.ext.web.handler.graphql.GraphiQLHandlerOptions;
import org.gaussian.vgraph.configuration.Configuration;
import org.gaussian.vgraph.configuration.SchemaConfig;
import org.gaussian.vgraph.configuration.qualifier.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static io.vertx.core.CompositeFuture.join;
import static org.gaussian.vgraph.configuration.guice.GraphQLBuilders.*;

public class GraphQLModule extends AbstractModule {

    private static final Logger log = LoggerFactory.getLogger(GraphQLModule.class);

    @Override
    protected void configure() {
    }

    @Inject
    @Provides
    @Singleton
    private Future<GraphQLHandler> getGraphQLHandler(Vertx vertx, Future<GraphQL> graphQL) {
        return buildGraphQLHandler(graphQL).onFailure(error -> failAndClose(vertx, error));
    }

    @Inject
    @Provides
    @Singleton
    private Future<GraphQL> getGraphQLApi(Vertx vertx, Future<GraphQLSchema> graphQLSchemaAsync) {
        return buildGraphQLApi(graphQLSchemaAsync).onFailure(error -> failAndClose(vertx, error));
    }

    @Inject
    @Provides
    @Singleton
    private Future<GraphQLSchema> getGraphQLSchema(Vertx vertx, Future<TypeDefinitionRegistry> typeDefRegistryAsync, Future<RuntimeWiring> runtimeWiringAsync) {
        return buildGraphQLSchema(typeDefRegistryAsync, runtimeWiringAsync).onFailure(error -> failAndClose(vertx, error));
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

}
