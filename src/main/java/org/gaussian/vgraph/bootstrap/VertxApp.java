package org.gaussian.vgraph.bootstrap;

import ch.qos.logback.classic.LoggerContext;
import com.google.inject.Injector;
import com.google.inject.Module;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.micrometer.MicrometerMetricsOptions;
import io.vertx.micrometer.VertxPrometheusOptions;
import io.vertx.micrometer.backends.BackendRegistries;
import org.gaussian.vgraph.common.Optionals;
import org.gaussian.vgraph.common.RuntimeEnv;
import org.gaussian.vgraph.metrics.DataDogMetricCollector;
import org.gaussian.vgraph.metrics.MetricCollector;
import org.gaussian.vgraph.metrics.NoOpMetricCollector;
import org.gaussian.vgraph.verticle.GraphQLServerVerticle;
import org.gaussian.vgraph.verticle.HttpClientVerticle;
import org.gaussian.vgraph.verticle.TrackingVerticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

import static com.google.inject.Guice.createInjector;
import static com.google.inject.util.Modules.override;
import static io.vertx.core.CompositeFuture.all;
import static java.lang.Math.max;
import static java.lang.Runtime.getRuntime;
import static java.lang.management.ManagementFactory.getRuntimeMXBean;
import static java.time.Duration.between;
import static java.time.Instant.now;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.rangeClosed;

/**
 * Vertx bootstrap.
 * <p>
 * Performs framework initialization, registers shutdown hooks, and loads application verticles.
 */
public class VertxApp {

    private static final Logger log = LoggerFactory.getLogger("main");

    private final Vertx vertx;
    private final Module modules;
    private final Instant startup;
    private final List<Verticle> verticles;

    public VertxApp(Class<? extends Application> applicationClass, Iterable<Module> applicationModules) {
        this.startup = now();
        final MetricCollector metricCollector = metricCollector();
        vertx = vertx(metricCollector);
        verticles = new ArrayList<>();
        modules = override(new VertxAppModule(applicationClass, vertx, metricCollector)).with(applicationModules);
        httpServersCount();
        rangeClosed(1, getCoreByVerticle(2, "GraphQL Server")).forEach(i -> verticles.add(instanceOf("graphql-" + i, GraphQLServerVerticle.class)));
        rangeClosed(1, getCoreByVerticle(1, "Tracking")).forEach(i -> verticles.add(instanceOf("tracking-" + i, TrackingVerticle.class)));
        rangeClosed(1, getCoreByVerticle(3, "HTTP Client")).forEach(i -> verticles.add(instanceOf("http-" + i, HttpClientVerticle.class)));
        interactiveQuitOnDev();
        registerMetrics();
    }

    private static int getCoreByVerticle(int verticles, String name) {
        log.info("Deploying {} instances for {} verticle.", verticles, name);
        // TODO: smart core distribution by verticles
        return verticles;
    }

    private static int httpServersCount() {
        final int available = getRuntime().availableProcessors();
        final int servers = max(available - 1, 1);
        log.info("Detected {} available CPUs; deploying {} server instances.", available, servers);
        return servers;
    }

    private void registerMetrics() {
        MeterRegistry registry = BackendRegistries.getDefaultNow();
        new ClassLoaderMetrics().bindTo(registry);
        new JvmMemoryMetrics().bindTo(registry);
        new JvmGcMetrics().bindTo(registry);
        new ProcessorMetrics().bindTo(registry);
        new JvmThreadMetrics().bindTo(registry);
        registry.config().meterFilter(buildStatisticMeterFiler());
    }

    /**
     * Starts the application by deploying all verticles.
     *
     * @return future to be completed when all deployments succeeded
     */
    public Future<Void> start() {
        onDeploy();
        final List<Future> deployments = verticles.stream().map(this::deploy).collect(toList());
        return all(deployments).map(this::printStartupDuration)
                               .mapEmpty();
    }

    private Future<String> deploy(Verticle verticle) {
        return vertx.deployVerticle(verticle);
    }

    /**
     * Stops the application, stops Vertx.
     */
    public void stop() {
        syncCloseVertx();
        flushLogs();
        log.info("Shutdown complete.");
    }

    /**
     * Vertx instance in which this app is running
     */
    public Vertx getVertx() {
        return vertx;
    }

    /**
     * Vertx HTTP server port
     */
    public int httpServerPort() {
        return GraphQLServerVerticle.DEFAULT_PORT;
    }

    /**
     * Prints application header and registers shutdown hook
     */
    private void onDeploy() {
        HeaderPrinter.printHeader();
        getRuntime().addShutdownHook(new Thread(this::stop));
    }

    /**
     * Prints startup duration
     */
    private Future<CompositeFuture> printStartupDuration(CompositeFuture composite) {
        final long appDuration = between(startup, now()).toMillis();
        final long jvmDuration = now().toEpochMilli() - getRuntimeMXBean().getStartTime();
        log.info("Started vGraph application in {} ms (JVM startup in {} ms)", appDuration, jvmDuration);
        return composite;
    }

    /**
     * Close vert.x instance and wait for proper undeployment
     */
    private void syncCloseVertx() {
        final CompletableFuture<Void> future = new CompletableFuture<>();
        vertx.close(handler -> future.complete(null));
        try {
            future.get();
        } catch (final Exception e) {
            log.error("Error waiting for shutdown", e);
        }
    }

    /**
     * Flush any asynchronous log buffers
     */
    private void flushLogs() {
        Optionals.safeCast(LoggerFactory.getILoggerFactory(), LoggerContext.class).ifPresent(LoggerContext::stop);
    }

    private void interactiveQuitOnDev() {
        if (RuntimeEnv.get().dev()) {
            new Thread(this::waitForDevQuit).start();
        }
    }

    private void waitForDevQuit() {
        System.out.println("Press ENTER to quit");
        Scanner scanner = new Scanner(System.in);
        if (scanner.hasNextLine()) {
            scanner.nextLine();
            System.exit(0);
        }
    }

    private <T extends Verticle> T instanceOf(String name, Class<T> clazz) {
        final Injector injector = createInjector(modules, binder -> {
            binder.bind(VerticleIdentifier.class).toInstance(new VerticleIdentifier(name));
        });
        return injector.getInstance(clazz);
    }

    private MetricCollector metricCollector() {
        return RuntimeEnv.get().dev() ? new NoOpMetricCollector() : new DataDogMetricCollector();
    }

    private Vertx vertx(MetricCollector metricCollector) {
        VertxPrometheusOptions prometheusOptions = new VertxPrometheusOptions().setEnabled(true)
                                                                               .setStartEmbeddedServer(true)
                                                                               .setEmbeddedServerOptions(new HttpServerOptions().setPort(8081))
                                                                               .setEmbeddedServerEndpoint("/metrics");
        MicrometerMetricsOptions metricsOptions = new MicrometerMetricsOptions().setPrometheusOptions(prometheusOptions)
                                                                                .setEnabled(true);
        final VertxOptions options = new VertxOptions().setMetricsOptions(metricsOptions);
        return Vertx.vertx(options);
    }

    private MeterFilter buildStatisticMeterFiler() {
        return new MeterFilter() {
            @Override
            public DistributionStatisticConfig configure(Meter.Id id, DistributionStatisticConfig config) {
                return DistributionStatisticConfig.builder()
                                                  .percentiles(0.95, 0.99)
                                                  .build()
                                                  .merge(config);
            }
        };
    }

}
