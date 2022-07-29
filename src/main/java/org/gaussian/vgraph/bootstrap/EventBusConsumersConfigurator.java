package org.gaussian.vgraph.bootstrap;

import io.vertx.core.eventbus.EventBus;

/**
 * Configures consumers for messages in the EventBus.
 */
public interface EventBusConsumersConfigurator {

	default void registerConsumers(EventBus eventBus) {
	}

}
