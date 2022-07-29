package org.gaussian.vgraph.datafetcher;

import com.google.common.collect.Sets;
import lombok.Getter;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptyMap;

@Getter
public class DataFetcherResult {
    final private Map<String, Object> indicators;
    final private Map<String, DataFetcherError> errors;

    public DataFetcherResult() {
        this.indicators = emptyMap();
        this.errors = emptyMap();
    }

    public DataFetcherResult(final Map<String, Object> indicators) {
        this.indicators = indicators;
        this.errors = emptyMap();
    }

    public DataFetcherResult(final Map<String, Object> indicators, final Map<String, DataFetcherError> errors) {
        this.indicators = Collections.unmodifiableMap(indicators);
        this.errors = Collections.unmodifiableMap(errors);
    }

    public static DataFetcherResult emptyResult() {
        return new DataFetcherResult();
    }

    public boolean isEmpty() {
        return this.indicators.isEmpty() && this.errors.isEmpty();
    }

    public Set<String> indicatorNames() {
        return Sets.union(this.indicators.keySet(), this.errors.keySet());
    }
}
