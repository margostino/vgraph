package org.gaussian.vgraph.datafetcher.http.metadata;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class MetadataIndicatorHttpResponse {

    private String name;
    private String type;
    private String lifecycle;
    private List<String> markets;

    public MetadataIndicatorHttpResponse() {
    }

    @Builder
    public MetadataIndicatorHttpResponse(String name,
                                         String type,
                                         String lifecycle,
                                         List<String> markets) {
        this.name = name;
        this.type = type;
        this.markets = markets;
        this.lifecycle = lifecycle;
    }
}
