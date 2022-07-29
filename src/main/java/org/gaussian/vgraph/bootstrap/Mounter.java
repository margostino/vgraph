package org.gaussian.vgraph.bootstrap;

/**
 * Mounts a Mountable on a specified path
 */
public interface Mounter {

	void mount(String path, Mountable unit);

}
