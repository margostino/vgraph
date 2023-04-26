package org.gaussian.vgraph.handler;

import com.google.inject.Inject;
import graphql.GraphQL;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.web.handler.graphql.GraphQLHandler;
import org.gaussian.vgraph.domain.DynamicGraphQLSchema;
import org.gaussian.vgraph.domain.UpdateSchemaRequest;

import java.util.List;

import static org.gaussian.vgraph.configuration.guice.GraphQLBuilders.*;

public class GraphQLHotHandler {

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

    public void updateSchema(Vertx vertx, UpdateSchemaRequest request) {
        Future<TypeDefinitionRegistry> updatedTypeDefRegistryAsync = graphQLSchemaAsync.map(graphQLSchema -> {
            GraphQLObjectType query = graphQLSchema.getQueryType();
            List<GraphQLFieldDefinition> fieldDefinitions = query.getFieldDefinitions();
            DynamicGraphQLSchema dynamicGraphQLSchema = new DynamicGraphQLSchema(fieldDefinitions);
            String graphQLSchemaAsString = dynamicGraphQLSchema.update(request);
            return buildSchemaRegistry(graphQLSchemaAsString);
        });

        this.graphQLSchemaAsync = buildGraphQLSchema(vertx, updatedTypeDefRegistryAsync, runtimeWiringAsync);
        updateHandler(vertx);
    }

    public void updateHandler(Vertx vertx) {
        Future<GraphQL> graphQL = buildGraphQLApi(vertx, graphQLSchemaAsync);
        this.graphQLHandlerAsync = buildGraphQLHandler(vertx, graphQL);
    }
}
