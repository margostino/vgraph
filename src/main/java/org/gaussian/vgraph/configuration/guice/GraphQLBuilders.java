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
import io.vertx.core.Vertx;
import org.gaussian.vgraph.configuration.Configuration;
import org.gaussian.vgraph.configuration.GraphQLConfig;
import org.gaussian.vgraph.configuration.NamespaceConfig;
import org.gaussian.vgraph.configuration.SchemaConfig;
import org.gaussian.vgraph.configuration.SchemaNamespaceConfig;
import org.gaussian.vgraph.datafetcher.DataFetcher;
import org.gaussian.vgraph.datafetcher.NamespaceFetcher;
import org.gaussian.vgraph.schema.CustomDataFetchingExceptionHandler;

import java.io.BufferedReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;

public class GraphQLBuilders {

    protected static TypeDefinitionRegistry buildSchemaRegistry(String schemaAsString) {
        final SchemaParser schemaParser = new SchemaParser();
        final Reader inputString = new StringReader(schemaAsString);
        final BufferedReader reader = new BufferedReader(inputString);
        final TypeDefinitionRegistry schemaRegistry = schemaParser.parse(reader);
        final ScalarTypeDefinition scalarTypeDefinition = ScalarTypeExtensionDefinition.newScalarTypeDefinition().name("Long").build();
        schemaRegistry.add(scalarTypeDefinition);
        return schemaRegistry;
    }

    protected static Optional<RuntimeWiring> buildRuntimeWiring(Vertx vertx, CompositeFuture composite) {
        final Optional<Configuration> configuration = extractAsyncResult(composite, Configuration.class);
        final Optional<SchemaConfig> schemaConfig = extractAsyncResult(composite, SchemaConfig.class);

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

    protected static Optional<GraphQL> buildGraphQLApi(CompositeFuture composite) {
        final Optional<RuntimeWiring> runtimeWiring = extractAsyncResult(composite, RuntimeWiring.class);
        final Optional<TypeDefinitionRegistry> typeDefinitionRegistry = extractAsyncResult(composite, TypeDefinitionRegistry.class);

        if (runtimeWiring.isPresent() && typeDefinitionRegistry.isPresent()) {
            final GraphQLSchema graphQLSchema = buildGraphQLSchema(typeDefinitionRegistry.get(), runtimeWiring.get());
            final GraphQL graphQLApi = GraphQL.newGraphQL(graphQLSchema)
                                              .defaultDataFetcherExceptionHandler(new CustomDataFetchingExceptionHandler())
                                              //.queryExecutionStrategy()
                                              .build();
            return of(graphQLApi);
        }

        return empty();
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


    private static GraphQLSchema buildGraphQLSchema(TypeDefinitionRegistry typeDefinitionRegistry, RuntimeWiring runtimeWiring) {
        SchemaGenerator schemaGenerator = new SchemaGenerator();
        GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);
        return graphQLSchema;
    }

    private static <T> Optional<T> extractAsyncResult(CompositeFuture joined, Class<T> type) {
        return joined.result().list().stream()
                     .filter(result -> result.getClass() == type)
                     .map(type::cast)
                     .findAny();
    }

}
