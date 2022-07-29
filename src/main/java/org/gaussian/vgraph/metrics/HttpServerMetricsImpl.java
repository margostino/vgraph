package org.gaussian.vgraph.metrics;

import org.gaussian.vgraph.common.RuntimeEnv;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.spi.metrics.HttpServerMetrics;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Reports HttpServer metrics using provided {@link MetricCollector}
 */
public class HttpServerMetricsImpl implements HttpServerMetrics<HttpServerRequestMetric, Void, Void> {

  private static final Pattern TRANSACTION_NAME_PATTERN = Pattern.compile("[\\w\\.\\-_/ ]+");

  private final MetricCollector metricCollector;

  public HttpServerMetricsImpl(MetricCollector metricCollector) {
    this.metricCollector = metricCollector;
  }

  public HttpServerRequestMetric requestBegin(Void socketMetric, HttpServerRequest request) {
    metricCollector.incrementCounter("vertx.http.server.request.total", applicationTags());
    return new HttpServerRequestMetric(System.currentTimeMillis());
  }

  public void requestReset(HttpServerRequestMetric requestMetric) {
    metricCollector.incrementCounter("vertx.http.server.request.total_reset", applicationTags());
  }

  private String transactionName(HttpServerResponse response) {
    return Optional.ofNullable(TransactionTag.get(response))
                   .filter(s -> TRANSACTION_NAME_PATTERN.matcher(s).matches())
                   .orElse("default");
  }

  public void responseEnd(HttpServerRequestMetric requestMetric, HttpServerResponse response) {
    String responseCodeTag = "response_code:" + response.getStatusCode();
    String transactionNameTag = "transaction_name:" + transactionName(response);

    String[] tags = new String[]{applicationNameTag(), applicationScopeTag(), responseCodeTag, transactionNameTag};

    metricCollector.recordExecutionTime("vertx.http.server.request.response_time", requestMetric.getDurationMillis(), tags);
  }

  private String[] applicationTags() {
    return new String[]{applicationNameTag(), applicationScopeTag()};
  }

  private String applicationScopeTag() {
    return "application_scope:" + RuntimeEnv.get().scope();
  }

  private String applicationNameTag() {
    return "application_name:" + RuntimeEnv.get().application();
  }

  public boolean isEnabled() {
    return true;
  }

  /**
   * Nothing to close here
   */
  @Override
  public void close() {
  }

  @Override
  public Void connected(SocketAddress remoteAddress, String remoteName) {
    metricCollector.incrementCounter("vertx.http.server.connection.total", applicationTags());
    return null;
  }

  @Override
  public void disconnected(Void socketMetric, SocketAddress remoteAddress) {
    metricCollector.incrementCounter("vertx.http.server.connection.disconnected", applicationTags());
  }

  @Override
  public void bytesRead(Void socketMetric, SocketAddress remoteAddress, long numberOfBytes) {
    metricCollector.count("vertx.http.server.connection.bytes_read", numberOfBytes, applicationTags());
  }

  @Override
  public void bytesWritten(Void socketMetric, SocketAddress remoteAddress, long numberOfBytes) {
    metricCollector.count("vertx.http.server.connection.bytes_written", numberOfBytes, applicationTags());
  }

  @Override
  public void exceptionOccurred(Void socketMetric, SocketAddress remoteAddress, Throwable t) {
    metricCollector.incrementCounter("vertx.http.server.connection.exceptions", applicationTags());
  }

  /* WebSocket and http2 push metrics not implemented */
  public Void upgrade(HttpServerRequestMetric requestMetric, ServerWebSocket serverWebSocket) {
    return null;
  }

  public Void connected(Void socketMetric, ServerWebSocket serverWebSocket) {
    return null;
  }

  @Override
  public void disconnected(Void serverWebSocketMetric) {
  }

  public HttpServerRequestMetric responsePushed(Void socketMetric, HttpMethod method, String uri, HttpServerResponse response) {
    return null;
  }

}
