package org.gaussian.vgraph.datafetcher.http.indicators;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class IndicatorHttpErrorResponse {

    @JsonProperty("error_message")
    private String errorMessage;

    public IndicatorHttpErrorResponse() {
    }

    public IndicatorHttpErrorResponse(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
