package org.gaussian.vgraph.metrics;

import com.google.common.base.Joiner;
import com.timgroup.statsd.NoOpStatsDClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link MetricCollector} for development environment
 */
public class NoOpMetricCollector extends DataDogMetricCollector {

	private static final Logger log = LoggerFactory.getLogger(MetricCollector.class);

	public NoOpMetricCollector() {
		super(new NoOpStatsDClient());
	}

	@Override
	public void incrementCounter(String metricName, String... tags) {
		super.incrementCounter(metricName, tags);
		String tagsDescription = tagsDescription(tags);
		log.debug(metricName + "-" + tagsDescription);
	}

	@Override
	public void recordExecutionTime(String metricName, long timeInMs, String... tags) {
		super.recordExecutionTime(metricName, timeInMs, tags);
		String tagsDescription = tagsDescription(tags);
		log.debug(metricName + "-execution_time:" + timeInMs + "-" + tagsDescription);
	}

	private String tagsDescription(String[] tags) {
		return Joiner.on("-").join(tags);
	}
}
