package org.gaussian.vgraph.configuration.circuitbreaker.errorrate;

public abstract class AbstractErrorRateConfig implements ErrorRateConfig {
    private final Float warningThreshold;
    private final Float criticalThreshold;

    protected AbstractErrorRateConfig(Float warningThreshold, Float criticalThreshold) {
        this.warningThreshold = warningThreshold;
        this.criticalThreshold = criticalThreshold;
    }

    @Override
    public Float warningThreshold() {
        return warningThreshold;
    }

    @Override
    public Float criticalThreshold() {
        return criticalThreshold;
    }
}
