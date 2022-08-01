package org.gaussian.vgraph.datafetcher;

import graphql.GraphQLError;
import graphql.execution.DataFetcherResult;
import graphql.language.Field;
import graphql.schema.DataFetchingEnvironment;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import org.gaussian.vgraph.datafetcher.error.IndicatorFetcherGraphQLError;
import org.gaussian.vgraph.domain.RequestMessage;
import org.gaussian.vgraph.processor.HttpRequestProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import static io.vertx.core.Future.future;
import static java.util.stream.Collectors.toList;
import static org.gaussian.vgraph.common.AppConstants.CORRELATION_ID_HEADER;
import static org.gaussian.vgraph.common.AppConstants.USERNAME_HEADER;
import static org.gaussian.vgraph.json.JsonCodec.encode;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.valid4j.Assertive.require;
import static org.valid4j.matchers.ArgumentMatchers.notEmptyString;

public class NamespaceFetcher implements graphql.schema.DataFetcher<CompletionStage<Object>> {

    private static final Logger log = LoggerFactory.getLogger(NamespaceFetcher.class);

    public final String namespace;
    private final EventBus eventBus;
    private final Map<String, String> schema;
    public final List<DataFetcher> indicatorFetchers;

    public NamespaceFetcher(EventBus eventBus, String namespace, List<DataFetcher> indicatorFetchers, Map<String, String> schema) {
        this.schema = schema;
        this.eventBus = eventBus;
        this.namespace = require(namespace, notEmptyString());
        this.indicatorFetchers = require(indicatorFetchers, hasSize(greaterThan(0)));
    }

    @Override
    public CompletionStage<Object> get(DataFetchingEnvironment environment) {
        log.info("fetching namespace {}", namespace);
        final String correlationId = getHeaderValue(environment, CORRELATION_ID_HEADER).orElse(null);
        final String clientName = getHeaderValue(environment, USERNAME_HEADER).orElse(null);
        final NamespaceRequest namespaceRequest = buildRequest(environment, correlationId, clientName);
        return future(promise -> fetchData(namespaceRequest, promise)).toCompletionStage();
    }

    private Future<DataFetcherResult> fetchData(NamespaceRequest request, Promise promise) {
        final RequestMessage message = new RequestMessage(namespace, request);
        return eventBus.request(HttpRequestProcessor.HTTP_REQUESTS_ADDRESS, encode(message))
                       .onFailure(promise::fail)
                       .map(Message::body)
                       .map(String::valueOf)
                       .map(JsonObject::new)
                       .map(this::buildDataFetcherResult)
                       .onSuccess(promise::complete);
    }

    private DataFetcherResult buildDataFetcherResult(JsonObject result) {
        final Map<String, Object> indicators = result.getJsonObject("indicators").getMap();
        final List<GraphQLError> errors = result.getJsonArray("errors").stream()
                                                .map(JsonObject.class::cast)
                                                .map(error -> {
                                                    final String message = error.getString("message");
                                                    final List<Object> path = error.getJsonArray("path").getList();
                                                    final Map<String, Object> extensions = error.getJsonObject("extensions").getMap();
                                                    final IndicatorFetcherGraphQLError indicatorFetcherGraphQLError = new IndicatorFetcherGraphQLError(message, path, extensions);
                                                    return indicatorFetcherGraphQLError;
                                                }).collect(toList());

        return DataFetcherResult.newResult().data(indicators).errors(errors).build();
    }

    private NamespaceRequest buildRequest(DataFetchingEnvironment environment, String correlationId, String username) {
        return NamespaceRequest.builder()
                               .parameters(environment.getArguments())
                               .indicators(getRequestedIndicators(environment))
                               .metadata(NamespaceRequest.NamespaceRequestMetadata.builder()
                                                                                  .correlationId(correlationId)
                                                                                  .username(username)
                                                                                  .build()
                               )
                               .build();
    }

    private Optional<String> getHeaderValue(DataFetchingEnvironment environment, String headerName) {
        return Optional.empty();
//        ((GraphQLContext) environment.getGraphQlContext())
//                .getHttpServletRequest()
//                .map(r -> r.getHeader(headerName));
    }

    private List<RequestedIndicatorDTO> getRequestedIndicators(DataFetchingEnvironment environment) {
        return environment.getFields().stream()
                          .flatMap(field -> field.getSelectionSet().getSelections().stream())
                          .map(selection -> ((Field) selection).getName())
                          .distinct()
                          .filter(schema::containsKey)
                          .map(indicatorName -> new RequestedIndicatorDTO(indicatorName, schema.get(indicatorName)))
                          .collect(toList());
    }

}
