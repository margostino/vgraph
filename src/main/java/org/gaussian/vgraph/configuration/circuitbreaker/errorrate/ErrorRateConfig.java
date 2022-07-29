package org.gaussian.vgraph.configuration.circuitbreaker.errorrate;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

public interface ErrorRateConfig {
    Float warningThreshold();

    Float criticalThreshold();
}
