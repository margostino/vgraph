package org.gaussian.vgraph.datafetcher.http.indicators;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.util.Map;

@Getter
public class IndicatorHttpResponse {

    private Map<String, String> errors;
    private Map<String, Object> metadata;
    private Map<String, Object> indicators;

    public IndicatorHttpResponse() {
    }

    @Builder
    public IndicatorHttpResponse(@Singular Map<String, String> errors,
                                 Map<String, Object> metadata,
                                 @Singular Map<String, Object> indicators) {
        this.errors = errors;
        this.metadata = metadata;
        this.indicators = indicators;
    }

    public boolean hasErrors() {
        return errors != null && errors.size() > 0;
    }

}
