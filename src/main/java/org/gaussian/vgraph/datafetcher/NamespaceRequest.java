package org.gaussian.vgraph.datafetcher;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.ALWAYS)
public record NamespaceRequest(Map<String, Object> parameters,
                               List<RequestedIndicatorDTO> indicators,
                               org.gaussian.vgraph.datafetcher.NamespaceRequest.NamespaceRequestMetadata metadata) {
    @Builder
    @JsonCreator
    public NamespaceRequest(@JsonProperty("requestParameters") Map<String, Object> parameters,
                            @JsonProperty("requestedindicators") List<RequestedIndicatorDTO> indicators,
                            @JsonProperty("metadata") NamespaceRequestMetadata metadata) {
        this.parameters = parameters;
        this.indicators = indicators;
        this.metadata = metadata;
    }

    public record NamespaceRequestMetadata(String username, String correlationId) {
        public static NamespaceRequestMetadata empty() {
            return new NamespaceRequestMetadata(null, null);
        }

        @Builder
        @JsonCreator
        public NamespaceRequestMetadata(@JsonProperty("user") String username,
                                        @JsonProperty("correlation_id") String correlationId) {
            this.username = username;
            this.correlationId = correlationId;
        }
    }

    public String correlationId() {
        return metadata != null ? metadata.correlationId() : null;
    }

    public String username() {
        return metadata != null ? metadata.username() : null;
    }
}
