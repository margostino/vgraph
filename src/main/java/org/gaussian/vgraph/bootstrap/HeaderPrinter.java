package org.gaussian.vgraph.bootstrap;

import com.google.common.io.Resources;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Prints application header.
 */
public class HeaderPrinter {

	private final static Logger log = LoggerFactory.getLogger("main");

	public static void printHeader() {
		final String[] lines = header().split(System.lineSeparator());
		for (final String line : lines) {
			log.info(line);
		}
	}

	private static String header() {
		try {
			final URL headerUrl = Resources.getResource("banner.txt");
			return Resources.toString(headerUrl, StandardCharsets.UTF_8);
		} catch (final IOException | IllegalArgumentException e) {
			log.error("Unable to reader header file", e);
			return "";
		}
	}

}
