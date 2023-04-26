package org.gaussian.vgraph.configuration.guice;

import graphql.GraphQL;
import graphql.language.ScalarTypeDefinition;
import graphql.language.ScalarTypeExtensionDefinition;
import graphql.scalars.ExtendedScalars;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.web.handler.graphql.GraphQLHandler;
import io.vertx.ext.web.handler.graphql.GraphQLHandlerOptions;
import org.gaussian.vgraph.configuration.*;
import org.gaussian.vgraph.datafetcher.DataFetcher;
import org.gaussian.vgraph.datafetcher.NamespaceFetcher;
import org.gaussian.vgraph.schema.CustomDataFetchingExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.Optional;

import static io.vertx.core.CompositeFuture.join;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;

public class GraphQLBuilders {

    private static final Logger log = LoggerFactory.getLogger(GraphQLBuilders.class);

    public static GraphQLHandler buildGraphQLHandler(GraphQL graphQLApi) {
        GraphQLHandlerOptions graphQLHandlerOptions = new GraphQLHandlerOptions();
        return GraphQLHandler.create(graphQLApi, graphQLHandlerOptions);
    }

    public static Future<GraphQLHandler> buildGraphQLHandler(Vertx vertx, Future<GraphQL> graphQL) {
        return graphQL.onFailure(error -> failAndClose(vertx, error))
                .map(GraphQLBuilders::buildGraphQLHandler);
    }

    public static TypeDefinitionRegistry buildSchemaRegistry(String schemaAsString) {
        final SchemaParser schemaParser = new SchemaParser();
        final Reader inputString = new StringReader(schemaAsString);
        final BufferedReader reader = new BufferedReader(inputString);
        final TypeDefinitionRegistry schemaRegistry = schemaParser.parse(reader);
        final ScalarTypeDefinition scalarTypeDefinition = ScalarTypeExtensionDefinition.newScalarTypeDefinition().name("Long").build();
        schemaRegistry.add(scalarTypeDefinition);
        return schemaRegistry;
    }

    public static Optional<RuntimeWiring> buildRuntimeWiring(Vertx vertx, CompositeFuture configAndSchemaAsync) {
        final Optional<Configuration> configuration = extractAsyncResult(configAndSchemaAsync, Configuration.class);
        final Optional<SchemaConfig> schemaConfig = extractAsyncResult(configAndSchemaAsync, SchemaConfig.class);

        if (configuration.isPresent() && schemaConfig.isPresent()) {
            final GraphQLConfig graphQLConfig = configuration.get().graphQLConfig();
            RuntimeWiring.Builder runtimeWiring = RuntimeWiring.newRuntimeWiring().scalar(ExtendedScalars.GraphQLLong);
            List<NamespaceFetcher> namespaceFetchers = graphQLConfig.namespaces().stream()
                    .map(namespaceConfig -> buildNamespaceFetcher(vertx, namespaceConfig, schemaConfig.get()))
                    .collect(toList());
            for (NamespaceFetcher fetcher : namespaceFetchers) {
                runtimeWiring = runtimeWiring.type("Query", typeWiring -> typeWiring.dataFetcher(fetcher.namespace, fetcher));
            }

            return of(runtimeWiring.build());
        }

        return empty();
    }

    public static GraphQL buildGraphQLApi(GraphQLSchema graphQLSchema) {
        return GraphQL.newGraphQL(graphQLSchema)
                .defaultDataFetcherExceptionHandler(new CustomDataFetchingExceptionHandler())
                //.queryExecutionStrategy()
                .build();
    }

    public static Future<GraphQL> buildGraphQLApi(Vertx vertx, Future<GraphQLSchema> graphQLSchemaAsync) {
        return graphQLSchemaAsync
                .onFailure(error -> failAndClose(vertx, error))
                .map(GraphQLBuilders::buildGraphQLApi);
    }

    public static Optional<GraphQLSchema> buildGraphQLSchema(CompositeFuture composite) {
        final Optional<RuntimeWiring> runtimeWiring = extractAsyncResult(composite, RuntimeWiring.class);
        final Optional<TypeDefinitionRegistry> typeDefinitionRegistry = extractAsyncResult(composite, TypeDefinitionRegistry.class);

        if (runtimeWiring.isPresent() && typeDefinitionRegistry.isPresent()) {
            SchemaGenerator schemaGenerator = new SchemaGenerator();
            GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry.get(), runtimeWiring.get());
            return of(graphQLSchema);
        }

        return empty();
    }

    public static Future<GraphQLSchema> buildGraphQLSchema(Vertx vertx, Future<TypeDefinitionRegistry> typeDefRegistryAsync, Future<RuntimeWiring> runtimeWiringAsync) {
        return join(typeDefRegistryAsync, runtimeWiringAsync)
                .onFailure(error -> failAndClose(vertx, error))
                .map(graphQLConfig -> buildGraphQLSchema(graphQLConfig).orElseThrow(() -> new RuntimeException("cannot build GraphQL Schema")));
    }

    private static NamespaceFetcher buildNamespaceFetcher(Vertx vertx, NamespaceConfig namespaceConfig, SchemaConfig schemaConfig) {
        final String namespaceName = namespaceConfig.name();
        final List<DataFetcher> dataFetchers = buildDataFetchers(vertx, namespaceConfig);
        Optional<SchemaNamespaceConfig> schemaNamespaceConfig = schemaConfig.namespaces().stream()
                .filter(config -> config.namespace().equalsIgnoreCase(namespaceName))
                .findAny();
        return new NamespaceFetcher(vertx.eventBus(), namespaceName, dataFetchers, schemaNamespaceConfig.get().indicators());
    }

    private static List<DataFetcher> buildDataFetchers(Vertx vertx, NamespaceConfig namespaceConfig) {
        final String namespace = namespaceConfig.name();
        return namespaceConfig.indicatorsFetchers().stream()
                .map(config -> config.buildDataFetcher(vertx, namespace))
                .collect(toList());
    }

    private static <T> Optional<T> extractAsyncResult(CompositeFuture joined, Class<T> type) {
        return joined.result().list().stream()
                .filter(result -> result.getClass() == type)
                .map(type::cast)
                .findAny();
    }

    public static void failAndClose(Vertx vertx, Throwable error) {
        log.error("GraphQL configuration failed: {}", error.getMessage());
        vertx.close();
    }
}
