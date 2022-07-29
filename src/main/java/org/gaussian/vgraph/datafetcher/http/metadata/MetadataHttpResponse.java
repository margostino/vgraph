package org.gaussian.vgraph.datafetcher.http.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class MetadataHttpResponse {

    private String team;
    private String namespace;
    @JsonProperty("indicators_endpoint")
    private String indicatorsEndpoint;
    private HealthMetadataResponse health;
    private List<MetadataIndicatorHttpResponse> indicators;

    public MetadataHttpResponse() {
    }

    @Builder
    public MetadataHttpResponse(String team,
                                String namespace,
                                String indicatorsEndpoint,
                                HealthMetadataResponse health,
                                List<MetadataIndicatorHttpResponse> indicators) {
        this.team = team;
        this.health = health;
        this.namespace = namespace;
        this.indicators = indicators;
        this.indicatorsEndpoint = indicatorsEndpoint;
    }

    public Long getDataFreshness() {
        return health != null ? health.getDataFreshness() : null;
    }
}
