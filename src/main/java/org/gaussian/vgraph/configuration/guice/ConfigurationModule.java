package org.gaussian.vgraph.configuration.guice;

import com.google.common.io.Resources;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import graphql.language.FieldDefinition;
import graphql.language.TypeDefinition;
import graphql.language.TypeName;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import org.gaussian.vgraph.configuration.Configuration;
import org.gaussian.vgraph.configuration.DataFetcherConfig;
import org.gaussian.vgraph.configuration.GraphQLConfig;
import org.gaussian.vgraph.configuration.NamespaceConfig;
import org.gaussian.vgraph.configuration.SchemaConfig;
import org.gaussian.vgraph.configuration.SchemaNamespaceConfig;
import org.gaussian.vgraph.configuration.qualifier.JsonConfig;
import org.gaussian.vgraph.configuration.qualifier.Schema;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static org.gaussian.vgraph.json.JsonCodec.from;

public class ConfigurationModule extends AbstractModule {

    @Override
    protected void configure() {
    }

    @Inject
    @Provides
    @Singleton
    @JsonConfig
    private Future<JsonObject> getJsonConfig(Vertx vertx) {
        final ConfigRetrieverOptions options = getConfigurationOptions();
        ConfigRetriever retriever = ConfigRetriever.create(vertx, options);
        return retriever.getConfig();
    }

    @Inject
    @Provides
    @Singleton
    private Future<Configuration> getConfiguration(@JsonConfig Future<JsonObject> jsonConfig) {
        return jsonConfig.map(json -> {
            final String schemaFile = json.getJsonObject("schema").getString("file");
            final List<NamespaceConfig> namespaceConfigs = getNamespacesConfig(json);
            final GraphQLConfig graphQLConfig = new GraphQLConfig(schemaFile, namespaceConfigs);
            return new Configuration(graphQLConfig);
        });
    }

    @Inject
    @Provides
    @Singleton
    @Schema
    private Future<String> getSchemaAsString(Vertx vertx, Future<Configuration> configuration) {
        return configuration.compose(value -> {
            final String schemaFile = value.graphQLConfig().schemaFile();
            return vertx.fileSystem().readFile(schemaFile).map(Buffer::toString);
        });
    }

    @Inject
    @Provides
    @Singleton
    private Future<SchemaConfig> getSchema(@Schema Future<String> schemaAsString) {
        return schemaAsString.map(value -> {
            TypeDefinitionRegistry parser = new SchemaParser().parse(value);
            Optional<TypeDefinition> typeDefinition = parser.getType("Query");
            List<FieldDefinition> queryFieldDefinitions = typeDefinition.get().getChildren();
            List<SchemaNamespaceConfig> namespaceConfig = queryFieldDefinitions
                    .stream()
                    .map(fieldDefinition -> buildSchemaNamespaceConfig(fieldDefinition, parser))
                    .collect(toList());
            return new SchemaConfig(namespaceConfig);
        });
    }

    private SchemaNamespaceConfig buildSchemaNamespaceConfig(FieldDefinition fieldDefinition, TypeDefinitionRegistry parser) {
        final String namespaceName = fieldDefinition.getName();
        final String indicatorName = ((TypeName) fieldDefinition.getType()).getName();
        final List<FieldDefinition> schemaFieldDefinitions = parser.getType(indicatorName).get().getChildren();
        final Map<String, String> indicators = schemaFieldDefinitions
                .stream()
                .collect(
                        Collectors.toMap(
                                FieldDefinition::getName,
                                fd -> ((TypeName) fd.getType()).getName()
                        )
                );
        return new SchemaNamespaceConfig(namespaceName, indicators);
    }

    private List<NamespaceConfig> getNamespacesConfig(JsonObject jsonConfig) {
        return jsonConfig.getJsonObject("namespaces")
                         .stream()
                         .map(JsonObject::mapFrom)
                         .map(this::getNamespaceConfig)
                         .collect(toList());
    }

    private NamespaceConfig getNamespaceConfig(JsonObject jsonConfig) {
        final String namespace = jsonConfig.fieldNames().stream()
                                           .filter(value -> !value.isBlank())
                                           .findFirst()
                                           .get();
        List<DataFetcherConfig> dataFetcherConfigs = jsonConfig.getJsonArray(namespace)
                                                               .stream()
                                                               .map(JsonObject.class::cast)
                                                               .map(fetcher -> from(fetcher, DataFetcherConfig.class))
                                                               .collect(toList());
        return new NamespaceConfig(namespace, dataFetcherConfigs);
    }

    private ConfigRetrieverOptions getConfigurationOptions() {
        final String configFile = getFilePath("config.yml");
        final ConfigStoreOptions store = new ConfigStoreOptions()
                .setType("file")
                .setFormat("yaml")
                .setConfig(new JsonObject().put("path", configFile)
                );
        ConfigRetrieverOptions options = new ConfigRetrieverOptions().addStore(store);
        return options;
    }

    private String getFilePath(String resourceName) throws IllegalArgumentException {
        final URL configUrl = Resources.getResource(resourceName);
        return configUrl.getPath();
    }

}
