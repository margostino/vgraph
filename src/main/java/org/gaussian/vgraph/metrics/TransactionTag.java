package org.gaussian.vgraph.metrics;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

import java.util.Optional;

/**
 * Names current transaction for response time tracking.
 * Adds the transaction name as a response header.
 */
public class TransactionTag {

	private static final String X_TRANSACTION_HEADER_NAME = "X-Transaction-Name";

	public static void put(HttpServerRequest request, String name) {
		Optional.ofNullable(name).ifPresent(s -> request.response().putHeader(X_TRANSACTION_HEADER_NAME, name));
	}

	public static String get(HttpServerResponse response) {
		return response.headers().get(X_TRANSACTION_HEADER_NAME);
	}

	public static Handler<RoutingContext> putTag(String name) {
		return (RoutingContext context) -> {
			TransactionTag.put(context.request(), name);
			context.next();
		};
	}

}
