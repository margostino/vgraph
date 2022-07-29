package org.gaussian.vgraph.configuration;

import java.util.List;

public record NamespaceConfig(String name, List<DataFetcherConfig> indicatorsFetchers) {
}
