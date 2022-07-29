package org.gaussian.vgraph;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Module;
import org.gaussian.vgraph.bootstrap.Application;
import org.gaussian.vgraph.bootstrap.Mounter;
import org.gaussian.vgraph.bootstrap.VertxApp;
import org.gaussian.vgraph.configuration.guice.ConfigurationModule;
import org.gaussian.vgraph.configuration.guice.GraphQLModule;
import org.gaussian.vgraph.configuration.guice.WebClientModule;
import org.gaussian.vgraph.configuration.guice.WebClientOptionsModule;
import org.gaussian.vgraph.processor.HttpRequestProcessor;
import org.gaussian.vgraph.router.GraphQLRouter;
import org.gaussian.vgraph.verticle.GraphQLServerVerticle;
import io.vertx.core.eventbus.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class Main implements Application {

  private static final String APPLICATION_PATH = "/";
  private static final Logger log = LoggerFactory.getLogger(Main.class);

  private final GraphQLRouter graphQLRouter;
  private final HttpRequestProcessor httpRequestProcessor;

  @Inject
  public Main(GraphQLRouter graphQLRouter, HttpRequestProcessor httpRequestProcessor) {
    this.graphQLRouter = graphQLRouter;
    this.httpRequestProcessor = httpRequestProcessor;
//    vertx.periodicStream(Duration.ofMinutes(5).toMillis()).handler(i ->
//                                                                     vertx.runOnContext((event) -> torIpsHolderService.updateIps()));
  }

  public static void main(final String[] args) {
    log.info("start with {} threads", Thread.activeCount());
    VertxApp app = new VertxApp(Main.class, modules());
    app.start();
    log.info("after starting with {} threads", Thread.activeCount());
  }

  @Override
  public void mountRoutes(Mounter mounter) {
    if (mounter instanceof GraphQLServerVerticle) {
      mounter.mount(APPLICATION_PATH, graphQLRouter);
    }
  }

  @Override
  public void registerConsumers(EventBus eventBus) {
    eventBus.consumer("http.request.events", httpRequestProcessor);
  }

  private static Set<Module> modules() {
    final ImmutableSet.Builder<Module> builder = ImmutableSet.<Module>builder();
    builder.add(new WebClientModule());
    builder.add(new ConfigurationModule());
    builder.add(new GraphQLModule());

    builder.add(new WebClientOptionsModule());

//    if (RuntimeEnv.get().dev()) {
//      builder.add(new LocalWebClientOptionsModule());
//    } else {
//      builder.add(new WebClientOptionsModule());
//    }

    return builder.build();
  }

}
