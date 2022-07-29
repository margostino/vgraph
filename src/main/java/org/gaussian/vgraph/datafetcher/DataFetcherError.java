package org.gaussian.vgraph.datafetcher;

public record DataFetcherError(String namespace, String fetcherName,
                               String indicatorName, String errorMessage) {
}
