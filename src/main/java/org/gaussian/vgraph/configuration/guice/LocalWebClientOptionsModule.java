package org.gaussian.vgraph.configuration.guice;

import com.google.inject.AbstractModule;
import io.vertx.ext.web.client.WebClientOptions;

public class LocalWebClientOptionsModule extends AbstractModule {

  private static final int DEFAULT_PORT = 8888;
  private final Integer port;

  public LocalWebClientOptionsModule() {
    this(DEFAULT_PORT);
  }

  public LocalWebClientOptionsModule(Integer port) {
    this.port = port;
  }

  @Override
  protected void configure() {
  }

  private WebClientOptions localhost() {
    return new WebClientOptions()
      .setDefaultHost("localhost")
      .setDefaultPort(port)
      .setTryUseCompression(true)
      .setMaxPoolSize(4)
      .setMaxWaitQueueSize(100)
      .setSsl(false);
  }

//	@Provides
//	@Singleton
//	@BigQueue
//	private JsonObject clusterHostsInfo() {
//		return new JsonObject()
//				.put("default", "localhost")
//				.put("default2", "localhost");
//	}
//
//	@Provides
//	@Singleton
//	@RecaptchaScorer
//	private WebClientOptions recaptchaScorerWebClientOptions() {
//		return localhost();
//	}

}
