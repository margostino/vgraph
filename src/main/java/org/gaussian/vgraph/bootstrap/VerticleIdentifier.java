package org.gaussian.vgraph.bootstrap;

public class VerticleIdentifier {

	private final String name;

	public VerticleIdentifier() {
		this("local");
	}

	VerticleIdentifier(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}

}
