package org.gaussian.vgraph.datafetcher;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record RequestedIndicatorDTO(String indicatorName, String indicatorType) {

    @JsonCreator
    public RequestedIndicatorDTO(@JsonProperty("indicatorName") String indicatorName,
                                 @JsonProperty("indicatorType") String indicatorType) {
        this.indicatorName = indicatorName;
        this.indicatorType = indicatorType;
    }

}
