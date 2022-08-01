package org.gaussian.vgraph.datafetcher.http;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.DecodeException;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.micrometer.backends.BackendRegistries;
import org.apache.commons.lang3.StringUtils;
import org.gaussian.vgraph.configuration.ArgumentConfig;
import org.gaussian.vgraph.datafetcher.DataFetcher;
import org.gaussian.vgraph.datafetcher.DataFetcherError;
import org.gaussian.vgraph.datafetcher.DataFetcherResult;
import org.gaussian.vgraph.datafetcher.NamespaceRequest;
import org.gaussian.vgraph.datafetcher.RequestedIndicatorDTO;
import org.gaussian.vgraph.datafetcher.http.indicators.IndicatorHttpErrorResponse;
import org.gaussian.vgraph.datafetcher.http.indicators.IndicatorHttpRequest;
import org.gaussian.vgraph.datafetcher.http.indicators.IndicatorHttpResponse;
import org.gaussian.vgraph.exception.FetcherExecutionException;
import org.gaussian.vgraph.json.JsonCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.vertx.core.Future.failedFuture;
import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.gaussian.vgraph.common.AppConstants.API_VERSION_HEADER;
import static org.gaussian.vgraph.common.AppConstants.CORRELATION_ID_HEADER;
import static org.gaussian.vgraph.common.AppConstants.USERNAME_HEADER;

public class HttpDataFetcher implements DataFetcher {

    private static final Logger log = LoggerFactory.getLogger(HttpDataFetcher.class);
    private static final String API_VERSION = "2.0.0";

    private final String path;
    private final String namespace;
    private final String fetcherName;
    private final WebClient webClient;
    private final Counter failureCounter;
    private final List<ArgumentConfig> fields;
    private final Map<String, String> headers;

    public HttpDataFetcher(String path,
                           String namespace,
                           String fetcherName,
                           WebClient webClient,
                           Map<String, String> headers,
                           List<ArgumentConfig> fields) {

        this.path = path;
        this.fields = fields;
        this.headers = headers;
        this.namespace = namespace;
        this.webClient = webClient;
        this.fetcherName = fetcherName;
        this.failureCounter = registerCounterMetric("http_data_fetcher_failures");
    }

    @Override
    public Future<DataFetcherResult> call(NamespaceRequest namespaceRequest) {
        log.info("calling data fetcher {} for namespace {}", fetcherName, namespace);

        Optional<IndicatorHttpRequest> request = createHttpRequest(namespaceRequest);

        return request.map(this::post)
                      .map(async -> async.map(this::processResponse))
                      .orElseGet(() -> fail("Missing or Invalid HTTP Request"));
    }

    private Future fail(String message) {
        failureCounter.increment();
        return failedFuture(message);
    }

    private Optional<IndicatorHttpRequest> createHttpRequest(NamespaceRequest namespaceRequest) {
        if (isMissingAllRequestArguments(namespaceRequest.parameters()) || isMissingAnyRequiredArgument(fields, namespaceRequest.parameters())) {
            return empty();
        }

        final List<String> indicators = namespaceRequest.indicators().stream()
                                                        .map(RequestedIndicatorDTO::indicatorName)
                                                        .collect(toList());
        IndicatorHttpRequest.Payload payload = IndicatorHttpRequest.Payload.builder()
                                                                           .namespace(namespace)
                                                                           .arguments(namespaceRequest.parameters())
                                                                           .indicators(indicators)
                                                                           .build();
        IndicatorHttpRequest request = IndicatorHttpRequest.builder()
                                                           .payload(payload)
                                                           .headers(buildHeaders(namespaceRequest))
                                                           .build();
        return ofNullable(request);
    }

    private Map<String, String> buildHeaders(NamespaceRequest fetcherRequest) {
        Map<String, String> headers = new HashMap<>();
        headers.put(API_VERSION_HEADER, API_VERSION);

        if (fetcherRequest.correlationId() != null) {
            headers.put(CORRELATION_ID_HEADER, fetcherRequest.correlationId());
        }

        if (fetcherRequest.username() != null) {
            headers.put(USERNAME_HEADER, fetcherRequest.username());
        }

        return headers;
    }

    private boolean isMissingAllRequestArguments(Map<String, Object> arguments) {
        return arguments.entrySet().stream()
                        .noneMatch(entry -> Objects.nonNull(entry.getValue()));
    }

    private boolean isMissingAnyRequiredArgument(List<ArgumentConfig> fields, Map<String, Object> incomingArguments) {
        return fields.stream().filter(field -> field.isRequired)
                     .map(ArgumentConfig::getName)
                     .anyMatch(requiredArg -> !incomingArguments.containsKey(requiredArg) || isMissingValue(incomingArguments.get(requiredArg)));
    }

    private boolean isMissingValue(Object value) {
        return value == null || (value instanceof String && StringUtils.isBlank((String) value));
    }

    private Future<HttpResponse<Buffer>> post(IndicatorHttpRequest request) {
        return webClient
                .post(path)
                .sendJson(request.getPayload());
    }

    private DataFetcherResult processResponse(HttpResponse response) {
        final String payload = response.bodyAsString();
        final HttpResponseStatus status = HttpResponseStatus.valueOf(response.statusCode());
        if (status == HttpResponseStatus.OK) {
            final IndicatorHttpResponse successfulResponse = JsonCodec.decode(payload, IndicatorHttpResponse.class);
            return getFetcherResult(successfulResponse);
        } else {
            DataFetcherError fetcherError;
            final Map<String, DataFetcherError> errors = new HashMap<>();
            try {
                final IndicatorHttpErrorResponse errorResponse = JsonCodec.decode(payload, IndicatorHttpErrorResponse.class);
                fetcherError = new DataFetcherError(namespace, fetcherName, null, errorResponse.getErrorMessage());
            } catch (DecodeException e) {
                fetcherError = new DataFetcherError(namespace, fetcherName, null, e.getMessage());
            }
            errors.put(namespace, fetcherError);
            final DataFetcherResult fetcherResult = new DataFetcherResult(emptyMap(), errors);
            return fetcherResult;
        }
    }

    private DataFetcherResult getFetcherResult(final IndicatorHttpResponse response) {
        if (response.getIndicators() == null && !response.hasErrors()) {
            final String clientErrorMessage = "The response is missing indicators section even though no error is reported.";
            final String errorMessage = format("%s \n Namespace: %s \n Data Provider: %s ", clientErrorMessage, namespace, fetcherName);
            throw new FetcherExecutionException(errorMessage, clientErrorMessage);
        }
        return buildFetcherResult(response);
    }

    private DataFetcherResult buildFetcherResult(IndicatorHttpResponse response) {
        final Map<String, String> errors = response.getErrors();
        Map<String, Object> indicators = response.getIndicators() != null ? response.getIndicators() : emptyMap();
        if (errors != null && errors.size() > 0) {
            final Map<String, DataFetcherError> errorsAsExceptions = extractIndicatorErrorContexts(errors);
            return new DataFetcherResult(indicators, errorsAsExceptions);
        }
        return new DataFetcherResult(indicators);
    }

    private Map<String, DataFetcherError> extractIndicatorErrorContexts(final Map<String, String> errorsSection) {
        return errorsSection.entrySet().stream()
                            .map(errorEntry -> new DataFetcherError(namespace, fetcherName, errorEntry.getKey(), errorEntry.getValue()))
                            .collect(Collectors.toMap(DataFetcherError::indicatorName, Function.identity()));
    }

    private Counter registerCounterMetric(String metricName) {
        MeterRegistry registry = BackendRegistries.getDefaultNow();
        final Tags tags = Tags.of("namespace", namespace)
                              .and("fetcher_name", fetcherName);
        return registry.counter(metricName, tags);
    }

}
