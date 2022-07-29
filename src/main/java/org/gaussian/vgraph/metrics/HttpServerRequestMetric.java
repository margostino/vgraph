package org.gaussian.vgraph.metrics;

/**
 * Accumulates metrics data of an {@link io.vertx.core.http.HttpServerRequest}
 */
public class HttpServerRequestMetric {
	private long startTimeMillis;

	public HttpServerRequestMetric(long startTimeMillis) {
		super();
		this.startTimeMillis = startTimeMillis;
	}

	public Long getDurationMillis() {
		return System.currentTimeMillis() - startTimeMillis;
	}
}
