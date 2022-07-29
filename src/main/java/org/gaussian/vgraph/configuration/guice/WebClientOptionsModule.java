package org.gaussian.vgraph.configuration.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.gaussian.vgraph.configuration.qualifier.HttpClientOptions;
import io.vertx.ext.web.client.WebClientOptions;

public class WebClientOptionsModule extends AbstractModule {

  @Override
  protected void configure() {
  }

  @Inject
  @Provides
  @Singleton
  @HttpClientOptions
  private WebClientOptions httpClientOptions() {
    return new WebClientOptions().setTryUseCompression(true)
                                 .setMaxPoolSize(10)
                                 .setKeepAlive(true)
                                 .setIdleTimeout(60)
                                 .setKeepAliveTimeout(60)
                                 .setConnectTimeout(300)
                                 //.setMaxWaitQueueSize(100)
                                 .setSsl(false);
  }
}
