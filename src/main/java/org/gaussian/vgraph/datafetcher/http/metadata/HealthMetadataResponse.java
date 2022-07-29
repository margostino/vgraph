package org.gaussian.vgraph.datafetcher.http.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
public class HealthMetadataResponse {

    private MetadataHttpHealthStatus status;
    private String message;
    @JsonProperty("data_freshness")
    private Long dataFreshness;

    public HealthMetadataResponse() {
    }

    @Builder
    public HealthMetadataResponse(String message,
                                  Long dataFreshness,
                                  MetadataHttpHealthStatus status) {
        this.status = status;
        this.message = message;
        this.dataFreshness = dataFreshness;
    }

}
