package org.gaussian.vgraph.datafetcher.error;

import com.google.common.collect.ImmutableMap;
import graphql.ErrorType;
import graphql.GraphQLError;
import graphql.language.SourceLocation;
import org.gaussian.vgraph.datafetcher.DataFetcherError;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IndicatorFetcherGraphQLError implements GraphQLError {

    private static final String FETCHER_NAME_EXTENSION_KEY = "fetcher_name";

    private final String message;
    private final List<Object> path;
    private final Map<String, Object> extensions;

    public IndicatorFetcherGraphQLError(String message, List<Object> path, Map<String, Object> extensions) {
        this.path = path;
        this.message = message;
        this.extensions = extensions;
    }

    public IndicatorFetcherGraphQLError(DataFetcherError indicatorFetcherError) {
        this.path = new ArrayList<>();
        this.extensions = getExtensionFor(FETCHER_NAME_EXTENSION_KEY, indicatorFetcherError.fetcherName());

        if (indicatorFetcherError.namespace() != null) {
            this.path.add(indicatorFetcherError.namespace());
        }
        if (indicatorFetcherError.indicatorName() != null) {
            this.path.add(indicatorFetcherError.indicatorName());
        }

        this.message = indicatorFetcherError.errorMessage();
    }

    @Override
    public List<SourceLocation> getLocations() {
        return null;
    }

    @Override
    public ErrorType getErrorType() {
        return ErrorType.DataFetchingException;
    }

    @Override
    public List<Object> getPath() {
        return path;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public Map<String, Object> getExtensions() {
        return extensions;
    }

    private Map<String, Object> getExtensionFor(String key, String value) {
        return ImmutableMap.<String, Object>builder()
                           .put(key, value)
                           .build();
    }
}
