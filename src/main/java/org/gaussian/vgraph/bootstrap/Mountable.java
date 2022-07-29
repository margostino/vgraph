package org.gaussian.vgraph.bootstrap;

import io.vertx.ext.web.Router;

/**
 * Trait implemented by classes that can be hooked to a route
 */
public interface Mountable {

	/**
	 * Register the methods provided by this class using the given router.
	 * <p>
	 * The router is expected to be a relative mount path unless explicitly documented.
	 *
	 * @param router Route where to hook event handlers
	 */
	void mount(Router router);

}
