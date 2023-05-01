package org.gaussian.vgraph.handler;

import com.google.inject.Inject;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.ext.web.handler.graphql.GraphQLHandler;
import org.gaussian.vgraph.configuration.guice.GraphQLBuilders;
import org.gaussian.vgraph.domain.DynamicGraphQLSchema;
import org.gaussian.vgraph.domain.UpdateSchemaRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static io.vertx.core.Future.succeededFuture;
import static org.gaussian.vgraph.configuration.guice.GraphQLBuilders.*;

public class GraphQLHotHandler {

    private static final Logger log = LoggerFactory.getLogger(GraphQLBuilders.class);

    private Future<GraphQLHandler> graphQLHandlerAsync;
    private Future<GraphQLSchema> graphQLSchemaAsync;
    private Future<RuntimeWiring> runtimeWiringAsync;

    @Inject
    public GraphQLHotHandler(Future<GraphQLSchema> graphQLSchemaAsync,
                             Future<RuntimeWiring> runtimeWiringAsync,
                             Future<GraphQLHandler> graphQLHandlerAsync) {
        this.graphQLSchemaAsync = graphQLSchemaAsync;
        this.runtimeWiringAsync = runtimeWiringAsync;
        this.graphQLHandlerAsync = graphQLHandlerAsync;
    }

    public Future<GraphQLHandler> get() {
        return this.graphQLHandlerAsync;
    }

    public Future<Boolean> updateSchema(UpdateSchemaRequest request) {
        Promise<Boolean> updateSuccessPromise = Promise.promise();
        Future<TypeDefinitionRegistry> updatedTypeDefRegistryAsync = graphQLSchemaAsync.map(graphQLSchema -> {
            GraphQLObjectType query = graphQLSchema.getQueryType();
            List<GraphQLFieldDefinition> fieldDefinitions = query.getFieldDefinitions();
            DynamicGraphQLSchema dynamicGraphQLSchema = new DynamicGraphQLSchema(fieldDefinitions);
            String graphQLSchemaAsString = dynamicGraphQLSchema.update(request);
            return buildSchemaRegistry(graphQLSchemaAsString);
        });

        buildGraphQLSchema(updatedTypeDefRegistryAsync, runtimeWiringAsync)
                .onSuccess(updatedGraphQLSchema -> {
                    this.graphQLSchemaAsync = succeededFuture(updatedGraphQLSchema);
                    reloadHandlerWith();
                    updateSuccessPromise.complete(true);
                })
                .onFailure(updateSuccessPromise::fail);

        return updateSuccessPromise.future();
    }

    private void reloadHandlerWith() {
        buildGraphQLApi(graphQLSchemaAsync)
                .onSuccess(graphQL -> this.graphQLHandlerAsync = buildGraphQLHandler(succeededFuture(graphQL)))
                .onFailure(error -> log.error("Unable to update GraphQL Handler: {}", error.getMessage()));
    }
}
