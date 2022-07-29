package org.gaussian.vgraph.processor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import graphql.GraphQLError;
import graphql.schema.DataFetcher;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import org.gaussian.vgraph.datafetcher.DataFetcherResult;
import org.gaussian.vgraph.datafetcher.NamespaceFetcher;
import org.gaussian.vgraph.datafetcher.NamespaceRequest;
import org.gaussian.vgraph.datafetcher.error.IndicatorFetcherGraphQLError;
import org.gaussian.vgraph.datafetcher.http.HttpDataFetcher;
import org.gaussian.vgraph.domain.RequestMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.vertx.core.CompositeFuture.join;
import static java.util.stream.Collectors.toList;
import static org.gaussian.vgraph.json.JsonCodec.from;

/**
 * Handles scoring events using provided {@link HttpRequestProcessor}.
 */
@Singleton
public class HttpRequestProcessor implements Handler<Message<Object>> {

    public static final String HTTP_REQUESTS_ADDRESS = "http.request.events";
    private static final Logger log = LoggerFactory.getLogger(HttpDataFetcher.class);

    private final Future<Map<String, DataFetcher>> namespaces;

    @Inject
    public HttpRequestProcessor(Future<Map<String, DataFetcher>> namespaces) {
        this.namespaces = namespaces;
    }

    public void handle(Message message) {
        final String messageBody = message.body().toString();
        final RequestMessage request = from(new JsonObject(messageBody), RequestMessage.class);
        final String namespace = request.namespace();
        final NamespaceRequest namespaceRequest = request.namespaceRequest();

        namespaces.onSuccess(fetchers -> {
            final NamespaceFetcher namespaceFetcher = (NamespaceFetcher) fetchers.get(namespace);
            final List<Future> asyncResults = namespaceFetcher.indicatorFetchers.stream()
                                                                                .map(fetcher -> fetcher.call(namespaceRequest))
                                                                                .collect(toList());

            join(asyncResults)
                    .onFailure(error -> message.fail(1, error.getMessage()))
                    .onSuccess(results -> message.reply(mergeResults(results)));
        });
    }

    private String mergeResults(CompositeFuture composite) {
        final JsonObject mergedResults = new JsonObject();
        final Map<String, Object> indicators = new HashMap<>();
        final List<GraphQLError> errors = new ArrayList<>();

        composite.result().list().stream()
                 .map(DataFetcherResult.class::cast)
                 .forEach(result -> {
                     indicators.putAll(result.getIndicators());
                     errors.addAll(extractErrors(result));
                 });

        return mergedResults.put("indicators", indicators)
                            .put("errors", errors)
                            .encode();
    }

    private List<GraphQLError> extractErrors(DataFetcherResult result) {
        return result.getErrors().entrySet().stream()
                     .map(Map.Entry::getValue)
                     .map(IndicatorFetcherGraphQLError::new)
                     .collect(toList());
    }

}
