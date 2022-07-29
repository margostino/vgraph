package org.gaussian.vgraph.configuration.circuitbreaker.errorrate;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("default")
public class DefaultErrorRateConfig extends AbstractErrorRateConfig {
    public DefaultErrorRateConfig() {
        super(15F, 20F);
    }
}
