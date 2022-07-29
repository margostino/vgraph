package org.gaussian.vgraph.configuration;

import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

public record GraphQLConfig(String schemaFile,
                            List<NamespaceConfig> namespaces) {

  public Set<String> getNamespaceNames() {
    return namespaces.stream().map(NamespaceConfig::name).collect(toSet());
  }

}
