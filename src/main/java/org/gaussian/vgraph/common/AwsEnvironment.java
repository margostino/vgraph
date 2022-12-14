package org.gaussian.vgraph.common;

import static com.google.common.base.MoreObjects.firstNonNull;

/**
 * Runtime Environment for Fury projects
 */
public class AwsEnvironment implements RuntimeEnv {

  private static final String DEV = "[DEV]";

  @Override
  public boolean dev() {
    return DEV.equals(scope());
  }

  @Override
  public String application() {
    return firstNonNull(System.getenv("APPLICATION"), "Unknown");
  }

  @Override
  public String scope() {
    return firstNonNull(System.getenv("SCOPE"), DEV);
  }

}
