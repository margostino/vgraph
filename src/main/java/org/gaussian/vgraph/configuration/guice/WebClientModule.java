package org.gaussian.vgraph.configuration.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.gaussian.vgraph.configuration.qualifier.HttpClient;
import org.gaussian.vgraph.configuration.qualifier.HttpClientOptions;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

public class WebClientModule extends AbstractModule {

  @Override
  protected void configure() {
  }

  @Inject
  @Provides
  @Singleton
  @HttpClient
  private WebClient httpClient(Vertx vertx, @HttpClientOptions WebClientOptions options) {
    return WebClient.create(vertx, options);
  }


}
