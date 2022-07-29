package org.gaussian.vgraph.datafetcher;

import io.vertx.core.Future;

public interface DataFetcher {

  Future<DataFetcherResult> call(NamespaceRequest namespaceRequest);

}
