package org.gaussian.vgraph.metrics;

public interface MetricCollector {
	/**
	 * Adjusts the specified counter by a given delta.
	 *
	 * @param aspect the name of the counter to adjust
	 * @param delta  the amount to adjust the counter by
	 * @param tags   array of tags to be added to the data
	 */
	void count(String aspect, long delta, String... tags);

	/**
	 * Increments the specified counter by one.
	 *
	 * @param aspect the name of the counter to increment
	 * @param tags   array of tags to be added to the data
	 */
	void incrementCounter(String aspect, String... tags);

	/**
	 * Decrements the specified counter by one.
	 *
	 * @param aspect the name of the counter to decrement
	 * @param tags   array of tags to be added to the data
	 */
	void decrementCounter(String aspect, String... tags);

	/**
	 * Records the latest fixed value for the specified named gauge.
	 *
	 * @param aspect the name of the gauge
	 * @param value  the new reading of the gauge
	 * @param tags   array of tags to be added to the data
	 */
	void recordGaugeValue(String aspect, double value, String... tags);

	/**
	 * Records the latest fixed value for the specified named gauge.
	 *
	 * @param aspect the name of the gauge
	 * @param value  the new reading of the gauge
	 * @param tags   array of tags to be added to the data
	 */
	void recordGaugeValue(String aspect, long value, String... tags);

	/**
	 * Records an execution time in milliseconds for the specified named operation.
	 *
	 * @param aspect   the name of the timed operation
	 * @param timeInMs the time in milliseconds
	 * @param tags     array of tags to be added to the data
	 */
	void recordExecutionTime(String aspect, long timeInMs, String... tags);

	/**
	 * Records a value for the specified named histogram.
	 *
	 * @param aspect the name of the histogram
	 * @param value  the value to be incorporated in the histogram
	 * @param tags   array of tags to be added to the data
	 */
	void recordHistogramValue(String aspect, double value, String... tags);

	/**
	 * Records a value for the specified named histogram.
	 *
	 * @param aspect the name of the histogram
	 * @param value  the value to be incorporated in the histogram
	 * @param tags   array of tags to be added to the data
	 */
	void recordHistogramValue(String aspect, long value, String... tags);

	/**
	 * Cleanly shut down this collector.
	 */
	void stop();
}
