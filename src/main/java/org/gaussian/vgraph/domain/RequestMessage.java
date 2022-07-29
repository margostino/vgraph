package org.gaussian.vgraph.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.gaussian.vgraph.datafetcher.NamespaceRequest;

public record RequestMessage(String namespace, @JsonProperty("namespace_request") NamespaceRequest namespaceRequest) {

}
