package org.gaussian.vgraph.bootstrap;

import com.google.inject.AbstractModule;
import org.gaussian.vgraph.metrics.MetricCollector;
import io.vertx.core.Vertx;

class VertxAppModule extends AbstractModule {

  private final Class<? extends Application> applicationClass;

  private Vertx vertx;

  private MetricCollector metricCollector;

  VertxAppModule(Class<? extends Application> applicationClass, Vertx vertx, MetricCollector metricCollector) {
    this.applicationClass = applicationClass;
    this.vertx = vertx;
    this.metricCollector = metricCollector;
  }

  @Override
  protected void configure() {
    bind(Application.class).to(applicationClass);
    bind(RoutingConfigurator.class).to(applicationClass);
    bind(EventBusConsumersConfigurator.class).to(applicationClass);
    bind(MetricCollector.class).toInstance(metricCollector);
    bind(Vertx.class).toInstance(vertx);
  }

}
